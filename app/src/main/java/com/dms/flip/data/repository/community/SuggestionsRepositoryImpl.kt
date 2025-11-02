package com.dms.flip.data.repository.community

import com.dms.flip.data.firebase.mapper.toDomain
import com.dms.flip.data.firebase.source.SuggestionsSource
import com.dms.flip.domain.model.community.FriendSuggestion
import com.dms.flip.domain.repository.community.SuggestionsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuggestionsRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val suggestionsSource: SuggestionsSource
) : SuggestionsRepository {

    override fun observeSuggestions(): Flow<List<FriendSuggestion>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return suggestionsSource.observeSuggestions(uid).map { suggestions ->
            suggestions.map { (id, dto) -> dto.toDomain(id) }
        }
    }

    override suspend fun hideSuggestion(userId: String) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        suggestionsSource.hideSuggestion(uid, userId)
    }
}
