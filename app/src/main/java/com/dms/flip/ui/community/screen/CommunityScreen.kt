package com.dms.flip.ui.community.screen

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
import com.dms.flip.ui.community.CommunityEvent
import com.dms.flip.ui.community.CommunityUiState
import com.dms.flip.domain.model.community.FriendPost
import com.dms.flip.R
import com.dms.flip.ui.component.ErrorState
import com.dms.flip.ui.component.LoadingState
import com.dms.flip.ui.community.component.CommunityTopBar
import com.dms.flip.ui.community.component.FriendsFeedContent
import com.dms.flip.ui.community.component.PostOptionsDialog
import com.dms.flip.ui.community.component.CommunityEmptyState
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewCommunityUiStateFull

@Composable
fun CommunityScreen(
    modifier: Modifier = Modifier,
    uiState: CommunityUiState,
    onEvent: (CommunityEvent) -> Unit
) {
    var selectedPost by remember { mutableStateOf<FriendPost?>(null) }

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
            when {
                uiState.error != null -> {
                    ErrorState(
                        modifier = Modifier.fillMaxSize(),
                        message = stringResource(id = uiState.error),
                        onRetry = { onEvent(CommunityEvent.OnRetryClicked) }
                    )
                }

                uiState.isLoading && uiState.friendsPosts.isEmpty() -> {
                    LoadingState(modifier = Modifier.fillMaxSize())
                }

                uiState.friendsPosts.isEmpty() -> {
                    CommunityEmptyState(
                        emoji = stringResource(id = R.string.community_empty_feed_emoji),
                        title = stringResource(id = R.string.community_empty_feed_title),
                        description = stringResource(id = R.string.community_empty_feed_description),
                        actionText = stringResource(id = R.string.community_empty_feed_action),
                        onActionClick = { onEvent(CommunityEvent.OnAddFriendClicked) }
                    )
                }

                else -> {
                    FriendsFeedContent(
                        posts = uiState.friendsPosts,
                        expandedPostId = uiState.expandedPostId,
                        onEvent = onEvent,
                        onPostMenuClick = { selectedPost = it }
                    )
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
