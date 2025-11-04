package com.dms.flip.ui.community.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dms.flip.R
import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.ui.community.CommunityEvent
import com.dms.flip.ui.community.CommunityUiState
import com.dms.flip.ui.community.component.CommunityEmptyState
import com.dms.flip.ui.community.component.CommunityTopBar
import com.dms.flip.ui.community.component.FeedContent
import com.dms.flip.ui.community.component.PostOptionsDialog
import com.dms.flip.ui.community.component.DeleteConfirmationDialog
import com.dms.flip.ui.component.ErrorState
import com.dms.flip.ui.component.LoadingState
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewCommunityUiStateFull

private sealed interface CommunityContentState {
    data object Loading : CommunityContentState
    data object Empty : CommunityContentState
    data class Error(val messageRes: Int) : CommunityContentState
    data object Content : CommunityContentState
}

@Composable
fun CommunityScreen(
    modifier: Modifier = Modifier,
    uiState: CommunityUiState,
    onEvent: (CommunityEvent) -> Unit
) {
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var selectedComment by remember {
        mutableStateOf<Pair<String, PostComment>?>(null)
    }
    var postPendingDeletion by remember { mutableStateOf<Post?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CommunityTopBar(
            pendingRequestsCount = uiState.pendingRequests.size,
            onFriendsListClick = { onEvent(CommunityEvent.OnFriendsListClicked) },
            onInvitationsClick = { onEvent(CommunityEvent.OnInvitationsClicked) }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            val contentState = when {
                uiState.error != null -> CommunityContentState.Error(uiState.error)
                uiState.isLoading && uiState.friendsPosts.isEmpty() -> CommunityContentState.Loading
                uiState.friendsPosts.isEmpty() -> CommunityContentState.Empty
                else -> CommunityContentState.Content
            }

            AnimatedContent(
                targetState = contentState,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(200)) +
                            scaleIn(initialScale = 0.98f, animationSpec = tween(200))) togetherWith
                            (fadeOut(animationSpec = tween(150)) +
                                    scaleOut(targetScale = 1.01f, animationSpec = tween(150)))
                },
                label = "CommunityContentTransition"
            ) { state ->
                when (state) {
                    CommunityContentState.Loading -> {
                        LoadingState(modifier = Modifier.fillMaxSize())
                    }

                    CommunityContentState.Empty -> {
                        CommunityEmptyState(
                            emoji = stringResource(id = R.string.community_empty_feed_emoji),
                            title = stringResource(id = R.string.community_empty_feed_title),
                            description = stringResource(id = R.string.community_empty_feed_description),
                            actionText = stringResource(id = R.string.community_empty_feed_action),
                            onActionClick = { onEvent(CommunityEvent.OnSearchClicked) }
                        )
                    }

                    is CommunityContentState.Error -> {
                        ErrorState(
                            modifier = Modifier.fillMaxSize(),
                            message = stringResource(id = state.messageRes),
                            onRetry = { onEvent(CommunityEvent.OnRetryClicked) }
                        )
                    }

                    is CommunityContentState.Content -> {
                        FeedContent(
                            posts = uiState.friendsPosts,
                            expandedPostId = uiState.expandedPostId,
                            currentUserId = uiState.currentUserId,
                            onEvent = onEvent,
                            onPostMenuClick = { selectedPost = it },
                            onOwnCommentLongPress = { postId, comment ->
                                selectedComment = postId to comment
                            },
                            onOwnPostLongPress = { post ->
                                postPendingDeletion = post
                            }
                        )
                    }
                }
            }
        }
    }

    selectedPost?.let { post ->
        PostOptionsDialog(
            post = post,
            onDismiss = { selectedPost = null },
            onViewProfile = {
                onEvent(CommunityEvent.OnFriendClicked(post.friend))
                selectedPost = null
            },
            onDelete = { selectedPost = null }
        )
    }

    selectedComment?.let { (postId, comment) ->
        DeleteConfirmationDialog(
            confirmText = stringResource(id = R.string.community_comment_delete),
            onConfirm = {
                onEvent(CommunityEvent.OnDeleteComment(postId, comment.id))
                selectedComment = null
            },
            onDismiss = { selectedComment = null }
        )
    }

    postPendingDeletion?.let { post ->
        DeleteConfirmationDialog(
            confirmText = stringResource(id = R.string.community_post_delete),
            onConfirm = {
                onEvent(CommunityEvent.OnDeletePost(post.id))
                postPendingDeletion = null
            },
            onDismiss = { postPendingDeletion = null }
        )
    }
}

@LightDarkPreview
@Composable
private fun CommunityScreenPreview() {
    FlipTheme {
        Surface {
            CommunityScreen(
                uiState = previewCommunityUiStateFull,
                onEvent = {}
            )
        }
    }
}
