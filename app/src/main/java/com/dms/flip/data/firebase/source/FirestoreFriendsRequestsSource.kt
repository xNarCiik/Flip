package com.dms.flip.data.firebase.source

import com.dms.flip.data.firebase.dto.PublicProfileDto
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
import kotlin.jvm.java

@Singleton
class FirestoreFriendsRequestsSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) : FriendsRequestsSource {

    override fun observeFriends(uid: String): Flow<List<Pair<String, PublicProfileDto>>> = callbackFlow {
        val friendsCollection = firestore.collection("users")
            .document(uid)
            .collection("friends")

        var registration: ListenerRegistration? = null
        val job = launch {
            registration = friendsCollection.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                this@callbackFlow.launch {
                    val friends = snapshot.documents.mapNotNull { doc ->
                        fetchPublicProfile(doc.id)
                    }
                    trySend(friends)
                }
            }
        }

        awaitClose {
            registration?.remove()
            job.cancel()
        }
    }

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
    override suspend fun sendFriendInvitation(fromUserId: String, toUserId: String) {
        val data = mapOf(
            "toUserId" to toUserId
        )

        functions
            .getHttpsCallable("sendFriendRequest")
            .call(data)
            .await()
    }

    // --- ✅ Accept friend request ---
    override suspend fun acceptFriend(requestId: String) {
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
    override suspend fun declineFriend(requestId: String) {
        functions
            .getHttpsCallable("declineFriendRequest")
            .call(
                mapOf(
                    "requestId" to requestId
                )
            )
            .await()
    }

    override suspend fun removeFriend(friendId: String) {
        functions
            .getHttpsCallable("removeFriend")
            .call(mapOf("friendId" to friendId))
            .await()
    }

    // --- 🗑️ Cancel friend request ---
    override suspend fun cancelSentInvitationFriend(requestId: String) {
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
    override suspend fun getFriendIds(uid: String): Set<String> {
        val snapshot = firestore.collection("users")
            .document(uid)
            .collection("friends")
            .get()
            .await()
        return snapshot.documents.map { it.id }.toSet()
    }

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

    private suspend fun fetchPublicProfile(profileId: String): Pair<String, PublicProfileDto>? {
        val profileSnapshot = firestore.collection("public_profiles")
            .document(profileId)
            .get()
            .await()
        if (!profileSnapshot.exists()) return null
        val dto = profileSnapshot.toObject(PublicProfileDto::class.java)
        return dto?.let { profileId to it }
    }
}