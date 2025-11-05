package com.dms.flip.data.firebase.dto

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class RequestDto(
    val fromUserId: String = "",
    val fromUsername: String = "",
    val fromHandle: String = "",
    val fromAvatarUrl: String? = null,
    val toUserId: String = "",
    val toUsername: String = "",
    val toHandle: String = "",
    val toAvatarUrl: String? = null,
    @ServerTimestamp var requestedAt: Date? = null
)
