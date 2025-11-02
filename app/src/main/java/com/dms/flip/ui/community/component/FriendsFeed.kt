package com.dms.flip.ui.community.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.dms.flip.R
import com.dms.flip.data.model.PleasureCategory
import com.dms.flip.ui.community.CommunityEvent
import com.dms.flip.domain.model.community.FriendPost
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.formatTimestamp
import com.dms.flip.ui.util.previewPosts

private val FireStreakColor = Color(0xFFFF6B35)

@Composable
fun FriendsFeedContent(
    posts: List<FriendPost>,
    expandedPostId: String?,
    onEvent: (CommunityEvent) -> Unit,
    onPostMenuClick: (FriendPost) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(items = posts, key = { it.id }) { post ->
            FriendPostCard(
                post = post,
                isExpanded = post.id == expandedPostId,
                onLike = { onEvent(CommunityEvent.OnPostLiked(post.id)) },
                onComment = { onEvent(CommunityEvent.OnToggleComments(post.id)) },
                onMenu = { onPostMenuClick(post) },
                onFriendClick = { onEvent(CommunityEvent.OnFriendClicked(post.friend)) },
                onAddComment = { comment ->
                    onEvent(CommunityEvent.OnAddComment(post.id, comment))
                }
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun FriendPostCard(
    post: FriendPost,
    isExpanded: Boolean,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onMenu: () -> Unit,
    onFriendClick: () -> Unit,
    onAddComment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val likeScale by animateFloatAsState(
        targetValue = if (post.isLiked) 1.1f else 1f,
        label = "likeScale"
    )
    val likeColor by animateColorAsState(
        targetValue = if (post.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "likeColor"
    )
    val commentColor by animateColorAsState(
        targetValue = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "commentColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onFriendClick)
            ) {
                if (post.friend.avatarUrl != null) {
                    GlideImage(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        model = post.friend.avatarUrl,
                        contentScale = ContentScale.Crop,
                        contentDescription = null
                    )
                } else {
                    PlaceholderAvatar(name = post.friend.username)
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = post.friend.username,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (post.friend.streak > 0) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(FireStreakColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = FireStreakColor,
                                    modifier = Modifier.size(10.dp)
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

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = post.friend.handle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatTimestamp(post.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(
                onClick = onMenu,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.community_post_menu),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (post.pleasureCategory != null && post.pleasureTitle != null) {
            PleasureCard(category = post.pleasureCategory, title = post.pleasureTitle)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = post.content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 60.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (!post.isLiked) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onLike()
                    }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (post.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = stringResource(R.string.community_like),
                    tint = likeColor,
                    modifier = Modifier
                        .size(20.dp)
                        .scale(likeScale)
                )
                Text(
                    text = post.likesCount.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (post.isLiked) FontWeight.SemiBold else FontWeight.Normal,
                    color = likeColor
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onComment)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = stringResource(R.string.community_comment),
                    tint = commentColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = post.commentsCount.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isExpanded) FontWeight.SemiBold else FontWeight.Normal,
                    color = commentColor
                )
            }
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(16.dp))
            CommentsSection(comments = post.comments, onAddComment = onAddComment)
        }
    }
}

@Composable
private fun PlaceholderAvatar(name: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
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

@Composable
private fun PleasureCard(
    category: PleasureCategory,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(category.iconTint.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(category.iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = category.iconTint.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodySmall,
                color = category.iconTint,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private class FriendsFeedPreviewParameterProvider : PreviewParameterProvider<List<FriendPost>> {
    override val values: Sequence<List<FriendPost>> = sequenceOf(
        previewPosts.take(3),
        previewPosts.take(2)
    )
}

@LightDarkPreview
@Composable
private fun FriendsFeedPreview(
    @PreviewParameter(FriendsFeedPreviewParameterProvider::class)
    posts: List<FriendPost>
) {
    FlipTheme {
        Surface {
            FriendsFeedContent(
                posts = posts,
                expandedPostId = posts.firstOrNull()?.id,
                onEvent = {},
                onPostMenuClick = {}
            )
        }
    }
}
