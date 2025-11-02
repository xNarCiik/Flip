package com.dms.flip.data.mock.community

import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.model.community.FriendRequestSource
import com.dms.flip.domain.repository.community.RequestsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockRequestsRepository @Inject constructor(
    private val dataSource: MockCommunityDataSource
) : RequestsRepository {

    override fun observePendingReceived(): Flow<List<FriendRequest>> = dataSource.pendingReceived

    override fun observePendingSent(): Flow<List<FriendRequest>> = dataSource.pendingSent

    override suspend fun accept(requestId: String) {
        val request = dataSource.pendingReceived.value.firstOrNull { it.id == requestId }
            ?: throw IllegalArgumentException("Friend request not found: $requestId")
        dataSource.removePendingReceived(requestId)
        val friend = request.toFriend(dataSource)
        dataSource.addFriend(friend)
    }

    override suspend fun decline(requestId: String) {
        dataSource.removePendingReceived(requestId)
    }

    override suspend fun cancelSent(requestId: String) {
        dataSource.removePendingSent(requestId)
    }

    override suspend fun send(toUserId: String): FriendRequest {
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
        return request
    }

    private fun FriendRequest.toFriend(dataSource: MockCommunityDataSource): Friend {
        val base = dataSource.getUser(userId)
        return base.copy(
            username = if (username.isNotBlank()) username else base.username,
            handle = if (handle.isNotBlank()) handle else base.handle,
            avatarUrl = avatarUrl ?: base.avatarUrl
        )
    }
}
