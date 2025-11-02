package com.dms.flip.data.repository.community

import com.dms.flip.data.firebase.mapper.toPendingReceived
import com.dms.flip.data.firebase.mapper.toPendingSent
import com.dms.flip.data.firebase.source.RequestsSource
import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.repository.community.RequestsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestsRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val requestsSource: RequestsSource
) : RequestsRepository {

    override fun observePendingReceived(): Flow<List<FriendRequest>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return requestsSource.observePendingReceived(uid).map { requests ->
            requests.map { (id, dto) -> dto.toPendingReceived(id) }
        }
    }

    override fun observePendingSent(): Flow<List<FriendRequest>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return requestsSource.observePendingSent(uid).map { requests ->
            requests.map { (id, dto) -> dto.toPendingSent(id) }
        }
    }

    override suspend fun accept(requestId: String) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        requestsSource.accept(uid, requestId)
    }

    override suspend fun decline(requestId: String) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        requestsSource.decline(uid, requestId)
    }

    override suspend fun cancelSent(requestId: String) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        requestsSource.cancelSent(uid, requestId)
    }

    override suspend fun send(toUserId: String): FriendRequest {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val (id, dto) = requestsSource.send(uid, toUserId)
        return dto.toPendingSent(id)
    }
}
