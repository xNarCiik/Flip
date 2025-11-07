package com.dms.flip.ui.community.component.feed

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.domain.model.community.icon
import com.dms.flip.domain.model.community.iconTint
import com.dms.flip.domain.model.community.label
import com.dms.flip.ui.component.PleasureCard
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewPosts

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PostCard(
    modifier: Modifier = Modifier,
    post: Post,
    isExpanded: Boolean,
    isOwnPost: Boolean,
    comments: List<PostComment> = emptyList(),
    isLoadingComments: Boolean = false,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onMenu: () -> Unit,
    onFriendClick: () -> Unit,
    onAddComment: (String) -> Unit,
    onCommentUserClick: (PostComment) -> Unit,
    onOwnCommentLongPress: (PostComment) -> Unit,
    onOwnPostLongPress: (() -> Unit)?,
    currentUserId: String?
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

    Column(
        modifier = modifier
            .then(pressModifier)
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (!post.photoUrl.isNullOrEmpty()) {
                GlideImage(
                    model = post.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                        .clickable { showFullImage = true }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                PostHeader(post = post, isOwnPost = isOwnPost, onFriendClick = onFriendClick)
            }
        }

        if (post.pleasureCategory != null && post.pleasureTitle != null) {
            PleasureCard(
                icon = post.pleasureCategory.icon,
                iconTint = post.pleasureCategory.iconTint,
                label = androidx.compose.ui.res.stringResource(post.pleasureCategory.label),
                title = post.pleasureTitle,
                description = null,
                showChevron = false,
                isCompleted = true,
                onClick = {},
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

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

        PostActions(
            post = post,
            isExpanded = isExpanded,
            onLike = onLike,
            onComment = onComment,
            onMenu = onMenu
        )

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
            ) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                when {
                    isLoadingComments -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    comments.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Soyez le premier à commenter",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.6f
                                )
                            )
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            comments.forEach { comment ->
                                CommentItem(
                                    comment = comment,
                                    isOwnComment = comment.userId == currentUserId,
                                    onUserClick = { onCommentUserClick(comment) },
                                    onLongPress = if (comment.userId == currentUserId) {
                                        { onOwnCommentLongPress(comment) }
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                    }
                }

                CommentInputField(
                    onSubmit = onAddComment,
                    placeholder = "Ajouter un commentaire..."
                )
            }
        }

        if (showFullImage && !post.photoUrl.isNullOrEmpty()) {
            PostImageDialog(
                imageUrl = post.photoUrl,
                onDismiss = { showFullImage = false })
        }
    }
}

@LightDarkPreview
@Composable
private fun PostCardPreview() {
    FlipTheme {
        Surface {
            PostCard(
                post = previewPosts.first(),
                isExpanded = false,
                isOwnPost = true,
                onLike = {},
                onComment = {},
                onMenu = {},
                onFriendClick = {},
                onAddComment = { _ -> },
                onCommentUserClick = { _ -> },
                onOwnCommentLongPress = { _ -> },
                onOwnPostLongPress = {},
                currentUserId = previewPosts.first().author.id
            )
        }
    }
}
