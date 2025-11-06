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
) : FeedSource {

    override fun observeFriendsFeed(
        uid: String,
        limit: Int,
        cursor: String?
    ): Flow<Paged<FeedSource.PostDocument>> = callbackFlow {
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

        // ✅ Récupérer le document cursor si fourni
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
                
                // ✅ Appliquer le cursor si disponible
                if (cursorDocument != null && cursorDocument.exists()) {
                    query = query.startAfter(cursorDocument)
                }
                
                query = query.limit(limit.toLong())

                val reg = query.addSnapshotListener { snap, err ->
                    if (err != null) {
                        Log.e(TAG, "❌ Error in chunk $index", err)
                        close(err)
                        return@addSnapshotListener
                    }
                    if (snap == null) {
                        Log.w(TAG, "⚠️ Null snapshot in chunk $index")
                        return@addSnapshotListener
                    }
                    
                    val docs = snap.documents.mapNotNull { d ->
                        d.toObject(PostDto::class.java)?.let { dto ->
                            FeedSource.PostDocument(d.id, dto)
                        }
                    }
                    
                    Log.d(TAG, "✅ Chunk $index updated: ${docs.size} posts")
                    trySend(docs)
                }
                awaitClose { 
                    Log.d(TAG, "🔴 STOP chunk $index")
                    reg.remove() 
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

    override fun observeComments(postId: String): Flow<List<Pair<String, CommentDto>>> = callbackFlow {
        Log.d(TAG, "🔵 START observing comments for post $postId")

        val reg = firestore.collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    if (err.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(TAG, "Listener 'comments' pour $postId détaché (post sûrement supprimé).")
                        // Ne pas fermer en erreur. Le listener est déjà mort.
                        // La mise à jour du feed principal (observeFriendsFeed) gérera la UI.
                        return@addSnapshotListener
                    }

                    Log.e(TAG, "❌ Error observing comments for $postId", err)
                    close(err)
                    return@addSnapshotListener
                }

                if (snap == null) {
                    Log.w(TAG, "⚠️ Null snapshot for comments of $postId")
                    return@addSnapshotListener
                }
                
                val comments = snap.documents.mapNotNull { d ->
                    d.toObject(CommentDto::class.java)?.let { dto -> d.id to dto }
                }
                
                Log.d(TAG, "✅ Comments updated for $postId: ${comments.size} comments")
                trySend(comments)
            }
        
        awaitClose { 
            Log.d(TAG, "🔴 STOP observing comments for post $postId")
            reg.remove() 
        }
    }

    override fun observePostLikeStatus(postId: String, uid: String): Flow<Boolean> = callbackFlow {
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

    override fun observePostLikeCount(postId: String): Flow<Int> = callbackFlow {
        Log.d(TAG, "🔵 START observing like count for post $postId")

        val reg = firestore.collection("posts")
            .document(postId)
            .collection("likes")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    if (err.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(TAG, "Listener 'like count' pour $postId détaché (post sûrement supprimé).")
                        return@addSnapshotListener
                    }

                    Log.e(TAG, "❌ Error observing like count for $postId", err)
                    close(err)
                    return@addSnapshotListener
                }

                val count = snap?.size() ?: 0
                Log.d(TAG, "✅ Like count updated for $postId: $count likes")
                trySend(count)
            }
        
        awaitClose { 
            Log.d(TAG, "🔴 STOP observing like count for post $postId")
            reg.remove() 
        }
    }

    override suspend fun createPost(
        postId: String,
        content: String,
        pleasureCategory: String?,
        pleasureTitle: String?,
        hasImage: Boolean
    ) {
        Log.d(TAG, "📝 Creating post document $postId...")
        call(
            name = "createPost",
            data = mapOf(
                "postId" to postId,
                "content" to content,
                "pleasureCategory" to pleasureCategory,
                "pleasureTitle" to pleasureTitle,
                "hasImage" to hasImage
            )
        )
        Log.d(TAG, "✅ Post document $postId created successfully")
    }

    override suspend fun toggleLike(postId: String) {
        Log.d(TAG, "❤️ Toggling like for post $postId")
        call("toggleLike", mapOf("postId" to postId))
        Log.d(TAG, "✅ Like toggled successfully")
    }

    override suspend fun addComment(postId: String, content: String): Pair<String, CommentDto> {
        Log.d(TAG, "💬 Adding comment to post $postId")
        val res = call("addComment", mapOf("postId" to postId, "content" to content))

        val data = res as Map<*, *>
        val id = data["id"] as String
        val c = (data["comment"] as Map<*, *>)

        val saved = CommentDto(
            userId = c["userId"] as String,
            content = c["content"] as String
        )
        Log.d(TAG, "✅ Comment added successfully: $id")
        return id to saved
    }

    override suspend fun deleteComment(postId: String, commentId: String) {
        Log.d(TAG, "🗑️ Deleting comment $commentId from post $postId")
        call("deleteComment", mapOf("postId" to postId, "commentId" to commentId))
        Log.d(TAG, "✅ Comment deleted successfully")
    }

    override suspend fun deletePost(postId: String) {
        call("deletePost", mapOf("postId" to postId))
        Log.d(TAG, "✅ Post deleted successfully")
    }

    override suspend fun getComments(postId: String, limit: Int): List<Pair<String, CommentDto>> {
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

    override suspend fun isPostLiked(postId: String, uid: String): Boolean {
        Log.d(TAG, "❤️ Checking if post $postId is liked by user $uid")
        val doc = firestore.collection("posts").document(postId)
            .collection("likes").document(uid)
            .get().await()
        val isLiked = doc.exists()
        Log.d(TAG, "✅ Like status: $isLiked")
        return isLiked
    }

    override suspend fun getLikeCount(postId: String): Int {
        Log.d(TAG, "❤️ Getting like count for post $postId")
        val snap = firestore.collection("posts")
            .document(postId)
            .collection("likes")
            .get()
            .await()
        val count = snap.size()
        Log.d(TAG, "✅ Like count: $count")
        return count
    }

    override suspend fun refreshPost(postId: String): PostDto? {
        Log.d(TAG, "🔄 Forcing refresh from server for post $postId")
        return try {
            val doc = firestore.collection("posts")
                .document(postId)
                .get(Source.SERVER) // ✅ Force fetch depuis le serveur
                .await()
            val post = doc.toObject(PostDto::class.java)
            Log.d(TAG, "✅ Post refreshed successfully")
            post
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to refresh post", e)
            null
        }
    }

    // ————————————————————————————————————————
    // Helpers
    // ————————————————————————————————————————
    private suspend fun call(name: String, data: Map<String, Any?>): Any? {
        try {
            Log.d(TAG, "☁️ Calling cloud function: $name")
            val result: HttpsCallableResult = functions
                .getHttpsCallable(name)
                .call(data)
                .await()
            Log.d(TAG, "✅ Cloud function $name completed successfully")
            return result.data
        } catch (exception: Exception) {
            Log.e(TAG, "❌ Cloud function $name failed", exception)
            throw exception
        }
    }

    companion object {
        private const val TAG = "FirestoreFeedSource"
    }
}
