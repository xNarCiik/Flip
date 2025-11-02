package com.dms.flip.ui.onboarding.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dms.flip.R
import com.dms.flip.ui.onboarding.OnboardingStep
import com.dms.flip.ui.theme.flipGradients

@Composable
fun OnboardingProgressBar(
    currentStep: OnboardingStep,
    totalSteps: Int,
    notificationStepSkipped: Boolean
) {
    val stepNumber = when (currentStep) {
        OnboardingStep.USERNAME -> 1
        OnboardingStep.PLEASURES -> 2
        OnboardingStep.NOTIFICATIONS -> 3
        OnboardingStep.REMINDER_TIME -> if (notificationStepSkipped) 3 else 4
    }

    val progress = when (currentStep) {
        OnboardingStep.USERNAME -> 1f / totalSteps
        OnboardingStep.PLEASURES -> 2f / totalSteps
        OnboardingStep.NOTIFICATIONS -> 3f / totalSteps
        OnboardingStep.REMINDER_TIME -> 1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "progress"
    )

    val gradients = flipGradients()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label "Étape X sur Y" - Plus grand et centré
        Text(
            text = stringResource(
                id = R.string.onboarding_progress_step,
                stepNumber,
                totalSteps
            ),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Progress bar avec gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(50)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(6.dp)
                    .background(
                        brush = gradients.card,
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}
