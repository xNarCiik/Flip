package com.dms.flip.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dms.flip.R
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.FriendPost
import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.model.community.FriendRequestSource
import com.dms.flip.domain.model.community.FriendSuggestion
import com.dms.flip.domain.model.community.PublicProfile
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val FEED_PAGE_SIZE = 20

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val observeFriendsFeedUseCase: ObserveFriendsFeedUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val addCommentUseCase: AddCommentUseCase,
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
    private val getPublicProfileUseCase: GetPublicProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    private val _publicProfiles = MutableStateFlow<Map<String, PublicProfile>>(emptyMap())
    val publicProfiles: StateFlow<Map<String, PublicProfile>> = _publicProfiles.asStateFlow()

    private val observationJobs = mutableListOf<Job>()

    init {
        refresh()
    }

    fun refresh() {
        cancelObservations()
        _uiState.update { it.copy(isLoading = true, error = null) }
        observationJobs += launchFeedObservation()
        observationJobs += launchFriendsObservation()
        observationJobs += launchSuggestionsObservation()
        observationJobs += launchPendingReceivedObservation()
        observationJobs += launchPendingSentObservation()
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
            is CommunityEvent.OnRetryClicked -> refresh()
            else -> Unit
        }
    }

    fun loadPublicProfile(userId: String) {
        if (_publicProfiles.value.containsKey(userId)) return
        viewModelScope.launch {
            when (val result = getPublicProfileUseCase(userId)) {
                is Result.Ok -> {
                    _publicProfiles.update { current -> current + (userId to result.data) }
                }
                is Result.Err -> {
                    _uiState.update { it.copy(error = R.string.error_load_friends) }
                }
            }
        }
    }

    private fun launchFeedObservation(): Job = viewModelScope.launch {
        observeFriendsFeedUseCase(FEED_PAGE_SIZE)
            .catch { handleError(it) }
            .collect { page ->
                _uiState.update { state ->
                    state.copy(
                        friendsPosts = page.items,
                        isLoading = false
                    )
                }
            }
    }

    private fun launchFriendsObservation(): Job = viewModelScope.launch {
        observeFriendsUseCase()
            .catch { handleError(it) }
            .collect { friends ->
                _uiState.update { it.copy(friends = friends, isLoading = false) }
            }
    }

    private fun launchPendingReceivedObservation(): Job = viewModelScope.launch {
        observePendingReceivedUseCase()
            .catch { handleError(it) }
            .collect { requests ->
                _uiState.update { it.copy(pendingRequests = requests, isLoading = false) }
            }
    }

    private fun launchPendingSentObservation(): Job = viewModelScope.launch {
        observePendingSentUseCase()
            .catch { handleError(it) }
            .collect { requests ->
                _uiState.update { it.copy(sentRequests = requests, isLoading = false) }
            }
    }

    private fun launchSuggestionsObservation(): Job = viewModelScope.launch {
        observeSuggestionsUseCase()
            .catch { handleError(it) }
            .collect { suggestions ->
                _uiState.update { it.copy(suggestions = suggestions, isLoading = false) }
            }
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

    private fun removeFriend(friend: Friend) {
        viewModelScope.launch {
            when (val result = removeFriendUseCase(friend.id)) {
                is Result.Err -> handleError(result.throwable)
                else -> Unit
            }
        }
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
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            when (val result = searchUsersUseCase(query)) {
                is Result.Ok -> _uiState.update {
                    it.copy(searchResults = result.data, isSearching = false)
                }
                is Result.Err -> {
                    _uiState.update { it.copy(isSearching = false) }
                    handleError(result.throwable)
                }
            }
        }
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

    private fun updateSearchResultStatus(userId: String, status: RelationshipStatus) {
        _uiState.update { state ->
            state.copy(
                searchResults = state.searchResults.map { result ->
                    if (result.id == userId) result.copy(relationshipStatus = status) else result
                }
            )
        }
    }

    private fun handleError(throwable: Throwable) {
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                error = R.string.error_load_friends
            )
        }
    }

    private fun cancelObservations() {
        observationJobs.forEach { it.cancel() }
        observationJobs.clear()
    }

    private fun List<FriendPost>.updatePost(
        postId: String,
        transform: (FriendPost) -> FriendPost
    ): List<FriendPost> = map { post -> if (post.id == postId) transform(post) else post }

    override fun onCleared() {
        super.onCleared()
        cancelObservations()
    }
}
