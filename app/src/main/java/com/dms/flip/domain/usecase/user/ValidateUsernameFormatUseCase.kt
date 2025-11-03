package com.dms.flip.domain.usecase.user

import javax.inject.Inject

/**
 * Use case pour valider le format d'un username
 * Règles:
 * - Minimum 3 caractères
 * - Maximum 20 caractères
 * - Uniquement lettres, chiffres, underscore et tiret
 * - Doit commencer par une lettre ou un chiffre
 */
class ValidateUsernameFormatUseCase @Inject constructor() {
    
    companion object {
        const val MIN_LENGTH = 3
        const val MAX_LENGTH = 20
        private val USERNAME_REGEX = Regex("^[a-zA-Z0-9][a-zA-Z0-9_-]*$")
    }
    
    operator fun invoke(username: String): UsernameValidationResult {
        return when {
            username.isBlank() -> {
                UsernameValidationResult.Invalid(UsernameError.EMPTY)
            }
            username.length < MIN_LENGTH -> {
                UsernameValidationResult.Invalid(UsernameError.TOO_SHORT)
            }
            username.length > MAX_LENGTH -> {
                UsernameValidationResult.Invalid(UsernameError.TOO_LONG)
            }
            !username.matches(USERNAME_REGEX) -> {
                UsernameValidationResult.Invalid(UsernameError.INVALID_CHARACTERS)
            }
            else -> {
                UsernameValidationResult.Valid
            }
        }
    }
}

sealed class UsernameValidationResult {
    data object Valid : UsernameValidationResult()
    data class Invalid(val error: UsernameError) : UsernameValidationResult()
}

enum class UsernameError {
    EMPTY,
    TOO_SHORT,
    TOO_LONG,
    INVALID_CHARACTERS,
    ALREADY_TAKEN
}
