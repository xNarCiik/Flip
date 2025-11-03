package com.dms.flip.data.repository

import com.dms.flip.domain.model.UserInfo
import com.dms.flip.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implémentation du UserRepository avec Firebase
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USERNAMES_COLLECTION = "usernames"
        private const val FIELD_USERNAME = "username"
        private const val FIELD_EMAIL = "email"
        private const val FIELD_AVATAR_URL = "avatar_url"
        private const val FIELD_UID = "uid"
    }

    override fun getUserInfo(): Flow<UserInfo?> = callbackFlow {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listenerRegistration = firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val userInfo = snapshot?.let {
                    UserInfo(
                        id = userId, // TODO REPLACE BY username ?
                        username = it.getString(FIELD_USERNAME) ?: "",
                        email = it.getString(FIELD_EMAIL) ?: "",
                        avatarUrl = it.getString(FIELD_AVATAR_URL)
                    )
                }
                trySend(userInfo)
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun isUsernameAvailable(username: String): Boolean {
        return try {
            val normalizedUsername = username.trim().lowercase()

            val querySnapshot = firestore
                .collection(USERNAMES_COLLECTION)
                .document(normalizedUsername)
                .get()
                .await()

            !querySnapshot.exists()
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateUserProfile(username: String?, avatarUrl: String?) {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")

        val updates = mutableMapOf<String, Any?>()
        username?.let { updates[FIELD_USERNAME] = it.trim() }
        avatarUrl?.let { updates[FIELD_AVATAR_URL] = it }

        if (updates.isEmpty()) return

        try {
            firestore.runTransaction { transaction ->
                val userRef = firestore.collection(USERS_COLLECTION).document(userId)

                username?.let { newUsername ->
                    val normalizedUsername = newUsername.trim().lowercase()

                    val currentData = transaction.get(userRef)
                    val oldUsername = currentData.getString(FIELD_USERNAME)?.lowercase()

                    if (oldUsername != null && oldUsername != normalizedUsername) {
                        val oldUsernameRef = firestore
                            .collection(USERNAMES_COLLECTION)
                            .document(oldUsername)
                        transaction.delete(oldUsernameRef)
                    }

                    val newUsernameRef = firestore
                        .collection(USERNAMES_COLLECTION)
                        .document(normalizedUsername)
                    transaction.set(newUsernameRef, mapOf(FIELD_UID to userId))
                }

                transaction.update(userRef, updates)
            }.await()
        } catch (e: Exception) {
            throw e
        }
    }
}
