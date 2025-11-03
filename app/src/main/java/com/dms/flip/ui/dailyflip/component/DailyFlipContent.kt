package com.dms.flip.ui.dailyflip.component

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dms.flip.R
import com.dms.flip.data.model.PleasureCategory
import com.dms.flip.ui.dailyflip.DailyFlipEvent
import com.dms.flip.ui.dailyflip.DailyFlipScreenState
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.theme.flipGradients
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewDailyPleasure
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun DailyFlipContent(
    modifier: Modifier = Modifier,
    uiState: DailyFlipScreenState.Ready,
    onEvent: (DailyFlipEvent) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showConfettiAnimation by rememberSaveable { mutableStateOf(false) }
    var showCategoryDialog by rememberSaveable { mutableStateOf(false) }

    // --- Vibrator
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    // --- SoundPool
    val (soundPool, soundId) = remember(context) {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val sp = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(attrs).build()
        val id = sp.load(context, R.raw.done, 1)
        sp to id
    }

    DisposableEffect(Unit) {
        onDispose { soundPool.release() }
    }

    // --- Lottie confetti
    val confettiComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti))
    val confettiIsPlaying by rememberUpdatedState(showConfettiAnimation)
    val confettiProgress by animateLottieCompositionAsState(
        composition = confettiComposition,
        isPlaying = confettiIsPlaying,
        restartOnPlay = false
    )

    LaunchedEffect(confettiIsPlaying) {
        if (confettiIsPlaying) {
            soundPool.setVolume(soundId, 0.25f, 0.25f)
            soundPool.play(soundId, 0.25f, 0.25f, 1, 0, 1f)

            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(220L, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(220L)
                }
            }
        }
    }

    LaunchedEffect(confettiProgress) {
        if (showConfettiAnimation && confettiProgress >= 1f) {
            delay(250)
            showConfettiAnimation = false
        }
    }

    // --- Hint animation
    val (hintOffsetX, hintRotation) = swipeHintAnimation()

    // --- Swipe physics
    val animatedOffsetX = remember { Animatable(0f) }
    val animatedRotationZ = remember { Animatable(0f) }
    val dragState = rememberDraggableState { delta ->
        val friction = 0.22f
        val next = animatedOffsetX.value + (delta * friction)
        scope.launch {
            animatedOffsetX.snapTo(next.coerceIn(-1000f, 1000f))
            animatedRotationZ.snapTo(
                (animatedOffsetX.value / 24f).coerceIn(-5f, 15f)
            )
        }
    }

    // 🎭 Dialogue de catégorie
    if (showCategoryDialog) {
        CategorySelectionDialog(
            selectedCategory = uiState.selectedCategory,
            availableCategories = uiState.availableCategories,
            onDismiss = { showCategoryDialog = false },
            onCategorySelected = { category ->
                onEvent(DailyFlipEvent.OnCategorySelected(category))
            }
        )
    }

    val cardContentDescription = if (uiState.isCardFlipped) {
        stringResource(R.string.daily_flip_card_back_description)
    } else {
        stringResource(R.string.daily_flip_card_front_description)
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .draggable(
                        orientation = Orientation.Horizontal,
                        enabled = uiState.isCardFlipped,
                        state = dragState,
                        onDragStopped = { velocity ->
                            scope.launch {
                                val threshold = 180f
                                val offset = animatedOffsetX.value
                                val v = velocity

                                val shouldComplete =
                                    offset > threshold || (v > 2500f && offset > 0f)

                                if (shouldComplete) {
                                    val target = 1200f
                                    launch {
                                        animatedOffsetX.animateTo(
                                            target,
                                            animationSpec = tween(
                                                durationMillis = 520,
                                                easing = LinearOutSlowInEasing
                                            )
                                        )
                                    }
                                    launch {
                                        animatedRotationZ.animateTo(
                                            15f,
                                            animationSpec = tween(520, easing = EaseInOut)
                                        )
                                    }
                                    delay(260)
                                    onEvent(DailyFlipEvent.OnCardMarkedAsDone)
                                } else {
                                    val stiffness = 500f
                                    val damping = 0.78f
                                    launch {
                                        animatedOffsetX.animateTo(
                                            0f,
                                            animationSpec = spring(
                                                stiffness = stiffness,
                                                dampingRatio = damping
                                            )
                                        )
                                    }
                                    launch {
                                        animatedRotationZ.animateTo(
                                            0f,
                                            animationSpec = spring(
                                                stiffness = stiffness,
                                                dampingRatio = damping
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                PleasureCard(
                    modifier = Modifier
                        .padding(horizontal = 42.dp)
                        .offset(
                            x = (animatedOffsetX.value + if (uiState.isCardFlipped) hintOffsetX else 0f).dp
                        )
                        .graphicsLayer {
                            rotationZ =
                                animatedRotationZ.value + if (uiState.isCardFlipped) hintRotation else 0f
                            alpha = 1f - (abs(animatedOffsetX.value) / 900f).coerceIn(0f, 1f)
                        }
                        .semantics { contentDescription = cardContentDescription },
                    pleasure = uiState.dailyPleasure,
                    flipped = uiState.isCardFlipped,
                    durationRotation = 1300,
                    onCardFlipped = {
                        showConfettiAnimation = true
                        onEvent(DailyFlipEvent.OnCardFlipped)
                    },
                    onClick = { onEvent(DailyFlipEvent.OnCardClicked) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                this@Column.AnimatedVisibility(
                    visible = !uiState.isCardFlipped,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    CategorySelector(
                        selectedCategory = uiState.selectedCategory,
                        onClick = { showCategoryDialog = true }
                    )
                }

                this@Column.AnimatedVisibility(
                    visible = uiState.isCardFlipped && uiState.dailyPleasure != null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    ShareButton(
                        onClick = { /* TODO: Lier au ViewModel */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (showConfettiAnimation) {
            LottieAnimation(
                modifier = Modifier.fillMaxWidth(),
                composition = confettiComposition,
                progress = { confettiProgress }
            )
        }
    }
}

/**
 * Animation de hint pour le swipe
 */
@Composable
fun swipeHintAnimation(): Pair<Float, Float> {
    val infinite =
        androidx.compose.animation.core.rememberInfiniteTransition(label = "swipeHint")
    val hintOffsetX by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3600
                0f at 0
                0f at 1200
                18f at 1700
                0f at 2400
                0f at 3600
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "hintOffset"
    )
    val hintRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3600
                0f at 0
                0f at 1200
                3f at 1700
                0f at 2400
                0f at 3600
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "hintRotation"
    )
    return hintOffsetX to hintRotation
}

/**
 * Sélecteur de catégorie
 */
@Composable
private fun CategorySelector(
    selectedCategory: PleasureCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    TextButton(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription =
                context.getString(R.string.daily_flip_category_selector_content_description)
        },
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.category_selector_title,
                    stringResource(selectedCategory.label)
                ),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = selectedCategory.icon,
                contentDescription = stringResource(R.string.choose_category),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Bouton Partager
 */
@Composable
private fun ShareButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gradients = flipGradients()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(gradients.actionButton)
    ) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 2.dp
            ),
            modifier = Modifier.semantics {
                contentDescription =
                    context.getString(R.string.daily_flip_share_button_content_description)
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.daily_flip_share_button_content_description),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.daily_flip_share_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ========== PREVIEWS ==========
@LightDarkPreview
@Composable
private fun DailyFlipContentNotFlippedPreview() {
    FlipTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DailyFlipContent(
                uiState = DailyFlipScreenState.Ready(
                    availableCategories = PleasureCategory.entries,
                    selectedCategory = PleasureCategory.ALL,
                    dailyPleasure = null,
                    isCardFlipped = false
                ),
                onEvent = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun DailyFlipContentFlippedPreview() {
    FlipTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DailyFlipContent(
                uiState = DailyFlipScreenState.Ready(
                    availableCategories = PleasureCategory.entries,
                    selectedCategory = PleasureCategory.ALL,
                    dailyPleasure = previewDailyPleasure,
                    isCardFlipped = true
                ),
                onEvent = {}
            )
        }
    }
}
