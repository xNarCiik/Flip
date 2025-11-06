package com.dms.flip.data.firebase.source

import com.dms.flip.data.firebase.dto.RecentActivityDto
import com.google.firebase.firestore.DocumentSnapshot

interface ProfileSource {
    suspend fun getPublicProfilesChunk(userIds: List<String>): List<DocumentSnapshot>
    suspend fun getRecentActivities(userId: String, limit: Int = 10): List<Pair<String, RecentActivityDto>>
}
