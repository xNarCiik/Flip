package com.dms.flip.domain.usecase.community

import com.dms.flip.domain.repository.SocialRepository
import javax.inject.Inject

class AddFriendUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    suspend operator fun invoke(username: String) = socialRepository.addFriend(username)
}
