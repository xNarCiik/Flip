package com.dms.flip.data.firebase.source

import com.dms.flip.data.firebase.dto.FriendDto
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
class FirestoreFriendsSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) : FriendsSource {

    override fun observeFriends(uid: String): Flow<List<Pair<String, FriendDto>>> = callbackFlow {
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
                        fetchFriendProfile(doc.id)
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

    override suspend fun removeFriend(friendId: String) {
        functions
            .getHttpsCallable("removeFriend")
            .call(mapOf("friendId" to friendId))
            .await()
    }

    override suspend fun getFriendIds(uid: String): Set<String> {
        val snapshot = firestore.collection("users")
            .document(uid)
            .collection("friends")
            .get()
            .await()
        return snapshot.documents.map { it.id }.toSet()
    }

    private suspend fun fetchFriendProfile(friendId: String): Pair<String, FriendDto>? {
        val profileSnapshot = firestore.collection("public_profiles")
            .document(friendId)
            .get()
            .await()
        if (!profileSnapshot.exists()) return null
        val dto = profileSnapshot.toObject(FriendDto::class.java)
        return dto?.let { friendId to it }
    }
}
