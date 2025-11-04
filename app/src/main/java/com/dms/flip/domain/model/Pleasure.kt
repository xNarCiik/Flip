package com.dms.flip.domain.model

import com.dms.flip.domain.model.community.PleasureCategory

data class Pleasure(
    val id: String,
    val title: String = "",
    val description: String = "",
    val category: PleasureCategory = PleasureCategory.OTHER,
    val isEnabled: Boolean = true
) {
    fun toPleasureHistory(id: String) = PleasureHistory(
        id = id,
        pleasureTitle = title,
        pleasureDescription = description,
        pleasureCategory = category
    )
}