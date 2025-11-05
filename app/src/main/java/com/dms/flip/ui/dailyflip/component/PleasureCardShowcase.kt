package com.dms.flip.ui.dailyflip.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dms.flip.ui.component.PleasureCard
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview

/**
 * Écran de démonstration des différentes variantes du composant PleasureCard
 * 
 * Cet écran montre tous les cas d'usage possibles du composant unifié.
 */
@Composable
fun PleasureCardShowcaseScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Text(
            text = "PleasureCard Showcase",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Section 1: Carte complète avec chevron (cas par défaut)
        SectionHeader("1. Carte complète avec chevron")
        PleasureCard(
            icon = Icons.Default.Restaurant,
            iconTint = Color(0xFF4CAF50),
            label = "Votre plaisir du jour",
            title = "Cuisiner un nouveau plat",
            description = "Essayez une recette que vous n'avez jamais faite auparavant et partagez-la avec vos proches.",
            showChevron = true,
            onClick = { /* Navigation */ }
        )
        
        // Section 2: Carte avec badge de complétion
        SectionHeader("2. Carte avec badge de complétion")
        PleasureCard(
            icon = Icons.Default.FitnessCenter,
            iconTint = Color(0xFFFF9800),
            label = "Lundi",
            title = "Faire 30 minutes d'exercice",
            description = "Une séance de yoga ou une marche rapide pour rester en forme.",
            showChevron = true,
            isCompleted = true,
            onClick = { /* Navigation vers détails */ }
        )
        
        // Section 3: Carte sans description
        SectionHeader("3. Carte sans description")
        PleasureCard(
            icon = Icons.Default.Book,
            iconTint = Color(0xFF2196F3),
            label = "Mardi",
            title = "Lire 20 pages d'un bon livre",
            description = null,
            showChevron = true,
            onClick = { /* Navigation */ }
        )
        
        // Section 4: Carte sans chevron (pour InfoCard)
        SectionHeader("4. Carte sans chevron (InfoCard)")
        PleasureCard(
            icon = Icons.Default.Event,
            iconTint = Color(0xFF9C27B0),
            label = "Date de complétion",
            title = "05 novembre 2024",
            description = null,
            showChevron = false,
            onClick = { /* Pas d'action */ }
        )
        
        // Section 5: Carte avec limitation de lignes
        SectionHeader("5. Carte avec limitation de lignes")
        PleasureCard(
            icon = Icons.Default.Restaurant,
            iconTint = Color(0xFFE91E63),
            label = "Mercredi",
            title = "Découvrir un nouveau restaurant dans votre ville et essayer un plat exotique",
            description = "Sortez de votre zone de confort culinaire et explorez de nouvelles saveurs. Partagez cette expérience avec des amis ou la famille pour rendre le moment encore plus mémorable.",
            showChevron = true,
            maxTitleLines = 1,
            maxDescriptionLines = 2,
            onClick = { /* Navigation */ }
        )
        
        // Section 6: Carte sans chevron mais avec description
        SectionHeader("6. Variante personnalisée")
        PleasureCard(
            icon = Icons.Default.FitnessCenter,
            iconTint = Color(0xFF00BCD4),
            label = "Catégorie",
            title = "Sport & Bien-être",
            description = "Activités physiques et exercices pour rester en forme",
            showChevron = false,
            onClick = { /* Peut avoir une action malgré l'absence de chevron */ }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@LightDarkPreview
@Composable
private fun PleasureCardShowcaseScreenPreview() {
    FlipTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PleasureCardShowcaseScreen()
        }
    }
}

/**
 * EXEMPLES D'UTILISATION DANS DIFFÉRENTS CONTEXTES
 * 
 * 1. DailyFlipCompletedContent - Plaisir complété du jour :
 * ```
 * PleasureCard(
 *     icon = pleasure.category.icon,
 *     iconTint = categoryColor,
 *     label = stringResource(R.string.daily_flip_completed_pleasure_label),
 *     title = pleasure.title,
 *     description = pleasure.description,
 *     showChevron = true,
 *     onClick = onPleasureClick
 * )
 * ```
 * 
 * 2. WeeklyPleasuresList - Liste des plaisirs de la semaine :
 * ```
 * PleasureCard(
 *     icon = category.icon,
 *     iconTint = categoryColor,
 *     label = weeklyDay.dayName,
 *     title = historyEntry.pleasureTitle ?: "",
 *     description = historyEntry.pleasureDescription,
 *     showChevron = true,
 *     showCompletedBadge = isCompleted,
 *     maxTitleLines = 1,
 *     maxDescriptionLines = 2,
 *     onClick = onClick
 * )
 * ```
 * 
 * 3. PleasureDetailScreen - Cartes d'information :
 * ```
 * PleasureCard(
 *     icon = Icons.Default.Event,
 *     iconTint = categoryColor,
 *     label = "Date de complétion",
 *     title = "05 novembre 2024",
 *     showChevron = false
 * )
 * ```
 */
