package com.dms.flip.data.firebase.dto

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class PublicProfileDto(
    val username: String = "",
    val handle: String = "",
    val avatarUrl: String? = null,
    val bio: String? = null,
    val stats: Map<String, Int> = emptyMap(),
    @ServerTimestamp var updatedAt: Date? = null
)
