package com.dms.flip.domain.usecase.user

import com.dms.flip.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case pour vérifier si un username est disponible
 */
class CheckUsernameAvailabilityUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(username: String): Boolean {
        return userRepository.isUsernameAvailable(username)
    }
}
