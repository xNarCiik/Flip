package com.dms.flip.data.firebase.source

import com.dms.flip.data.firebase.dto.CommentDto
import com.dms.flip.data.firebase.dto.PostDto
import com.dms.flip.domain.model.community.Paged
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
        val feedCollection =
            firestore.collection("users")
                .document(uid)
                .collection("feed")

        suspend fun buildQuery(): Query {
            var query: Query = feedCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
            if (cursor != null) {
                val cursorSnapshot = feedCollection.document(cursor).get().await()
                if (cursorSnapshot.exists()) {
                    query = query.startAfter(cursorSnapshot)
                }
            }
            return query
        }

        var registration: ListenerRegistration? = null
        val job = launch {
            val query = buildQuery()
            registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                val documents = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PostDto::class.java)?.let { dto ->
                        FeedSource.PostDocument(id = doc.id, data = dto)
                    }
                }
                val nextCursor =
                    if (documents.size < limit) null else snapshot.documents.lastOrNull()?.id
                trySend(Paged(documents, nextCursor))
            }
        }

        awaitClose {
            registration?.remove()
            job.cancel()
        }
    }

    override suspend fun toggleLike(postId: String, uid: String, like: Boolean) {
        val likesCollection = firestore.collection("posts").document(postId).collection("likes")
        val postRef = firestore.collection("posts").document(postId)
        firestore.runTransaction { transaction ->
            val likesCountField = FieldValue.increment(if (like) 1 else -1)
            if (like) {
                transaction.set(likesCollection.document(uid), mapOf("liked" to true))
            } else {
                transaction.delete(likesCollection.document(uid))
            }
            transaction.update(postRef, mapOf("likes_count" to likesCountField))
        }.await()
    }

    override suspend fun addComment(postId: String, comment: CommentDto): Pair<String, CommentDto> {
        val commentsCollection = firestore.collection("posts")
            .document(postId)
            .collection("comments")
        val document = commentsCollection.document()
        document.set(comment).await()
        firestore.collection("posts").document(postId)
            .update("comments_count", FieldValue.increment(1))
            .await()
        return document.id to comment
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
            doc.toObject(CommentDto::class.java)?.let { doc.id to it }
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
}
