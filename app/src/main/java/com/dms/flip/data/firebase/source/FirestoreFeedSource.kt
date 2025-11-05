package com.dms.flip.data.firebase.source

import com.dms.flip.data.firebase.dto.CommentDto
import com.dms.flip.data.firebase.dto.PostDto
import com.dms.flip.domain.model.community.Paged
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
        val friendIds = (firestore.collection("users")
            .document(uid)
            .collection("friends")
            .get()
            .await()
            .documents.map { it.id } + uid)

        val chunks = friendIds.chunked(10)
        if (chunks.isEmpty()) {
            trySend(Paged(emptyList(), null))
            close()
            return@callbackFlow
        }

        val chunkFlows = chunks.map { ids ->
            callbackFlow {
                val q = firestore.collection("posts")
                    .whereIn("authorId", ids)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit.toLong())

                val reg = q.addSnapshotListener { snap, err ->
                    if (err != null) {
                        close(err)
                        return@addSnapshotListener
                    }
                    if (snap == null) return@addSnapshotListener
                    val docs = snap.documents.mapNotNull { d ->
                        d.toObject(PostDto::class.java)?.let { dto ->
                            FeedSource.PostDocument(d.id, dto)
                        }
                    }
                    trySend(docs)
                }
                awaitClose { reg.remove() }
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
                trySend(Paged(posts, posts.lastOrNull()?.id))
            }
        }

        awaitClose { job.cancel() }
    }

    override suspend fun createPost(
        content: String,
        pleasureCategory: String?,
        pleasureTitle: String?,
        photoUrl: String?
    ) {
        call(
            name = "createPost",
            data = mapOf(
                "content" to content,
                "pleasureCategory" to pleasureCategory,
                "pleasureTitle" to pleasureTitle,
                "photoUrl" to photoUrl
            )
        )
    }

    override suspend fun toggleLike(postId: String) {
        call("toggleLike", mapOf("postId" to postId))
    }

    override suspend fun addComment(postId: String, comment: CommentDto): Pair<String, CommentDto> {
        val res = call("addComment", mapOf("postId" to postId, "content" to comment.content))
        val data = res as Map<*, *>
        val id = data["id"] as String
        val c = (data["comment"] as Map<*, *>)
        val saved = CommentDto(
            userId = c["userId"] as String,
            username = c["username"] as? String ?: "",
            userHandle = c["userHandle"] as? String ?: "",
            avatarUrl = c["avatarUrl"] as? String,
            content = c["content"] as String
        )
        return id to saved
    }

    override suspend fun deleteComment(postId: String, commentId: String) {
        call("deleteComment", mapOf("postId" to postId, "commentId" to commentId))
    }

    override suspend fun deletePost(postId: String) {
        call("deletePost", mapOf("postId" to postId))
    }

    override suspend fun getComments(postId: String, limit: Int): List<Pair<String, CommentDto>> {
        val snap = firestore.collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        return snap.documents.mapNotNull { d ->
            d.toObject(CommentDto::class.java)?.let { dto -> d.id to dto }
        }
    }

    override suspend fun isPostLiked(postId: String, uid: String): Boolean {
        val doc = firestore.collection("posts").document(postId)
            .collection("likes").document(uid)
            .get().await()
        return doc.exists()
    }

    // ———————————————————————————————————————
    // Helpers
    // ———————————————————————————————————————
    private suspend fun call(name: String, data: Map<String, Any?>): Any? {
        try {
            val result: HttpsCallableResult = functions
                .getHttpsCallable(name)
                .call(data)
                .await()
            return result.data
        } catch (exception: Exception) {
            return null // TODO REMOVE ITS ONLY FOR DEBUG
        }
    }
}
