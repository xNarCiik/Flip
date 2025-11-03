package com.dms.flip.data.firebase.dto

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class RecentActivityDto(
    val pleasureTitle: String = "",
    val category: String = "",
    @ServerTimestamp var completedAt: Date? = null,
    val isCompleted: Boolean = false
)
