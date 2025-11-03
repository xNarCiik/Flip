package com.dms.flip.ui.onboarding

import android.net.Uri
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.usecase.user.UsernameError

data class OnboardingUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val showWelcome: Boolean = true,
    val currentStep: OnboardingStep = OnboardingStep.USERNAME,
    val username: String = "",
    val usernameError: UsernameError? = null,
    val isCheckingUsername: Boolean = false,
    val avatarUrl: String? = null,
    val isUploadingAvatar: Boolean = false,
    val availablePleasures: List<Pleasure> = emptyList(),
    val notificationInitiallyEnabled: Boolean = false,
    val notificationEnabled: Boolean = false,
    val reminderTime: String = "09:00",
    val showNotificationSkipWarning: Boolean = false,
    val hasShownNotificationWarning: Boolean = false,
    val completed: Boolean = false
)

enum class OnboardingStep {
    USERNAME,
    PLEASURES,
    NOTIFICATIONS,
    REMINDER_TIME
}

sealed interface OnboardingEvent {
    data object OnStartClick : OnboardingEvent
    data object NextStep : OnboardingEvent
    data object PreviousStep : OnboardingEvent
    data class UpdateUsername(val username: String) : OnboardingEvent
    data class OnAvatarSelected(val uri: Uri) : OnboardingEvent
    data class TogglePleasure(val index: Int) : OnboardingEvent
    data class UpdateNotificationEnabled(val enabled: Boolean) : OnboardingEvent
    data class UpdateReminderTime(val time: String) : OnboardingEvent
    data object DismissNotificationWarning : OnboardingEvent
    data object CompleteOnboarding : OnboardingEvent
}
