package com.dms.flip.data.model

import androidx.annotation.Keep
import com.dms.flip.domain.model.PleasureHistory
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Keep
@IgnoreExtraProperties
data class PleasureHistoryDto(
    val id: String = "",
    val pleasureTitle: String? = null,
    val pleasureCategory: String? = null,
    val pleasureDescription: String? = null,
    @ServerTimestamp var dateDrawn: Date? = null,
    @ServerTimestamp var completedAt: Date? = null,
    val completed: Boolean = false
) {
    fun toDomain(): PleasureHistory {
        val categoryEnum = pleasureCategory?.let {
            runCatching { PleasureCategory.valueOf(it) }.getOrNull()
        }

        return PleasureHistory(
            id = id,
            pleasureTitle = pleasureTitle,
            pleasureCategory = categoryEnum,
            pleasureDescription = pleasureDescription,
            dateDrawn = dateDrawn?.time ?: 0L,
            completedAt = completedAt?.time,
            completed = completed
        )
    }
}

fun PleasureHistory.toFirestoreCreateData(): Map<String, Any?> {
    val data = mutableMapOf<String, Any?>(
        "id" to id,
        "completed" to completed,
        "dateDrawn" to FieldValue.serverTimestamp(),
    )

    pleasureTitle?.let { data["pleasureTitle"] = it }
    pleasureCategory?.let { data["pleasureCategory"] = it.name }
    pleasureDescription?.let { data["pleasureDescription"] = it }

    return data
}
