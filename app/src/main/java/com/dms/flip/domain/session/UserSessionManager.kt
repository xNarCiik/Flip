package com.dms.flip.domain.session

import android.util.Log
import com.dms.flip.data.cache.ProfileBatchLoader
import com.dms.flip.data.firebase.source.FriendsSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gère la session utilisateur et le préchargement des données
 */
@Singleton
class UserSessionManager @Inject constructor(
    private val profileBatchLoader: ProfileBatchLoader,
    private val friendsSource: FriendsSource
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "UserSessionManager"
    }

    /**
     * Précharge les profils d'amis en arrière-plan après la connexion
     * Cette opération est non-bloquante et améliore l'UX
     */
    fun prefetchFriendsOnLogin(uid: String) {
        Log.d(TAG, "🔮 Starting prefetch for user $uid")

        scope.launch {
            try {
                val friendIds = friendsSource.getFriendIds(uid).toList()

                if (friendIds.isEmpty()) {
                    Log.d(TAG, "📭 No friends to prefetch")
                    return@launch
                }

                Log.d(TAG, "🔮 Prefetching ${friendIds.size} friend profiles...")

                profileBatchLoader.prefetch(friendIds)

                Log.d(TAG, "✅ Prefetched ${friendIds.size} friends successfully")

                val stats = profileBatchLoader.getCacheStats()
                Log.d(TAG, "📊 Cache now has ${stats.validEntries} profiles")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Prefetch failed (non-critical)", e)
            }
        }
    }

    /**
     * Nettoie la session (appelé au logout)
     */
    fun clearSession() {
        Log.d(TAG, "🧹 Clearing session cache")
        profileBatchLoader.invalidateAll()
    }
}