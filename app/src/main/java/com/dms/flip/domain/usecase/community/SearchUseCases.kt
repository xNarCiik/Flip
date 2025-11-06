package com.dms.flip.domain.usecase.community

import com.dms.flip.domain.model.community.UserSearchResult
import com.dms.flip.domain.repository.community.SearchRepository
import com.dms.flip.domain.util.Result
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke(query: String, limit: Int = 20): Result<List<UserSearchResult>> =
        runCatching { searchRepository.searchUsers(query, limit) }
            .fold(
                onSuccess = { Result.Ok(it) },
                onFailure = { Result.Err(it) }
            )
}
