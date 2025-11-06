package com.dms.flip.data.repository.community

import com.dms.flip.data.firebase.dto.RequestDto
import com.dms.flip.data.firebase.source.FriendsSource
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RequestsRepositoryImplTest {

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var requestsSource: FakeFriendsSource
    private lateinit var repository: FriendsRepositoryImpl

    @Before
    fun setUp() {
        auth = mock()
        user = mock()
        whenever(auth.currentUser).thenReturn(user)
        whenever(user.uid).thenReturn("uid")
        requestsSource = FakeFriendsSource()
        repository = FriendsRepositoryImpl(auth, requestsSource)
    }

    @Test
    fun observePendingReceived_emitsRequests() = runBlocking {
        requestsSource.emitReceived(
            listOf(
                "req" to RequestDto(
                    fromUserId = "user",
                    fromUsername = "Alice",
                    fromHandle = "@alice"
                )
            )
        )

        val requests = repository.observePendingReceived().first()

        assertThat(requests).hasSize(1)
        assertThat(requests.first().userId).isEqualTo("user")
    }

    @Test
    fun send_createsRequest() = runBlocking {
        requestsSource.sendResult =
            "req" to RequestDto(toUserId = "target", toUsername = "Bob", toHandle = "@bob")

        repository.sendFriendInvitation("target")

        assertThat(requestsSource.lastSendUid).isEqualTo("uid")
    }

    @Test
    fun accept_delegatesToSource() = runBlocking {
        repository.acceptFriend("req")

        assertThat(requestsSource.acceptedId).isEqualTo("req")
        assertThat(requestsSource.acceptedUid).isEqualTo("uid")
    }

    private class FakeFriendsSource : FriendsSource {
        private val receivedFlow = MutableSharedFlow<List<Pair<String, RequestDto>>>(replay = 1)
        private val sentFlow = MutableSharedFlow<List<Pair<String, RequestDto>>>(replay = 1)
        var sendResult: Pair<String, RequestDto> = "" to RequestDto()
        var lastSendUid: String? = null
        var acceptedId: String? = null
        var acceptedUid: String? = null

        suspend fun emitReceived(value: List<Pair<String, RequestDto>>) {
            receivedFlow.emit(value)
        }

        override fun observeFriendIds(uid: String): Flow<List<String>> {
            TODO("Not yet implemented")
        }

        override fun observePendingReceived(uid: String) = receivedFlow

        override fun observePendingSent(uid: String) = sentFlow

        override suspend fun acceptFriend(requestId: String) {
            TODO("Not yet implemented")
        }

        override suspend fun declineFriend(requestId: String) {
            TODO("Not yet implemented")
        }

        override suspend fun cancelSentInvitationFriend(requestId: String) {
            TODO("Not yet implemented")
        }

        override suspend fun sendFriendInvitation(fromUserId: String, toUserId: String) {
            lastSendUid = fromUserId
        }

        override suspend fun removeFriend(friendId: String) {
            TODO("Not yet implemented")
        }

        override suspend fun getFriendIds(uid: String): Set<String> {
            TODO("Not yet implemented")
        }

        override suspend fun getPendingReceivedIds(uid: String): Set<String> = emptySet()

        override suspend fun getPendingSentIds(uid: String): Set<String> = emptySet()
    }
}
