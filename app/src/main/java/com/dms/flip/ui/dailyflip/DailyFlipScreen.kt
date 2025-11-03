package com.dms.flip.ui.dailyflip

import android.R.attr.label
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dms.flip.R
import com.dms.flip.data.model.PleasureCategory
import com.dms.flip.ui.community.component.CommunityAvatar
import com.dms.flip.ui.component.ErrorState
import com.dms.flip.ui.component.FlipTopBar
import com.dms.flip.ui.component.LoadingState
import com.dms.flip.ui.component.TopBarIcon
import com.dms.flip.ui.dailyflip.component.DailyFlipCompletedContent
import com.dms.flip.ui.dailyflip.component.DailyFlipContent
import com.dms.flip.ui.dailyflip.component.DailyFlipSetupContent
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewDailyPleasure
import com.google.android.play.integrity.internal.u

@Composable
fun DailyFlipScreen(
    modifier: Modifier = Modifier,
    uiState: DailyFlipUiState,
    onEvent: (DailyFlipEvent) -> Unit = {},
    navigateToManagePleasures: () -> Unit = {},
    navigateToSettings: () -> Unit = {}
) {
    val screenState = uiState.screenState
    val avatarFallback = uiState.userInfo?.username?.firstOrNull()?.uppercase() ?: "?"

    Column(modifier = modifier.fillMaxSize()) {
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
                            size = 40.dp
                        )
                    }
                )
            )
        )

        when (screenState) {
            is DailyFlipScreenState.Error -> {
                ErrorState(message = uiState.screenState.message) {
                    onEvent(DailyFlipEvent.Reload)
                }
            }

            is DailyFlipScreenState.Loading -> {
                LoadingState(modifier = Modifier.fillMaxSize())
            }

            is DailyFlipScreenState.Completed -> {
                DailyFlipCompletedContent(modifier = Modifier.fillMaxSize())
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
}

@Composable
private fun ContentWithHeader(
    modifier: Modifier = Modifier, uiState: DailyFlipUiState,
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
fun DailyFlipNotFlippedScreenPreview() {
    FlipTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DailyFlipScreen(
                uiState = DailyFlipUiState(
                    screenState = DailyFlipScreenState.Ready(
                        availableCategories = PleasureCategory.entries,
                        dailyPleasure = null,
                        isCardFlipped = false
                    )
                )
            )
        }
    }
}

@LightDarkPreview
@Composable
fun DailyFlipFlippedScreenPreview() {
    FlipTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DailyFlipScreen(
                uiState = DailyFlipUiState(
                    headerMessage = "Votre plaisir du jour",
                    screenState = DailyFlipScreenState.Ready(
                        availableCategories = PleasureCategory.entries,
                        dailyPleasure = previewDailyPleasure,
                        isCardFlipped = true
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
                    screenState = DailyFlipScreenState.Completed
                )
            )
        }
    }
}
