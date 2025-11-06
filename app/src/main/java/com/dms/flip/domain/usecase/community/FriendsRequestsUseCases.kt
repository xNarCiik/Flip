package com.dms.flip.domain.usecase.community

import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.repository.community.FriendsRepository
import com.dms.flip.domain.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFriendsUseCase @Inject constructor(
    private val friendsRepository: FriendsRepository
) {
    operator fun invoke(): Flow<List<PublicProfile>> = friendsRepository.observeFriends()
}

class ObservePendingReceivedUseCase @Inject constructor(
    private val friendsRepository: FriendsRepository
) {
    operator fun invoke(): Flow<List<FriendRequest>> =
        friendsRepository.observePendingReceived()
}

class ObservePendingSentUseCase @Inject constructor(
    private val friendsRepository: FriendsRepository
) {
    operator fun invoke(): Flow<List<FriendRequest>> =
        friendsRepository.observePendingSent()
}

class AcceptFriendRequestUseCase @Inject constructor(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(requestId: String): Result<Unit> =
        runCatching { friendsRepository.acceptFriend(requestId) }
            .fold(
                onSuccess = { Result.Ok(Unit) },
                onFailure = { Result.Err(it) }
            )
}

class DeclineFriendRequestUseCase @Inject constructor(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(requestId: String): Result<Unit> =
        runCatching { friendsRepository.declineFriend(requestId) }
            .fold(
                onSuccess = { Result.Ok(Unit) },
                onFailure = { Result.Err(it) }
            )
}

class CancelSentRequestUseCase @Inject constructor(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(requestId: String): Result<Unit> =
        runCatching { friendsRepository.cancelSentInvitationFriend(requestId) }
            .fold(
                onSuccess = { Result.Ok(Unit) },
                onFailure = { Result.Err(it) }
            )
}

class SendFriendRequestUseCase @Inject constructor(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> =
        runCatching { friendsRepository.sendFriendInvitation(userId) }
            .fold(
                onSuccess = { Result.Ok(Unit) },
                onFailure = { Result.Err(it) }
            )
}

class RemoveFriendUseCase @Inject constructor(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(friendId: String): Result<Unit> =
        runCatching { friendsRepository.removeFriend(friendId) }
            .fold(
                onSuccess = { Result.Ok(Unit) },
                onFailure = { Result.Err(it) }
            )
}
