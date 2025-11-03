package com.dms.flip.data.firebase.dto

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class RequestDto(
    val userId: String = "",
    val username: String = "",
    val handle: String = "",
    val avatarUrl: String? = null,
    @ServerTimestamp var requestedAt: Date? = null
)
