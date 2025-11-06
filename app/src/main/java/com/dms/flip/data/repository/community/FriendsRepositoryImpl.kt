package com.dms.flip.data.repository.community

import com.dms.flip.data.cache.ProfileBatchLoader
import com.dms.flip.data.firebase.mapper.toPendingReceived
import com.dms.flip.data.firebase.mapper.toPendingSent
import com.dms.flip.data.firebase.source.FriendsSource
import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.repository.community.FriendsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map

@Singleton
class FriendsRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val friendsSource: FriendsSource,
    private val profileBatchLoader: ProfileBatchLoader
) : FriendsRepository {

    override fun observeFriends(): Flow<List<PublicProfile>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())

        return friendsSource.observeFriendIds(uid)
            .map { friendIds ->
                if (friendIds.isEmpty()) return@map emptyList()
                val profiles = profileBatchLoader.loadProfiles(friendIds)
                friendIds.mapNotNull { profiles[it] }
            }
    }

    override fun observePendingReceived(): Flow<List<FriendRequest>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return friendsSource.observePendingReceived(uid).map { requests ->
            requests.map { (id, dto) -> dto.toPendingReceived(id) }
        }
    }

    override fun observePendingSent(): Flow<List<FriendRequest>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return friendsSource.observePendingSent(uid).map { requests ->
            requests.map { (id, dto) -> dto.toPendingSent(id) }
        }
    }

    override suspend fun acceptFriend(requestId: String) {
        auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        friendsSource.acceptFriend(requestId)
        profileBatchLoader.invalidate(requestId)
    }

    override suspend fun declineFriend(requestId: String) {
        auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        friendsSource.declineFriend(requestId)
        profileBatchLoader.invalidate(requestId)
    }

    override suspend fun cancelSentInvitationFriend(requestId: String) {
        auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        friendsSource.cancelSentInvitationFriend(requestId)
        profileBatchLoader.invalidate(requestId)
        profileBatchLoader.invalidate(requestId)
    }

    override suspend fun sendFriendInvitation(toUserId: String) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        friendsSource.sendFriendInvitation(uid, toUserId)
        profileBatchLoader.invalidate(toUserId)
    }

    override suspend fun removeFriend(friendId: String) {
        auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        friendsSource.removeFriend(friendId)
        profileBatchLoader.invalidate(friendId)
    }
}
