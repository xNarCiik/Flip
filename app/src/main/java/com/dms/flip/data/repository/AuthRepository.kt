package com.dms.flip.data.repository

import android.util.Log
import com.dms.flip.domain.session.UserSessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val userSessionManager: UserSessionManager
) {

    companion object {
        private const val TAG = "AuthRepository"
    }

    /**
     * Observer l'état d'authentification
     *  Précharge automatiquement les profils d'amis lors de la connexion
     */
    fun getAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            if (user != null) {
                Log.d(TAG, "👤 User logged in: ${user.uid}")
                userSessionManager.prefetchFriendsOnLogin(user.uid)
            } else {
                Log.d(TAG, "👤 User logged out")
                userSessionManager.clearSession()
            }

            trySend(user).isSuccess
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    /**
     * Déconnexion
     * ✅ Nettoie le cache des profils
     */
    fun signOut() {
        Log.d(TAG, "👋 Signing out")

        userSessionManager.clearSession()
        auth.signOut()

        Log.d(TAG, "✅ Signed out successfully")
    }

    /**
     * Suppression du compte
     *  Nettoie le cache des profils
     */
    suspend fun deleteAccount() {
        val user = auth.currentUser ?: return

        // TODO MIGRATE TO CREDENTIAL MANAGER
        // https://developer.android.com/identity/sign-in/legacy-gsi-migration?hl=fr
        try {
            Log.d(TAG, "🗑️ Deleting account: ${user.uid}")

            userSessionManager.clearSession()

            user.delete().await()

            Log.d(TAG, "✅ Account deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to delete account", e)
            throw e
        }
    }
}