package com.dms.flip.data.firebase.source

import com.dms.flip.data.firebase.dto.CommentDto
import com.dms.flip.data.firebase.dto.PostDto
import com.dms.flip.domain.model.community.Paged
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    private val firestore: FirebaseFirestore
) : FeedSource {

    override fun observeFriendsFeed(
        uid: String,
        limit: Int,
        cursor: String?
    ): Flow<Paged<FeedSource.PostDocument>> = callbackFlow {
        // 🔹 Get friend IDs + self
        val friendsSnapshot = firestore
            .collection("users")
            .document(uid)
            .collection("friends")
            .get()
            .await()
        val friendIds = friendsSnapshot.documents.map { it.id } + uid

        // 🔹 Split into chunks of 10 (Firestore limit)
        val chunks = friendIds.chunked(10)

        if (chunks.isEmpty()) {
            trySend(Paged(emptyList(), null))
            close()
            return@callbackFlow
        }

        // 🔹 Create one Flow per chunk
        val chunkFlows = chunks.map { chunkIds ->
            callbackFlow {
                val query = firestore.collection("posts")
                    .whereIn("authorId", chunkIds)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit.toLong())

                val registration = query.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener

                    val docs = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PostDto::class.java)?.let { dto ->
                            FeedSource.PostDocument(id = doc.id, data = dto)
                        }
                    }
                    trySend(docs)
                }

                awaitClose { registration.remove() }
            }
        }

        // 🔹 Combine all chunk flows
        val combinedFlow = combine(chunkFlows) { chunkResults ->
            chunkResults.toList().flatten()
                .distinctBy { it.id } // remove duplicates
                .sortedByDescending { it.data.timestamp }
                .take(limit)
        }

        // 🔹 Collect and emit merged pages
        val job = launch {
            combinedFlow.collect { posts ->
                val nextCursor = posts.lastOrNull()?.id
                trySend(Paged(posts, nextCursor))
            }
        }

        awaitClose { job.cancel() }
    }

    override suspend fun toggleLike(postId: String, uid: String, like: Boolean) {
        val likesCollection = firestore.collection("posts").document(postId).collection("likes")
        val postRef = firestore.collection("posts").document(postId)
        firestore.runTransaction { transaction ->
            val increment = if (like) 1 else -1
            if (like) transaction.set(likesCollection.document(uid), mapOf("liked" to true))
            else transaction.delete(likesCollection.document(uid))
            transaction.update(postRef, "likes_count", FieldValue.increment(increment.toLong()))
        }.await()
    }

    override suspend fun addComment(postId: String, comment: CommentDto): Pair<String, CommentDto> {
        val commentsCollection = firestore.collection("posts")
            .document(postId)
            .collection("comments")
        val doc = commentsCollection.document()
        doc.set(comment).await()
        firestore.collection("posts").document(postId)
            .update("comments_count", FieldValue.increment(1))
            .await()
        return doc.id to comment
    }

    override suspend fun getComments(postId: String, limit: Int): List<Pair<String, CommentDto>> {
        val snapshot = firestore.collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(CommentDto::class.java)?.let { dto -> doc.id to dto }
        }
    }

    override suspend fun isPostLiked(postId: String, uid: String): Boolean {
        val doc = firestore.collection("posts")
            .document(postId)
            .collection("likes")
            .document(uid)
            .get()
            .await()
        return doc.exists()
    }

    override suspend fun deletePost(postId: String, uid: String) {
        val postRef = firestore.collection("posts").document(postId)
        val postSnapshot = postRef.get().await()
        val postAuthor = postSnapshot.getString("authorId")

        if (postAuthor != uid) throw IllegalAccessException("You can only delete your own posts.")
        postRef.delete().await()
    }

    override suspend fun deleteComment(postId: String, commentId: String, uid: String) {
        val commentRef = firestore.collection("posts")
            .document(postId)
            .collection("comments")
            .document(commentId)

        val snapshot = commentRef.get().await()
        val commentUserId = snapshot.getString("userId")
        if (commentUserId != uid) throw IllegalAccessException("Cannot delete someone else's comment.")

        commentRef.delete().await()
        firestore.collection("posts").document(postId)
            .update("comments_count", FieldValue.increment(-1))
            .await()
    }
}
