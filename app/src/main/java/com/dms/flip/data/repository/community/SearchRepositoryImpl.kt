package com.dms.flip.data.repository.community

import com.dms.flip.data.firebase.source.FriendsRequestsSource
import com.dms.flip.data.firebase.source.SearchSource
import com.dms.flip.domain.model.community.RelationshipStatus
import com.dms.flip.domain.model.community.UserSearchResult
import com.dms.flip.domain.repository.community.SearchRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val searchSource: SearchSource,
    private val friendsSource: FriendsRequestsSource,
    private val friendsRequestsSource: FriendsRequestsSource
) : SearchRepository {

    override suspend fun searchUsers(query: String, limit: Int): List<UserSearchResult> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val friends = friendsSource.getFriendIds(uid)
        val pendingReceived = friendsRequestsSource.getPendingReceivedIds(uid)
        val pendingSent = friendsRequestsSource.getPendingSentIds(uid)
        return searchSource.searchUsers(query, limit)
            .filter { it.id != uid }
            .map { dto ->
                val relationship = when {
                    friends.contains(dto.id) -> RelationshipStatus.FRIEND
                    pendingSent.contains(dto.id) -> RelationshipStatus.PENDING_SENT
                    pendingReceived.contains(dto.id) -> RelationshipStatus.PENDING_RECEIVED
                    else -> RelationshipStatus.NONE
                }
                UserSearchResult(
                    id = dto.id,
                    username = dto.profile.username,
                    handle = dto.profile.handle,
                    avatarUrl = dto.profile.avatarUrl,
                    relationshipStatus = relationship
                )
            }
    }
}
