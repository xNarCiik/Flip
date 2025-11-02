package com.dms.flip.domain.usecase.community

import com.dms.flip.domain.model.community.Friend
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFriendsUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    operator fun invoke(): Flow<List<Friend>> = socialRepository.getFriends()
}
