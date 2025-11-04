package com.dms.flip.data.firebase.dto

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class RequestDto(
    val fromUserId: String = "",
    val toUserId: String = "",
    val fromUsername: String = "",
    val fromHandle: String = "",
    val fromAvatarUrl: String? = null,
    @ServerTimestamp var requestedAt: Date? = null
)
