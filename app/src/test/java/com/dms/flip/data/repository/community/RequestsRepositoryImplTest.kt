package com.dms.flip.data.repository.community

import com.dms.flip.data.firebase.dto.RequestDto
import com.dms.flip.data.firebase.source.FriendsRequestsSource
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private lateinit var requestsSource: FakeFriendsRequestsSource
    private lateinit var repository: FriendsRequestsRepositoryImpl

    @Before
    fun setUp() {
        auth = mock()
        user = mock()
        whenever(auth.currentUser).thenReturn(user)
        whenever(user.uid).thenReturn("uid")
        requestsSource = FakeFriendsRequestsSource()
        repository = FriendsRequestsRepositoryImpl(auth, requestsSource)
    }

    @Test
    fun observePendingReceived_emitsRequests() = runBlocking {
        requestsSource.emitReceived(listOf("req" to RequestDto(userId = "user", username = "Alice", handle = "@alice")))

        val requests = repository.observePendingReceived().first()

        assertThat(requests).hasSize(1)
        assertThat(requests.first().userId).isEqualTo("user")
    }

    @Test
    fun send_createsRequest() = runBlocking {
        requestsSource.sendResult = "req" to RequestDto(userId = "target", username = "Bob", handle = "@bob")

        val request = repository.sendFriendInvitation("target")

        assertThat(request.id).isEqualTo("req")
        assertThat(request.userId).isEqualTo("target")
        assertThat(requestsSource.lastSendUid).isEqualTo("uid")
    }

    @Test
    fun accept_delegatesToSource() = runBlocking {
        repository.acceptFriend("req")

        assertThat(requestsSource.acceptedId).isEqualTo("req")
        assertThat(requestsSource.acceptedUid).isEqualTo("uid")
    }

    private class FakeFriendsRequestsSource : FriendsRequestsSource {
        private val receivedFlow = MutableSharedFlow<List<Pair<String, RequestDto>>>(replay = 1)
        private val sentFlow = MutableSharedFlow<List<Pair<String, RequestDto>>>(replay = 1)
        var sendResult: Pair<String, RequestDto> = "" to RequestDto()
        var lastSendUid: String? = null
        var acceptedId: String? = null
        var acceptedUid: String? = null

        suspend fun emitReceived(value: List<Pair<String, RequestDto>>) {
            receivedFlow.emit(value)
        }

        override fun observePendingReceived(uid: String) = receivedFlow

        override fun observePendingSent(uid: String) = sentFlow

        override suspend fun accept(uid: String, requestId: String) {
            acceptedUid = uid
            acceptedId = requestId
        }

        override suspend fun decline(uid: String, requestId: String) {}

        override suspend fun cancelSent(uid: String, requestId: String) {}

        override suspend fun sendFriendInvitation(uid: String, toUserId: String): Pair<String, RequestDto> {
            lastSendUid = uid
            return sendResult
        }

        override suspend fun getPendingReceivedIds(uid: String): Set<String> = emptySet()

        override suspend fun getPendingSentIds(uid: String): Set<String> = emptySet()
    }
}
