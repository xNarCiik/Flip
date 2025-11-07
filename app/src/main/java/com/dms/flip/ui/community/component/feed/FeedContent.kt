package com.dms.flip.ui.community.component.feed

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.ui.community.CommunityEvent
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewPosts

@Composable
fun FeedContent(
    modifier: Modifier = Modifier,
    posts: List<Post>,
    expandedPostId: String?,
    currentUserId: String?,
    isLoadingMore: Boolean = false,
    hasMorePages: Boolean = true,
    onEvent: (CommunityEvent) -> Unit,
    onPostMenuClick: (Post) -> Unit,
    onOwnCommentLongPress: (String, PostComment) -> Unit,
    onOwnPostLongPress: (Post) -> Unit,
    scrollToTopTrigger: Int = 0,
    onScrollStateChanged: ((Boolean) -> Unit)? = null
) {
    val listState = rememberLazyListState()

    // Détection si on est en haut
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    // Détection si on est proche de la fin (3 items avant la fin)
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1

            // ✅ Charger plus uniquement si :
            // 1. Il y a au moins 6 items (donc liste scrollable)
            // 2. On est à moins de 3 items de la fin
            totalItems > 6 && lastVisibleIndex >= totalItems - 3
        }
    }

    // Notifier le scroll en haut
    LaunchedEffect(isAtTop) {
        onScrollStateChanged?.invoke(isAtTop)
    }

    // Déclencher le chargement automatiquement
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoadingMore && hasMorePages) {
            onEvent(CommunityEvent.OnLoadMorePosts)
        }
    }

    // Scroll vers le haut quand demandé
    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = posts, key = { it.id }) { post ->
            val isOwnPost = post.author.id == currentUserId
            PostCard(
                post = post,
                isExpanded = post.id == expandedPostId,
                isOwnPost = isOwnPost,
                onLike = { onEvent(CommunityEvent.OnPostLiked(post.id)) },
                onComment = { onEvent(CommunityEvent.OnToggleComments(post.id)) },
                onMenu = { onPostMenuClick(post) },
                onFriendClick = { onEvent(CommunityEvent.OnViewProfile(post.author)) },
                onAddComment = { comment ->
                    onEvent(CommunityEvent.OnAddComment(post.id, comment))
                },
                onCommentUserClick = { comment ->
                    // TODO onEvent(CommunityEvent.OnViewProfile(comment.id))
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
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            )
        }

        // ✅ Indicateur de chargement en bas
        if (isLoadingMore && hasMorePages) {
            item(key = "loading_more") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun FeedSkeleton(modifier: Modifier = Modifier, count: Int = 5) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(count) { _ ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(0.dp, RoundedCornerShape(24.dp))
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(14.dp)
                                    .fillMaxWidth(0.3f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            )
                            Box(
                                modifier = Modifier
                                    .height(12.dp)
                                    .fillMaxWidth(0.5f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.8f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.08f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        )
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .width(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                        )
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        )
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .width(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    )
                }
            }
        }
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
                currentUserId = posts.firstOrNull()?.author?.id,
                onEvent = {},
                onPostMenuClick = {},
                onOwnCommentLongPress = { _, _ -> },
                onOwnPostLongPress = {}
            )
        }
    }
}
