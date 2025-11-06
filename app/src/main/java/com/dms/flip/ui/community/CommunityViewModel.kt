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
import com.dms.flip.domain.usecase.community.AddCommentUseCase
import com.dms.flip.domain.usecase.community.CancelSentRequestUseCase
import com.dms.flip.domain.usecase.community.DeclineFriendRequestUseCase
import com.dms.flip.domain.usecase.community.DeleteCommentUseCase
import com.dms.flip.domain.usecase.community.DeletePostUseCase
import com.dms.flip.domain.usecase.community.HideSuggestionUseCase
import com.dms.flip.domain.usecase.community.ObserveFriendsFeedUseCase
import com.dms.flip.domain.usecase.community.ObserveFriendsUseCase
import com.dms.flip.domain.usecase.community.ObservePendingReceivedUseCase
import com.dms.flip.domain.usecase.community.ObservePendingSentUseCase
import com.dms.flip.domain.usecase.community.ObserveSuggestionsUseCase
import com.dms.flip.domain.usecase.community.RemoveFriendUseCase
import com.dms.flip.domain.usecase.community.SearchUsersUseCase
import com.dms.flip.domain.usecase.community.SendFriendRequestUseCase
import com.dms.flip.domain.usecase.community.ToggleLikeUseCase
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
    private val observeFriendsFeedUseCase: ObserveFriendsFeedUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val deletePostUseCase: DeletePostUseCase,
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
                    val page = observeFriendsFeedUseCase(FEED_PAGE_SIZE).first()
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
                    newPostsCount = 0
                )
            }
            lastSeenPostTimestamp =
                _uiState.value.posts.firstOrNull()?.timestamp ?: System.currentTimeMillis()
        }
    }

    fun onScrollStateChanged(isAtTop: Boolean) {
        val wasAtTop = isScrolledToTop
        isScrolledToTop = isAtTop

        if (isAtTop && !wasAtTop && _uiState.value.showNewPostsAlert) {
            handleScrolledToTop()
        }
    }

    private fun ensureObservationsStarted() {
        if (observationJob != null) {
            Log.d(TAG, "⚠️ Observations already started, skipping")
            return
        }

        Log.d(TAG, "🔵 Starting observations...")

        _uiState.update { it.copy(isLoadingInitial = true) }

        val feedFlow = observeFriendsFeedUseCase(FEED_PAGE_SIZE)
            .catch { throwable ->
                Log.e(TAG, "❌ Error in feed flow", throwable)
                handleError(throwable)
                emit(Paged(emptyList(), null))
            }

        val friendsFlow = observeFriendsUseCase()
            .catch { throwable ->
                Log.e(TAG, "❌ Error in friends flow", throwable)
                handleError(throwable)
                emit(emptyList())
            }

        val pendingReceivedFlow = observePendingReceivedUseCase()
            .catch { throwable ->
                Log.e(TAG, "❌ Error in pending received flow", throwable)
                handleError(throwable)
                emit(emptyList())
            }

        val pendingSentFlow = observePendingSentUseCase()
            .catch { throwable ->
                Log.e(TAG, "❌ Error in pending sent flow", throwable)
                handleError(throwable)
                emit(emptyList())
            }

        val suggestionsFlow = observeSuggestionsUseCase()
            .catch { throwable ->
                Log.e(TAG, "❌ Error in suggestions flow", throwable)
                handleError(throwable)
                emit(emptyList())
            }

        var hasReceivedData = false

        viewModelScope.launch {
            delay(5000)
            if (!hasReceivedData) {
                Log.w(TAG, "⚠️ No data received after 3s, stopping loading state")
                _uiState.update { it.copy(isLoadingInitial = false) }
            }
        }

        observationJob = combine(
            feedFlow, friendsFlow, pendingReceivedFlow, pendingSentFlow, suggestionsFlow
        ) { feed, friends, received, sent, suggestions ->
            CommunityData(
                feed = feed,
                friends = friends,
                pendingReceived = received,
                pendingSent = sent,
                suggestions = suggestions
            )
        }
            .onEach { data ->
                hasReceivedData = true
                Log.d(
                    TAG,
                    "📥 Received update: ${data.feed.items.size} posts, ${data.friends.size} friends"
                )

                val currentPosts = _uiState.value.posts
                val newPosts = data.feed.items

                val hasNewPosts = if (currentPosts.isNotEmpty() && newPosts.isNotEmpty()) {
                    val newestCurrentTimestamp = currentPosts.firstOrNull()?.timestamp ?: 0
                    newPosts.any { it.timestamp > newestCurrentTimestamp }
                } else {
                    false
                }

                if (hasNewPosts && !isScrolledToTop && !_uiState.value.isRefreshing) {
                    val newPostCount = newPosts.count { it.timestamp > lastSeenPostTimestamp }
                    if (newPostCount > 0) {
                        Log.d(TAG, "🆕 $newPostCount new posts detected")
                        _uiState.update { state ->
                            state.copy(
                                showNewPostsAlert = true,
                                newPostsCount = newPostCount
                            )
                        }
                    }
                }

                _uiState.update { state ->
                    state.copy(
                        posts = data.feed.items.toPersistentList(),
                        feedNextCursor = data.feed.nextCursor,
                        friends = data.friends.toPersistentList(),
                        pendingRequests = data.pendingReceived.toPersistentList(),
                        sentRequests = data.pendingSent.toPersistentList(),
                        suggestions = data.suggestions.toPersistentList(),
                        isLoadingInitial = false
                    )
                }
            }
            .launchIn(viewModelScope)

        Log.d(TAG, "✅ Observations started successfully")
    }

    private fun loadInitial(force: Boolean = false) {
        if (force) {
            Log.d(TAG, "♻️ Force loading initial data")
            cancelObservations()
        }
        if (!force && observationJob != null) {
            Log.d(TAG, "⚠️ Initial data already loading, skipping")
            return
        }

        Log.d(TAG, "🔵 Loading initial data...")
        _uiState.update { it.copy(isLoadingInitial = true, errorMessage = null) }
        lastSeenPostTimestamp = System.currentTimeMillis()
        ensureObservationsStarted()
    }

    private fun togglePostLike(postId: String) {
        val currentPost = _uiState.value.posts.find { it.id == postId } ?: return
        val wasLiked = currentPost.isLiked

        Log.d(TAG, "❤️ Toggling like for post $postId (currently: $wasLiked)")

        _uiState.update { state ->
            state.copy(
                posts = state.posts.updatePost(postId) { post ->
                    post.copy(
                        isLiked = !wasLiked,
                        likesCount = if (wasLiked) post.likesCount - 1 else post.likesCount + 1
                    )
                }
            )
        }

        viewModelScope.launch {
            when (val result = toggleLikeUseCase(postId)) {
                is Result.Err -> {
                    Log.e(TAG, "❌ Failed to toggle like", result.throwable)
                    _uiState.update { state ->
                        state.copy(
                            posts = state.posts.updatePost(postId) { post ->
                                post.copy(
                                    isLiked = wasLiked,
                                    likesCount = if (wasLiked) post.likesCount + 1 else post.likesCount - 1
                                )
                            }
                        )
                    }
                    handleError(result.throwable, stopInitial = false, stopRefresh = false)
                }

                is Result.Ok -> {
                    Log.d(TAG, "✅ Like toggled successfully, waiting for real-time confirmation")
                }
            }
        }
    }

    private fun toggleComments(postId: String) {
        _uiState.update { state ->
            state.copy(expandedPostId = if (state.expandedPostId == postId) null else postId)
        }
    }

    private fun addComment(postId: String, comment: String) {
        Log.d(TAG, "💬 Adding comment to post $postId")

        viewModelScope.launch {
            when (val result = addCommentUseCase(postId, comment)) {
                is Result.Err -> {
                    Log.e(TAG, "❌ Failed to add comment", result.throwable)
                    handleError(result.throwable, stopInitial = false, stopRefresh = false)
                }

                is Result.Ok -> {
                    Log.d(TAG, "✅ Comment added successfully: ${result.data.id}")
                }
            }
        }
    }

    private fun deleteComment(postId: String, commentId: String) {
        Log.d(TAG, "🗑️ Deleting comment $commentId from post $postId")

        viewModelScope.launch {
            when (val result = deleteCommentUseCase(postId, commentId)) {
                is Result.Ok -> {
                    Log.d(TAG, "✅ Comment deleted successfully")
                }

                is Result.Err -> {
                    Log.e(TAG, "❌ Failed to delete comment", result.throwable)
                    handleError(result.throwable, stopInitial = false, stopRefresh = false)
                }
            }
        }
    }

    private fun deletePost(postId: String) {
        Log.d(TAG, "🗑️ Deleting post $postId")

        val previousPosts = _uiState.value.posts
        val previousExpanded = _uiState.value.expandedPostId

        _uiState.update { state ->
            state.copy(
                posts = state.posts.filterNot { it.id == postId }.toPersistentList(),
                expandedPostId = state.expandedPostId.takeUnless { it == postId }
            )
        }

        viewModelScope.launch {
            when (val result = deletePostUseCase(postId)) {
                is Result.Err -> {
                    Log.e(TAG, "❌ Failed to delete post", result.throwable)
                    _uiState.update { state ->
                        state.copy(
                            posts = previousPosts,
                            expandedPostId = previousExpanded
                        )
                    }
                    handleError(result.throwable, stopInitial = false, stopRefresh = false)
                }

                is Result.Ok -> {
                    Log.d(TAG, "✅ Post deleted successfully")
                }
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

    private fun removeFriendFromProfile(userId: String) {
        val friend = _uiState.value.friends.firstOrNull { it.id == userId } ?: return
        removeFriend(friend)
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
            when (val sendResult = sendFriendRequestUseCase(suggestion.id)) {
                is Result.Ok -> {
                    when (hideSuggestionUseCase(suggestion.id)) {
                        is Result.Err -> {
                            _uiState.update { state ->
                                state.copy(actionStatus = state.actionStatus - actionKey)
                            }
                        }

                        else -> {
                            _uiState.update { state ->
                                state.copy(actionStatus = state.actionStatus - actionKey)
                            }
                        }
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
                    handleError(
                        sendResult.throwable,
                        stopInitial = false,
                        stopRefresh = false
                    )
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
        val actionKey = "accept_request_${request.id}"
        pendingActions[actionKey]?.cancel()

        _uiState.update { state ->
            state.copy(
                pendingRequests = state.pendingRequests.filter { it.id != request.id }
                    .toPersistentList(),
                actionStatus = state.actionStatus + (actionKey to ActionStatus.Processing)
            )
        }

        pendingActions[actionKey] = viewModelScope.launch {
            delay(300)
            when (val result = acceptFriendRequestUseCase(request.id)) {
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
        val request = _uiState.value.pendingRequests.firstOrNull { it.userId == userId }
            ?: return
        acceptFriendRequest(request)
    }

    private fun declineFriendRequest(request: FriendRequest) {
        val actionKey = "decline_request_${request.id}"
        pendingActions[actionKey]?.cancel()

        _uiState.update { state ->
            state.copy(
                pendingRequests = state.pendingRequests.filter { it.id != request.id }
                    .toPersistentList(),
                actionStatus = state.actionStatus + (actionKey to ActionStatus.Processing)
            )
        }

        pendingActions[actionKey] = viewModelScope.launch {
            delay(300)
            when (val result = declineFriendRequestUseCase(request.id)) {
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
        val actionKey = "cancel_request_${request.id}"
        pendingActions[actionKey]?.cancel()

        _uiState.update { state ->
            state.copy(
                sentRequests = state.sentRequests.filter { it.id != request.id }
                    .toPersistentList(),
                actionStatus = state.actionStatus + (actionKey to ActionStatus.Processing)
            )
        }

        pendingActions[actionKey] = viewModelScope.launch {
            delay(300)
            when (val result = cancelSentRequestUseCase(request.id)) {
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
                val page = observeFriendsFeedUseCase(FEED_PAGE_SIZE, cursor).first()
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

        pendingActions.values.forEach { it.cancel() }
        pendingActions.clear()

        logCacheStats()

        Log.d(TAG, "✅ ViewModel cleanup completed")
    }
}