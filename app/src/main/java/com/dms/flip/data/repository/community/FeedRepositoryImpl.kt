package com.dms.flip.data.repository.community

import android.net.Uri
import android.util.Log
import com.dms.flip.data.cache.ProfileBatchLoader
import com.dms.flip.data.firebase.dto.PostDto
import com.dms.flip.data.firebase.mapper.toDomain
import com.dms.flip.data.firebase.source.FeedSource
import com.dms.flip.data.repository.StorageRepository
import com.dms.flip.domain.model.community.Paged
import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.repository.community.FeedRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storageRepository: StorageRepository,
    private val feedSource: FeedSource,
    private val profileBatchLoader: ProfileBatchLoader
) : FeedRepository {

    override fun observeFriendsFeed(limit: Int, cursor: String?): Flow<Paged<Post>> {
        val uid = auth.currentUser?.uid ?: return flowOf(Paged(emptyList(), null))

        Log.d(TAG, "🔵 START observeFriendsFeed (limit: $limit, cursor: $cursor)")

        return feedSource.observeFriendsFeed(uid, limit, cursor)
            .map { page ->
                Log.d(TAG, "📦 Received ${page.items.size} posts from Firestore")

                // ✅ OPTIMISATION 1 : Batch load de tous les auteurs en UNE opération
                val authorIds = page.items.map { it.data.authorId }.distinct()
                Log.d(TAG, "👥 Loading ${authorIds.size} unique authors...")

                val authorProfiles = profileBatchLoader.loadProfiles(authorIds)
                Log.d(TAG, "✅ Loaded ${authorProfiles.size} author profiles")

                // ✅ OPTIMISATION 2 : Créer les Flows de posts avec les profils déjà chargés
                val posts = coroutineScope {
                    page.items.map { document ->
                        async {
                            val author = authorProfiles[document.data.authorId]
                                ?: createFallbackProfile(document.data.authorId)

                            observePostWithRealTimeUpdates(
                                postId = document.id,
                                postDto = document.data,
                                author = author,
                                currentUserId = uid
                            )
                        }
                    }.awaitAll()
                }

                Log.d(TAG, "🔄 Created ${posts.size} post flows with real-time updates")

                // Combiner tous les Flows de posts
                combine(posts) { postArray ->
                    Log.d(TAG, "📤 Emitting page with ${postArray.size} posts")
                    Paged(postArray.toList(), page.nextCursor)
                }
            }
            .flatMapLatest { it }
    }

    /**
     * ✅ Observer un post avec mises à jour en temps réel
     * Refactorisé pour charger les profils des commentateurs
     */
    private fun observePostWithRealTimeUpdates(
        postId: String,
        postDto: PostDto,
        author: PublicProfile,
        currentUserId: String
    ): Flow<Post> {
        Log.d(TAG, "🔵 Setting up real-time updates for post $postId")

        return combine(
            feedSource.observeComments(postId),
            feedSource.observePostLikeStatus(postId, currentUserId),
            feedSource.observePostLikeCount(postId)
        ) { comments, isLiked, likeCount ->

            // ✅ Batch load des profils de commentateurs
            val commenterIds = comments.map { (_, dto) -> dto.userId }.distinct()
            val commenterProfiles = if (commenterIds.isNotEmpty()) {
                Log.d(TAG, "👥 Loading ${commenterIds.size} unique commenters for post $postId")
                profileBatchLoader.loadProfiles(commenterIds)
            } else {
                emptyMap()
            }

            // ✅ Enrichir les DTOs avec les profils chargés
            val domainComments = comments.map { (id, dto) ->
                val profile = commenterProfiles[dto.userId]
                dto.toDomain(
                    id = id,
                    profile = profile ?: createFallbackProfile(dto.userId)
                )
            }

            postDto.toDomain(
                id = postId,
                author = author,
                comments = domainComments,
                isLiked = isLiked,
                likesCount = likeCount
            )
        }
    }

    /**
     * Crée un Profile de fallback si le profil n'a pas pu être chargé
     */
    private fun createFallbackProfile(userId: String): PublicProfile {
        Log.w(TAG, "⚠️ Using fallback profile for user $userId")
        return PublicProfile(
            id = userId,
            username = "Utilisateur inconnu",
            handle = "",
            avatarUrl = null
        )
    }

    /**
     * 1. Génère un PostId
     * 2. Uploade l'image (si présente) vers le chemin "posts/uid/postId/original.jpg"
     * 3. Appelle la Cloud Function createPost
     */
    override suspend fun createPost(
        content: String,
        pleasureCategory: String?,
        pleasureTitle: String?,
        photoUri: Uri?
    ) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

        // 1. Générer l'ID côté client
        val postId = firestore.collection("posts").document().id
        var hasImage = false

        try {
            // 2. Uploader l'image si elle existe
            if (photoUri != null) {
                Log.d(TAG, "🔼 Uploading image for post $postId...")

                storageRepository.uploadPostImage(uid, postId, photoUri)

                Log.d(TAG, "✅ Image uploaded for post $postId")
                hasImage = true
            }

            // 3. Appeler la Cloud Function
            Log.d(TAG, "📝 Creating post document $postId...")
            feedSource.createPost(postId, content, pleasureCategory, pleasureTitle, hasImage)
            Log.d(TAG, "✅ Post $postId créé")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create post $postId", e)

            if (hasImage) {
                Log.w(TAG, "Post creation failed, rolling back image upload for $postId")
                try {
                    storageRepository.deletePostImage(uid, postId)
                    Log.d(TAG, "✅ Image rollback successful for $postId")
                } catch (deleteError: Exception) {
                    Log.e(TAG, "❌ CRITICAL: Failed to rollback image for $postId", deleteError)
                }
            }
            throw e
        }
    }

    override suspend fun toggleLike(postId: String) {
        Log.d(TAG, "❤️ Toggling like for post $postId")
        feedSource.toggleLike(postId)
        // ✅ Les listeners temps réel mettront à jour automatiquement
        Log.d(TAG, "✅ Like toggled, waiting for real-time update...")
    }

    override suspend fun addComment(postId: String, content: String): PostComment {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

        Log.d(TAG, "💬 Adding comment to post $postId")

        // ✅ Utiliser le cache pour le profil
        val friend = profileBatchLoader.loadProfile(uid) ?: createFallbackProfile(uid)

        val (id, dto) = feedSource.addComment(postId, content)

        Log.d(TAG, "✅ Comment added: $id, waiting for real-time update...")
        return dto.toDomain(id, friend)
    }

    override suspend fun deleteComment(postId: String, commentId: String) {
        Log.d(TAG, "🗑️ Deleting comment $commentId from post $postId")
        feedSource.deleteComment(postId, commentId)
        // ✅ Les listeners temps réel mettront à jour automatiquement
        Log.d(TAG, "✅ Comment deleted, waiting for real-time update...")
    }

    override suspend fun deletePost(postId: String) {
        feedSource.deletePost(postId)
        // ✅ Les listeners temps réel mettront à jour automatiquement
        Log.d(TAG, "✅ Post deleted, waiting for real-time update...")
    }

    // TODO Put in other repo?
    override suspend fun getPublicProfile(userId: String): PublicProfile? {
        return profileBatchLoader.loadProfile(userId = userId)
    }

    /**
     * Invalider le cache d'un profil
     * À appeler après une mise à jour de profil
     */
    fun invalidateProfileCache(userId: String) {
        Log.d(TAG, "🗑️ Invalidating profile cache for $userId")
        profileBatchLoader.invalidate(userId)
    }

    /**
     * Prefetch des profils pour la pagination
     * À appeler avant de charger la page suivante
     */
    suspend fun prefetchProfiles(userIds: List<String>) {
        Log.d(TAG, "🔮 Prefetching ${userIds.size} profiles")
        profileBatchLoader.prefetch(userIds)
    }

    /**
     * ✅ Obtenir les stats du cache
     */
    fun getCacheStats() = profileBatchLoader.getCacheStats()

    /**
     * ✅ Nettoyer les entrées expirées
     */
    fun cleanupExpiredCache() {
        Log.d(TAG, "🧹 Cleaning up expired cache entries")
        profileBatchLoader.cleanupExpired()
    }

    companion object {
        private const val TAG = "FeedRepositoryImpl"
    }
}
