package com.dms.flip.ui.settings

import android.net.Uri
import com.dms.flip.domain.model.Theme
import com.dms.flip.domain.model.UserInfo

data class SettingsUiState(
    val isUploading: Boolean = false,
    val userInfo: UserInfo? = null,
    val theme: Theme = Theme.SYSTEM,
    val dailyReminderEnabled: Boolean = false,
    val reminderTime: String = "11:00",
    val useMockCommunityData: Boolean = false,
    val error: String? = null
)

sealed interface SettingsEvent {
    data class OnAvatarSelected(val avatar: Uri) : SettingsEvent
    data class OnThemeChanged(val theme: Theme) : SettingsEvent
    data class OnDailyReminderEnabledChanged(val enabled: Boolean) : SettingsEvent
    data class OnReminderTimeChanged(val time: String) : SettingsEvent
    data class OnUseMockCommunityDataChanged(val enabled: Boolean) : SettingsEvent
    data object OnSignOut : SettingsEvent
    data object DeleteAccount : SettingsEvent
}