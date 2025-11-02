package com.dms.flip.domain.usecase.community

import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.repository.community.FriendsRepository
import com.dms.flip.domain.repository.community.RequestsRepository
import com.dms.flip.domain.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFriendsUseCase @Inject constructor(
    private val friendsRepository: FriendsRepository
) {
    operator fun invoke(): Flow<List<Friend>> = friendsRepository.observeFriends()
}

typealias GetFriendsUseCase = ObserveFriendsUseCase

class AddFriendUseCase @Inject constructor(
    private val requestsRepository: RequestsRepository
) {
    suspend operator fun invoke(userId: String): Result<FriendRequest> =
        runCatching { requestsRepository.send(userId) }
            .fold(
                onSuccess = { Result.Ok(it) },
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
