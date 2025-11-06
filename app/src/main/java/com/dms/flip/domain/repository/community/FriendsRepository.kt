package com.dms.flip.domain.repository.community

import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.model.community.PublicProfile
import kotlinx.coroutines.flow.Flow

interface FriendsRepository {
    fun observeFriends(): Flow<List<PublicProfile>>
    fun observePendingReceived(): Flow<List<FriendRequest>>
    fun observePendingSent(): Flow<List<FriendRequest>>
    suspend fun acceptFriend(requestId: String)
    suspend fun declineFriend(requestId: String)
    suspend fun cancelSentInvitationFriend(requestId: String)
    suspend fun sendFriendInvitation(toUserId: String)
    suspend fun removeFriend(friendId: String)
}
