package com.dms.flip.domain.model

import android.os.Parcelable
import com.dms.flip.domain.model.community.PleasureCategory
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Entrée d'historique d'un plaisir tiré / réalisé.
 */
@Serializable
@Parcelize
data class PleasureHistory(
    val id: String = "",
    val dateDrawn: Long = 0L,
    val completed: Boolean = false,
    val pleasureTitle: String? = null,
    val pleasureCategory: PleasureCategory = PleasureCategory.OTHER,
    val pleasureDescription: String? = null,
    val completedAt: Long? = null
): Parcelable {
    fun toPleasureOrNull(): Pleasure? {
        val title = pleasureTitle ?: return null
        val category = pleasureCategory
        return Pleasure(
            id = id,
            title = title,
            description = pleasureDescription.orEmpty(),
            category = category
        )
    }
}
