package com.dms.flip.data.mock.community

import com.dms.flip.domain.model.community.UserSearchResult
import com.dms.flip.domain.repository.community.SearchRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockSearchRepository @Inject constructor(
    private val dataSource: MockCommunityDataSource
) : SearchRepository {

    override suspend fun searchUsers(query: String, limit: Int): List<UserSearchResult> =
        dataSource.searchUsers(query, limit)
}
