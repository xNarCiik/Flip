package com.dms.flip.domain.repository.community

/**
 * Repository pour la gestion des posts
 */
interface PostRepository {
    
    /**
     * Créer un nouveau post
     */
    suspend fun createPost(
        authorId: String,
        content: String,
        pleasureCategory: String?,
        pleasureTitle: String?,
        photoUrl: String? = null
    ): String // return id
}
