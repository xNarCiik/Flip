package com.dms.flip.data.repository

import com.dms.flip.data.model.toDto
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.repository.onboarding.OnboardingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OnboardingRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : OnboardingRepository {
    override fun getOnboardingStatus(userId: String): Flow<Boolean> = callbackFlow {
        val docRef = firestore.collection("users").document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val isCompleted = snapshot?.getBoolean("onboarding_completed") ?: false
            trySend(isCompleted).isSuccess
        }
        awaitClose { listener.remove() }
    }

    override suspend fun saveOnboardingStatus(
        username: String,
        avatarUrl: String?,
        pleasures: List<Pleasure>
    ) {
        val user = firebaseAuth.currentUser ?: return
        val userDoc = firestore.collection("users").document(user.uid)

        firestore.runBatch { batch ->
            batch.set(
                userDoc, mapOf(
                    "username" to username,
                    "avatar_url" to avatarUrl,
                    "onboarding_completed" to true
                )
            )

            pleasures.forEach { pleasure ->
                val docRef = userDoc.collection("pleasures").document()
                batch.set(docRef, pleasure.toDto())
            }
        }.await()
    }
}
