package com.dms.flip.data.repository.community

import com.dms.flip.data.firebase.mapper.toDomain
import com.dms.flip.data.firebase.source.FriendsSource
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.repository.community.FriendsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendsRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val friendsSource: FriendsSource
) : FriendsRepository {

    override fun observeFriends(): Flow<List<Friend>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return friendsSource.observeFriends(uid).map { friends ->
            friends.map { (id, dto) -> dto.toDomain(id) }
        }
    }

    override suspend fun removeFriend(friendId: String) {
        auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        friendsSource.removeFriend(friendId)
    }
}
