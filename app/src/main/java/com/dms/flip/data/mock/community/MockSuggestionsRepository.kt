package com.dms.flip.data.mock.community

import com.dms.flip.domain.model.community.FriendSuggestion
import com.dms.flip.domain.repository.community.SuggestionsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockSuggestionsRepository @Inject constructor(
    private val dataSource: MockCommunityDataSource
) : SuggestionsRepository {

    override fun observeSuggestions(): Flow<List<FriendSuggestion>> = dataSource.suggestions

    override suspend fun hideSuggestion(userId: String) {
        dataSource.hideSuggestion(userId)
    }
}
