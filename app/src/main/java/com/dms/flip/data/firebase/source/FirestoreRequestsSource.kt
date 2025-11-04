package com.dms.flip.data.firebase.source

import com.dms.flip.data.firebase.dto.RequestDto
import com.dms.flip.data.firebase.mapper.toRequestDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRequestsSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) : RequestsSource {

    override fun observePendingReceived(uid: String): Flow<List<Pair<String, RequestDto>>> =
        callbackFlow {
            val collection = firestore.collection("users")
                .document(uid)
                .collection("friend_requests_received")
            var registration: ListenerRegistration? = null
            val job = launch {
                registration = collection.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toRequestDto()?.let { dto -> doc.id to dto }
                    }
                    trySend(items)
                }
            }
            awaitClose {
                registration?.remove()
                job.cancel()
            }
        }

    override fun observePendingSent(uid: String): Flow<List<Pair<String, RequestDto>>> =
        callbackFlow {
            val collection = firestore.collection("users")
                .document(uid)
                .collection("friend_requests_sent")
            var registration: ListenerRegistration? = null
            val job = launch {
                registration = collection.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toRequestDto()?.let { dto -> doc.id to dto }
                    }
                    trySend(items)
                }
            }
            awaitClose {
                registration?.remove()
                job.cancel()
            }
        }

    // --- 📨 Send friend request ---
    override suspend fun send(fromUserId: String, toUserId: String): Pair<String, RequestDto> {
        val data = mapOf(
            "toUserId" to toUserId
        )

        val result = functions
            .getHttpsCallable("sendFriendRequest")
            .call(data)
            .await()
            .data as Map<*, *>

        val dto = RequestDto(
            fromUserId = result["fromUserId"] as String,
            toUserId = result["toUserId"] as String,
            fromUsername = result["fromUsername"] as String,
            fromHandle = result["fromHandle"] as String,
            fromAvatarUrl = result["fromAvatarUrl"] as? String
        )

        val id = result["id"] as String
        return id to dto
    }

    // --- ✅ Accept friend request ---
    override suspend fun accept(requestId: String) {
        functions
            .getHttpsCallable("acceptFriendRequest")
            .call(
                mapOf(
                    "requestId" to requestId
                )
            )
            .await()
    }

    // --- ❌ Decline friend request ---
    override suspend fun decline(requestId: String) {
        functions
            .getHttpsCallable("declineFriendRequest")
            .call(
                mapOf(
                    "requestId" to requestId
                )
            )
            .await()
    }

    // --- 🗑️ Cancel friend request ---
    override suspend fun cancelSent( requestId: String) {
        functions
            .getHttpsCallable("cancelFriendRequest")
            .call(
                mapOf(
                    "requestId" to requestId
                )
            )
            .await()
    }

    // --- 📊 Utils ---
    override suspend fun getPendingReceivedIds(uid: String): Set<String> {
        val snapshot = firestore.collection("users")
            .document(uid)
            .collection("friend_requests_received")
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.getString("fromUserId") }.toSet()
    }

    override suspend fun getPendingSentIds(uid: String): Set<String> {
        val snapshot = firestore.collection("users")
            .document(uid)
            .collection("friend_requests_sent")
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.getString("toUserId") }.toSet()
    }
}
