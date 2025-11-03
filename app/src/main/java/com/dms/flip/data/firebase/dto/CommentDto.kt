package com.dms.flip.data.firebase.dto

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CommentDto(
    val userId: String = "",
    val username: String = "",
    val userHandle: String = "",
    val avatarUrl: String? = null,
    val content: String = "",
    @ServerTimestamp var timestamp: Date? = null
)
