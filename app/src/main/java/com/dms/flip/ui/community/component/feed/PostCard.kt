package com.dms.flip.ui.community.component.feed

import android.R.attr.iconTint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.dms.flip.ui.community.component.CommentsSection
import com.dms.flip.ui.component.PleasureCard
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewPosts

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

    val borderColor = post.pleasureCategory?.iconTint
        ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)

    Column(
        modifier = modifier
            .then(pressModifier)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.5.dp,
                color = borderColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(24.dp))
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

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.08f)
        )

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
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp)
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

    if (showFullImage && !post.photoUrl.isNullOrEmpty()) {
        PostImageDialog(
            imageUrl = post.photoUrl,
            onDismiss = { showFullImage = false })
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
                currentUserId = previewPosts.first().friend.id
            )
        }
    }
}
