package com.dms.flip.data.mock.community

import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.repository.community.FriendsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockFriendsRepository @Inject constructor(
    private val dataSource: MockCommunityDataSource
) : FriendsRepository {

    override fun observeFriends(): Flow<List<Friend>> = dataSource.friends

    override suspend fun removeFriend(friendId: String) {
        dataSource.removeFriend(friendId)
    }
}
