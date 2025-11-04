package com.dms.flip.ui.community

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dms.flip.R
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.model.community.FriendRequestSource
import com.dms.flip.domain.model.community.FriendSuggestion
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.model.community.Paged
import com.dms.flip.domain.model.community.RelationshipStatus
import com.dms.flip.domain.util.Result
import com.dms.flip.domain.usecase.community.AcceptFriendRequestUseCase
import com.dms.flip.domain.usecase.community.AddCommentUseCase
import com.dms.flip.domain.usecase.community.CancelSentRequestUseCase
import com.dms.flip.domain.usecase.community.DeclineFriendRequestUseCase
import com.dms.flip.domain.usecase.community.GetPublicProfileUseCase
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
import com.dms.flip.domain.usecase.community.DeleteCommentUseCase
import com.dms.flip.domain.usecase.community.DeletePostUseCase
import com.dms.flip.domain.usecase.user.GetUserInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val FEED_PAGE_SIZE = 20

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val observeFriendsFeedUseCase: ObserveFriendsFeedUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val observeFriendsUseCase: ObserveFriendsUseCase,
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
    private val getPublicProfileUseCase: GetPublicProfileUseCase,
    getUserInfoUseCase: GetUserInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    private val _publicProfiles = MutableStateFlow<Map<String, PublicProfile>>(emptyMap())
    val publicProfiles: StateFlow<Map<String, PublicProfile>> = _publicProfiles.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private var observationJob: Job? = null
    private var searchJob: Job? = null
    private var isLoadingMoreFeed = false
    private val loadingProfiles = mutableSetOf<String>()

    init {
        observeSearch()
        refresh()
        viewModelScope.launch {
            getUserInfoUseCase().collect { userInfo ->
                _uiState.update { it.copy(currentUserId = userInfo?.id) }
            }
        }
    }

    fun refresh(force: Boolean = false) {
        _uiState.update { it.copy(isLoading = true, isLoadingMorePosts = false, error = null) }
        if (force) {
            restartObservations()
        } else {
            ensureObservationsStarted()
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
            is CommunityEvent.OnRetryClicked -> refresh(force = true)
            is CommunityEvent.OnDeleteComment -> deleteComment(event.postId, event.commentId)
            is CommunityEvent.OnDeletePost -> deletePost(event.postId)
            is CommunityEvent.OnAcceptFriendRequestFromProfile ->
                acceptFriendRequestFromProfile(event.userId)
            is CommunityEvent.OnRemoveFriendFromProfile -> removeFriendFromProfile(event.userId)
            else -> Unit
        }
    }

    fun loadPublicProfile(userId: String) {
        if (_publicProfiles.value.containsKey(userId)) return
        val shouldLoad = synchronized(loadingProfiles) {
            if (loadingProfiles.contains(userId)) {
                false
            } else {
                loadingProfiles.add(userId)
                true
            }
        }
        if (!shouldLoad) return
        viewModelScope.launch {
            try {
                when (val result = getPublicProfileUseCase(userId)) {
                    is Result.Ok -> {
                        _publicProfiles.update { current -> current + (userId to result.data) }
                    }
                    is Result.Err -> {
                        _uiState.update { it.copy(error = R.string.error_load_friends) }
                    }
                }
            } finally {
                synchronized(loadingProfiles) {
                    loadingProfiles.remove(userId)
                }
            }
        }
    }

    private fun ensureObservationsStarted() {
        if (observationJob != null) return

        val feedFlow = observeFriendsFeedUseCase(FEED_PAGE_SIZE)
            .catch { throwable ->
                handleError(throwable)
                val currentState = _uiState.value
                emit(Paged(currentState.friendsPosts, currentState.feedNextCursor))
            }

        val friendsFlow = observeFriendsUseCase()
            .catch { throwable ->
                handleError(throwable)
                emit(emptyList())
            }
            .onStart { emit(emptyList()) }

        val pendingReceivedFlow = observePendingReceivedUseCase()
            .catch { throwable ->
                handleError(throwable)
                emit(emptyList())
            }
            .onStart { emit(emptyList()) }

        val pendingSentFlow = observePendingSentUseCase()
            .catch { throwable ->
                handleError(throwable)
                emit(emptyList())
            }
            .onStart { emit(emptyList()) }

        val suggestionsFlow = observeSuggestionsUseCase()
            .catch { throwable ->
                handleError(throwable)
                emit(emptyList())
            }
            .onStart { emit(emptyList()) }

        observationJob = combine(
            feedFlow,
            friendsFlow,
            pendingReceivedFlow,
            pendingSentFlow,
            suggestionsFlow
        ) { feedPage, friends, pendingReceived, pendingSent, suggestions ->
            _uiState.update { state ->
                val mergedPosts = mergeFeedPosts(state.friendsPosts, feedPage.items)
                state.copy(
                    friendsPosts = mergedPosts,
                    feedNextCursor = feedPage.nextCursor,
                    friends = friends,
                    pendingRequests = pendingReceived,
                    sentRequests = pendingSent,
                    suggestions = suggestions,
                    isLoading = false,
                    error = null
                )
            }
        }
            .launchIn(viewModelScope)
            .also { job ->
                job.invokeOnCompletion {
                    if (observationJob == job) {
                        observationJob = null
                    }
                }
            }
    }

    private fun restartObservations() {
        cancelObservations()
        ensureObservationsStarted()
    }

    private fun mergeFeedPosts(
        existing: List<Post>,
        incoming: List<Post>
    ): List<Post> {
        if (incoming.isEmpty()) return emptyList()
        val incomingIds = incoming.map { it.id }.toSet()
        val preserved = existing.filterNot { it.id in incomingIds }
        return incoming + preserved
    }

    private fun togglePostLike(postId: String) {
        val post = _uiState.value.friendsPosts.find { it.id == postId } ?: return
        val like = !post.isLiked
        _uiState.update { state ->
            state.copy(
                friendsPosts = state.friendsPosts.updatePost(postId) {
                    it.copy(
                        isLiked = like,
                        likesCount = if (like) it.likesCount + 1 else (it.likesCount - 1).coerceAtLeast(0)
                    )
                }
            )
        }
        viewModelScope.launch {
            when (toggleLikeUseCase(postId, like)) {
                is Result.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            friendsPosts = state.friendsPosts.updatePost(postId) {
                                it.copy(
                                    isLiked = !like,
                                    likesCount = if (like) (it.likesCount - 1).coerceAtLeast(0) else it.likesCount + 1
                                )
                            },
                            error = R.string.error_load_friends
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    private fun toggleComments(postId: String) {
        _uiState.update { state ->
            state.copy(
                expandedPostId = if (state.expandedPostId == postId) null else postId
            )
        }
    }

    private fun addComment(postId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            when (val result = addCommentUseCase(postId, content)) {
                is Result.Ok -> {
                    val comment = result.data
                    _uiState.update { state ->
                        state.copy(
                            friendsPosts = state.friendsPosts.updatePost(postId) {
                                it.copy(
                                    comments = it.comments + comment,
                                    commentsCount = it.commentsCount + 1
                                )
                            }
                        )
                    }
                }
                is Result.Err -> handleError(result.throwable)
            }
        }
    }

    private fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            when (val result = deleteCommentUseCase(postId, commentId)) {
                is Result.Ok -> {
                    _uiState.update { state ->
                        state.copy(
                            friendsPosts = state.friendsPosts.updatePost(postId) { post ->
                                val updatedComments = post.comments.filterNot { it.id == commentId }
                                post.copy(
                                    comments = updatedComments,
                                    commentsCount = updatedComments.size
                                )
                            }
                        )
                    }
                }
                is Result.Err -> handleError(result.throwable)
            }
        }
    }

    private fun deletePost(postId: String) {
        val previousPosts = _uiState.value.friendsPosts
        val previousExpanded = _uiState.value.expandedPostId

        _uiState.update { state ->
            state.copy(
                friendsPosts = state.friendsPosts.filterNot { it.id == postId },
                expandedPostId = state.expandedPostId.takeUnless { it == postId }
            )
        }

        viewModelScope.launch {
            when (deletePostUseCase(postId)) {
                is Result.Err -> {
                    _uiState.update { state ->
                        state.copy(
                            friendsPosts = previousPosts,
                            expandedPostId = previousExpanded,
                            error = R.string.error_delete_post
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    private fun removeFriend(friend: Friend) {
        viewModelScope.launch {
            when (val result = removeFriendUseCase(friend.id)) {
                is Result.Err -> handleError(result.throwable)
                else -> Unit
            }
        }
    }

    private fun removeFriendFromProfile(userId: String) {
        val friend = _uiState.value.friends.firstOrNull { it.id == userId } ?: return
        removeFriend(friend)
    }

    private fun addSuggestion(suggestion: FriendSuggestion) {
        viewModelScope.launch {
            when (val result = sendFriendRequestUseCase(suggestion.id)) {
                is Result.Ok -> {
                    when (val hideResult = hideSuggestionUseCase(suggestion.id)) {
                        is Result.Err -> handleError(hideResult.throwable)
                        else -> Unit
                    }
                    _uiState.update { state ->
                        state.copy(
                            sentRequests = state.sentRequests + result.data.copy(source = FriendRequestSource.SUGGESTION)
                        )
                    }
                }
                is Result.Err -> handleError(result.throwable)
            }
        }
    }

    private fun hideSuggestion(suggestion: FriendSuggestion) {
        viewModelScope.launch {
            when (val result = hideSuggestionUseCase(suggestion.id)) {
                is Result.Err -> handleError(result.throwable)
                else -> Unit
            }
        }
    }

    private fun acceptFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            when (val result = acceptFriendRequestUseCase(request.id)) {
                is Result.Err -> handleError(result.throwable)
                else -> Unit
            }
        }
    }

    private fun acceptFriendRequestFromProfile(userId: String) {
        val request = _uiState.value.pendingRequests.firstOrNull { it.userId == userId }
            ?: return
        acceptFriendRequest(request)
    }

    private fun declineFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            when (val result = declineFriendRequestUseCase(request.id)) {
                is Result.Err -> handleError(result.throwable)
                else -> Unit
            }
        }
    }

    private fun cancelFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            when (val result = cancelSentRequestUseCase(request.id)) {
                is Result.Err -> handleError(result.throwable)
                else -> Unit
            }
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
        viewModelScope.launch {
            when (val result = sendFriendRequestUseCase(userId)) {
                is Result.Ok -> {
                    updateSearchResultStatus(userId, RelationshipStatus.PENDING_SENT)
                    _uiState.update { state ->
                        state.copy(sentRequests = state.sentRequests + result.data)
                    }
                }
                is Result.Err -> handleError(result.throwable)
            }
        }
    }

    private fun observeSearch() {
        searchJob?.cancel()
        searchJob = searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isBlank()) {
                    _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
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
                        handleError(result.throwable, shouldStopLoading = false)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadMorePosts() {
        val cursor = _uiState.value.feedNextCursor ?: return
        if (isLoadingMoreFeed) return

        isLoadingMoreFeed = true
        _uiState.update { it.copy(isLoadingMorePosts = true) }
        viewModelScope.launch {
            try {
                val page = observeFriendsFeedUseCase(FEED_PAGE_SIZE, cursor).first()
                _uiState.update { state ->
                    val updatedPosts = LinkedHashMap<String, Post>().apply {
                        state.friendsPosts.forEach { put(it.id, it) }
                        page.items.forEach { put(it.id, it) }
                    }.values.toList()
                    state.copy(
                        friendsPosts = updatedPosts,
                        feedNextCursor = page.nextCursor,
                        isLoadingMorePosts = false
                    )
                }
            } catch (throwable: Throwable) {
                _uiState.update { it.copy(isLoadingMorePosts = false) }
                handleError(throwable, shouldStopLoading = false)
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
        throwable: Throwable,
        @StringRes messageRes: Int = R.string.error_load_friends,
        shouldStopLoading: Boolean = true
    ) {
        // TODO TIMBER
        Log.e("CommunityViewModel", "handleError: ", throwable)
        _uiState.update { state ->
            state.copy(
                isLoading = if (shouldStopLoading) false else state.isLoading,
                error = messageRes
            )
        }
    }

    private fun cancelObservations() {
        observationJob?.cancel()
        observationJob = null
        isLoadingMoreFeed = false
        _uiState.update { it.copy(isLoadingMorePosts = false) }
    }

    private fun List<Post>.updatePost(
        postId: String,
        transform: (Post) -> Post
    ): List<Post> = map { post -> if (post.id == postId) transform(post) else post }

    override fun onCleared() {
        super.onCleared()
        cancelObservations()
        searchJob?.cancel()
        searchJob = null
    }
}
