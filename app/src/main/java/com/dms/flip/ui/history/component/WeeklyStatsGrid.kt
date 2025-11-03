package com.dms.flip.ui.history.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dms.flip.R
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import kotlinx.coroutines.delay

@Composable
fun WeeklyStatsGrid(
    pleasuresCount: Int,
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Carte 1 : Plaisirs de la semaine
        StatCard(
            value = pleasuresCount,
            label = stringResource(R.string.history_pleasures_this_week),
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            textColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        // Carte 2 : Jours de série (Streak)
        StatCard(
            value = streakDays,
            label = stringResource(R.string.history_streak_days),
            backgroundColor = Color(0xFFFCD34D).copy(alpha = 0.2f),
            textColor = Color(0xFFFCD34D),
            icon = {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFCD34D),
                    modifier = Modifier.size(28.dp)
                )
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: Int,
    label: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) icon()

                AnimatedCounter(
                    targetValue = value,
                    textColor = textColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedCounter(
    targetValue: Int,
    textColor: Color,
    durationMillis: Int = 300
) {
    var displayedValue by remember { mutableIntStateOf(targetValue) }
    var previousValue by remember { mutableIntStateOf(targetValue) }

    LaunchedEffect(targetValue) {
        previousValue = displayedValue
        val step = if (targetValue > previousValue) 1 else -1
        val diff = kotlin.math.abs(targetValue - previousValue)
        if (diff == 0) return@LaunchedEffect

        val stepDuration = durationMillis / diff
        for (i in 1..diff) {
            delay(stepDuration.toLong())
            displayedValue += step
        }
    }

    AnimatedContent(
        targetState = displayedValue,
        transitionSpec = {
            slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(200, easing = LinearOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(200)) with
                    slideOutVertically(
                        targetOffsetY = { -it / 3 },
                        animationSpec = tween(200, easing = LinearOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(200))
        },
        label = "animatedCounter"
    ) { value ->
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@LightDarkPreview
@Composable
private fun WeeklyStatsGridPreview() {
    FlipTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                WeeklyStatsGrid(
                    pleasuresCount = 2,
                    streakDays = 5
                )

                WeeklyStatsGrid(
                    pleasuresCount = 7,
                    streakDays = 12
                )
            }
        }
    }
}
