package com.dms.flip.ui.history.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dms.flip.R
import com.dms.flip.domain.model.PleasureHistory
import com.dms.flip.ui.history.WeeklyDay
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.theme.flipGradients
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewWeeklyDays
import kotlinx.coroutines.delay
import java.util.Calendar

private enum class DayType {
    PLEASURE_INFO,
    DISCOVER,
    LOADING,
    LOCKED
}

@Composable
private fun getDayType(weeklyDay: WeeklyDay, isTodayLoading: Boolean): DayType {
    val item = weeklyDay.historyEntry

    val todayCalendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayMillis = todayCalendar.timeInMillis

    val dayCalendar = Calendar.getInstance().apply {
        timeInMillis = weeklyDay.dateMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val weeklyDayMillis = dayCalendar.timeInMillis

    return when {
        item != null -> DayType.PLEASURE_INFO
        weeklyDayMillis == todayMillis && isTodayLoading -> DayType.LOADING
        weeklyDayMillis == todayMillis -> DayType.DISCOVER
        else -> DayType.LOCKED
    }
}

@Composable
fun WeeklyPleasuresList(
    modifier: Modifier = Modifier,
    items: List<WeeklyDay>,
    isTodayLoading: Boolean = false,
    onCardClicked: (PleasureHistory) -> Unit,
    onDiscoverTodayClicked: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEachIndexed { index, weeklyDay ->
            val dayType = getDayType(weeklyDay, isTodayLoading)

            AnimatedDayItem(
                weeklyDay = weeklyDay,
                dayType = dayType,
                onClick = { weeklyDay.historyEntry?.let(onCardClicked) },
                onDiscoverClick = onDiscoverTodayClicked,
                animationDelay = index * 50
            )
        }
    }
}

@Composable
private fun AnimatedDayItem(
    weeklyDay: WeeklyDay,
    dayType: DayType,
    onClick: () -> Unit,
    onDiscoverClick: () -> Unit,
    animationDelay: Int = 0
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "alpha"
    )

    Box(modifier = Modifier.alpha(alpha)) {
        when (dayType) {
            DayType.PLEASURE_INFO -> PleasureInfoCard(
                weeklyDay = weeklyDay,
                onClick = onClick
            )

            DayType.LOADING -> TodayLoadingCard(
                weeklyDay = weeklyDay
            )

            DayType.DISCOVER -> TodayCard(
                weeklyDay = weeklyDay,
                onDiscoverClick = onDiscoverClick
            )

            DayType.LOCKED -> LockedDayCard(
                dayName = weeklyDay.dayName
            )
        }
    }
}

@Composable
private fun PleasureInfoCard(
    weeklyDay: WeeklyDay,
    onClick: () -> Unit
) {
    val historyEntry = weeklyDay.historyEntry ?: return
    val isCompleted = historyEntry.completed
    val category = historyEntry.pleasureCategory
    val categoryColor = category?.iconTint ?: MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isCompleted) 4.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = categoryColor.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        categoryColor.copy(alpha = 0.12f),
                        categoryColor.copy(alpha = 0.05f)
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container avec design moderne
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(14.dp),
                        spotColor = categoryColor.copy(alpha = 0.3f)
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                categoryColor.copy(alpha = 0.2f),
                                categoryColor.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                category?.let {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Contenu texte
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = weeklyDay.dayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = categoryColor
                )
                Text(
                    text = historyEntry.pleasureTitle ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                historyEntry.pleasureDescription?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        ),
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Indicateur de statut ou flèche
            if (isCompleted) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.status_done_checked),
                        tint = categoryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.see_details),
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.see_details),
                    tint = categoryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun TodayLoadingCard(weeklyDay: WeeklyDay) {
    val gradients = flipGradients()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(gradients.card),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = weeklyDay.dayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )

                Text(
                    text = stringResource(id = R.string.history_discovery_loading),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TodayCard(
    weeklyDay: WeeklyDay,
    onDiscoverClick: () -> Unit
) {
    val gradients = flipGradients()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(gradients.card),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = weeklyDay.dayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.history_ready_for_joy),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onDiscoverClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(24.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.history_discover_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LockedDayCard(dayName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .alpha(0.6f)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = stringResource(R.string.history_locked_day_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun WeeklyPleasuresListPreview() {
    FlipTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                WeeklyPleasuresList(
                    items = previewWeeklyDays,
                    isTodayLoading = false,
                    onCardClicked = {},
                    onDiscoverTodayClicked = {}
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun TodayLoadingCardPreview() {
    FlipTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TodayLoadingCard(
                    weeklyDay = WeeklyDay(
                        dayName = "Aujourd'hui",
                        historyEntry = null,
                        dateMillis = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PleasureInfoCardPreview() {
    FlipTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                previewWeeklyDays.filter { it.historyEntry != null }.forEach { day ->
                    PleasureInfoCard(
                        weeklyDay = day,
                        onClick = {}
                    )
                }
            }
        }
    }
}
