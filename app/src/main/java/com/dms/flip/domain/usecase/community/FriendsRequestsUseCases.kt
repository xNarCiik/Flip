package com.dms.flip.domain.usecase.community

import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.repository.community.FriendsRequestsRepository
import com.dms.flip.domain.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFriendsUseCase @Inject constructor(
    private val friendsRepository: FriendsRequestsRepository
) {
    operator fun invoke(): Flow<List<Friend>> = friendsRepository.observeFriends()
}

class ObservePendingReceivedUseCase @Inject constructor(
    private val friendsRequestsRepository: FriendsRequestsRepository
) {
    operator fun invoke(): Flow<List<FriendRequest>> =
        friendsRequestsRepository.observePendingReceived()
}

class ObservePendingSentUseCase @Inject constructor(
    private val friendsRequestsRepository: FriendsRequestsRepository
) {
    operator fun invoke(): Flow<List<FriendRequest>> =
        friendsRequestsRepository.observePendingSent()
}

class AcceptFriendRequestUseCase @Inject constructor(
    private val friendsRequestsRepository: FriendsRequestsRepository
) {
    suspend operator fun invoke(requestId: String): Result<Unit> =
        runCatching { friendsRequestsRepository.acceptFriend(requestId) }
            .fold(
                onSuccess = { Result.Ok(Unit) },
                onFailure = { Result.Err(it) }
            )
}

class DeclineFriendRequestUseCase @Inject constructor(
    private val friendsRequestsRepository: FriendsRequestsRepository
) {
    suspend operator fun invoke(requestId: String): Result<Unit> =
        runCatching { friendsRequestsRepository.declineFriend(requestId) }
            .fold(
                onSuccess = { Result.Ok(Unit) },
                onFailure = { Result.Err(it) }
            )
}

class CancelSentRequestUseCase @Inject constructor(
    private val friendsRequestsRepository: FriendsRequestsRepository
) {
    suspend operator fun invoke(requestId: String): Result<Unit> =
        runCatching { friendsRequestsRepository.cancelSentInvitationFriend(requestId) }
            .fold(
                onSuccess = { Result.Ok(Unit) },
                onFailure = { Result.Err(it) }
            )
}

class SendFriendRequestUseCase @Inject constructor(
    private val friendsRequestsRepository: FriendsRequestsRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> =
        runCatching { friendsRequestsRepository.sendFriendInvitation(userId) }
            .fold(
                onSuccess = { Result.Ok(Unit) },
                onFailure = { Result.Err(it) }
            )
}

class RemoveFriendUseCase @Inject constructor(
    private val friendsRepository: FriendsRequestsRepository
) {
    suspend operator fun invoke(friendId: String): Result<Unit> =
        runCatching { friendsRepository.removeFriend(friendId) }
            .fold(
                onSuccess = { Result.Ok(Unit) },
                onFailure = { Result.Err(it) }
            )
}
