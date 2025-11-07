// FirestoreFeedSource.kt
package com.dms.flip.data.firebase.source

import android.util.Log
import com.dms.flip.data.firebase.dto.CommentDto
import com.dms.flip.data.firebase.dto.PostDto
import com.dms.flip.domain.model.community.Paged
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreFeedSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) {
    data class PostDocument(val id: String, val data: PostDto)

    fun observeFriendsFeed(
        uid: String,
        limit: Int,
        cursor: String?
    ): Flow<Paged<PostDocument>> = callbackFlow {
        Log.d(TAG, "🔵 START observeFriendsFeed for user $uid (cursor: $cursor)")

        val friendIds = (firestore.collection("users")
            .document(uid)
            .collection("friends")
            .get()
            .await()
            .documents.map { it.id } + uid)

        Log.d(TAG, "👥 Found ${friendIds.size} friends (including self)")

        val chunks = friendIds.chunked(10)
        if (chunks.isEmpty()) {
            Log.d(TAG, "❌ No friends found, returning empty feed")
            trySend(Paged(emptyList(), null))
            close()
            return@callbackFlow
        }

        var cursorDocument: DocumentSnapshot? = null
        if (cursor != null) {
            try {
                cursorDocument = firestore.collection("posts")
                    .document(cursor)
                    .get()
                    .await()
                Log.d(TAG, "📍 Cursor document loaded: ${cursorDocument.id}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to load cursor document", e)
            }
        }

        val chunkFlows = chunks.mapIndexed { index, ids ->
            callbackFlow {
                Log.d(TAG, "🔷 START chunk $index with ${ids.size} friends")

                var query = firestore.collection("posts")
                    .whereIn("authorId", ids)
                    .orderBy("timestamp", Query.Direction.DESCENDING)

                if (cursorDocument != null && cursorDocument.exists()) {
                    query = query.startAfter(cursorDocument)
                }

                query = query.limit(limit.toLong())

                val registration = query.addSnapshotListener { snap, err ->
                    if (err != null) {
                        if (err.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            Log.w(TAG, "⚠️ Chunk $index skipped (permission denied for one or more authors)")
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        Log.e(TAG, "❌ Error in chunk $index", err)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    if (snap == null) {
                        Log.w(TAG, "⚠️ Null snapshot in chunk $index")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val docs = snap.documents.mapNotNull { d ->
                        d.toObject(PostDto::class.java)?.let { dto ->
                            PostDocument(d.id, dto)
                        }
                    }

                    Log.d(TAG, "✅ Chunk $index updated: ${docs.size} posts")
                    trySend(docs)
                }

                awaitClose {
                    Log.d(TAG, "🔴 STOP chunk $index")
                    registration.remove()
                }
            }
        }

        val job = launch {
            combine(chunkFlows) { parts ->
                parts.toList()
                    .flatten()
                    .distinctBy { it.id }
                    .sortedByDescending { it.data.timestamp }
                    .take(limit)
            }.collect { posts ->
                Log.d(TAG, "📦 Feed updated: ${posts.size} posts total")
                trySend(Paged(posts, posts.lastOrNull()?.id))
            }
        }

        awaitClose {
            Log.d(TAG, "🔴 STOP observeFriendsFeed")
            job.cancel()
        }
    }

    // ✅ On continue d’observer en temps réel UNIQUEMENT le like status
    fun observePostLikeStatus(postId: String, uid: String): Flow<Boolean> = callbackFlow {
        Log.d(TAG, "🔵 START observing like status for post $postId by user $uid")

        val reg = firestore.collection("posts")
            .document(postId)
            .collection("likes")
            .document(uid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    if (err.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(TAG, "Listener 'like status' pour $postId détaché (post sûrement supprimé).")
                        return@addSnapshotListener
                    }
                    Log.e(TAG, "❌ Error observing like status for $postId", err)
                    close(err)
                    return@addSnapshotListener
                }
                val isLiked = snap?.exists() == true
                Log.d(TAG, "✅ Like status updated for $postId: $isLiked")
                trySend(isLiked)
            }

        awaitClose {
            Log.d(TAG, "🔴 STOP observing like status for post $postId")
            reg.remove()
        }
    }

    // ✅ Chargement “à la demande” des commentaires
    suspend fun getComments(postId: String, limit: Int): List<Pair<String, CommentDto>> {
        Log.d(TAG, "📖 Getting comments snapshot for post $postId (limit: $limit)")
        val snap = firestore.collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        val comments = snap.documents.mapNotNull { d ->
            d.toObject(CommentDto::class.java)?.let { dto -> d.id to dto }
        }

        Log.d(TAG, "✅ Retrieved ${comments.size} comments")
        return comments
    }

    suspend fun isPostLiked(postId: String, uid: String): Boolean {
        val doc = firestore.collection("posts").document(postId)
            .collection("likes").document(uid)
            .get().await()
        return doc.exists()
    }

    suspend fun refreshPost(postId: String): PostDto? =
        try {
            firestore.collection("posts")
                .document(postId)
                .get(Source.SERVER)
                .await()
                .toObject(PostDto::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to refresh post", e)
            null
        }

    suspend fun createPost(
        postId: String,
        content: String,
        pleasureCategory: String?,
        pleasureTitle: String?,
        hasImage: Boolean
    ) {
        call("createPost", mapOf(
            "postId" to postId,
            "content" to content,
            "pleasureCategory" to pleasureCategory,
            "pleasureTitle" to pleasureTitle,
            "hasImage" to hasImage
        ))
    }

    suspend fun toggleLike(postId: String) {
        call("toggleLike", mapOf("postId" to postId))
    }

    suspend fun addComment(postId: String, content: String): Pair<String, CommentDto> {
        val res = call("addComment", mapOf("postId" to postId, "content" to content)) as Map<*, *>
        val id = res["id"] as String
        val c = (res["comment"] as Map<*, *>)
        return id to CommentDto(userId = c["userId"] as String, content = c["content"] as String)
    }

    suspend fun deleteComment(postId: String, commentId: String) {
        call("deleteComment", mapOf("postId" to postId, "commentId" to commentId))
    }

    suspend fun deletePost(postId: String) {
        call("deletePost", mapOf("postId" to postId))
    }

    // — Helpers —
    suspend fun call(name: String, data: Map<String, Any?>): Any? {
        return try {
            val result: HttpsCallableResult = functions.getHttpsCallable(name).call(data).await()
            result.data
        } catch (e: Exception) {
            Log.e(TAG, "❌ Cloud function $name failed", e)
            throw e
        }
    }

    companion object { private const val TAG = "FirestoreFeedSource" }
}
