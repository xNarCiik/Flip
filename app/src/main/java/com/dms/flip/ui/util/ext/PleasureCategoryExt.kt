package com.dms.flip.domain.model.community

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.dms.flip.R

val PleasureCategory.icon: ImageVector
    get() = when (this) {
        PleasureCategory.ALL -> Icons.Default.AutoAwesome
        PleasureCategory.FOOD -> Icons.Default.Fastfood
        PleasureCategory.ENTERTAINMENT -> Icons.Default.Theaters
        PleasureCategory.SOCIAL -> Icons.Default.People
        PleasureCategory.WELLNESS -> Icons.Default.Spa
        PleasureCategory.CREATIVE -> Icons.Default.Palette
        PleasureCategory.OUTDOOR -> Icons.Default.Landscape
        PleasureCategory.SPORT -> Icons.Default.FitnessCenter
        PleasureCategory.SHOPPING -> Icons.Default.ShoppingCart
        PleasureCategory.CULTURE -> Icons.Default.Museum
        PleasureCategory.LEARNING -> Icons.Default.School
        PleasureCategory.OTHER -> Icons.Default.MoreHoriz
    }

@get:StringRes
val PleasureCategory.label: Int
    get() = when (this) {
        PleasureCategory.ALL -> R.string.category_all
        PleasureCategory.FOOD -> R.string.category_food
        PleasureCategory.ENTERTAINMENT -> R.string.category_entertainment
        PleasureCategory.SOCIAL -> R.string.category_social
        PleasureCategory.WELLNESS -> R.string.category_wellness
        PleasureCategory.CREATIVE -> R.string.category_creative
        PleasureCategory.OUTDOOR -> R.string.category_outdoor
        PleasureCategory.SPORT -> R.string.category_sport
        PleasureCategory.SHOPPING -> R.string.category_shopping
        PleasureCategory.CULTURE -> R.string.category_culture
        PleasureCategory.LEARNING -> R.string.category_learning
        PleasureCategory.OTHER -> R.string.category_other
    }

val PleasureCategory.iconTint: Color
    get() = when (this) {
        PleasureCategory.ALL -> Color(0xFFF59E0B)
        PleasureCategory.FOOD -> Color(0xFFEF4444)
        PleasureCategory.ENTERTAINMENT -> Color(0xFF8B5CF6)
        PleasureCategory.SOCIAL -> Color(0xFF3B82F6)
        PleasureCategory.WELLNESS -> Color(0xFF06B6D4)
        PleasureCategory.CREATIVE -> Color(0xFFEC4899)
        PleasureCategory.OUTDOOR -> Color(0xFF10B981)
        PleasureCategory.SPORT -> Color(0xFF22C55E)
        PleasureCategory.SHOPPING -> Color(0xFF6366F1)
        PleasureCategory.CULTURE -> Color(0xFFA855F7)
        PleasureCategory.LEARNING -> Color(0xFF14B8A6)
        PleasureCategory.OTHER -> Color(0xFF6B7280)
    }
