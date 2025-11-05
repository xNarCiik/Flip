package com.dms.flip.data.repository.community

import com.dms.flip.data.firebase.dto.PublicProfileDto
import com.dms.flip.data.firebase.dto.FriendDto
import com.dms.flip.data.firebase.dto.RequestDto
import com.dms.flip.data.firebase.source.FriendsRequestsSource
import com.dms.flip.data.firebase.source.SearchResultDto
import com.dms.flip.data.firebase.source.SearchSource
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SearchRepositoryImplTest {

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var searchSource: FakeSearchSource
    private lateinit var friendsSource: FakeFriendsRequestsSource
    private lateinit var repository: SearchRepositoryImpl

    @Before
    fun setUp() {
        auth = mock()
        user = mock()
        whenever(auth.currentUser).thenReturn(user)
        whenever(user.uid).thenReturn("uid")
        searchSource = FakeSearchSource()
        friendsSource = FakeFriendsRequestsSource()
        repository = SearchRepositoryImpl(auth, searchSource, friendsSource, friendsSource)
    }

    @Test
    fun searchUsers_setsRelationshipStatus() = runBlocking {
        friendsSource.friendIds = setOf("friend")
        friendsSource.pendingSent = setOf("pending")
        friendsSource.pendingReceived = setOf("received")
        searchSource.results = listOf(
            SearchResultDto("friend", PublicProfileDto(username = "Friend", handle = "@friend")),
            SearchResultDto("pending", PublicProfileDto(username = "Pending", handle = "@pending")),
            SearchResultDto(
                "received",
                PublicProfileDto(username = "Received", handle = "@received")
            ),
            SearchResultDto("other", PublicProfileDto(username = "Other", handle = "@other"))
        )

        val results = repository.searchUsers("a")

        val statuses = results.associateBy({ it.id }, { it.relationshipStatus })
        assertThat(statuses["friend"]?.name).isEqualTo("FRIEND")
        assertThat(statuses["pending"]?.name).isEqualTo("PENDING_SENT")
        assertThat(statuses["received"]?.name).isEqualTo("PENDING_RECEIVED")
        assertThat(statuses["other"]?.name).isEqualTo("NONE")
    }

    private class FakeSearchSource : SearchSource {
        var results: List<SearchResultDto> = emptyList()
        override suspend fun searchUsers(query: String, limit: Int): List<SearchResultDto> = results
    }

    private class FakeFriendsRequestsSource : FriendsRequestsSource {
        var pendingReceived: Set<String> = emptySet()
        var pendingSent: Set<String> = emptySet()
        var friendIds: Set<String> = emptySet()

        override fun observeFriends(uid: String): Flow<List<Pair<String, FriendDto>>> =
            flowOf(emptyList())

        override fun observePendingReceived(uid: String) =
            flowOf(emptyList<Pair<String, RequestDto>>())
        override fun observePendingSent(uid: String) = flowOf(emptyList<Pair<String, RequestDto>>())
        override suspend fun acceptFriend(requestId: String) {}
        override suspend fun declineFriend(requestId: String) {}
        override suspend fun cancelSentInvitationFriend(requestId: String) {}
        override suspend fun sendFriendInvitation(fromUserId: String, toUserId: String) {}
        override suspend fun removeFriend(friendId: String) {}
        override suspend fun getFriendIds(uid: String): Set<String> = friendIds
        override suspend fun getPendingReceivedIds(uid: String): Set<String> = pendingReceived
        override suspend fun getPendingSentIds(uid: String): Set<String> = pendingSent
    }
}
