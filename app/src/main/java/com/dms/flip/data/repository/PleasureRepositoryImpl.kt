package com.dms.flip.data.repository

import com.dms.flip.data.model.PleasureCategory
import com.dms.flip.data.model.PleasureDto
import com.dms.flip.data.model.PleasureHistoryDto
import com.dms.flip.data.model.toDto
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.model.PleasureHistory
import com.dms.flip.domain.repository.PleasureRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PleasureRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : PleasureRepository {

    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    override fun getPleasures(): Flow<List<Pleasure>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val collectionRef = firestore
            .collection("users")
            .document(userId)
            .collection("pleasures")

        val listener = collectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }

            val pleasures = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(PleasureDto::class.java)?.toDomain(doc.id)
            }.orEmpty()

            trySend(pleasures)
        }

        awaitClose { listener.remove() }
    }

    override fun getPleasuresCount(): Flow<Int> {
        return getPleasures().map { pleasures ->
            pleasures.count { it.isEnabled }
        }
    }

    override fun getRandomPleasure(category: PleasureCategory?): Flow<Pleasure> {
        return getPleasures().map { pleasures ->
            var filteredList =
                pleasures.filter { it.isEnabled && (category == PleasureCategory.ALL || category == null || it.category == category) }
            if (filteredList.isEmpty()) {
                filteredList = pleasures.filter { it.isEnabled }
            }
            filteredList.random()
        }
    }

    override suspend fun insert(pleasure: Pleasure) {
        firestore.collection("users").document(userId).collection("pleasures")
            .add(pleasure.toDto()).await()
    }

    override suspend fun update(pleasure: Pleasure) {
        firestore.collection("users").document(userId).collection("pleasures")
            .document(pleasure.id).set(pleasure.toDto()).await()
    }

    override suspend fun delete(pleasuresId: List<String>) {
        val batch = firestore.batch()
        pleasuresId.forEach { pleasureId ->
            val docRef = firestore.collection("users").document(userId).collection("pleasures")
                .document(pleasureId)
            batch.delete(docRef)
        }
        batch.commit().await()
    }

    override suspend fun upsertPleasureHistory(pleasureHistory: PleasureHistory) {
        firestore.collection("users").document(userId).collection("history")
            .document(pleasureHistory.id).set(pleasureHistory.toDto(), SetOptions.merge()).await()
    }

    override suspend fun getPleasureHistory(id: String): PleasureHistory? {
        val document = firestore.collection("users")
            .document(userId)
            .collection("history")
            .document(id)
            .get()
            .await()

        if (!document.exists()) {
            return null
        }

        val dto = document.toObject(PleasureHistoryDto::class.java) ?: return null

        return dto.toDomain()
    }

    override fun observePleasureHistory(id: String): Flow<PleasureHistory?> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val documentRef = firestore.collection("users")
            .document(userId)
            .collection("history")
            .document(id)

        var lastNonNull: PleasureHistory? = null
        var hasEmittedInitial = false

        val registration = documentRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val dto = snapshot.toObject(PleasureHistoryDto::class.java)
                val history = dto?.toDomain()
                if (history != null && history != lastNonNull) {
                    lastNonNull = history
                    hasEmittedInitial = true
                    trySend(history).isSuccess
                }
            } else {
                if (!hasEmittedInitial) {
                    hasEmittedInitial = true
                    trySend(null).isSuccess
                } else if (lastNonNull != null) {
                    lastNonNull = null
                    trySend(null).isSuccess
                }
            }
        }

        awaitClose { registration.remove() }
    }.distinctUntilChanged()
}
