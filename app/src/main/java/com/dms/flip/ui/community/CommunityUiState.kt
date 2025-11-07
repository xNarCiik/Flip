package com.dms.flip.ui.community

import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.model.community.FriendSuggestion
import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.model.community.UserSearchResult
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

sealed class ActionStatus {
    object Processing : ActionStatus()
}

data class CommunityUiState(
    val posts: PersistentList<Post> = persistentListOf(),
    val friends: PersistentList<PublicProfile> = persistentListOf(),
    val pendingRequests: PersistentList<FriendRequest> = persistentListOf(),
    val sentRequests: PersistentList<FriendRequest> = persistentListOf(),
    val suggestions: PersistentList<FriendSuggestion> = persistentListOf(),
    val searchResults: List<UserSearchResult> = emptyList(),
    val searchQuery: String = "",
    val expandedPostId: String? = null,
    val feedNextCursor: String? = null,
    val isLoadingInitial: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMorePosts: Boolean = false,
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
    val currentUserId: String? = null,
    val actionStatus: Map<String, ActionStatus> = emptyMap(),
    val newPostsCount: Int = 0,
    val showNewPostsAlert: Boolean = false,
    val scrollToTopTrigger: Int = 0,
    val activeComments: Map<String, List<PostComment>> = emptyMap(),
    val isLoadingComments: Map<String, Boolean> = emptyMap()
)


sealed interface CommunityEvent {
    data object OnSearchClicked : CommunityEvent
    data object OnInvitationsClicked : CommunityEvent
    data object OnFriendsListClicked : CommunityEvent

    data class OnPostLiked(val postId: String) : CommunityEvent
    data class OnAddComment(val postId: String, val comment: String) : CommunityEvent
    data class OnToggleComments(val postId: String) : CommunityEvent

    data class OnViewProfile(val profile: PublicProfile) : CommunityEvent
    data class OnInviteFriendToPleasure(val friend: PublicProfile) : CommunityEvent
    data class OnRemoveFriend(val friend: PublicProfile) : CommunityEvent

    data class OnAddSuggestion(val suggestion: FriendSuggestion) : CommunityEvent
    data class OnHideSuggestion(val suggestion: FriendSuggestion) : CommunityEvent

    data class OnAcceptFriendRequest(val request: FriendRequest) : CommunityEvent
    data class OnDeclineFriendRequest(val request: FriendRequest) : CommunityEvent
    data class OnCancelSentRequest(val request: FriendRequest) : CommunityEvent

    data class OnSearchQueryChanged(val query: String) : CommunityEvent
    data class OnSearchResultClicked(val result: UserSearchResult) : CommunityEvent
    data class OnAddUserFromSearch(val userId: String) : CommunityEvent

    data class OnDeleteComment(val postId: String, val commentId: String) : CommunityEvent
    data class OnDeletePost(val postId: String) : CommunityEvent

    data class OnAcceptFriendRequestFromProfile(val userId: String) : CommunityEvent
    data class OnRemoveFriendFromProfile(val userId: String) : CommunityEvent

    data object OnLoadMorePosts : CommunityEvent
    data object OnRetryClicked : CommunityEvent
    data class OnRefresh(val forceReload: Boolean = false) : CommunityEvent
    data object OnNewPostsAlertClicked : CommunityEvent
    data class OnFeedScrolled(val isAtTop: Boolean) : CommunityEvent
}
