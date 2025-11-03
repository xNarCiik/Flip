package com.dms.flip.ui.community

import androidx.annotation.StringRes
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.FriendPost
import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.model.community.FriendRequestSource
import com.dms.flip.domain.model.community.FriendSuggestion
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.domain.model.community.RelationshipStatus
import com.dms.flip.domain.model.community.UserSearchResult

data class CommunityUiState(
    val isLoading: Boolean = false,

    val friendsPosts: List<FriendPost> = emptyList(),
    val expandedPostId: String? = null,
    val currentUserId: String? = null,
    val friends: List<Friend> = emptyList(),
    val suggestions: List<FriendSuggestion> = emptyList(),
    val pendingRequests: List<FriendRequest> = emptyList(),
    val sentRequests: List<FriendRequest> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<UserSearchResult> = emptyList(),

    @StringRes val error: Int? = null
)

sealed interface CommunityEvent {
    data object OnSearchClicked : CommunityEvent
    data object OnInvitationsClicked : CommunityEvent
    data object OnFriendsListClicked : CommunityEvent

    data class OnPostLiked(val postId: String) : CommunityEvent
    data class OnAddComment(val postId: String, val comment: String) : CommunityEvent
    data class OnToggleComments(val postId: String) : CommunityEvent

    data class OnFriendClicked(val friend: Friend) : CommunityEvent
    data class OnInviteFriendToPleasure(val friend: Friend) : CommunityEvent
    data class OnRemoveFriend(val friend: Friend) : CommunityEvent

    data class OnAddSuggestion(val suggestion: FriendSuggestion) : CommunityEvent
    data class OnHideSuggestion(val suggestion: FriendSuggestion) : CommunityEvent

    data class OnAcceptFriendRequest(val request: FriendRequest) : CommunityEvent
    data class OnDeclineFriendRequest(val request: FriendRequest) : CommunityEvent
    data class OnCancelSentRequest(val request: FriendRequest) : CommunityEvent

    data class OnSearchQueryChanged(val query: String) : CommunityEvent
    data class OnSearchResultClicked(val result: UserSearchResult) : CommunityEvent
    data class OnAddUserFromSearch(val userId: String) : CommunityEvent

    data class OnViewProfile(val userId: String) : CommunityEvent

    data class OnDeleteComment(val postId: String, val commentId: String) : CommunityEvent
    data class OnDeletePost(val postId: String) : CommunityEvent

    data class OnAcceptFriendRequestFromProfile(val userId: String) : CommunityEvent
    data class OnRemoveFriendFromProfile(val userId: String) : CommunityEvent

    data object OnRetryClicked : CommunityEvent
}