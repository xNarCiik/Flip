package com.dms.flip.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dms.flip.ui.community.component.FriendListItem
import com.dms.flip.ui.component.dialog.UploadingDialog
import com.dms.flip.ui.onboarding.component.OnboardingNavigation
import com.dms.flip.ui.onboarding.component.OnboardingProgressBar
import com.dms.flip.ui.onboarding.component.step.NotificationPermissionStep
import com.dms.flip.ui.onboarding.component.step.PleasuresStep
import com.dms.flip.ui.onboarding.component.step.ReminderTimeStep
import com.dms.flip.ui.onboarding.component.step.UsernameStep
import com.dms.flip.ui.onboarding.component.step.WelcomeScreen
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewFriends

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    UploadingDialog(
        isVisible = uiState.isUploadingAvatar
    )

    AnimatedContent(
        targetState = uiState.showWelcome,
        transitionSpec = {
            slideInHorizontally { fullWidth -> fullWidth } + fadeIn() togetherWith
                    slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut()
        },
        label = "Onboarding transition"
    ) { showWelcome ->
        when (showWelcome) {
            true -> {
                WelcomeScreen(
                    modifier = modifier,
                    onStartClick = { viewModel.onEvent(OnboardingEvent.OnStartClick) }
                )
            }

            else -> {
                OnboardingContent(
                    modifier = modifier,
                    uiState = uiState,
                    onEvent = viewModel::onEvent
                )
            }
        }
    }
}

@Composable
private fun OnboardingContent(
    modifier: Modifier = Modifier,
    uiState: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit
) {
    val totalSteps = if (uiState.notificationInitiallyEnabled) 3 else 4

    Column(modifier = modifier.fillMaxSize()) {
        OnboardingProgressBar(
            currentStep = uiState.currentStep,
            totalSteps = totalSteps,
            notificationStepSkipped = uiState.notificationInitiallyEnabled
        )
        var isForward by remember { mutableStateOf(true) }

        // Détecter la direction de navigation
        val currentStepOrder = uiState.currentStep.ordinal
        var previousStep by remember { mutableStateOf(OnboardingStep.USERNAME) }
        val previousStepOrder = previousStep.ordinal

        if (currentStepOrder != previousStepOrder) {
            isForward = currentStepOrder > previousStepOrder
            previousStep = uiState.currentStep
        }

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    val slideDirection = if (isForward) 1 else -1

                    (slideInHorizontally(
                        initialOffsetX = { it * slideDirection },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(
                        animationSpec = tween(300)
                    )) togetherWith (slideOutHorizontally(
                        targetOffsetX = { -it * slideDirection },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeOut(
                        animationSpec = tween(300)
                    )) using SizeTransform(clip = false)
                },
                label = "onboardingContent"
            ) { step ->
                when (step) {
                    OnboardingStep.USERNAME -> UsernameStep(
                        username = uiState.username,
                        usernameError = uiState.usernameError,
                        isCheckingUsername = uiState.isCheckingUsername,
                        avatarUrl = uiState.avatarUrl,
                        onUsernameChange = { username ->
                            onEvent(OnboardingEvent.UpdateUsername(username))
                        },
                        onAvatarSelected = { uri ->
                            onEvent(OnboardingEvent.OnAvatarSelected(uri))
                        }
                    )

                    OnboardingStep.PLEASURES -> PleasuresStep(
                        pleasures = uiState.availablePleasures,
                        onTogglePleasure = { onEvent(OnboardingEvent.TogglePleasure(it)) }
                    )

                    OnboardingStep.NOTIFICATIONS -> NotificationPermissionStep(
                        notificationEnabled = uiState.notificationEnabled,
                        onNotificationToggle = {
                            onEvent(
                                OnboardingEvent.UpdateNotificationEnabled(
                                    it
                                )
                            )
                        },
                        showSkipWarning = uiState.showNotificationSkipWarning,
                        onDismissWarning = { onEvent(OnboardingEvent.DismissNotificationWarning) }
                    )

                    OnboardingStep.REMINDER_TIME -> ReminderTimeStep(
                        reminderTime = uiState.reminderTime,
                        onTimeChange = { onEvent(OnboardingEvent.UpdateReminderTime(it)) }
                    )
                }
            }
        }

        // Navigation buttons (cachés sur WELCOME)
        OnboardingNavigation(
            currentStep = uiState.currentStep,
            canGoNext = canGoNext(uiState = uiState),
            onPrevious = { onEvent(OnboardingEvent.PreviousStep) },
            onNext = { onEvent(OnboardingEvent.NextStep) }
        )
    }
}

private fun canGoNext(uiState: OnboardingUiState): Boolean {
    return when (uiState.currentStep) {
        OnboardingStep.USERNAME -> uiState.username.isNotBlank()
        OnboardingStep.PLEASURES -> uiState.availablePleasures.any { it.isEnabled }
        OnboardingStep.NOTIFICATIONS -> true
        OnboardingStep.REMINDER_TIME -> true
    }
}

@LightDarkPreview
@Composable
private fun OnboardingContentPreview() {
    FlipTheme {
        Surface {
            OnboardingContent(
                uiState = OnboardingUiState(),
                onEvent = {}
            )
        }
    }
}
