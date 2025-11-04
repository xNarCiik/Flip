package com.dms.flip.data.firebase.source

import com.dms.flip.data.firebase.dto.FriendDto
import kotlinx.coroutines.flow.Flow

interface FriendsSource {
    fun observeFriends(uid: String): Flow<List<Pair<String, FriendDto>>>
    suspend fun removeFriend(friendId: String)
    suspend fun getFriendIds(uid: String): Set<String>
}
