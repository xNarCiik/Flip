package com.dms.flip.domain.repository.community

import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.Paged
import com.dms.flip.domain.model.community.PostComment
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun observeFriendsFeed(limit: Int, cursor: String? = null): Flow<Paged<Post>>

    suspend fun createPost(
        content: String,
        pleasureCategory: String?,
        pleasureTitle: String?,
        photoUrl: String? = null
    )

    suspend fun toggleLike(postId: String)
    suspend fun addComment(postId: String, content: String): PostComment
    suspend fun deleteComment(postId: String, commentId: String)
    suspend fun deletePost(postId: String)
}
