package com.dms.flip.data.repository.community

import com.dms.flip.data.firebase.mapper.toDomain
import com.dms.flip.data.firebase.mapper.toPendingReceived
import com.dms.flip.data.firebase.mapper.toPendingSent
import com.dms.flip.data.firebase.source.FriendsRequestsSource
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.repository.community.FriendsRequestsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map

@Singleton
class FriendsRequestsRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val friendsRequestsSource: FriendsRequestsSource
) : FriendsRequestsRepository {

    override fun observePendingReceived(): Flow<List<FriendRequest>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return friendsRequestsSource.observePendingReceived(uid).map { requests ->
            requests.map { (id, dto) -> dto.toPendingReceived(id) }
        }
    }

    override fun observePendingSent(): Flow<List<FriendRequest>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return friendsRequestsSource.observePendingSent(uid).map { requests ->
            requests.map { (id, dto) -> dto.toPendingSent(id) }
        }
    }

    override suspend fun acceptFriend(requestId: String) {
        auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        friendsRequestsSource.acceptFriend(requestId)
    }

    override suspend fun declineFriend(requestId: String) {
        auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        friendsRequestsSource.declineFriend(requestId)
    }

    override suspend fun cancelSentInvitationFriend(requestId: String) {
        auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        friendsRequestsSource.cancelSentInvitationFriend(requestId)
    }

    override suspend fun sendFriendInvitation(toUserId: String) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        friendsRequestsSource.sendFriendInvitation(uid, toUserId)
    }

    override fun observeFriends(): Flow<List<Friend>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return friendsRequestsSource.observeFriends(uid).map { friends ->
            friends.map { (id, dto) -> dto.toDomain(id) }
        }
    }

    override suspend fun removeFriend(friendId: String) {
        auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        friendsRequestsSource.removeFriend(friendId)
    }
}
