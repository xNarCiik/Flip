package com.dms.flip.data.mock.community

import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.model.community.FriendRequestSource
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.repository.community.FriendsRequestsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockFriendsRequestsRepository @Inject constructor(
    private val dataSource: MockCommunityDataSource
) : FriendsRequestsRepository {
    override fun observeFriends(): Flow<List<PublicProfile>> = dataSource.friends

    override fun observePendingReceived(): Flow<List<FriendRequest>> = dataSource.pendingReceived

    override fun observePendingSent(): Flow<List<FriendRequest>> = dataSource.pendingSent

    override suspend fun acceptFriend(requestId: String) {
        val request = dataSource.pendingReceived.value.firstOrNull { it.id == requestId }
            ?: throw IllegalArgumentException("Friend request not found: $requestId")
        dataSource.removePendingReceived(requestId)
        val friend = request.toFriend(dataSource)
        dataSource.addFriend(friend)
    }

    override suspend fun declineFriend(requestId: String) {
        dataSource.removePendingReceived(requestId)
    }

    override suspend fun cancelSentInvitationFriend(requestId: String) {
        dataSource.removePendingSent(requestId)
    }

    override suspend fun sendFriendInvitation(toUserId: String) {
        val user = dataSource.getUser(toUserId)
        val request = FriendRequest(
            id = dataSource.nextRequestId(),
            userId = user.id,
            username = user.username,
            handle = user.handle,
            avatarUrl = user.avatarUrl,
            requestedAt = System.currentTimeMillis(),
            source = FriendRequestSource.SEARCH
        )
        dataSource.addPendingSent(request)
    }

    override suspend fun removeFriend(friendId: String) {
        dataSource.removeFriend(friendId)
    }

    private fun FriendRequest.toFriend(dataSource: MockCommunityDataSource): PublicProfile {
        val base = dataSource.getUser(userId)
        return base.copy(
            username = if (username.isNotBlank()) username else base.username,
            handle = if (handle.isNotBlank()) handle else base.handle,
            avatarUrl = avatarUrl ?: base.avatarUrl
        )
    }
}
