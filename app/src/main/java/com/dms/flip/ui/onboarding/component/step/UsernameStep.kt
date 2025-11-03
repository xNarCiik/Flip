package com.dms.flip.ui.onboarding.component.step

import android.Manifest
import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dms.flip.R
import com.dms.flip.domain.usecase.user.UsernameError
import com.dms.flip.domain.usecase.user.ValidateUsernameFormatUseCase
import com.dms.flip.ui.component.AvatarPicker
import com.dms.flip.ui.settings.PermissionDialogType
import com.dms.flip.ui.settings.component.dialog.AvatarSourceBottomSheet
import com.dms.flip.ui.settings.component.dialog.CameraPermissionDialog
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.CameraPermissionHandler
import com.dms.flip.ui.util.FileProviderHelper
import com.dms.flip.ui.util.LightDarkPreview

@Composable
fun UsernameStep(
    username: String,
    usernameError: UsernameError?,
    isCheckingUsername: Boolean,
    avatarUrl: String?,
    onUsernameChange: (String) -> Unit,
    onAvatarSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var showAvatarSourceBottomSheet by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraPermissionDialogType by remember { mutableStateOf(PermissionDialogType.NONE) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { onAvatarSelected(it) }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                cameraImageUri?.let { onAvatarSelected(it) }
            }
        }
    )

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
            val uri = FileProviderHelper.createImageUri(context)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
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

        PermissionDialogType.NONE -> {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_username_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(id = R.string.onboarding_username_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        AvatarPicker(
            avatarUrl = avatarUrl,
            size = 120.dp,
            onClick = { showAvatarSourceBottomSheet = true }
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(id = R.string.onboarding_username_label)) },
            placeholder = { Text(stringResource(id = R.string.onboarding_username_placeholder)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = when {
                    usernameError != null -> MaterialTheme.colorScheme.error
                    username.isNotBlank() && !isCheckingUsername -> {
                        MaterialTheme.colorScheme.primary
                    }

                    else -> MaterialTheme.colorScheme.outline
                },
                unfocusedBorderColor = when {
                    usernameError != null -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.outline
                },
                focusedLabelColor = when {
                    usernameError != null -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null
                )
            },
            trailingIcon = {
                when {
                    isCheckingUsername -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    usernameError != null -> {
                        Icon(
                            imageVector = Icons.Outlined.Error,
                            contentDescription = "Erreur",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    username.length >= ValidateUsernameFormatUseCase.MIN_LENGTH -> {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Valide",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            isError = usernameError != null,
            supportingText = {
                AnimatedVisibility(visible = usernameError != null || username.isNotBlank()) {
                    when (usernameError) {
                        UsernameError.EMPTY -> {
                            Text(
                                text = stringResource(R.string.error_username_empty),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        UsernameError.TOO_SHORT -> {
                            Text(
                                text = stringResource(
                                    R.string.error_username_too_short,
                                    ValidateUsernameFormatUseCase.MIN_LENGTH
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        UsernameError.TOO_LONG -> {
                            Text(
                                text = stringResource(
                                    R.string.error_username_too_long,
                                    ValidateUsernameFormatUseCase.MAX_LENGTH
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        UsernameError.INVALID_CHARACTERS -> {
                            Text(
                                text = stringResource(R.string.error_username_invalid_characters),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        UsernameError.ALREADY_TAKEN -> {
                            Text(
                                text = stringResource(R.string.error_username_already_taken),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        null -> {
                            if (username.isNotBlank() && !isCheckingUsername) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.username_available),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(
                R.string.username_requirements,
                ValidateUsernameFormatUseCase.MIN_LENGTH,
                ValidateUsernameFormatUseCase.MAX_LENGTH
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@LightDarkPreview
@Composable
private fun UsernameStepEmptyPreview() {
    FlipTheme {
        Surface {
            UsernameStep(
                username = "",
                usernameError = null,
                isCheckingUsername = false,
                avatarUrl = null,
                onUsernameChange = {},
                onAvatarSelected = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun UsernameStepValidPreview() {
    FlipTheme {
        Surface {
            UsernameStep(
                username = "Marie",
                usernameError = null,
                isCheckingUsername = false,
                avatarUrl = null,
                onUsernameChange = {},
                onAvatarSelected = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun UsernameStepErrorPreview() {
    FlipTheme {
        Surface {
            UsernameStep(
                username = "Ma",
                usernameError = UsernameError.TOO_SHORT,
                isCheckingUsername = false,
                avatarUrl = null,
                onUsernameChange = {},
                onAvatarSelected = {}
            )
        }
    }
}
