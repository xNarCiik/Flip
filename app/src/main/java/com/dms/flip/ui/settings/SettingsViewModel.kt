package com.dms.flip.ui.settings

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dms.flip.R
import com.dms.flip.data.repository.AuthRepository
import com.dms.flip.domain.model.Theme
import com.dms.flip.domain.usecase.settings.GetDailyReminderStateUseCase
import com.dms.flip.domain.usecase.settings.GetReminderTimeUseCase
import com.dms.flip.domain.usecase.settings.GetThemeUseCase
import com.dms.flip.domain.usecase.settings.SetDailyReminderStateUseCase
import com.dms.flip.domain.usecase.settings.SetReminderTimeUseCase
import com.dms.flip.domain.usecase.settings.SetThemeUseCase
import com.dms.flip.domain.usecase.settings.GetUseMockCommunityDataUseCase
import com.dms.flip.domain.usecase.settings.SetUseMockCommunityDataUseCase
import com.dms.flip.domain.usecase.storage.UploadAvatarUseCase
import com.dms.flip.domain.usecase.user.GetUserInfoUseCase
import com.dms.flip.notification.DailyReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val authRepository: AuthRepository,
    private val getThemeUseCase: GetThemeUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val getDailyReminderStateUseCase: GetDailyReminderStateUseCase,
    private val setDailyReminderStateUseCase: SetDailyReminderStateUseCase,
    private val getReminderTimeUseCase: GetReminderTimeUseCase,
    private val setReminderTimeUseCase: SetReminderTimeUseCase,
    private val getUseMockCommunityDataUseCase: GetUseMockCommunityDataUseCase,
    private val setUseMockCommunityDataUseCase: SetUseMockCommunityDataUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val dailyReminderManager = DailyReminderManager(application)

    init {
        loadSettings()
        loadUserInfo()
    }

    private fun loadUserInfo() {
        getUserInfoUseCase().onEach { userInfo ->
            _uiState.value = _uiState.value.copy(userInfo = userInfo)
        }.launchIn(viewModelScope)
    }

    private fun loadSettings() = viewModelScope.launch {
        combine(
            getThemeUseCase(),
            getDailyReminderStateUseCase(),
            getReminderTimeUseCase(),
            getUseMockCommunityDataUseCase()
        ) { theme, dailyReminderEnabled, reminderTime, useMockCommunityData ->
            _uiState.value.copy(
                theme = theme,
                dailyReminderEnabled = dailyReminderEnabled,
                reminderTime = reminderTime,
                useMockCommunityData = useMockCommunityData
            )
        }.collect { combinedState ->
            _uiState.value = combinedState
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnAvatarSelected -> onAvatarSelected(event.avatar)
            is SettingsEvent.OnThemeChanged -> onThemeChange(event.theme)
            is SettingsEvent.OnDailyReminderEnabledChanged -> onDailyReminderEnabledChange(event.enabled)
            is SettingsEvent.OnReminderTimeChanged -> onReminderTimeChange(event.time)
            is SettingsEvent.OnUseMockCommunityDataChanged -> onUseMockCommunityDataChanged(event.enabled)
            is SettingsEvent.OnSignOut -> signOut()
            is SettingsEvent.DeleteAccount -> deleteAccount()
        }
    }

    private fun onThemeChange(theme: Theme) = viewModelScope.launch {
        setThemeUseCase(theme)
    }

    private fun onDailyReminderEnabledChange(enabled: Boolean) = viewModelScope.launch {
        setDailyReminderStateUseCase(enabled)
        if (enabled) {
            dailyReminderManager.schedule(uiState.value.reminderTime)
        } else {
            dailyReminderManager.cancel()
        }
    }

    private fun onReminderTimeChange(time: String) = viewModelScope.launch {
        setReminderTimeUseCase(time)
        dailyReminderManager.schedule(time)
    }

    private fun onUseMockCommunityDataChanged(enabled: Boolean) = viewModelScope.launch {
        setUseMockCommunityDataUseCase(enabled)
    }

    fun onAvatarSelected(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isUploading = true) }

                val url = uploadAvatarUseCase(imageUri)

                _uiState.update {
                    it.copy(
                        isUploading = false,
                        userInfo = it.userInfo?.copy(avatarUrl = url)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        error = e.message ?: application.getString(R.string.generic_error_message)
                    )
                }
            }
        }
    }

    private fun signOut() {
        authRepository.signOut()
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            try {
                authRepository.deleteAccount()
            } catch (e: Exception) {
                Log.e("SettingsViewModel", e.message ?: "Unknown error")
                // TODO Generic error toast ?
            }
        }
    }
}
