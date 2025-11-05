package com.dms.flip.ui.dailyflip

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dms.flip.R
import com.dms.flip.domain.model.PleasureHistory
import com.dms.flip.domain.model.community.iconTint
import com.dms.flip.ui.component.CommunityAvatar
import com.dms.flip.ui.component.ErrorState
import com.dms.flip.ui.component.FlipTopBar
import com.dms.flip.ui.component.LoadingState
import com.dms.flip.ui.component.TopBarIcon
import com.dms.flip.ui.dailyflip.component.DailyFlipCompletedContent
import com.dms.flip.ui.dailyflip.component.DailyFlipContent
import com.dms.flip.ui.dailyflip.component.DailyFlipSetupContent
import com.dms.flip.ui.dailyflip.component.ShareLoadingDialog
import com.dms.flip.ui.dailyflip.component.ShareMomentBottomSheet
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyFlipScreen(
    modifier: Modifier = Modifier,
    uiState: DailyFlipUiState,
    onEvent: (DailyFlipEvent) -> Unit = {},
    navigateToManagePleasures: () -> Unit = {},
    navigateToSettings: () -> Unit = {},
    navigateToPleasureDetail: (PleasureHistory) -> Unit = {}
) {
    val context = LocalContext.current
    val screenState = uiState.screenState
    val avatarFallback = uiState.userInfo?.username?.firstOrNull()?.uppercase() ?: "?"
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSharing, uiState.shareError, uiState.showShareBottomSheet) {
        val justSharedSuccessfully =
            !uiState.isSharing &&
                    !uiState.showShareBottomSheet &&
                    uiState.shareError == null &&
                    uiState.shareComment.isEmpty() && uiState.lastShareCompleted

        if (justSharedSuccessfully) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.share_success)
            )
            onEvent(DailyFlipEvent.OnShareSnackbarShown)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            val pleasure = when (screenState) {
                is DailyFlipScreenState.Completed -> screenState.dailyPleasure
                is DailyFlipScreenState.Ready -> screenState.dailyPleasure
                else -> null
            }

            // TopBar
            FlipTopBar(
                title = stringResource(R.string.app_name),
                endTopBarIcons = listOf(
                    TopBarIcon(
                        contentDescription = stringResource(R.string.settings_title),
                        onClick = navigateToSettings,
                        customContent = {
                            CommunityAvatar(
                                imageUrl = uiState.userInfo?.avatarUrl,
                                fallbackText = avatarFallback,
                                size = 40.dp,
                                borderColor = pleasure?.category?.iconTint ?: Color.Transparent
                            )
                        }
                    )
                )
            )

            when (screenState) {
                is DailyFlipScreenState.Error -> {
                    ErrorState(message = screenState.message) {
                        onEvent(DailyFlipEvent.Reload)
                    }
                }

                is DailyFlipScreenState.Loading -> {
                    LoadingState(modifier = Modifier.fillMaxSize())
                }

                is DailyFlipScreenState.Completed -> {
                    DailyFlipCompletedContent(
                        modifier = Modifier.fillMaxSize(),
                        completedPleasure = screenState.dailyPleasure,
                        onShareClick = { onEvent(DailyFlipEvent.OnShareClicked) },
                        onPleasureClick = {
                            screenState.dailyPleasure?.let {
                                navigateToPleasureDetail(
                                    screenState.dailyPleasure.toPleasureHistory("")
                                        .copy(completedAt = Date().time)
                                )
                            }
                        }
                    )
                }

                else -> {
                    ContentWithHeader(
                        uiState = uiState,
                        onEvent = onEvent,
                        navigateToManagePleasures = navigateToManagePleasures
                    )
                }
            }
        }

        // Share bottom sheet
        if (screenState is DailyFlipScreenState.Completed) {
            ShareMomentBottomSheet(
                isVisible = uiState.showShareBottomSheet,
                pleasure = screenState.dailyPleasure,
                comment = uiState.shareComment,
                photoUri = uiState.sharePhotoUri,
                isSharing = uiState.isSharing,
                error = uiState.shareError,
                onDismiss = { onEvent(DailyFlipEvent.OnShareDismissed) },
                onCommentChange = { onEvent(DailyFlipEvent.OnShareCommentChanged(it)) },
                onPhotoSelected = { onEvent(DailyFlipEvent.OnSharePhotoSelected(it)) },
                onPhotoRemoved = { onEvent(DailyFlipEvent.OnSharePhotoRemoved) },
                onSubmit = { onEvent(DailyFlipEvent.OnShareSubmit) }
            )
        }

        // Loading dialog for sharing
        ShareLoadingDialog(isVisible = uiState.isSharing)

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun ContentWithHeader(
    modifier: Modifier = Modifier,
    uiState: DailyFlipUiState,
    onEvent: (DailyFlipEvent) -> Unit = {},
    navigateToManagePleasures: () -> Unit = {}
) {
    Column(modifier = modifier.padding(vertical = 24.dp)) {
        // Header Message
        AnimatedContent(
            modifier = Modifier.zIndex(0f),
            targetState = uiState.headerMessage,
            transitionSpec = {
                (fadeIn(
                    tween(
                        500,
                        delayMillis = 100
                    )
                ) + scaleIn(initialScale = 0.95f)) togetherWith
                        (fadeOut(tween(500)) + scaleOut(targetScale = 1.05f))
            },
            label = "HeaderMessageContent"
        ) { animatedText ->
            HeaderMessage(message = animatedText)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .zIndex(1f),
            contentAlignment = Alignment.Center
        ) {
            when (uiState.screenState) {
                is DailyFlipScreenState.SetupRequired -> {
                    DailyFlipSetupContent(
                        currentPleasureCount = uiState.screenState.pleasureCount,
                        requiredCount = MinimumPleasuresCount,
                        onConfigureClick = navigateToManagePleasures
                    )
                }

                is DailyFlipScreenState.Ready -> {
                    DailyFlipContent(
                        uiState = uiState.screenState,
                        onEvent = onEvent
                    )
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun HeaderMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .animateContentSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

// ========== PREVIEWS ==========
@LightDarkPreview
@Composable
fun DailyFlipErrorScreenPreview() {
    FlipTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DailyFlipScreen(
                uiState = DailyFlipUiState(
                    screenState = DailyFlipScreenState.Error(message = stringResource(R.string.generic_error_message))
                )
            )
        }
    }
}

@LightDarkPreview
@Composable
fun DailyFlipSetupScreenPreview() {
    FlipTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DailyFlipScreen(
                uiState = DailyFlipUiState(
                    headerMessage = "À deux pas de l'aventure...",
                    screenState = DailyFlipScreenState.SetupRequired(
                        pleasureCount = 1
                    )
                )
            )
        }
    }
}

@LightDarkPreview
@Composable
fun DailyFlipCompletedScreenPreview() {
    FlipTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DailyFlipScreen(
                uiState = DailyFlipUiState(
                    screenState = DailyFlipScreenState.Completed()
                )
            )
        }
    }
}
