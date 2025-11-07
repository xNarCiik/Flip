package com.dms.flip.data.cache

import android.util.Log
import com.dms.flip.data.firebase.dto.PublicProfileDto
import com.dms.flip.data.firebase.mapper.toDomain
import com.dms.flip.data.firebase.source.FriendsSource
import com.dms.flip.data.firebase.source.ProfileSource
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.model.community.RelationshipStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache et batch loader pour les profils utilisateur.
 */
@Singleton
class ProfileBatchLoader @Inject constructor(
    private val auth: FirebaseAuth,
    private val profileSource: ProfileSource,
    private val friendsSource: FriendsSource
) {
    private val cache = ConcurrentHashMap<String, CachedProfile>()
    private val mutex = Mutex()

    data class CachedProfile(
        val profile: PublicProfile,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        // Durée de validité du cache (10 minutes)
        fun isExpired(duration: Long = 10 * 60 * 1000L): Boolean =
            System.currentTimeMillis() - timestamp > duration
    }

    /**
     * Charge plusieurs profils en optimisant les appels réseau.
     *
     * Stratégie :
     * 1. Vérifier le cache pour chaque userId
     * 2. Charger seulement les profils manquants/expirés en parallèle
     * 3. Mettre à jour le cache
     *
     * @param userIds Liste des IDs utilisateur à charger
     * @return Map des profils chargés (userId -> Profile)
     */
    suspend fun loadProfiles(userIds: List<String>): Map<String, PublicProfile> = mutex.withLock {
        if (userIds.isEmpty()) {
            Log.d(TAG, "🔭 No profiles to load")
            return emptyMap()
        }

        Log.d(TAG, "🔥 Loading ${userIds.size} profiles...")
        val now = System.currentTimeMillis()

        // Séparer entre profils en cache et profils à charger
        val (cached, toLoad) = userIds.partition { userId ->
            cache[userId]?.let { !it.isExpired() } ?: false
        }

        Log.d(TAG, "💾 ${cached.size} profiles in cache, ${toLoad.size} to load")

        // Récupérer depuis le cache
        val cachedProfiles = cached.mapNotNull { userId ->
            cache[userId]?.let { userId to it.profile }
        }.toMap()

        // Si tout est en cache, retourner directement
        if (toLoad.isEmpty()) {
            Log.d(TAG, "✅ All profiles loaded from cache")
            return@withLock cachedProfiles
        }

        // Charger les profils manquants en parallèle
        val loadedProfiles = loadProfilesInParallel(toLoad)

        // Mettre à jour le cache
        loadedProfiles.forEach { (userId, profile) ->
            cache[userId] = CachedProfile(profile, now)
        }

        Log.d(
            TAG,
            "✅ Loaded ${loadedProfiles.size} new profiles, total ${cachedProfiles.size + loadedProfiles.size}"
        )

        return@withLock cachedProfiles + loadedProfiles
    }

    /**
     * Charge un seul profil (utilise le cache si disponible).
     */
    suspend fun loadProfile(userId: String): PublicProfile? {
        Log.d(TAG, "🔥 Loading profile for user $userId")
        return loadProfiles(listOf(userId))[userId]
    }

    /**
     * Charge les profils en parallèle pour maximiser la vitesse.
     */
    private suspend fun loadProfilesInParallel(userIds: List<String>): Map<String, PublicProfile> =
        coroutineScope {
            val currentUid =
                auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

            val chunks = userIds.chunked(30)
            Log.d(TAG, "⚡ Loading ${userIds.size} profiles in ${chunks.size} parallel chunks...")

            val deferreds = chunks.map { chunk ->
                async {
                    try {
                        val snapshot = profileSource.getPublicProfilesChunk(chunk)
                        snapshot.mapNotNull { doc ->
                            val profile = doc.toObject(PublicProfileDto::class.java)
                            profile?.let {
                                doc.id to it.toDomain(
                                    id = doc.id,
                                    relationshipStatus = determineRelationship(currentUid, doc.id)
                                )
                            }
                        }.toMap()
                    } catch (e: Exception) {
                        val message = e.message ?: ""
                        if (message.contains("PERMISSION_DENIED", ignoreCase = true)) {
                            Log.w(TAG, "⚠️ Permission denied while loading chunk (${chunk.size} users). Ignoring.")

                            chunk.forEach { userId ->
                                cache.remove(userId)
                                Log.d(TAG, "🗑️ Removed $userId from cache due to permission loss")
                            }

                            emptyMap()
                        } else {
                            Log.e(TAG, "❌ Failed to load chunk (${chunk.size} users)", e)
                            emptyMap()
                        }
                    }
                }
            }

            val results = deferreds.awaitAll().flatMap { it.toList() }.toMap()
            Log.d(TAG, "✅ Loaded ${results.size}/${userIds.size} profiles successfully")
            return@coroutineScope results
        }

    // TODO VALUE UPDATED IN CLOUD FONCTION ?
    private suspend fun determineRelationship(
        currentUid: String,
        otherUserId: String
    ): RelationshipStatus {
        if (currentUid == otherUserId) return RelationshipStatus.FRIEND

        val friends = friendsSource.getFriendIds(currentUid)
        if (friends.contains(otherUserId)) return RelationshipStatus.FRIEND

        val pendingSent = friendsSource.getPendingSentIds(currentUid)
        if (pendingSent.contains(otherUserId)) return RelationshipStatus.PENDING_SENT

        val pendingReceived = friendsSource.getPendingReceivedIds(currentUid)
        if (pendingReceived.contains(otherUserId)) return RelationshipStatus.PENDING_RECEIVED

        return RelationshipStatus.NONE
    }

    /**
     * Invalide le cache pour un utilisateur spécifique.
     * Utile après une mise à jour de profil.
     */
    fun invalidate(userId: String) {
        cache.remove(userId)
        Log.d(TAG, "🗑️ Invalidated cache for user $userId")
    }

    /**
     * Invalide tous les profils du cache.
     */
    fun invalidateAll() {
        val size = cache.size
        cache.clear()
        Log.d(TAG, "🗑️ Invalidated all cache ($size entries)")
    }

    /**
     * Nettoie les entrées expirées du cache.
     * À appeler périodiquement (ex: toutes les 10 minutes).
     */
    fun cleanupExpired() {
        val expired = cache.filter { (_, cached) -> cached.isExpired() }
        expired.keys.forEach { cache.remove(it) }

        if (expired.isNotEmpty()) {
            Log.d(TAG, "🧹 Cleaned up ${expired.size} expired cache entries")
        }
    }

    /**
     * Pré-charge des profils en arrière-plan.
     * Utile pour le prefetching.
     */
    suspend fun prefetch(userIds: List<String>) {
        Log.d(TAG, "🔮 Prefetching ${userIds.size} profiles...")
        loadProfiles(userIds)
    }

    /**
     * Retourne les statistiques du cache pour le monitoring.
     */
    fun getCacheStats(): CacheStats {
        val total = cache.size
        val expired = cache.count { (_, cached) -> cached.isExpired() }
        return CacheStats(
            totalEntries = total,
            expiredEntries = expired,
            validEntries = total - expired
        )
    }

    data class CacheStats(
        val totalEntries: Int,
        val expiredEntries: Int,
        val validEntries: Int
    )

    companion object {
        private const val TAG = "ProfileBatchLoader"
    }
}
