package com.dms.flip.ui.onboarding

import android.app.Application
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dms.flip.R
import com.dms.flip.domain.usecase.onboarding.InitOnboardingStatusUseCase
import com.dms.flip.domain.usecase.onboarding.SaveOnboardingStatusUseCase
import com.dms.flip.domain.usecase.pleasures.GetLocalPleasuresUseCase
import com.dms.flip.domain.usecase.storage.UploadAvatarUseCase
import com.dms.flip.domain.usecase.user.CheckUsernameAvailabilityUseCase
import com.dms.flip.domain.usecase.user.UsernameError
import com.dms.flip.domain.usecase.user.UsernameValidationResult
import com.dms.flip.domain.usecase.user.ValidateUsernameFormatUseCase
import com.dms.flip.notification.DailyReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val application: Application,
    private val initOnboardingStatusUseCase: InitOnboardingStatusUseCase,
    private val saveOnboardingStatusUseCase: SaveOnboardingStatusUseCase,
    getLocalPleasuresUseCase: GetLocalPleasuresUseCase,
    private val validateUsernameFormatUseCase: ValidateUsernameFormatUseCase,
    private val checkUsernameAvailabilityUseCase: CheckUsernameAvailabilityUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase
) : ViewModel() {
    private val dailyReminderManager = DailyReminderManager(application)
    private var areNotificationsInitiallyEnabled: Boolean = false

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val usernameFlow = MutableStateFlow("")

    init {
        areNotificationsInitiallyEnabled =
            NotificationManagerCompat.from(application).areNotificationsEnabled()
        _uiState.value = _uiState.value.copy(
            notificationInitiallyEnabled = areNotificationsInitiallyEnabled,
            notificationEnabled = areNotificationsInitiallyEnabled,
            availablePleasures = getLocalPleasuresUseCase()
        )

        viewModelScope.launch {
            initOnboardingStatusUseCase()

            usernameFlow
                .debounce(500)
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .map { username ->
                    when (val formatResult = validateUsernameFormatUseCase(username)) {
                        is UsernameValidationResult.Valid -> {
                            _uiState.value = _uiState.value.copy(isCheckingUsername = true)
                            val isAvailable = try {
                                checkUsernameAvailabilityUseCase(username)
                            } catch (e: Exception) {
                                true
                            }
                            _uiState.value = _uiState.value.copy(isCheckingUsername = false)

                            if (isAvailable) null else UsernameError.ALREADY_TAKEN
                        }

                        is UsernameValidationResult.Invalid -> formatResult.error
                    }
                }
                .collect { error ->
                    _uiState.value = _uiState.value.copy(usernameError = error)
                }
        }
    }

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.OnStartClick -> onStartClick()
            is OnboardingEvent.NextStep -> nextStep()
            is OnboardingEvent.PreviousStep -> previousStep()
            is OnboardingEvent.UpdateUsername -> updateUsername(event.username)
            is OnboardingEvent.OnAvatarSelected -> onAvatarSelected(event.uri)
            is OnboardingEvent.TogglePleasure -> togglePleasure(event.index)
            is OnboardingEvent.UpdateNotificationEnabled -> updateNotificationEnabled(event.enabled)
            is OnboardingEvent.UpdateReminderTime -> updateReminderTime(event.time)
            is OnboardingEvent.DismissNotificationWarning -> dismissNotificationWarning()
            is OnboardingEvent.CompleteOnboarding -> completeOnboarding()
        }
    }

    private fun onStartClick() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(showWelcome = false)
    }

    private fun nextStep() {
        viewModelScope.launch {
            val currentStep = _uiState.value.currentStep

            if (currentStep == OnboardingStep.USERNAME) {
                val username = _uiState.value.username.trim()

                if (username.isBlank()) {
                    _uiState.value = _uiState.value.copy(usernameError = UsernameError.EMPTY)
                    return@launch
                }

                when (val result = validateUsernameFormatUseCase(username)) {
                    is UsernameValidationResult.Invalid -> {
                        _uiState.value = _uiState.value.copy(usernameError = result.error)
                        return@launch
                    }

                    is UsernameValidationResult.Valid -> {
                        _uiState.value = _uiState.value.copy(isCheckingUsername = true)
                        val isAvailable = try {
                            checkUsernameAvailabilityUseCase(username)
                        } catch (e: Exception) {
                            _uiState.value = _uiState.value.copy(
                                isCheckingUsername = false,
                                error = e.message
                                    ?: application.getString(R.string.generic_error_message)
                            )
                            return@launch
                        }
                        _uiState.value = _uiState.value.copy(isCheckingUsername = false)

                        if (!isAvailable) {
                            _uiState.value =
                                _uiState.value.copy(usernameError = UsernameError.ALREADY_TAKEN)
                            return@launch
                        }
                    }
                }
            }

            val nextStep = when (currentStep) {
                OnboardingStep.USERNAME -> OnboardingStep.PLEASURES
                OnboardingStep.PLEASURES -> {
                    if (areNotificationsInitiallyEnabled) {
                        OnboardingStep.REMINDER_TIME
                    } else {
                        OnboardingStep.NOTIFICATIONS
                    }
                }

                OnboardingStep.NOTIFICATIONS -> {
                    if (!_uiState.value.notificationEnabled && !_uiState.value.hasShownNotificationWarning) {
                        _uiState.value = _uiState.value.copy(
                            showNotificationSkipWarning = true,
                            hasShownNotificationWarning = true
                        )
                        return@launch
                    } else if (_uiState.value.notificationEnabled) {
                        _uiState.value =
                            _uiState.value.copy(currentStep = OnboardingStep.REMINDER_TIME)
                        return@launch
                    } else {
                        completeOnboarding()
                        return@launch
                    }
                }

                OnboardingStep.REMINDER_TIME -> {
                    completeOnboarding()
                    return@launch
                }
            }
            _uiState.value = _uiState.value.copy(currentStep = nextStep)
        }
    }

    private fun previousStep() {
        viewModelScope.launch {
            val currentStep = _uiState.value.currentStep
            val previousStep = when (currentStep) {
                OnboardingStep.USERNAME -> OnboardingStep.USERNAME
                OnboardingStep.PLEASURES -> OnboardingStep.USERNAME
                OnboardingStep.NOTIFICATIONS -> OnboardingStep.PLEASURES
                OnboardingStep.REMINDER_TIME -> {
                    if (areNotificationsInitiallyEnabled) {
                        OnboardingStep.PLEASURES
                    } else {
                        OnboardingStep.NOTIFICATIONS
                    }
                }
            }
            _uiState.value = _uiState.value.copy(currentStep = previousStep)
        }
    }

    private fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            usernameError = null
        )
        usernameFlow.value = username
    }

    private fun onAvatarSelected(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isUploadingAvatar = true)

                val url = uploadAvatarUseCase(uri)

                _uiState.value = _uiState.value.copy(
                    isUploadingAvatar = false,
                    avatarUrl = url
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploadingAvatar = false,
                    error = e.message ?: application.getString(R.string.generic_error_message)
                )
            }
        }
    }

    private fun togglePleasure(index: Int) {
        val updatedPleasures =
            _uiState.value.availablePleasures.mapIndexed { indexPleasure, pleasure ->
                if (index == indexPleasure) pleasure.copy(isEnabled = !pleasure.isEnabled)
                else pleasure
            }
        _uiState.value = _uiState.value.copy(availablePleasures = updatedPleasures)
    }

    private fun updateNotificationEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationEnabled = enabled)
    }

    private fun updateReminderTime(time: String) {
        _uiState.value = _uiState.value.copy(reminderTime = time)
    }

    private fun dismissNotificationWarning() {
        _uiState.value = _uiState.value.copy(showNotificationSkipWarning = false)
    }

    private fun completeOnboarding() = viewModelScope.launch {
        val pleasures = _uiState.value.availablePleasures.filter { it.isEnabled }
        val hasNotificationsEnabled = _uiState.value.notificationEnabled
        val reminderTime = _uiState.value.reminderTime

        if (hasNotificationsEnabled) {
            dailyReminderManager.schedule(reminderTime)
        }

        saveOnboardingStatusUseCase(
            username = _uiState.value.username.trim(),
            avatarUrl = _uiState.value.avatarUrl,
            pleasures = pleasures,
            notificationEnabled = hasNotificationsEnabled,
            reminderTime = reminderTime
        )
        _uiState.value = _uiState.value.copy(completed = true)
    }
}
