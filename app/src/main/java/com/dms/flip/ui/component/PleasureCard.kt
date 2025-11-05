package com.dms.flip.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dms.flip.domain.model.community.icon
import com.dms.flip.domain.model.community.iconTint
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewDailyPleasure

/**
 * Composant réutilisable pour afficher une carte de plaisir avec un design unifié.
 *
 * @param icon L'icône à afficher dans le coin gauche
 * @param iconTint La couleur de l'icône et des accents de la carte
 * @param label Le label supérieur (ex: "Votre plaisir du jour", "Lundi")
 * @param title Le titre principal du plaisir
 * @param description La description du plaisir (optionnelle)
 * @param showChevron Si vrai, affiche le chevron de navigation à droite
 * @param isCompleted Si vrai, affiche la carte avec un style "completed" (opacité réduite)
 * @param maxTitleLines Nombre maximum de lignes pour le titre (défaut: pas de limite)
 * @param maxDescriptionLines Nombre maximum de lignes pour la description (défaut: pas de limite)
 * @param onClick Action au clic sur la carte
 * @param modifier Modificateur pour personnaliser la carte
 */
@Composable
fun PleasureCard(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    title: String,
    description: String? = null,
    showChevron: Boolean = true,
    isCompleted: Boolean = false,
    maxTitleLines: Int = Int.MAX_VALUE,
    maxDescriptionLines: Int = Int.MAX_VALUE,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isCompleted) 0.7f else 1f)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        iconTint.copy(alpha = if (isCompleted) 0.08f else 0.12f),
                        iconTint.copy(alpha = if (isCompleted) 0.03f else 0.05f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                color = iconTint.copy(alpha = if (isCompleted) 0.15f else 0.25f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconTint.copy(alpha = if (isCompleted) 0.1f else 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = iconTint.copy(alpha = if (isCompleted) 0.8f else 1f)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = maxTitleLines,
                    overflow = if (maxTitleLines < Int.MAX_VALUE) TextOverflow.Ellipsis else TextOverflow.Clip
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        ),
                        lineHeight = 18.sp,
                        maxLines = maxDescriptionLines,
                        overflow = if (maxDescriptionLines < Int.MAX_VALUE) TextOverflow.Ellipsis else TextOverflow.Clip
                    )
                }
            }

            // Chevron
            if (showChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PleasureCardPreview() {
    FlipTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Normal card
                PleasureCard(
                    icon = previewDailyPleasure.category.icon,
                    iconTint = previewDailyPleasure.category.iconTint,
                    label = "Votre plaisir du jour",
                    title = previewDailyPleasure.title,
                    description = previewDailyPleasure.description
                )

                // Completed card
                PleasureCard(
                    icon = previewDailyPleasure.category.icon,
                    iconTint = previewDailyPleasure.category.iconTint,
                    label = "Lundi",
                    title = previewDailyPleasure.title,
                    description = previewDailyPleasure.description,
                    isCompleted = true
                )
            }
        }
    }
}