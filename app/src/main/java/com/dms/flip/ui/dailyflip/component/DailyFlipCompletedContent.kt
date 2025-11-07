package com.dms.flip.ui.dailyflip.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dms.flip.R
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.model.community.icon
import com.dms.flip.domain.model.community.iconTint
import com.dms.flip.ui.component.PleasureCard
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewDailyPleasure
import kotlinx.coroutines.delay

@Composable
fun DailyFlipCompletedContent(
    modifier: Modifier = Modifier,
    completedPleasure: Pleasure? = null,
    onShareClick: () -> Unit = {},
    onPleasureClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    var hasAnimated by rememberSaveable { mutableStateOf(false) }
    var showHero by rememberSaveable { mutableStateOf(false) }
    var showContent by rememberSaveable { mutableStateOf(false) }
    var showExtras by rememberSaveable { mutableStateOf(false) }

    var hasPlayedConfetti by rememberSaveable { mutableStateOf(false) }
    var playConfetti by remember { mutableStateOf(false) }

    val checkmarkComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.checkmark))
    val confettiComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti))

    val checkmarkProgress by animateLottieCompositionAsState(
        composition = checkmarkComposition,
        isPlaying = showHero,
        restartOnPlay = false,
        speed = 0.7f
    )

    val confettiProgress by animateLottieCompositionAsState(
        composition = confettiComposition,
        isPlaying = playConfetti,
        restartOnPlay = false
    )

    LaunchedEffect(Unit) {
        if (!hasAnimated) {
            delay(150)
            showHero = true
            delay(300)
            showContent = true
            delay(300)
            showExtras = true
            hasAnimated = true
        } else {
            showHero = true
            showContent = true
            showExtras = true
        }
    }

    LaunchedEffect(checkmarkProgress) {
        if (checkmarkProgress > 0.95f && !hasPlayedConfetti) {
            hasPlayedConfetti = true
            playConfetti = true
        }
    }

    val categoryColor = completedPleasure?.category?.iconTint ?: MaterialTheme.colorScheme.primary

    Box(modifier = modifier.fillMaxSize()) {
        // Contenu scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 100.dp), // Espace pour le bouton sticky
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AnimatedVisibility(
                visible = showHero,
                enter = fadeIn(tween(600, easing = FastOutSlowInEasing)) +
                        scaleIn(
                            initialScale = 0.85f,
                            animationSpec = tween(600, easing = FastOutSlowInEasing)
                        )
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(categoryColor.copy(0.3f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
                        0.9f, 1.1f,
                        animationSpec = infiniteRepeatable(
                            tween(1200, easing = FastOutSlowInEasing),
                            RepeatMode.Reverse
                        ),
                        label = "pulse"
                    )
                    Box(
                        modifier = Modifier
                            .size((110 * pulse).dp)
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(categoryColor.copy(0.25f), Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    )
                    LottieAnimation(
                        composition = checkmarkComposition,
                        progress = { if (hasPlayedConfetti) 1f else checkmarkProgress },
                        modifier = Modifier.size(110.dp)
                    )
                    this@Column.AnimatedVisibility(visible = playConfetti) {
                        LottieAnimation(
                            composition = confettiComposition,
                            progress = { confettiProgress },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 100)) +
                        scaleIn(initialScale = 0.9f, animationSpec = tween(600))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.daily_flip_completed_title),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.daily_flip_completed_subtitle),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(700, delayMillis = 250)) +
                        scaleIn(initialScale = 0.95f, animationSpec = tween(700))
            ) {
                completedPleasure?.let {
                    PleasureCard(
                        icon = it.category.icon,
                        iconTint = categoryColor,
                        label = stringResource(R.string.daily_flip_completed_pleasure_label),
                        title = it.title,
                        description = it.description,
                        isCompleted = true,
                        showChevron = true,
                        onClick = onPleasureClick
                    )
                }
            }

            val quotes = listOf(
                "Chaque petit plaisir construit ton bonheur.",
                "Les moments simples font les plus beaux souvenirs.",
                "Savourer, c'est déjà vivre deux fois.",
                "Aujourd'hui, tu t'es choisi. Et c'est beau."
            )
            val randomQuote = remember { quotes.random() }

            AnimatedVisibility(
                visible = showExtras,
                enter = fadeIn(tween(700, delayMillis = 300)) +
                        scaleIn(initialScale = 0.95f, animationSpec = tween(700))
            ) {
                Text(
                    text = "« $randomQuote »",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)
                )
            }

            AnimatedVisibility(
                visible = showExtras,
                enter = fadeIn(tween(800, delayMillis = 400)) +
                        scaleIn(initialScale = 0.95f, animationSpec = tween(800))
            ) {
                NextDrawInfoCard()
            }
        }

        // Dégradé de transition
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Bouton sticky en bas
        AnimatedVisibility(
            visible = showExtras,
            enter = fadeIn(tween(900, delayMillis = 500)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            val scale = remember { Animatable(1f) }
            LaunchedEffect(showExtras) {
                if (showExtras) {
                    delay(1500)
                    while (true) {
                        scale.animateTo(1.05f, tween(1200, easing = FastOutSlowInEasing))
                        scale.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = onShareClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(scale.value)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = categoryColor.copy(alpha = 0.35f)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = categoryColor,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.share_moment_button),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun NextDrawInfoCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.daily_flip_completed_next_draw),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.daily_flip_completed_next_draw_subtitle),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun DailyFlipCompletedContentPreview() {
    FlipTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DailyFlipCompletedContent(
                completedPleasure = previewDailyPleasure,
                onPleasureClick = {}
            )
        }
    }
}
