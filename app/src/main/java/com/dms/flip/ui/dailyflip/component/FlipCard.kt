package com.dms.flip.ui.dailyflip.component

import android.R.attr.rotation
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dms.flip.R
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.theme.flipGradients
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewDailyPleasure
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PleasureCard(
    modifier: Modifier = Modifier,
    pleasure: Pleasure?,
    flipped: Boolean,
    durationRotation: Int,
    onCardFlipped: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var isFlipped by remember { mutableStateOf(flipped) }
    var shouldAnimate by remember { mutableStateOf(false) }

    LaunchedEffect(flipped) {
        if (isFlipped != flipped) {
            shouldAnimate = false
            isFlipped = flipped
        }
    }

    LaunchedEffect(pleasure) {
        if (pleasure != null) {
            if (!isFlipped) {
                shouldAnimate = true
                isFlipped = true
            }
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = if (shouldAnimate) tween(durationRotation, easing = LinearOutSlowInEasing)
        else tween(0),
        label = "rotationAnim",
        finishedListener = { value ->
            if (value == 180f && !flipped) {
                onCardFlipped()
                if (!isPreview) playFlipFeedback(context)
            }
        }
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density
            },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        if (rotation < 90f) {
            BackCard()
        } else {
            pleasure?.let {
                CardContent(
                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                    pleasure = it
                )
            }
        }
    }
}

private fun playFlipFeedback(context: Context) {
    try {
        val mp = MediaPlayer.create(context, R.raw.done)
        mp?.apply {
            setOnCompletionListener { release() }
            start()
        }

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.vibrate(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            else @Suppress("DEPRECATION") VibrationEffect.createOneShot(500, 1)
        )
    } catch (_: Exception) { }
}

@Composable
private fun BackCard() {
    val gradients = flipGradients()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradients.card)
            .padding(horizontal = 32.dp, vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(36.dp))

            Text(
                text = stringResource(R.string.pleasure_card_back_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.pleasure_card_back_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CardContent(
    modifier: Modifier = Modifier,
    pleasure: Pleasure
) {
    val gradients = flipGradients()

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(gradients.card)
        )

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            ) {
                Text(
                    text = stringResource(pleasure.category.label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Text(
                text = pleasure.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = pleasure.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun NotFlippedCardPreview() {
    FlipTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            PleasureCard(
                pleasure = null,
                durationRotation = 0,
                flipped = false,
                onCardFlipped = {},
                onClick = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun FlippedCardPreview() {
    FlipTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            PleasureCard(
                pleasure = previewDailyPleasure,
                durationRotation = 0,
                flipped = true,
                onCardFlipped = {},
                onClick = {}
            )
        }
    }
}
