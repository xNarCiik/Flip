package com.dms.flip.data.repository.community

import android.util.Log
import com.dms.flip.data.cache.ProfileBatchLoader
import com.dms.flip.data.firebase.dto.CommentDto
import com.dms.flip.data.firebase.dto.PostDto
import com.dms.flip.data.firebase.mapper.toDomain
import com.dms.flip.data.firebase.source.FeedSource
import com.dms.flip.data.firebase.source.ProfileSource
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.Paged
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.domain.repository.community.FeedRepository
import com.google.firebase.auth.FirebaseAuth
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
                                ?: createFallbackFriend(document.data.authorId)
                            
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
     * Version optimisée qui reçoit déjà l'auteur pour éviter les requêtes redondantes
     */
    private fun observePostWithRealTimeUpdates(
        postId: String,
        postDto: PostDto,
        author: Friend,
        currentUserId: String
    ): Flow<Post> {
        Log.d(TAG, "🔵 Setting up real-time updates for post $postId")
        
        return combine(
            feedSource.observeComments(postId),
            feedSource.observePostLikeStatus(postId, currentUserId),
            feedSource.observePostLikeCount(postId)
        ) { comments, isLiked, likeCount ->
            // Charger les profils des commentateurs en batch
            val commenterIds = comments.map { (_, dto) -> dto.userId }.distinct()
            val commenterProfiles = if (commenterIds.isNotEmpty()) {
                profileBatchLoader.loadProfiles(commenterIds)
            } else {
                emptyMap()
            }
            
            val domainComments = comments.map { (id, dto) ->
                dto.toDomain(id)
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
     * Crée un Friend de fallback si le profil n'a pas pu être chargé
     */
    private fun createFallbackFriend(userId: String): Friend {
        Log.w(TAG, "⚠️ Using fallback friend for user $userId")
        return Friend(
            id = userId,
            username = "Unknown User",
            handle = "",
            avatarUrl = null,
            streak = 0
        )
    }

    override suspend fun createPost(
        content: String,
        pleasureCategory: String?,
        pleasureTitle: String?,
        photoUrl: String?
    ) {
        Log.d(TAG, "📝 Creating post...")
        feedSource.createPost(content, pleasureCategory, pleasureTitle, photoUrl)
        Log.d(TAG, "✅ Post created successfully")
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
        val friend = profileBatchLoader.loadProfile(uid)
        
        val comment = CommentDto(
            userId = uid,
            username = friend?.username ?: "",
            userHandle = friend?.handle ?: "",
            avatarUrl = friend?.avatarUrl,
            content = content
        )
        
        val (id, dto) = feedSource.addComment(postId, comment)
        Log.d(TAG, "✅ Comment added: $id, waiting for real-time update...")
        return dto.toDomain(id)
    }

    override suspend fun deleteComment(postId: String, commentId: String) {
        Log.d(TAG, "🗑️ Deleting comment $commentId from post $postId")
        feedSource.deleteComment(postId, commentId)
        // ✅ Les listeners temps réel mettront à jour automatiquement
        Log.d(TAG, "✅ Comment deleted, waiting for real-time update...")
    }

    override suspend fun deletePost(postId: String) {
        Log.d(TAG, "🗑️ Deleting post $postId")
        feedSource.deletePost(postId)
        // ✅ Les listeners temps réel mettront à jour automatiquement
        Log.d(TAG, "✅ Post deleted, waiting for real-time update...")
    }
    
    /**
     * ✅ NOUVELLE MÉTHODE : Invalider le cache d'un profil
     * À appeler après une mise à jour de profil
     */
    fun invalidateProfileCache(userId: String) {
        Log.d(TAG, "🗑️ Invalidating profile cache for $userId")
        profileBatchLoader.invalidate(userId)
    }
    
    /**
     * ✅ NOUVELLE MÉTHODE : Prefetch des profils pour la pagination
     * À appeler avant de charger la page suivante
     */
    suspend fun prefetchProfiles(userIds: List<String>) {
        Log.d(TAG, "🔮 Prefetching ${userIds.size} profiles")
        profileBatchLoader.prefetch(userIds)
    }
    
    /**
     * ✅ NOUVELLE MÉTHODE : Obtenir les stats du cache
     */
    fun getCacheStats() = profileBatchLoader.getCacheStats()
    
    /**
     * ✅ NOUVELLE MÉTHODE : Nettoyer les entrées expirées
     */
    fun cleanupExpiredCache() {
        Log.d(TAG, "🧹 Cleaning up expired cache entries")
        profileBatchLoader.cleanupExpired()
    }

    companion object {
        private const val TAG = "FeedRepositoryImpl"
    }
}
