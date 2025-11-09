package com.dms.flip.data.mock.community

import com.dms.flip.data.repository.community.FriendsRepositoryImpl
import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.repository.SettingsRepository
import com.dms.flip.domain.repository.community.FriendsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockableFriendsRepository @Inject constructor(
    private val real: FriendsRepositoryImpl,
    private val mock: MockFriendsRepository,
    private val settingsRepository: SettingsRepository,
) : FriendsRepository {

    override fun observeFriends(): Flow<List<PublicProfile>> =
        settingsRepository.useMockCommunityData.flatMapLatest { useMock ->
            if (useMock) {
                mock.observeFriends()
            } else {
                real.observeFriends()
            }
        }

    override fun observePendingReceived(): Flow<List<FriendRequest>> =
        settingsRepository.useMockCommunityData.flatMapLatest { useMock ->
            if (useMock) {
                mock.observePendingReceived()
            } else {
                real.observePendingReceived()
            }
        }

    override fun observePendingSent(): Flow<List<FriendRequest>> =
        settingsRepository.useMockCommunityData.flatMapLatest { useMock ->
            if (useMock) {
                mock.observePendingSent()
            } else {
                real.observePendingSent()
            }
        }

    private suspend fun delegate(): FriendsRepository {
        return if (settingsRepository.useMockCommunityData.first()) {
            mock
        } else {
            real
        }
    }

    override suspend fun acceptFriend(requestId: String) {
        delegate().acceptFriend(requestId)
    }

    override suspend fun declineFriend(requestId: String) {
        delegate().declineFriend(requestId)
    }

    override suspend fun cancelSentInvitationFriend(requestId: String) {
        delegate().cancelSentInvitationFriend(requestId)
    }

    override suspend fun sendFriendInvitation(toUserId: String) {
        delegate().sendFriendInvitation(toUserId)
    }

    override suspend fun removeFriend(friendId: String) {
        delegate().removeFriend(friendId)
    }
}
