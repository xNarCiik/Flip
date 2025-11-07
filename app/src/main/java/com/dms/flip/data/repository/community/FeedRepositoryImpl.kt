package com.dms.flip.data.repository.community

import android.net.Uri
import android.util.Log
import com.dms.flip.data.cache.ProfileBatchLoader
import com.dms.flip.data.firebase.dto.PostDto
import com.dms.flip.data.firebase.mapper.toDomain
import com.dms.flip.data.firebase.source.FirestoreFeedSource
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map

@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storageRepository: StorageRepository,
    private val feedSource: FirestoreFeedSource,
    private val profileBatchLoader: ProfileBatchLoader
) : FeedRepository {

    override fun observeFriendsFeed(limit: Int, cursor: String?): Flow<Paged<Post>> {
        val uid = auth.currentUser?.uid ?: return flowOf(Paged(emptyList(), null))
        Log.d(TAG, "🔵 START observeFriendsFeed (limit: $limit, cursor: $cursor)")

        return feedSource.observeFriendsFeed(uid, limit, cursor)
            .map { page ->
                Log.d(TAG, "📦 Received ${page.items.size} posts from Firestore")

                // Batch load auteurs
                val authorIds = page.items.map { it.data.authorId }.distinct()
                val authorProfiles = try {
                    profileBatchLoader.loadProfiles(authorIds)
                } catch (e: Exception) {
                    val message = e.message ?: ""
                    if (message.contains("PERMISSION_DENIED", ignoreCase = true)) {
                        Log.w(TAG, "⚠️ Permission denied while loading authors — ignoring restricted users")
                        emptyMap()
                    } else {
                        Log.e(TAG, "❌ Unexpected error loading authors", e)
                        emptyMap()
                    }
                }

                // Crée 1 flow par post (like status uniquement)
                val postFlows = coroutineScope {
                    page.items.map { document ->
                        async {
                            val author = authorProfiles[document.data.authorId]
                                ?: createFallbackProfile(document.data.authorId)

                            try {
                                observeLightPost(
                                    postId = document.id,
                                    postDto = document.data,
                                    author = author,
                                    currentUserId = uid
                                )
                            } catch (e: Exception) {
                                val msg = e.message ?: ""
                                Log.e(TAG, "❌ Failed to observe (light) post ${document.id}", e)
                                if (msg.contains("PERMISSION_DENIED", ignoreCase = true)) {
                                    // On “supprime” silencieusement cet item du feed
                                    flowOf<Post?>(null)
                                } else {
                                    // On renvoie une projection minimale (fallback)
                                    flowOf(
                                        document.data.toDomain(
                                            id = document.id,
                                            author = author,
                                            comments = emptyList(),
                                            isLiked = false
                                        )
                                    )
                                }
                            }
                        }
                    }.awaitAll()
                }

                combine(postFlows) { arr ->
                    val posts = arr.filterNotNull()
                    Log.d(TAG, "📤 Emitting page with ${posts.size} posts")
                    Paged(posts, page.nextCursor)
                }
            }
            .flatMapLatest { it }
    }

    /**
     * 🔦 Version “light” : only like status (temps réel) + infos statiques du post/auteur
     * Pas de commentaires ici.
     */
    private fun observeLightPost(
        postId: String,
        postDto: PostDto,
        author: PublicProfile,
        currentUserId: String
    ): Flow<Post> {
        val likeFlow = feedSource.observePostLikeStatus(postId, currentUserId)

        return likeFlow
            .map { isLiked ->
                postDto.toDomain(
                    id = postId,
                    author = author,
                    comments = emptyList(),
                    isLiked = isLiked
                )
            }
            // éviter des recompositions inutiles si la projection n'a pas changé
            .distinctUntilChanged()
            // émettre un état initial en attendant le premier snapshot
            .onStart {
                emit(
                    postDto.toDomain(
                        id = postId,
                        author = author,
                        comments = emptyList(),
                        isLiked = false
                    )
                )
            }
            // ignorer proprement un PERMISSION_DENIED
            .catch { e ->
                val msg = e.message.orEmpty()
                if (msg.contains("PERMISSION_DENIED", ignoreCase = true)) {
                    // On garde le post visible mais sans like local
                    emit(
                        postDto.toDomain(
                            id = postId,
                            author = author,
                            comments = emptyList(),
                            isLiked = false
                        )
                    )
                } else {
                    throw e
                }
            }
    }

    // ——————————————
    // Chargements à la demande
    // ——————————————
    override suspend fun fetchComments(postId: String, limit: Int): List<PostComment> {
        val comments = feedSource.getComments(postId, limit)
        val ids = comments.map { it.second.userId }.distinct()
        val profiles = if (ids.isNotEmpty()) profileBatchLoader.loadProfiles(ids) else emptyMap()
        return comments.map { (id, dto) ->
            val p = profiles[dto.userId] ?: createFallbackProfile(dto.userId)
            dto.toDomain(id = id, profile = p)
        }
    }

    override suspend fun refreshPost(postId: String): Post? {
        val uid = auth.currentUser?.uid ?: return null
        val dto = feedSource.refreshPost(postId) ?: return null
        val author = profileBatchLoader.loadProfile(dto.authorId) ?: createFallbackProfile(dto.authorId)
        val isLiked = try { feedSource.isPostLiked(postId, uid) } catch (_: Exception) { false }
        return dto.toDomain(
            id = postId,
            author = author,
            comments = emptyList(),
            isLiked = isLiked
        )
    }

    // ——————————————
    // Mutations
    // ——————————————
    override suspend fun toggleLike(postId: String) {
        Log.d(TAG, "❤️ Toggling like for post $postId")
        feedSource.toggleLike(postId)
    }

    override suspend fun addComment(postId: String, content: String): PostComment {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val (id, dto) = feedSource.addComment(postId, content)
        val me = profileBatchLoader.loadProfile(uid) ?: createFallbackProfile(uid)
        return dto.toDomain(id, me)
    }

    override suspend fun deleteComment(postId: String, commentId: String) {
        feedSource.deleteComment(postId, commentId)
    }

    override suspend fun deletePost(postId: String) {
        feedSource.deletePost(postId)
    }

    override suspend fun createPost(
        content: String,
        pleasureCategory: String?,
        pleasureTitle: String?,
        photoUri: Uri?
    ) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val postId = firestore.collection("posts").document().id
        var hasImage = false
        try {
            if (photoUri != null) {
                storageRepository.uploadPostImage(uid, postId, photoUri)
                hasImage = true
            }
            feedSource.createPost(postId, content, pleasureCategory, pleasureTitle, hasImage)
        } catch (e: Exception) {
            if (hasImage) runCatching { storageRepository.deletePostImage(uid, postId) }
            throw e
        }
    }

    override suspend fun getPublicProfile(userId: String): PublicProfile? {
        return profileBatchLoader.loadProfile(userId)
    }

    private fun createFallbackProfile(userId: String): PublicProfile =
        PublicProfile(id = userId, username = "Utilisateur inconnu", handle = "", avatarUrl = null)

    fun invalidateProfileCache(userId: String) = profileBatchLoader.invalidate(userId)
    suspend fun prefetchProfiles(userIds: List<String>) = profileBatchLoader.prefetch(userIds)
    fun getCacheStats() = profileBatchLoader.getCacheStats()
    fun cleanupExpiredCache() = profileBatchLoader.cleanupExpired()

    companion object { private const val TAG = "FeedRepositoryImpl" }
}
