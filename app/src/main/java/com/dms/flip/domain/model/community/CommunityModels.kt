package com.dms.flip.domain.model.community

import com.dms.flip.data.model.PleasureCategory

data class Post(
    val id: String,
    val friend: Friend,
    val photoUrl: String? = null,
    val photoUrlThumb: String? = null,
    val content: String,
    val timestamp: Long,
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean = false,
    val pleasureCategory: PleasureCategory? = null,
    val pleasureTitle: String? = null,
    val comments: List<PostComment> = emptyList()
)

data class PostComment(
    val id: String,
    val userId: String,
    val username: String,
    val userHandle: String,
    val avatarUrl: String? = null,
    val content: String,
    val timestamp: Long
)

data class Friend(
    val id: String,
    val username: String,
    val handle: String,
    val avatarUrl: String? = null,
    val streak: Int = 0,
    val isOnline: Boolean = false,
    val currentPleasure: FriendPleasure? = null,
    val favoriteCategory: PleasureCategory? = null
)

data class FriendPleasure(
    val title: String,
    val category: PleasureCategory,
    val status: PleasureStatus
)

enum class PleasureStatus {
    IN_PROGRESS,
    COMPLETED
}

data class FriendSuggestion(
    val id: String,
    val username: String,
    val handle: String,
    val avatarUrl: String? = null,
    val mutualFriendsCount: Int = 0
)

data class FriendRequest(
    val id: String,
    val userId: String,
    val username: String,
    val handle: String,
    val avatarUrl: String? = null,
    val requestedAt: Long,
    val source: FriendRequestSource = FriendRequestSource.SEARCH
)

enum class FriendRequestSource {
    SEARCH,
    SUGGESTION
}

data class UserSearchResult(
    val id: String,
    val username: String,
    val handle: String,
    val avatarUrl: String? = null,
    val relationshipStatus: RelationshipStatus = RelationshipStatus.NONE
)

enum class RelationshipStatus {
    NONE,
    FRIEND,
    PENDING_SENT,
    PENDING_RECEIVED
}

data class PublicProfile(
    val id: String,
    val username: String,
    val handle: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val friendsCount: Int = 0,
    val daysCompleted: Int = 0,
    val currentStreak: Int = 0,
    val recentActivities: List<RecentActivity> = emptyList(),
    val relationshipStatus: RelationshipStatus = RelationshipStatus.NONE
)

data class RecentActivity(
    val id: String,
    val pleasureTitle: String,
    val category: PleasureCategory,
    val completedAt: Long,
    val isCompleted: Boolean
)
