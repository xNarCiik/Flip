package com.dms.flip.data.firebase.dto

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class PostDto(
    val authorId: String = "",
    val content: String = "",
    @ServerTimestamp var timestamp: Date? = null,
    val pleasureCategory: String? = null,
    val pleasureTitle: String? = null,
    val photoUrl: String? = null,
    val photoUrlThumb: String? = null,
    val likeCount: Int = 0,
    val commentsCount: Int = 0
)
