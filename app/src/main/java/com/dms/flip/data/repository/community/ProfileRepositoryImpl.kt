package com.dms.flip.data.repository.community

import com.dms.flip.data.firebase.mapper.toDomain
import com.dms.flip.data.firebase.source.FriendsRequestsSource
import com.dms.flip.data.firebase.source.ProfileSource
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.model.community.RelationshipStatus
import com.dms.flip.domain.repository.community.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val profileSource: ProfileSource,
    private val friendsSource: FriendsRequestsSource,
    private val friendsRequestsSource: FriendsRequestsSource
) : ProfileRepository {

    override suspend fun getPublicProfile(userId: String): PublicProfile {
        val currentUid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val profileDto = profileSource.getPublicProfile(userId)
            ?: throw IllegalStateException("Profile not found")
        //val activities = profileSource.getRecentActivities(userId) // TODO RULES
        //    .mapNotNull { (id, dto) -> dto.toDomain(id) }
        val relationship = determineRelationship(currentUid, userId)
        return profileDto.toDomain(
            id = userId,
            recentActivities = listOf(),
            relationshipStatus = relationship
        )
    }

    private suspend fun determineRelationship(currentUid: String, otherUserId: String): RelationshipStatus {
        if (currentUid == otherUserId) return RelationshipStatus.FRIEND
        val friends = friendsSource.getFriendIds(currentUid)
        if (friends.contains(otherUserId)) return RelationshipStatus.FRIEND
        val pendingSent = friendsRequestsSource.getPendingSentIds(currentUid)
        if (pendingSent.contains(otherUserId)) return RelationshipStatus.PENDING_SENT
        val pendingReceived = friendsRequestsSource.getPendingReceivedIds(currentUid)
        if (pendingReceived.contains(otherUserId)) return RelationshipStatus.PENDING_RECEIVED
        return RelationshipStatus.NONE
    }
}
