package com.dms.flip.ui.pleasuredetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dms.flip.R
import com.dms.flip.domain.model.PleasureHistory
import com.dms.flip.domain.model.community.icon
import com.dms.flip.domain.model.community.iconTint
import com.dms.flip.domain.model.community.label
import com.dms.flip.ui.component.FlipTopBar
import com.dms.flip.ui.component.PleasureCard
import com.dms.flip.ui.component.TopBarIcon
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewDailyPleasure
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PleasureDetailScreen(
    modifier: Modifier = Modifier,
    pleasureHistory: PleasureHistory,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showHero by remember { mutableStateOf(false) }
    var showCards by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showHero = true
        delay(250)
        showCards = true
    }

    val categoryColor = pleasureHistory.pleasureCategory.iconTint

    Column(modifier = modifier.fillMaxSize()) {
        FlipTopBar(
            title = stringResource(R.string.pleasure_detail_title),
            startTopBarIcon = TopBarIcon(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.navigate_back),
                onClick = onNavigateBack
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AnimatedVisibility(
                visible = showHero,
                enter = fadeIn(tween(600, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.9f, animationSpec = tween(600, easing = FastOutSlowInEasing))
            ) {
                HeroSection(
                    pleasureHistory = pleasureHistory,
                    categoryColor = categoryColor,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            AnimatedVisibility(
                visible = showCards,
                enter = fadeIn(tween(700, delayMillis = 150, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(700, easing = FastOutSlowInEasing))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoCard(
                        icon = Icons.Default.Title,
                        label = stringResource(R.string.pleasure_detail_title_label),
                        value = pleasureHistory.pleasureTitle ?: "",
                        categoryColor = categoryColor
                    )

                    InfoCard(
                        icon = Icons.Default.Description,
                        label = stringResource(R.string.pleasure_detail_description_label),
                        value = pleasureHistory.pleasureDescription ?: "",
                        categoryColor = categoryColor
                    )

                    InfoCard(
                        icon = Icons.Default.Category,
                        label = stringResource(R.string.pleasure_detail_category_label),
                        value = stringResource(pleasureHistory.pleasureCategory.label),
                        categoryColor = categoryColor
                    )

                    pleasureHistory.completedAt?.let { completedAt ->
                        InfoCard(
                            icon = Icons.Default.Event,
                            label = stringResource(R.string.pleasure_detail_completed_date_label),
                            value = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                                .format(Date(completedAt)),
                            categoryColor = categoryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSection(
    pleasureHistory: PleasureHistory,
    categoryColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(categoryColor.copy(0.25f), Color.Transparent)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = pleasureHistory.pleasureCategory.icon,
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(70.dp)
            )
        }

        Text(
            text = pleasureHistory.pleasureTitle ?: "",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    categoryColor: Color,
    modifier: Modifier = Modifier
) {
    // TODO NEW ONE
    PleasureCard(
        icon = icon,
        iconTint = categoryColor,
        label = label,
        title = value,
        description = null,
        showChevron = false,
        isCompleted = true,
        onClick = {},
        modifier = modifier
    )
}

@LightDarkPreview
@Composable
private fun PleasureDetailScreenPreview() {
    FlipTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PleasureDetailScreen(
                pleasureHistory = previewDailyPleasure.toPleasureHistory(""),
                onNavigateBack = {}
            )
        }
    }
}
