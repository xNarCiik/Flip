package com.dms.flip.data.repository

import com.dms.flip.data.firebase.mapper.toPleasureHistoryDto
import com.dms.flip.domain.model.PleasureHistory
import com.dms.flip.domain.repository.HistoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : HistoryRepository {

    /**
     * Lit l’historique dans l’intervalle [startDate, endDate[ (millis epoch).
     */
    override fun getHistoryForDateRange(
        startDate: Long,
        endDate: Long
    ): Flow<List<PleasureHistory>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())

        return callbackFlow {
            val ref = firestore.collection("users")
                .document(uid)
                .collection("history")

            val startTimestamp = Timestamp(Date(startDate))
            val endTimestamp = Timestamp(Date(endDate))

            val query = ref
                .whereGreaterThanOrEqualTo("dateDrawn", startTimestamp)
                .whereLessThan("dateDrawn", endTimestamp)
                .orderBy("dateDrawn")

            val registration = query.addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList()).isSuccess
                    return@addSnapshotListener
                }

                val list = snap?.documents
                    ?.mapNotNull { doc -> doc.toPleasureHistoryDto()?.toDomain() }
                    ?.filter { it.dateDrawn != 0L }
                    ?.sortedBy { it.dateDrawn }
                    .orEmpty()

                trySend(list).isSuccess
            }

            awaitClose { registration.remove() }
        }
    }
}
