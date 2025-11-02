package com.dms.flip.ui.dailyflip

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import com.dms.flip.R
import com.dms.flip.data.model.PleasureCategory
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

@Composable
fun DailyFlipScreen(
    modifier: Modifier = Modifier,
    uiState: DailyFlipUiState,
    onEvent: (DailyFlipEvent) -> Unit = {},
    navigateToManagePleasures: () -> Unit = {},
    navigateToSettings: () -> Unit = {}
) {
    val screenState = uiState.screenState

    Column(modifier = modifier.fillMaxSize()) {
        FlipTopBar(
            title = stringResource(R.string.app_name),
            endTopBarIcons = listOf(
                TopBarIcon(
                    icon = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings_title),
                    onClick = navigateToSettings
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
        ) {
            // Header Message
            if (uiState.headerMessage.isNotBlank()) {
                HeaderMessage(message = uiState.headerMessage)
            }

            val contentModifier = Modifier
                .fillMaxWidth()
                .weight(1f)

            if (screenState is DailyFlipScreenState.Loading) {
                LoadingState(modifier = contentModifier)
            } else {
                Box(
                    modifier = contentModifier.animateContentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(targetState = screenState, label = "DailyFlipTransition") { state ->
                        when (state) {
                            is DailyFlipScreenState.Error -> {
                                ErrorState(message = state.message) {
                                    onEvent(DailyFlipEvent.Reload)
                                }
                            }

                            is DailyFlipScreenState.SetupRequired -> {
                                DailyFlipSetupContent(
                                    currentPleasureCount = state.pleasureCount,
                                    requiredCount = MinimumPleasuresCount,
                                    onConfigureClick = navigateToManagePleasures
                                )
                            }

                            is DailyFlipScreenState.Ready -> {
                                DailyFlipContent(
                                    uiState = state,
                                    onEvent = onEvent
                                )
                            }

                            is DailyFlipScreenState.Completed -> {
                                DailyFlipCompletedContent()
                            }

                            else -> {}
                        }
                    }
                }
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
            .padding(vertical = 16.dp)
            .animateContentSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.headlineMedium.lineHeight * 1.2
        )
    }
}

// ========== PREVIEWS ==========
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
                    headerMessage = "Découvrez votre plaisir du jour",
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
                    headerMessage = "Félicitations pour cette belle journée ! 🎉",
                    screenState = DailyFlipScreenState.Completed
                )
            )
        }
    }
}
