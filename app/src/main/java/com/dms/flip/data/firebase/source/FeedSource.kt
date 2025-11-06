package com.dms.flip.data.firebase.source

import com.dms.flip.data.firebase.dto.CommentDto
import com.dms.flip.data.firebase.dto.PostDto
import com.dms.flip.domain.model.community.Paged
import kotlinx.coroutines.flow.Flow

interface FeedSource {
    data class PostDocument(val id: String, val data: PostDto)

    /**
     * Observe le feed des amis en temps réel
     */
    fun observeFriendsFeed(
        uid: String,
        limit: Int,
        cursor: String? = null
    ): Flow<Paged<PostDocument>>

    /**
     * Observe les commentaires d'un post en temps réel
     */
    fun observeComments(postId: String): Flow<List<Pair<String, CommentDto>>>
    
    /**
     * Observe le statut de like de l'utilisateur pour un post
     */
    fun observePostLikeStatus(postId: String, uid: String): Flow<Boolean>
    
    /**
     * Observe le nombre total de likes d'un post
     */
    fun observePostLikeCount(postId: String): Flow<Int>

    /**
     * Crée un nouveau post
     */
    suspend fun createPost(
        postId: String,
        content: String,
        pleasureCategory: String?,
        pleasureTitle: String?,
        hasImage: Boolean
    )

    /**
     * Toggle le like d'un post
     */
    suspend fun toggleLike(postId: String)

    /**
     * Ajoute un commentaire à un post
     */
    suspend fun addComment(postId: String, content: String): Pair<String, CommentDto>

    /**
     * Supprime un commentaire
     */
    suspend fun deleteComment(postId: String, commentId: String)

    /**
     * Supprime un post
     */
    suspend fun deletePost(postId: String)

    /**
     * Récupère les commentaires d'un post (snapshot, pas de temps réel)
     */
    suspend fun getComments(postId: String, limit: Int = 50): List<Pair<String, CommentDto>>

    /**
     * Vérifie si un post est liké par un utilisateur (snapshot, pas de temps réel)
     */
    suspend fun isPostLiked(postId: String, uid: String): Boolean
    
    /**
     * Récupère le nombre de likes d'un post (snapshot, pas de temps réel)
     */
    suspend fun getLikeCount(postId: String): Int

    /**
     * Force le refresh d'un post depuis le serveur
     */
    suspend fun refreshPost(postId: String): PostDto?
}
