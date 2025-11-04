package com.dms.flip.data.firebase.mapper

import com.dms.flip.data.firebase.dto.CommentDto
import com.dms.flip.data.firebase.dto.FriendDto
import com.dms.flip.data.firebase.dto.PostDto
import com.dms.flip.data.firebase.dto.PublicProfileDto
import com.dms.flip.data.firebase.dto.RecentActivityDto
import com.dms.flip.data.firebase.dto.RequestDto
import com.dms.flip.data.firebase.dto.SuggestionDto
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.model.community.FriendRequestSource
import com.dms.flip.domain.model.community.FriendSuggestion
import com.dms.flip.domain.model.community.PleasureCategory
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.model.community.RecentActivity
import com.dms.flip.domain.model.community.RelationshipStatus

private fun String.toPleasureCategoryOrNull(): PleasureCategory? =
    runCatching { PleasureCategory.valueOf(uppercase()) }.getOrNull()

fun PostDto.toDomain(
    id: String,
    author: Friend,
    comments: List<PostComment> = emptyList(),
    isLiked: Boolean = false
): Post = Post(
    id = id,
    friend = author,
    content = content,
    timestamp = timestamp?.time ?: 0L,
    photoUrl = photoUrl,
    photoUrlThumb = photoUrlThumb,
    likesCount = likeCount,
    commentsCount = commentsCount,
    isLiked = isLiked,
    pleasureCategory = pleasureCategory?.toPleasureCategoryOrNull(),
    pleasureTitle = pleasureTitle,
    comments = comments
)

fun CommentDto.toDomain(id: String): PostComment = PostComment(
    id = id,
    userId = userId,
    username = username,
    userHandle = userHandle,
    avatarUrl = avatarUrl,
    content = content,
    timestamp = timestamp?.time ?: 0L
)

fun FriendDto.toDomain(id: String): Friend = Friend(
    id = id,
    username = username,
    handle = handle,
    avatarUrl = avatarUrl
)

fun RequestDto.toPendingReceived(id: String): FriendRequest = FriendRequest(
    id = id,
    userId = fromUserId,
    username = fromUsername,
    handle = fromHandle,
    avatarUrl = fromAvatarUrl,
    requestedAt = requestedAt?.time ?: 0L,
    source = FriendRequestSource.SEARCH
)

fun RequestDto.toPendingSent(id: String): FriendRequest = FriendRequest(
    id = id,
    userId = fromUserId,
    username = fromUsername,
    handle = fromHandle,
    avatarUrl = fromAvatarUrl,
    requestedAt = requestedAt?.time ?: 0L,
    source = FriendRequestSource.SEARCH
)

fun SuggestionDto.toDomain(id: String): FriendSuggestion = FriendSuggestion(
    id = id,
    username = username,
    handle = handle,
    avatarUrl = avatarUrl,
    mutualFriendsCount = mutualFriendsCount
)

fun PublicProfileDto.toDomain(
    id: String,
    recentActivities: List<RecentActivity>,
    relationshipStatus: RelationshipStatus
): PublicProfile = PublicProfile(
    id = id,
    username = username,
    handle = handle,
    avatarUrl = avatarUrl,
    bio = bio,
    friendsCount = stats["friends"] ?: 0,
    daysCompleted = stats["daysCompleted"] ?: 0,
    currentStreak = stats["currentStreak"] ?: 0,
    recentActivities = recentActivities,
    relationshipStatus = relationshipStatus
)

fun RecentActivityDto.toDomain(id: String): RecentActivity? {
    val categoryEnum = category.toPleasureCategoryOrNull() ?: return null
    return RecentActivity(
        id = id,
        pleasureTitle = pleasureTitle,
        category = categoryEnum,
        completedAt = completedAt?.time ?: 0L,
        isCompleted = isCompleted
    )
}
