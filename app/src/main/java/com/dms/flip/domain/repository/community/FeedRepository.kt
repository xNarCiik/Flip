package com.dms.flip.domain.repository.community

import android.net.Uri
import com.dms.flip.domain.model.community.Paged
import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.PostComment
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    /**
     * Observe le feed des amis en temps réel
     */
    fun observeFriendsFeed(limit: Int, cursor: String? = null): Flow<Paged<Post>>
    
    /**
     * Crée un nouveau post
     */
    suspend fun createPost(
        content: String,
        pleasureCategory: String? = null,
        pleasureTitle: String? = null,
        photoUri: Uri? = null
    )
    
    /**
     * Toggle le like d'un post
     */
    suspend fun toggleLike(postId: String)
    
    /**
     * Ajoute un commentaire à un post
     */
    suspend fun addComment(postId: String, content: String): PostComment
    
    /**
     * Supprime un commentaire
     */
    suspend fun deleteComment(postId: String, commentId: String)
    
    /**
     * Supprime un post
     */
    suspend fun deletePost(postId: String)
}
