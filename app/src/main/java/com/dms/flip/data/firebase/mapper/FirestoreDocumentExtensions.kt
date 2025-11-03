package com.dms.flip.data.firebase.mapper

import com.dms.flip.data.firebase.dto.CommentDto
import com.dms.flip.data.firebase.dto.PostDto
import com.dms.flip.data.firebase.dto.RecentActivityDto
import com.dms.flip.data.firebase.dto.RequestDto
import com.dms.flip.data.model.PleasureHistoryDto
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

private fun DocumentSnapshot.resolveDate(field: String): Date? {
    return getTimestamp(field)?.toDate() ?: getLong(field)?.let { Date(it) }
}

fun DocumentSnapshot.toPostDto(): PostDto? {
    val dto = toObject(PostDto::class.java) ?: return null
    if (dto.timestamp == null) {
        dto.timestamp = resolveDate("timestamp")
    }
    return dto
}

fun DocumentSnapshot.toCommentDto(): CommentDto? {
    val dto = toObject(CommentDto::class.java) ?: return null
    if (dto.timestamp == null) {
        dto.timestamp = resolveDate("timestamp")
    }
    return dto
}

fun DocumentSnapshot.toRecentActivityDto(): RecentActivityDto? {
    val dto = toObject(RecentActivityDto::class.java) ?: return null
    if (dto.completedAt == null) {
        dto.completedAt = resolveDate("completedAt")
    }
    return dto
}

fun DocumentSnapshot.toRequestDto(): RequestDto? {
    val dto = toObject(RequestDto::class.java) ?: return null
    if (dto.requestedAt == null) {
        dto.requestedAt = resolveDate("requestedAt")
    }
    return dto
}

fun DocumentSnapshot.toPleasureHistoryDto(): PleasureHistoryDto? {
    val dto = toObject(PleasureHistoryDto::class.java) ?: return null
    if (dto.dateDrawn == null) {
        dto.dateDrawn = resolveDate("dateDrawn")
    }
    if (dto.completedAt == null) {
        dto.completedAt = resolveDate("completedAt")
    }
    return dto
}
