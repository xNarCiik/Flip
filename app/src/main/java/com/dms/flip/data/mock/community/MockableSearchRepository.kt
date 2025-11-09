package com.dms.flip.data.mock.community

import com.dms.flip.data.repository.community.SearchRepositoryImpl
import com.dms.flip.domain.model.community.UserSearchResult
import com.dms.flip.domain.repository.SettingsRepository
import com.dms.flip.domain.repository.community.SearchRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockableSearchRepository @Inject constructor(
    private val real: SearchRepositoryImpl,
    private val mock: MockSearchRepository,
    private val settingsRepository: SettingsRepository,
) : SearchRepository {

    override suspend fun searchUsers(query: String, limit: Int): List<UserSearchResult> {
        return if (settingsRepository.useMockCommunityData.first()) {
            mock.searchUsers(query, limit)
        } else {
            real.searchUsers(query, limit)
        }
    }
}
