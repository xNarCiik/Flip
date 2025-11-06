package com.dms.flip.data.firebase.source

import com.dms.flip.data.firebase.dto.RequestDto
import kotlinx.coroutines.flow.Flow

interface FriendsSource {
    fun observeFriendIds(uid: String): Flow<List<String>>
    fun observePendingReceived(uid: String): Flow<List<Pair<String, RequestDto>>>
    fun observePendingSent(uid: String): Flow<List<Pair<String, RequestDto>>>
    suspend fun acceptFriend(requestId: String)
    suspend fun declineFriend(requestId: String)
    suspend fun cancelSentInvitationFriend(requestId: String)
    suspend fun sendFriendInvitation(fromUserId: String, toUserId: String)
    suspend fun removeFriend(friendId: String)
    suspend fun getFriendIds(uid: String): Set<String>
    suspend fun getPendingReceivedIds(uid: String): Set<String>
    suspend fun getPendingSentIds(uid: String): Set<String>
}
