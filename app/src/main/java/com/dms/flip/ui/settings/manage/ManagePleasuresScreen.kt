package com.dms.flip.ui.settings.manage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dms.flip.R
import com.dms.flip.ui.component.FlipTopBar
import com.dms.flip.ui.component.SelectablePleasureItem
import com.dms.flip.ui.component.TopBarIcon
import com.dms.flip.ui.settings.manage.component.AddPleasureBottomSheet
import com.dms.flip.ui.settings.manage.component.DeleteConfirmationDialog
import com.dms.flip.ui.settings.manage.component.ManagePleasuresEmptyState
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewDailyPleasure

@Composable
fun ManagePleasuresScreen(
    modifier: Modifier = Modifier,
    uiState: ManagePleasuresUiState,
    onEvent: (ManagePleasuresEvent) -> Unit,
    navigateBack: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        FlipTopBar(
            title = stringResource(R.string.manage_pleasures_title),
            startTopBarIcon = TopBarIcon(
                icon = if (uiState.isSelectionMode) Icons.Default.Cancel else Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                onClick = {
                    if (uiState.isSelectionMode) {
                        onEvent(ManagePleasuresEvent.OnLeaveSelectionMode)
                    } else {
                        navigateBack()
                    }
                }
            ),
            endTopBarIcons = if (!uiState.isSelectionMode) {
                listOf(
                    TopBarIcon(
                        icon = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.edit_mode),
                        onClick = { onEvent(ManagePleasuresEvent.OnEnterSelectionMode) }
                    )
                )
            } else {
                emptyList()
            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                InfoCard(
                    onAddClick = { onEvent(ManagePleasuresEvent.OnAddPleasureClicked) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.pleasures.isEmpty()) {
                    ManagePleasuresEmptyState(
                        onAddClick = { onEvent(ManagePleasuresEvent.OnAddPleasureClicked) }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = if (uiState.selectedPleasures.isNotEmpty()) 80.dp else 0.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.pleasures,
                            key = { it.id }
                        ) { pleasure ->
                            SelectablePleasureItem(
                                pleasure = pleasure,
                                isSelectionMode = uiState.isSelectionMode,
                                isSelected = uiState.selectedPleasures.contains(pleasure.id),
                                onToggle = { onEvent(ManagePleasuresEvent.OnPleasureToggled(pleasure)) },
                                onSelect = { onEvent(ManagePleasuresEvent.OnPleasureSelected(pleasure.id)) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }

            this@Column.AnimatedVisibility(
                visible = uiState.isSelectionMode && uiState.selectedPleasures.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                DeleteButton(
                    count = uiState.selectedPleasures.size,
                    onClick = { onEvent(ManagePleasuresEvent.OnDeleteMultiplePleasuresClicked) }
                )
            }
        }
    }

    if (uiState.showAddDialog) {
        AddPleasureBottomSheet(
            title = uiState.newPleasureTitle,
            description = uiState.newPleasureDescription,
            category = uiState.newPleasureCategory,
            titleError = uiState.titleError,
            descriptionError = uiState.descriptionError,
            onDismiss = { onEvent(ManagePleasuresEvent.OnBottomSheetDismissed) },
            onTitleChange = { onEvent(ManagePleasuresEvent.OnTitleChanged(it)) },
            onDescriptionChange = { onEvent(ManagePleasuresEvent.OnDescriptionChanged(it)) },
            onCategoryChange = { onEvent(ManagePleasuresEvent.OnCategoryChanged(it)) },
            onSave = { onEvent(ManagePleasuresEvent.OnSavePleasureClicked) }
        )
    }

    if (uiState.showDeleteConfirmation) {
        DeleteConfirmationDialog(
            onConfirm = { onEvent(ManagePleasuresEvent.OnDeleteConfirmed) },
            onDismiss = { onEvent(ManagePleasuresEvent.OnDeleteCancelled) }
        )
    }
}

@Composable
private fun InfoCard(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.manage_pleasures_info_card_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = stringResource(R.string.manage_pleasures_info_card_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.add_new_pleasure_button),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.delete_pleasures_button, count),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun ManagePleasuresScreenPreview() {
    FlipTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ManagePleasuresScreen(
                uiState = ManagePleasuresUiState(
                    pleasures = List(10) { previewDailyPleasure },
                ),
                onEvent = {},
                navigateBack = {}
            )
        }
    }
}
