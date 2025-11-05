package com.dms.flip.domain.repository.community

import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.FriendRequest
import kotlinx.coroutines.flow.Flow

interface FriendsRequestsRepository {
    fun observeFriends(): Flow<List<Friend>>
    fun observePendingReceived(): Flow<List<FriendRequest>>
    fun observePendingSent(): Flow<List<FriendRequest>>
    suspend fun acceptFriend(requestId: String)
    suspend fun declineFriend(requestId: String)
    suspend fun cancelSentInvitationFriend(requestId: String)
    suspend fun sendFriendInvitation(toUserId: String)
    suspend fun removeFriend(friendId: String)
}
