package com.dms.flip.data.firebase.source

import com.dms.flip.data.firebase.dto.PublicProfileDto
import com.dms.flip.data.firebase.dto.RecentActivityDto
import com.dms.flip.data.firebase.mapper.toRecentActivityDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreProfileSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProfileSource {

    override suspend fun getPublicProfile(userId: String): PublicProfileDto? {
        val snapshot = firestore.collection("public_profiles")
            .document(userId)
            .get()
            .await()
        return snapshot.toObject(PublicProfileDto::class.java)
    }

    override suspend fun getRecentActivities(
        userId: String,
        limit: Int
    ): List<Pair<String, RecentActivityDto>> {
        val collection = firestore.collection("users")
            .document(userId)
            .collection("recent_activities")
        val snapshot = collection
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        val activities = snapshot.documents.mapNotNull { doc ->
            doc.toRecentActivityDto()?.let { dto -> doc.id to dto }
        }
        return activities
    }
}
