package com.dms.flip.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val LocalFlipThemeIsDark = staticCompositionLocalOf { false }

@Composable
fun FlipTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalFlipThemeIsDark provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

object FlipGradient {

    /**
     * Gradient principal pour la carte (lavande → bleu clair)
     */
    fun cardGradient(isDark: Boolean): Brush {
        return if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFB39DDB), // Lavender
                    Color(0xFF90CAF9)  // Light blue
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFCE93D8), // Light lavender
                    Color(0xFF81D4FA)  // Lighter blue
                )
            )
        }
    }

    /**
     * Gradient secondaire pour les boutons d'action (pêche/coral)
     * Conforme à l'image 2 (bouton "Partager")
     */
    fun actionButtonGradient(isDark: Boolean): Brush {
        return if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFAB91), // Coral
                    Color(0xFFFF8A80)  // Light coral
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFF8A65), // Darker coral
                    Color(0xFFFF7043)  // Deeper coral
                )
            )
        }
    }

    /**
     * Gradient unique pour le logo Flip
     * Indépendant du thème - gradient moderne bleu → violet → rose
     * Cohérent avec l'identité visuelle de l'app
     */
    fun logoGradient(): Brush {
        return Brush.linearGradient(
            colors = listOf(
                Color(0xFF6366F1), // Indigo moderne (Tailwind indigo-500)
                Color(0xFF8B5CF6), // Violet (Tailwind violet-500)
                Color(0xFFA855F7)  // Purple/Fuchsia (Tailwind purple-500)
            )
        )
    }

    /**
     * Gradient pour le fond de l'écran de login/setup
     * Mis à jour pour être plus subtil et moderne
     */
    fun setupBackground(isDark: Boolean): Brush {
        return if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1C1B1F), // DarkBackground du thème
                    Color(0xFF2A292D)  // DarkSurface pour une transition douce
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF6F7F8), // LightBackground du thème
                    Color(0xFFFFFFFF)  // Blanc pur
                )
            )
        }
    }
}

/**
 * Helper pour accéder facilement aux gradients dans les Composables
 */
data class FlipGradients(
    val card: Brush,
    val actionButton: Brush,
    val setupBackground: Brush,
    val logo: Brush
)

@Composable
fun flipGradients(): FlipGradients {
    val useDarkTheme = LocalFlipThemeIsDark.current
    return FlipGradients(
        card = FlipGradient.cardGradient(useDarkTheme),
        actionButton = FlipGradient.actionButtonGradient(useDarkTheme),
        setupBackground = FlipGradient.setupBackground(useDarkTheme),
        logo = FlipGradient.logoGradient()
    )
}