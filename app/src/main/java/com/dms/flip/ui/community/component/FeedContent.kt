package com.dms.flip.ui.community.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.dms.flip.R
import com.dms.flip.data.model.PleasureCategory
import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.ui.community.CommunityEvent
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.formatTimestamp
import com.dms.flip.ui.util.previewPosts

private val FireStreakColor = Color(0xFFFF6B35)

@Composable
fun FeedContent(
    posts: List<Post>,
    expandedPostId: String?,
    currentUserId: String?,
    onEvent: (CommunityEvent) -> Unit,
    onPostMenuClick: (Post) -> Unit,
    onOwnCommentLongPress: (String, PostComment) -> Unit,
    onOwnPostLongPress: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = posts, key = { it.id }) { post ->
            val isOwnPost = post.friend.id == currentUserId
            PostCard(
                post = post,
                isExpanded = post.id == expandedPostId,
                isOwnPost = isOwnPost,
                onLike = { onEvent(CommunityEvent.OnPostLiked(post.id)) },
                onComment = { onEvent(CommunityEvent.OnToggleComments(post.id)) },
                onMenu = { onPostMenuClick(post) },
                onFriendClick = { onEvent(CommunityEvent.OnFriendClicked(post.friend)) },
                onAddComment = { comment ->
                    onEvent(CommunityEvent.OnAddComment(post.id, comment))
                },
                onCommentUserClick = { comment ->
                    onEvent(CommunityEvent.OnViewProfile(comment.userId))
                },
                onOwnCommentLongPress = { comment ->
                    onOwnCommentLongPress(post.id, comment)
                },
                onOwnPostLongPress = if (isOwnPost) {
                    { onOwnPostLongPress(post) }
                } else {
                    null
                },
                currentUserId = currentUserId,
                modifier = Modifier.animateItem(
                    fadeInSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    fadeOutSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    placementSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PostCard(
    post: Post,
    isExpanded: Boolean,
    isOwnPost: Boolean,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onMenu: () -> Unit,
    onFriendClick: () -> Unit,
    onAddComment: (String) -> Unit,
    onCommentUserClick: (PostComment) -> Unit,
    onOwnCommentLongPress: (PostComment) -> Unit,
    onOwnPostLongPress: (() -> Unit)?,
    currentUserId: String?,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val pressModifier = if (isOwnPost && onOwnPostLongPress != null) {
        Modifier.pointerInput(Unit) {
            detectTapGestures(
                onLongPress = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOwnPostLongPress()
                }
            )
        }
    } else Modifier

    var showFullImage by remember { mutableStateOf(false) }

    val likeScale by animateFloatAsState(
        targetValue = if (post.isLiked) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "likeScale"
    )

    val likeColor by animateColorAsState(
        targetValue = if (post.isLiked) MaterialTheme.colorScheme.error else
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        animationSpec = tween(150),
        label = "likeColor"
    )

    val commentColor by animateColorAsState(
        targetValue = if (isExpanded) MaterialTheme.colorScheme.primary else
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        animationSpec = tween(150),
        label = "commentColor"
    )

    val categoryColor = post.pleasureCategory?.iconTint ?: MaterialTheme.colorScheme.primary

    // Main card container with glassmorphism effect
    Column(
        modifier = modifier
            .then(pressModifier)
            .fillMaxWidth()
            .shadow(
                elevation = 0.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        // Image with overlay header
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Main image
            if (!post.photoUrl.isNullOrEmpty()) {
                GlideImage(
                    model = post.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { showFullImage = true }
                )
            }

            // Overlay header with glassmorphism
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar and user info
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onFriendClick)
                    ) {
                        // Avatar with category-colored border
                        Box {
                            if (post.friend.avatarUrl != null) {
                                GlideImage(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .border(
                                            width = 2.dp,
                                            color = categoryColor.copy(alpha = 0.6f),
                                            shape = CircleShape
                                        )
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .shadow(4.dp, CircleShape),
                                    model = post.friend.avatarUrl,
                                    contentScale = ContentScale.Crop,
                                    contentDescription = null
                                )
                            } else {
                                PlaceholderAvatar(
                                    name = post.friend.username,
                                    borderColor = categoryColor
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val displayName = if (isOwnPost) {
                                    stringResource(id = R.string.community_comment_author_me)
                                } else post.friend.username

                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.95f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (post.friend.streak > 0) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = FireStreakColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = post.friend.streak.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = FireStreakColor
                                        )
                                    }
                                }
                            }

                            Text(
                                text = formatTimestamp(post.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Category badge
                    post.pleasureCategory?.let { category ->
                        Row(
                            modifier = Modifier
                                .background(
                                    color = categoryColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = post.pleasureTitle ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Post content text
        if (post.content.isNotBlank()) {
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4f
                ),
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        // Subtle divider
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.08f)
        )

        // Actions bar (Like, Comment, Menu)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like and Comment buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like button
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (!post.isLiked) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onLike()
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.community_like),
                        tint = likeColor,
                        modifier = Modifier
                            .size(20.dp)
                            .scale(likeScale)
                    )
                    Text(
                        text = post.likesCount.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (post.isLiked) FontWeight.Medium else FontWeight.Normal,
                        color = likeColor
                    )
                }

                // Comment button
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onComment)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = stringResource(R.string.community_comment),
                        tint = commentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = post.commentsCount.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isExpanded) FontWeight.Medium else FontWeight.Normal,
                        color = commentColor
                    )
                }
            }

            // Menu button
            IconButton(
                onClick = onMenu,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Comments section with smooth expand animation
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
                    .padding(16.dp)
            ) {
                CommentsSection(
                    comments = post.comments,
                    currentUserId = currentUserId,
                    onAddComment = onAddComment,
                    onCommentClick = onCommentUserClick,
                    onOwnCommentLongPress = onOwnCommentLongPress
                )
            }
        }
    }

    // Full screen image dialog
    if (showFullImage && !post.photoUrl.isNullOrEmpty()) {
        Dialog(onDismissRequest = { showFullImage = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                GlideImage(
                    model = post.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showFullImage = false }
                )
            }
        }
    }
}

@Composable
private fun PlaceholderAvatar(
    name: String,
    borderColor: Color = Color.Transparent
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(
                width = 2.dp,
                color = borderColor.copy(alpha = 0.6f),
                shape = CircleShape
            )
            .padding(2.dp)
            .clip(CircleShape)
            .shadow(4.dp, CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private class FeedPreviewParameterProvider : PreviewParameterProvider<List<Post>> {
    override val values: Sequence<List<Post>> = sequenceOf(
        previewPosts.take(3),
        previewPosts.take(2)
    )
}

@LightDarkPreview
@Composable
private fun FeedPreview(
    @PreviewParameter(FeedPreviewParameterProvider::class)
    posts: List<Post>
) {
    FlipTheme {
        Surface {
            FeedContent(
                posts = posts,
                expandedPostId = posts.firstOrNull()?.id,
                currentUserId = posts.firstOrNull()?.friend?.id,
                onEvent = {},
                onPostMenuClick = {},
                onOwnCommentLongPress = { _, _ -> },
                onOwnPostLongPress = {}
            )
        }
    }
}