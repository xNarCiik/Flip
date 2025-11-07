package com.dms.flip.ui.community

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dms.flip.data.repository.community.FeedRepositoryImpl
import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.model.community.FriendSuggestion
import com.dms.flip.domain.model.community.Paged
import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.model.community.RelationshipStatus
import com.dms.flip.domain.usecase.community.AcceptFriendRequestUseCase
import com.dms.flip.domain.usecase.community.CancelSentRequestUseCase
import com.dms.flip.domain.usecase.community.DeclineFriendRequestUseCase
import com.dms.flip.domain.usecase.community.FeedUseCases
import com.dms.flip.domain.usecase.community.HideSuggestionUseCase
import com.dms.flip.domain.usecase.community.ObserveFriendsUseCase
import com.dms.flip.domain.usecase.community.ObservePendingReceivedUseCase
import com.dms.flip.domain.usecase.community.ObservePendingSentUseCase
import com.dms.flip.domain.usecase.community.ObserveSuggestionsUseCase
import com.dms.flip.domain.usecase.community.RemoveFriendUseCase
import com.dms.flip.domain.usecase.community.SearchUsersUseCase
import com.dms.flip.domain.usecase.community.SendFriendRequestUseCase
import com.dms.flip.domain.usecase.user.GetUserInfoUseCase
import com.dms.flip.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

private const val FEED_PAGE_SIZE = 20
private const val COMMENTS_PAGE_SIZE = 50
private const val TAG = "CommunityViewModel"

