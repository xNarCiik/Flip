package com.dms.flip.ui.dailyflip.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    val categoryColor = pleasureHistory.pleasureCategory.iconTint

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero section with icon
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                    scaleIn(
                        initialScale = 0.8f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )
        ) {
            HeroSection(
                pleasureHistory = pleasureHistory,
                categoryColor = categoryColor
            )
        }

        // Information cards
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(
                spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            ) + scaleIn(
                initialScale = 0.9f,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            )
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

                InfoCard(
                    icon = Icons.Default.Event,
                    label = stringResource(R.string.pleasure_detail_completed_date_label),
                    value = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                        .format(Date()), // TODO: Use actual completedAt from pleasure
                    categoryColor = categoryColor
                )
            }
        }

        // Bottom decorative element
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                    scaleIn(
                        initialScale = 0.95f,
                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                    )
        ) {
            DecorativeQuote(categoryColor = categoryColor)
        }
    }
}

@Composable
private fun HeroSection(
    pleasureHistory: PleasureHistory,
    categoryColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Large icon with gradient background
        Box(
            modifier = Modifier
                .size(140.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = categoryColor.copy(alpha = 0.4f)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            categoryColor.copy(alpha = 0.3f),
                            categoryColor.copy(alpha = 0.1f)
                        ) + listOf(Color.Transparent)
                    )
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

        // Title
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        categoryColor.copy(alpha = 0.08f),
                        categoryColor.copy(alpha = 0.03f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = categoryColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = categoryColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DecorativeQuote(
    categoryColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        categoryColor.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) + listOf(Color.Transparent) // Add transparent color to fill the gradient
                )
            )
            .border(
                width = 1.dp,
                color = categoryColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                tint = categoryColor.copy(alpha = 0.6f),
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = stringResource(R.string.pleasure_detail_quote),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
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
