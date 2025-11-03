package com.dms.flip.domain.repository

import com.dms.flip.domain.model.UserInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour la gestion des utilisateurs
 */
interface UserRepository {
    /**
     * Obtenir les informations de l'utilisateur connecté
     */
    fun getUserInfo(): Flow<UserInfo?>

    /**
     * Vérifier si un username est disponible
     * @param username Le username à vérifier
     * @return true si disponible, false sinon
     */
    suspend fun isUsernameAvailable(username: String): Boolean

    /**
     * Mettre à jour le profil utilisateur
     */
    suspend fun updateUserProfile(
        username: String? = null,
        avatarUrl: String? = null
    )
}
