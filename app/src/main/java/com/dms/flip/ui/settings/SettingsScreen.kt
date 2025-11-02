package com.dms.flip.ui.settings

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.dms.flip.R
import com.dms.flip.domain.model.Theme
import com.dms.flip.domain.model.UserInfo
import com.dms.flip.ui.community.component.CommunityAvatar
import com.dms.flip.ui.component.FlipTopBar
import com.dms.flip.ui.component.TimePicker
import com.dms.flip.ui.component.TopBarIcon
import com.dms.flip.ui.settings.component.dialog.AvatarSourceBottomSheet
import com.dms.flip.ui.settings.component.dialog.CameraPermissionDialog
import com.dms.flip.ui.settings.component.dialog.NotificationPermissionDialog
import com.dms.flip.ui.settings.component.dialog.ThemeDialog
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.CameraPermissionHandler
import com.dms.flip.ui.util.FileProviderHelper
import com.dms.flip.ui.util.LightDarkPreview

enum class PermissionDialogType {
    NONE,
    RATIONALE,
    PERMANENTLY_DENIED
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    onNavigateBack: () -> Unit = {},
    onNavigateToManagePleasures: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var showThemeDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    var showAvatarSourceBottomSheet by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraPermissionDialogType by remember { mutableStateOf(PermissionDialogType.NONE) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                showNotificationPermissionDialog = true
            }
            onEvent(SettingsEvent.OnDailyReminderEnabledChanged(isGranted))
        }
    )

    // Launcher pour la galerie
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { onEvent(SettingsEvent.OnAvatarSelected(it)) }
        }
    )

    // Launcher pour la caméra
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                cameraImageUri?.let { onEvent(SettingsEvent.OnAvatarSelected(it)) }
            }
        }
    )

    // Launcher pour la permission caméra
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val uri = FileProviderHelper.createImageUri(context)
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            } else {
                activity?.let {
                    if (CameraPermissionHandler.isPermanentlyDenied(it)) {
                        cameraPermissionDialogType = PermissionDialogType.PERMANENTLY_DENIED
                    } else {
                        cameraPermissionDialogType = PermissionDialogType.RATIONALE
                    }
                }
            }
        }
    )

    // Fonction pour demander la permission caméra
    fun requestCameraPermission() {
        activity?.let {
            when {
                CameraPermissionHandler.isPermissionGranted(context) -> {
                    val uri = FileProviderHelper.createImageUri(context)
                    cameraImageUri = uri
                    cameraLauncher.launch(uri)
                }

                CameraPermissionHandler.shouldShowRationale(it) -> {
                    cameraPermissionDialogType = PermissionDialogType.RATIONALE
                }

                else -> {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        } ?: run {
            // Fallback si activity est null
            val uri = FileProviderHelper.createImageUri(context)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    if (showThemeDialog) {
        ThemeDialog(
            currentTheme = uiState.theme,
            onThemeSelected = { newTheme ->
                onEvent(SettingsEvent.OnThemeChanged(newTheme))
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showTimePicker) {
        TimePicker(context, uiState.reminderTime, {
            onEvent(SettingsEvent.OnReminderTimeChanged(it))
        }) {
            showTimePicker = false
        }
    }

    if (showNotificationPermissionDialog) {
        NotificationPermissionDialog(
            onDismiss = { showNotificationPermissionDialog = false }
        )
    }

    if (showAvatarSourceBottomSheet) {
        AvatarSourceBottomSheet(
            onDismiss = { showAvatarSourceBottomSheet = false },
            onCameraClick = {
                requestCameraPermission()
            },
            onGalleryClick = {
                galleryLauncher.launch("image/*")
            }
        )
    }

    // Dialog pour la permission caméra
    when (cameraPermissionDialogType) {
        PermissionDialogType.RATIONALE -> {
            CameraPermissionDialog(
                onDismiss = {
                    cameraPermissionDialogType = PermissionDialogType.NONE
                },
                isPermanentlyDenied = false
            )
        }

        PermissionDialogType.PERMANENTLY_DENIED -> {
            CameraPermissionDialog(
                onDismiss = {
                    cameraPermissionDialogType = PermissionDialogType.NONE
                },
                isPermanentlyDenied = true
            )
        }

        PermissionDialogType.NONE -> { /* Pas de dialog */
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        FlipTopBar(
            title = stringResource(R.string.settings_title),
            startTopBarIcon = TopBarIcon(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.settings_back),
                onClick = onNavigateBack
            )
        )

        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // User Profile Card
            item {
                uiState.userInfo?.let {
                    UserProfileCard(
                        userInfo = it,
                        isUploading = uiState.isUploading,
                        onEditProfile = { /* TODO: Navigate to profile edit */ },
                        onAvatarClicked = { showAvatarSourceBottomSheet = true }
                    )
                }
            }

            // Notifications Section
            item {
                SettingsSection(title = stringResource(R.string.settings_notifications_title)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        SettingsSwitchItem(
                            icon = Icons.Outlined.Notifications,
                            title = stringResource(R.string.settings_daily_reminder_title),
                            checked = uiState.dailyReminderEnabled,
                            onCheckedChange = {
                                if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onEvent(SettingsEvent.OnDailyReminderEnabledChanged(it))
                                }
                            }
                        )

                        AnimatedVisibility(visible = uiState.dailyReminderEnabled) {
                            Column {
                                SettingsDivider()
                                SettingsClickableItem(
                                    icon = Icons.Outlined.AccessTime,
                                    title = stringResource(R.string.settings_reminder_time_title),
                                    value = uiState.reminderTime,
                                    onClick = { showTimePicker = true },
                                    showChevron = false
                                )
                            }
                        }
                    }
                }
            }

            // Appearance Section
            item {
                SettingsSection(title = "Configuration") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        SettingsClickableItem(
                            icon = Icons.Outlined.Edit,
                            title = stringResource(R.string.manage_pleasures_title),
                            onClick = onNavigateToManagePleasures
                        )

                        SettingsDivider()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            val themeLabel = when (uiState.theme) {
                                Theme.LIGHT -> stringResource(R.string.settings_theme_light)
                                Theme.DARK -> stringResource(R.string.settings_theme_dark)
                                Theme.SYSTEM -> stringResource(R.string.settings_theme_system)
                            }
                            SettingsThemeItem(
                                title = stringResource(R.string.settings_theme_title),
                                subtitle = themeLabel,
                                onClick = { showThemeDialog = true }
                            )
                        }
                    }
                }
            }

            // About Section
            item {
                SettingsSection(title = stringResource(R.string.settings_about_title)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        SettingsClickableItem(
                            icon = Icons.Outlined.Policy,
                            title = stringResource(R.string.privacy_policy),
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    context.getString(R.string.privacy_policy_url).toUri()
                                )
                                context.startActivity(intent)
                            }
                        )

                        SettingsDivider()

                        SettingsClickableItem(
                            icon = Icons.Outlined.Gavel,
                            title = stringResource(R.string.terms_of_use),
                            onClick = { /* TODO: Navigate to terms */ }
                        )

                        SettingsDivider()

                        SettingsClickableItem(
                            icon = Icons.Outlined.StarRate,
                            title = stringResource(R.string.settings_rate_app),
                            onClick = { rateApp(context) }
                        )

                        SettingsDivider()

                        SettingsClickableItem(
                            icon = Icons.Outlined.Info,
                            title = stringResource(R.string.settings_app_version),
                            value = "1.0.0",
                            onClick = {},
                            showChevron = false
                        )
                    }
                }
            }

            // Account Actions
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sign Out Button
                    Button(
                        onClick = { onEvent(SettingsEvent.OnSignOut) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.settings_sign_out),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Delete Account Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEvent(SettingsEvent.DeleteAccount) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.settings_delete_account),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun UserProfileCard(
    userInfo: UserInfo,
    isUploading: Boolean,
    onEditProfile: () -> Unit,
    onAvatarClicked: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable(enabled = !isUploading) { onAvatarClicked() }
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                CommunityAvatar(
                    imageUrl = userInfo.avatarUrl,
                    fallbackText = userInfo.username?.firstOrNull()?.uppercase() ?: "?",
                    textStyle = MaterialTheme.typography.headlineLarge,
                    size = 100.dp
                )

                // Overlay sombre pendant le chargement
                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    )
                }
            }

            // Loader circulaire pendant l'upload
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                // Icône caméra en bas à droite
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onAvatarClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Changer la photo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // User name
        Text(
            text = userInfo.username ?: "",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Edit profile button
        Button(
            onClick = onEditProfile,
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(R.string.settings_edit_profile),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
        )
        content()
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onSurface,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit,
    showChevron: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showChevron) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsThemeItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Outlined.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 56.dp, end = 16.dp)
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.1f))
    )
}

private fun rateApp(context: Context) {
    val packageName = context.packageName
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
        )
    } catch (_: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri()
            )
        )
    }
}

@LightDarkPreview
@Composable
private fun SettingsScreenPreview() {
    FlipTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SettingsScreen(
                uiState = SettingsUiState(
                    userInfo = UserInfo(
                        username = "Aya Nakamura",
                        email = "aya.nakamura@email.com"
                    ),
                    dailyReminderEnabled = true,
                    reminderTime = "09:00",
                    theme = Theme.DARK,
                    isUploading = false
                ),
                onEvent = {},
                onNavigateBack = {},
                onNavigateToManagePleasures = {},
                onNavigateToStatistics = {}
            )
        }
    }
}