private data class CommunityData(
    val feed: Paged<Post>,
    val friends: List<PublicProfile>,
    val pendingReceived: List<FriendRequest>,
    val pendingSent: List<FriendRequest>,
    val suggestions: List<FriendSuggestion>
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val observeFriendsUseCase: ObserveFriendsUseCase,
    private val feedUseCases: FeedUseCases,
    private val removeFriendUseCase: RemoveFriendUseCase,
    private val observePendingReceivedUseCase: ObservePendingReceivedUseCase,
    private val observePendingSentUseCase: ObservePendingSentUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val declineFriendRequestUseCase: DeclineFriendRequestUseCase,
    private val cancelSentRequestUseCase: CancelSentRequestUseCase,
    private val sendFriendRequestUseCase: SendFriendRequestUseCase,
    private val observeSuggestionsUseCase: ObserveSuggestionsUseCase,
    private val hideSuggestionUseCase: HideSuggestionUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val feedRepository: FeedRepositoryImpl,
    getUserInfoUseCase: GetUserInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private var observationJob: Job? = null
    private var searchJob: Job? = null
    private var refreshJob: Job? = null
    private var cacheCleanupJob: Job? = null
    private var isLoadingMoreFeed = false
    private var isScrolledToTop = true
    private var lastSeenPostTimestamp: Long = System.currentTimeMillis()

    private val pendingActions = mutableMapOf<String, Job>()

    // 💬 Jobs de chargement des commentaires pour éviter les appels multiples
    private val commentLoadingJobs = mutableMapOf<String, Job>()

    init {
        observeSearch()
        loadInitial()
        startCacheCleanup()

        viewModelScope.launch {
            getUserInfoUseCase().collect { userInfo ->
                _uiState.update { it.copy(currentUserId = userInfo?.id) }
            }
        }
    }

    private fun startCacheCleanup() {
        cacheCleanupJob = viewModelScope.launch {
            while (true) {
                delay(10.minutes)
                feedRepository.cleanupExpiredCache()
                logCacheStats()
            }
        }
    }

    private fun logCacheStats() {
        val stats = feedRepository.getCacheStats()
        Log.d(
            TAG, """
            📊 Cache Stats:
              - Total entries: ${stats.totalEntries}
              - Valid entries: ${stats.validEntries}
              - Expired entries: ${stats.expiredEntries}
        """.trimIndent()
        )
    }

    fun refresh(forceReload: Boolean = false) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            Log.d(TAG, "🔄 Refresh triggered (forceReload: $forceReload)")
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }

            if (forceReload) {
                Log.d(TAG, "♻️ Force reload: canceling and restarting observations")
                cancelObservations()
                ensureObservationsStarted()
            } else {
                try {
                    Log.d(TAG, "🔄 Soft refresh: taking first emission")
                    val page = feedUseCases.observeFriendsFeed(FEED_PAGE_SIZE, null).first()
                    _uiState.update { it.copy(posts = page.items.toPersistentList()) }
                    Log.d(TAG, "✅ Soft refresh completed: ${page.items.size} posts")
                } catch (t: Throwable) {
                    Log.e(TAG, "❌ Soft refresh failed", t)
                    handleError(t, stopInitial = false, stopRefresh = true)
                }
            }

            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun onEvent(event: CommunityEvent) {
        when (event) {
            is CommunityEvent.OnPostLiked -> togglePostLike(event.postId)
            is CommunityEvent.OnToggleComments -> toggleComments(event.postId)
            is CommunityEvent.OnAddComment -> addComment(event.postId, event.comment)
            is CommunityEvent.OnRemoveFriend -> removeFriend(event.friend)
            is CommunityEvent.OnAddSuggestion -> addSuggestion(event.suggestion)
            is CommunityEvent.OnHideSuggestion -> hideSuggestion(event.suggestion)
            is CommunityEvent.OnAcceptFriendRequest -> acceptFriendRequest(event.request)
            is CommunityEvent.OnDeclineFriendRequest -> declineFriendRequest(event.request)
            is CommunityEvent.OnCancelSentRequest -> cancelFriendRequest(event.request)
            is CommunityEvent.OnSearchQueryChanged -> handleSearchQuery(event.query)
            is CommunityEvent.OnAddUserFromSearch -> addUserFromSearch(event.userId)
            is CommunityEvent.OnLoadMorePosts -> loadMorePosts()
            is CommunityEvent.OnRetryClicked -> {
                if (_uiState.value.posts.isEmpty()) {
                    loadInitial(force = true)
                } else {
                    refresh()
                }
            }

            is CommunityEvent.OnDeleteComment -> deleteComment(event.postId, event.commentId)
            is CommunityEvent.OnDeletePost -> deletePost(event.postId)
            is CommunityEvent.OnAcceptFriendRequestFromProfile ->
                acceptFriendRequestFromProfile(event.userId)

            is CommunityEvent.OnRemoveFriendFromProfile -> removeFriendFromProfile(event.userId)
            is CommunityEvent.OnRefresh -> refresh(event.forceReload)
            is CommunityEvent.OnNewPostsAlertClicked -> handleNewPostsAlertClicked()
            is CommunityEvent.OnFeedScrolled -> onScrollStateChanged(event.isAtTop)
            else -> Unit
        }
    }

    suspend fun getPublicProfileById(userId: String): PublicProfile? =
        feedRepository.getPublicProfile(userId = userId)

    private fun handleNewPostsAlertClicked() {
        Log.d(TAG, "📌 New posts alert clicked")
        _uiState.update {
            it.copy(
                showNewPostsAlert = false,
                newPostsCount = 0
            )
        }
        lastSeenPostTimestamp =
            _uiState.value.posts.firstOrNull()?.timestamp ?: System.currentTimeMillis()
    }

    private fun handleScrolledToTop() {
        isScrolledToTop = true
        if (_uiState.value.showNewPostsAlert) {
            _uiState.update {
                it.copy(
                    showNewPostsAlert = false,
                    newPostsCount = 0,
                    scrollToTopTrigger = it.scrollToTopTrigger + 1
                )
            }
            lastSeenPostTimestamp =
                _uiState.value.posts.firstOrNull()?.timestamp ?: System.currentTimeMillis()
        }
    }

    private fun handleScrolledAwayFromTop() {
        isScrolledToTop = false
    }

    private fun onScrollStateChanged(isAtTop: Boolean) {
        if (isAtTop && !isScrolledToTop) {
            handleScrolledToTop()
        } else if (!isAtTop && isScrolledToTop) {
            handleScrolledAwayFromTop()
        }
    }

    private fun loadInitial(force: Boolean = false) {
        if (observationJob?.isActive == true && !force) {
            Log.d(TAG, "⏭️ Observations already active, skipping loadInitial")
            return
        }

        Log.d(TAG, "🚀 Loading initial community data (force: $force)")
        if (force) {
            cancelObservations()
        }

        _uiState.update {
            it.copy(
                isLoadingInitial = true,
                errorMessage = null
            )
        }

        ensureObservationsStarted()
    }

    private fun ensureObservationsStarted() {
        if (observationJob?.isActive == true) {
            Log.d(TAG, "⏭️ Observations already running")
            return
        }

        Log.d(TAG, "👀 Starting fresh observations")
        val observeFriendsFeedUseCase = feedUseCases.observeFriendsFeed

        observationJob = viewModelScope.launch {
            try {
                combine(
                    observeFriendsFeedUseCase(FEED_PAGE_SIZE, null),
                    observeFriendsUseCase(),
                    observePendingReceivedUseCase(),
                    observePendingSentUseCase(),
                    observeSuggestionsUseCase()
                ) { feed, friends, pendingReceived, pendingSent, suggestions ->
                    CommunityData(feed, friends, pendingReceived, pendingSent, suggestions)
                }
                    .catch { t ->
                        Log.e(TAG, "❌ Error in data observation", t)
                        handleError(t, stopInitial = true, stopRefresh = false)
                    }
                    .distinctUntilChanged()
                    .collect { data ->
                        val (feed, friends, pendingReceived, pendingSent, suggestions) = data

                        val friendsList = friends.sortedBy { it.username }.toPersistentList()
                        val pendingReceivedList =
                            pendingReceived.sortedBy { it.username }.toPersistentList()
                        val pendingSentList =
                            pendingSent.sortedBy { it.username }.toPersistentList()

                        val newPostsCount =
                            if (!isScrolledToTop) feed.items.count { it.timestamp > lastSeenPostTimestamp } else 0

                        _uiState.update { state ->
                            state.copy(
                                posts = feed.items.toPersistentList(),
                                friends = friendsList,
                                pendingRequests = pendingReceivedList,
                                sentRequests = pendingSentList,
                                suggestions = suggestions.toPersistentList(),
                                feedNextCursor = feed.nextCursor,
                                isLoadingInitial = false,
                                newPostsCount = newPostsCount,
                                showNewPostsAlert = newPostsCount > 0,
                                errorMessage = null
                            )
                        }

                        if (_uiState.value.isLoadingInitial) {
                            lastSeenPostTimestamp =
                                feed.items.firstOrNull()?.timestamp ?: System.currentTimeMillis()
                        }
                    }
            } catch (t: Throwable) {
                Log.e(TAG, "❌ Fatal error in observations", t)
                handleError(t, stopInitial = true, stopRefresh = false)
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 💬 GESTION DES COMMENTAIRES À LA DEMANDE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Toggle l'affichage des commentaires pour un post.
     * Si les commentaires ne sont pas chargés, les charge depuis Firestore.
     * Si déjà chargés, les masque simplement.
     */
    private fun toggleComments(postId: String) {
        val currentState = _uiState.value
        val isCurrentlyExpanded = currentState.expandedPostId == postId
        val hasComments = currentState.activeComments.containsKey(postId)

        if (isCurrentlyExpanded) {
            // 🔽 Fermer les commentaires (les garder en cache)
            Log.d(TAG, "🔽 Collapsing comments for post $postId")
            _uiState.update { it.copy(expandedPostId = null) }
        } else {
            // 🔼 Ouvrir les commentaires
            Log.d(TAG, "🔼 Expanding comments for post $postId")
            _uiState.update { it.copy(expandedPostId = postId) }

            // Charger les commentaires si pas déjà en cache
            if (!hasComments) {
                loadComments(postId)
            }
        }
    }

    /**
     * Charge les commentaires d'un post depuis Firestore.
     * Évite les appels multiples grâce au tracking des jobs.
     */
    private fun loadComments(postId: String) {
        // Éviter les appels multiples pour le même post
        if (commentLoadingJobs.containsKey(postId)) {
            Log.d(TAG, "⏭️ Already loading comments for post $postId, skipping")
            return
        }

        // Ne pas recharger si déjà en cache
        if (_uiState.value.activeComments.containsKey(postId)) {
            Log.d(TAG, "📦 Comments already cached for post $postId")
            return
        }

        Log.d(TAG, "💬 Loading comments for post $postId")

        // Marquer comme en cours de chargement
        _uiState.update { state ->
            state.copy(
                isLoadingComments = state.isLoadingComments + (postId to true)
            )
        }

        commentLoadingJobs[postId] = viewModelScope.launch {
            try {
                val comments = feedUseCases.fetchComments(postId, COMMENTS_PAGE_SIZE)
                Log.d(TAG, "✅ Loaded ${comments.size} comments for post $postId")

                _uiState.update { state ->
                    state.copy(
                        activeComments = state.activeComments + (postId to comments),
                        isLoadingComments = state.isLoadingComments - postId
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to load comments for post $postId", e)
                _uiState.update { state ->
                    state.copy(
                        isLoadingComments = state.isLoadingComments - postId
                    )
                }
                // On n'affiche pas d'erreur globale pour les commentaires
            } finally {
                commentLoadingJobs.remove(postId)
            }
        }
    }

    /**
     * Ajoute un commentaire optimiste puis synchronise avec le serveur.
     */
    private fun addComment(postId: String, content: String) {
        if (content.isBlank()) {
            Log.w(TAG, "⚠️ Empty comment, skipping")
            return
        }

        val actionKey = "comment_$postId"
        pendingActions[actionKey]?.cancel()

        Log.d(TAG, "💬 Adding comment to post $postId")

        pendingActions[actionKey] = viewModelScope.launch {
            try {
                // ✅ Appel serveur
                val newComment = feedUseCases.addComment(postId, content)
                Log.d(TAG, "✅ Comment added successfully: ${newComment.id}")

                // ✅ Mettre à jour le cache local des commentaires
                _uiState.update { state ->
                    val currentComments = state.activeComments[postId] ?: emptyList()
                    val updatedComments = currentComments + newComment

                    state.copy(
                        activeComments = state.activeComments + (postId to updatedComments),
                        actionStatus = state.actionStatus - actionKey
                    )
                }

                // ✅ Rafraîchir le post pour mettre à jour le compteur de commentaires
                refreshPostInList(postId)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to add comment", e)
                _uiState.update { state ->
                    state.copy(actionStatus = state.actionStatus - actionKey)
                }
                handleError(e, stopInitial = false, stopRefresh = false)
            } finally {
                pendingActions.remove(actionKey)
            }
        }
    }

    /**
     * Supprime un commentaire du post.
     */
    private fun deleteComment(postId: String, commentId: String) {
        val actionKey = "delete_comment_${postId}_$commentId"
        pendingActions[actionKey]?.cancel()

        Log.d(TAG, "🗑️ Deleting comment $commentId from post $postId")

        // ✅ Optimistic update: retirer le commentaire immédiatement
        _uiState.update { state ->
            val currentComments = state.activeComments[postId] ?: emptyList()
            val updatedComments = currentComments.filter { it.id != commentId }

            state.copy(
                activeComments = state.activeComments + (postId to updatedComments),
                actionStatus = state.actionStatus + (actionKey to ActionStatus.Processing)
            )
        }

        pendingActions[actionKey] = viewModelScope.launch {
            try {
                feedUseCases.deleteComment(postId, commentId)
                Log.d(TAG, "✅ Comment deleted successfully")

                _uiState.update { state ->
                    state.copy(actionStatus = state.actionStatus - actionKey)
                }

                // ✅ Rafraîchir le post pour mettre à jour le compteur
                refreshPostInList(postId)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to delete comment", e)

                // ❌ Rollback: remettre le commentaire
                _uiState.update { state ->
                    // On ne peut pas facilement le rollback sans re-fetch,
                    // donc on invalide le cache et force un reload
                    state.copy(
                        activeComments = state.activeComments - postId,
                        actionStatus = state.actionStatus - actionKey
                    )
                }

                handleError(e, stopInitial = false, stopRefresh = false)
            } finally {
                pendingActions.remove(actionKey)
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🔄 REFRESH INDIVIDUEL D'UN POST
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Rafraîchit les données d'un post spécifique dans la liste
     * (utile après ajout/suppression de commentaire pour mettre à jour le compteur).
     */
    private fun refreshPostInList(postId: String) {
        viewModelScope.launch {
            try {
                val refreshedPost = feedUseCases.refreshPost(postId)
                if (refreshedPost != null) {
                    _uiState.update { state ->
                        state.copy(
                            posts = state.posts.updatePost(postId) { refreshedPost }
                        )
                    }
                    Log.d(TAG, "✅ Post $postId refreshed in list")
                }
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Failed to refresh post $postId in list", e)
                // Non-bloquant, on ne remonte pas l'erreur
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // AUTRES ACTIONS (INCHANGÉ)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private fun togglePostLike(postId: String) {
        val actionKey = "like_$postId"
        pendingActions[actionKey]?.cancel()

        val currentPost = _uiState.value.posts.find { it.id == postId } ?: return
        val wasLiked = currentPost.isLiked
        val newLikesCount = if (wasLiked) currentPost.likesCount - 1 else currentPost.likesCount + 1

        _uiState.update { state ->
            state.copy(
                posts = state.posts.updatePost(postId) { post ->
                    post.copy(isLiked = !wasLiked, likesCount = newLikesCount)
                },
                actionStatus = state.actionStatus + (actionKey to ActionStatus.Processing)
            )
        }

        pendingActions[actionKey] = viewModelScope.launch {
            delay(300)
            try {
                feedUseCases.toggleLike(postId)
                _uiState.update { state ->
                    state.copy(actionStatus = state.actionStatus - actionKey)
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        posts = state.posts.updatePost(postId) { post ->
                            post.copy(isLiked = wasLiked, likesCount = currentPost.likesCount)
                        },
                        actionStatus = state.actionStatus - actionKey
                    )
                }
            } finally {
                pendingActions.remove(actionKey)
            }
        }
    }

    private fun deletePost(postId: String) {
        val actionKey = "delete_post_$postId"
        pendingActions[actionKey]?.cancel()

        _uiState.update { state ->
            state.copy(
                posts = state.posts.filter { it.id != postId }.toPersistentList(),
                actionStatus = state.actionStatus + (actionKey to ActionStatus.Processing)
            )
        }

        pendingActions[actionKey] = viewModelScope.launch {
            try {
                feedUseCases.deletePost(postId)
                _uiState.update { state ->
                    state.copy(actionStatus = state.actionStatus - actionKey)
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(actionStatus = state.actionStatus - actionKey)
                }
                handleError(e, stopInitial = false, stopRefresh = false)
            } finally {
                pendingActions.remove(actionKey)
            }
        }
    }

    private fun removeFriend(friend: PublicProfile) {
        val actionKey = "remove_friend_${friend.id}"
        pendingActions[actionKey]?.cancel()

        _uiState.update { state ->
            state.copy(
                friends = state.friends.filter { it.id != friend.id }.toPersistentList(),
                actionStatus = state.actionStatus + (actionKey to ActionStatus.Processing)
            )
        }

        pendingActions[actionKey] = viewModelScope.launch {
            delay(300)
            when (val result = removeFriendUseCase(friend.id)) {
                is Result.Ok -> {
                    _uiState.update { state ->
                        state.copy(actionStatus = state.actionStatus - actionKey)
                    }
                }

                is Result.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            friends = (state.friends + friend).sortedBy { it.username }
                                .toPersistentList(),
                            actionStatus = state.actionStatus - actionKey
                        )
                    }
                    handleError(result.throwable, stopInitial = false, stopRefresh = false)
                }
            }
            pendingActions.remove(actionKey)
        }
    }

    private fun addSuggestion(suggestion: FriendSuggestion) {
        val actionKey = "add_suggestion_${suggestion.id}"
        pendingActions[actionKey]?.cancel()

        _uiState.update { state ->
            state.copy(
                suggestions = state.suggestions.filter { it.id != suggestion.id }
                    .toPersistentList(),
                actionStatus = state.actionStatus + (actionKey to ActionStatus.Processing)
            )
        }

        pendingActions[actionKey] = viewModelScope.launch {
            delay(300)
            when (val result = sendFriendRequestUseCase(suggestion.id)) {
                is Result.Ok -> {
                    _uiState.update { state ->
                        state.copy(actionStatus = state.actionStatus - actionKey)
                    }
                }

                is Result.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            suggestions = (state.suggestions + suggestion).sortedBy { it.username }
                                .toPersistentList(),
                            actionStatus = state.actionStatus - actionKey
                        )
                    }
                    handleError(result.throwable, stopInitial = false, stopRefresh = false)
                }
            }
            pendingActions.remove(actionKey)
        }
    }

    private fun hideSuggestion(suggestion: FriendSuggestion) {
        val actionKey = "hide_suggestion_${suggestion.id}"
        pendingActions[actionKey]?.cancel()

        _uiState.update { state ->
            state.copy(
                suggestions = state.suggestions.filter { it.id != suggestion.id }
                    .toPersistentList(),
                actionStatus = state.actionStatus + (actionKey to ActionStatus.Processing)
            )
        }

        pendingActions[actionKey] = viewModelScope.launch {
            delay(300)
            when (val result = hideSuggestionUseCase(suggestion.id)) {
                is Result.Ok -> {
                    _uiState.update { state ->
                        state.copy(actionStatus = state.actionStatus - actionKey)
                    }
                }

                is Result.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            suggestions = (state.suggestions + suggestion).sortedBy { it.username }
                                .toPersistentList(),
                            actionStatus = state.actionStatus - actionKey
                        )
                    }
                    handleError(result.throwable, stopInitial = false, stopRefresh = false)
                }
            }
            pendingActions.remove(actionKey)
        }
    }

    private fun acceptFriendRequest(request: FriendRequest) {
        val actionKey = "accept_request_${request.userId}"
        pendingActions[actionKey]?.cancel()

        _uiState.update { state ->
            state.copy(
                pendingRequests = state.pendingRequests.filter { it.userId != request.userId }
                    .toPersistentList(),
                actionStatus = state.actionStatus + (actionKey to ActionStatus.Processing)
            )
        }

        pendingActions[actionKey] = viewModelScope.launch {
            delay(300)
            when (val result = acceptFriendRequestUseCase(request.userId)) {
                is Result.Ok -> {
                    _uiState.update { state ->
                        state.copy(actionStatus = state.actionStatus - actionKey)
                    }
                }

                is Result.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            pendingRequests = (state.pendingRequests + request).sortedBy { it.username }
                                .toPersistentList(),
                            actionStatus = state.actionStatus - actionKey
                        )
                    }
                    handleError(result.throwable, stopInitial = false, stopRefresh = false)
                }
            }
            pendingActions.remove(actionKey)
        }
    }

    private fun acceptFriendRequestFromProfile(userId: String) {
        val actionKey = "accept_request_from_profile_$userId"
        pendingActions[actionKey]?.cancel()

        pendingActions[actionKey] = viewModelScope.launch {
            when (val result = acceptFriendRequestUseCase(userId)) {
                is Result.Ok -> Unit
                is Result.Err -> handleError(
                    result.throwable,
                    stopInitial = false,
                    stopRefresh = false
                )
            }
            pendingActions.remove(actionKey)
        }
    }

    private fun removeFriendFromProfile(userId: String) {
        val actionKey = "remove_friend_from_profile_$userId"
        pendingActions[actionKey]?.cancel()

        pendingActions[actionKey] = viewModelScope.launch {
            when (val result = removeFriendUseCase(userId)) {
                is Result.Ok -> Unit
                is Result.Err -> handleError(
                    result.throwable,
                    stopInitial = false,
                    stopRefresh = false
                )
            }
            pendingActions.remove(actionKey)
        }
    }

    private fun declineFriendRequest(request: FriendRequest) {
        val actionKey = "decline_request_${request.userId}"
        pendingActions[actionKey]?.cancel()

        _uiState.update { state ->
            state.copy(
                pendingRequests = state.pendingRequests.filter { it.userId != request.userId }
                    .toPersistentList(),
                actionStatus = state.actionStatus + (actionKey to ActionStatus.Processing)
            )
        }

        pendingActions[actionKey] = viewModelScope.launch {
            delay(300)
            when (val result = declineFriendRequestUseCase(request.userId)) {
                is Result.Ok -> {
                    _uiState.update { state ->
                        state.copy(actionStatus = state.actionStatus - actionKey)
                    }
                }

                is Result.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            pendingRequests = (state.pendingRequests + request).sortedBy { it.username }
                                .toPersistentList(),
                            actionStatus = state.actionStatus - actionKey
                        )
                    }
                    handleError(result.throwable, stopInitial = false, stopRefresh = false)
                }
            }
            pendingActions.remove(actionKey)
        }
    }

    private fun cancelFriendRequest(request: FriendRequest) {
        val actionKey = "cancel_request_${request.userId}"
        pendingActions[actionKey]?.cancel()

        _uiState.update { state ->
            state.copy(
                sentRequests = state.sentRequests.filter { it.userId != request.userId }
                    .toPersistentList(),
                actionStatus = state.actionStatus + (actionKey to ActionStatus.Processing)
            )
        }

        pendingActions[actionKey] = viewModelScope.launch {
            delay(300)
            when (val result = cancelSentRequestUseCase(request.userId)) {
                is Result.Ok -> {
                    _uiState.update { state ->
                        state.copy(actionStatus = state.actionStatus - actionKey)
                    }
                }

                is Result.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            sentRequests = (state.sentRequests + request).sortedBy { it.username }
                                .toPersistentList(),
                            actionStatus = state.actionStatus - actionKey
                        )
                    }
                    handleError(result.throwable, stopInitial = false, stopRefresh = false)
                }
            }
            pendingActions.remove(actionKey)
        }
    }

    private fun handleSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
        }
        searchQuery.value = query
    }

    private fun addUserFromSearch(userId: String) {
        val actionKey = "search_add_$userId"
        pendingActions[actionKey]?.cancel()

        updateSearchResultStatus(userId, RelationshipStatus.PENDING_SENT)

        pendingActions[actionKey] = viewModelScope.launch {
            delay(300)
            when (val result = sendFriendRequestUseCase(userId)) {
                is Result.Ok -> {
                    _uiState.update { state ->
                        state.copy(actionStatus = state.actionStatus - actionKey)
                    }
                }

                is Result.Err -> {
                    updateSearchResultStatus(userId, RelationshipStatus.NONE)
                    _uiState.update { state ->
                        state.copy(actionStatus = state.actionStatus - actionKey)
                    }
                    handleError(result.throwable, stopInitial = false, stopRefresh = false)
                }
            }
            pendingActions.remove(actionKey)
        }
    }

    private fun observeSearch() {
        searchJob?.cancel()
        searchJob = searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isBlank()) {
                    _uiState.update {
                        it.copy(
                            searchResults = emptyList(),
                            isSearching = false
                        )
                    }
                }
            }
            .filter { it.isNotBlank() }
            .mapLatest { query ->
                _uiState.update { it.copy(isSearching = true) }
                searchUsersUseCase(query)
            }
            .onEach { result ->
                when (result) {
                    is Result.Ok -> _uiState.update {
                        it.copy(searchResults = result.data, isSearching = false)
                    }

                    is Result.Err -> {
                        _uiState.update { it.copy(isSearching = false) }
                        handleError(
                            result.throwable,
                            stopInitial = false,
                            stopRefresh = false
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadMorePosts() {
        val cursor = _uiState.value.feedNextCursor
        if (cursor == null) {
            Log.d(TAG, "⚠️ No cursor available, can't load more")
            return
        }
        if (isLoadingMoreFeed) {
            Log.d(TAG, "⚠️ Already loading more posts, skipping")
            return
        }

        Log.d(TAG, "📄 Loading more posts with cursor: $cursor")
        isLoadingMoreFeed = true
        _uiState.update { it.copy(isLoadingMorePosts = true) }

        viewModelScope.launch {
            try {
                val page = feedUseCases.observeFriendsFeed(FEED_PAGE_SIZE, cursor).first()
                Log.d(TAG, "✅ Loaded ${page.items.size} more posts")

                _uiState.update { state ->
                    val currentPosts = state.posts.toList()
                    val newPosts = page.items
                    val allPosts = (currentPosts + newPosts)
                        .distinctBy { it.id }
                        .sortedByDescending { it.timestamp }
                        .toPersistentList()

                    state.copy(
                        posts = allPosts,
                        feedNextCursor = page.nextCursor,
                        isLoadingMorePosts = false
                    )
                }
            } catch (throwable: Throwable) {
                Log.e(TAG, "❌ Failed to load more posts", throwable)
                _uiState.update { it.copy(isLoadingMorePosts = false) }
                handleError(throwable, stopInitial = false, stopRefresh = false)
            } finally {
                isLoadingMoreFeed = false
            }
        }
    }

    private fun updateSearchResultStatus(userId: String, status: RelationshipStatus) {
        _uiState.update { state ->
            state.copy(
                searchResults = state.searchResults.map { result ->
                    if (result.id == userId) result.copy(relationshipStatus = status) else result
                }
            )
        }
    }

    private fun handleError(
        throwable: Throwable?,
        stopInitial: Boolean = true,
        stopRefresh: Boolean = true
    ) {
        Log.e(TAG, "handleError: ", throwable)
        val message = mapErrorMessage(throwable)
        _uiState.update { state ->
            state.copy(
                isLoadingInitial = if (stopInitial) false else state.isLoadingInitial,
                isRefreshing = if (stopRefresh) false else state.isRefreshing,
                errorMessage = message
            )
        }
    }

    private fun mapErrorMessage(throwable: Throwable?): String {
        return "Une erreur est survenue. Vérifiez votre connexion et réessayez."
    }

    private fun cancelObservations() {
        Log.d(TAG, "🔴 Canceling observations")
        observationJob?.cancel()
        observationJob = null
        isLoadingMoreFeed = false
        _uiState.update { it.copy(isLoadingMorePosts = false) }
    }

    private fun PersistentList<Post>.updatePost(
        postId: String,
        transform: (Post) -> Post
    ): PersistentList<Post> =
        map { post -> if (post.id == postId) transform(post) else post }.toPersistentList()

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🔴 ViewModel cleared, cleaning up...")

        cancelObservations()

        cacheCleanupJob?.cancel()
        cacheCleanupJob = null

        searchJob?.cancel()
        searchJob = null

        refreshJob?.cancel()
        refreshJob = null

        // ✅ Annuler tous les jobs de chargement de commentaires
        commentLoadingJobs.values.forEach { it.cancel() }
        commentLoadingJobs.clear()

        pendingActions.values.forEach { it.cancel() }
        pendingActions.clear()

        logCacheStats()

        Log.d(TAG, "✅ ViewModel cleanup completed")
    }
}