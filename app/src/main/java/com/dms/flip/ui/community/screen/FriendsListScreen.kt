package com.dms.flip.ui.community.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.ui.community.CommunityEvent
import com.dms.flip.ui.community.CommunityUiState
import com.dms.flip.ui.community.component.CommunityEmptyState
import com.dms.flip.ui.community.component.FriendListItem
import com.dms.flip.ui.community.component.FriendOptionsDialog
import com.dms.flip.ui.community.component.FriendsListTopBar
import com.dms.flip.ui.component.ErrorState
import com.dms.flip.ui.component.LoadingState
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewCommunityUiStateFull

@Composable
fun FriendsListScreen(
    modifier: Modifier = Modifier,
    uiState: CommunityUiState,
    onEvent: (CommunityEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedFriend by remember { mutableStateOf<Friend?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        FriendsListTopBar(
            onNavigateBack = onNavigateBack,
            onSearchClick = { onEvent(CommunityEvent.OnSearchClicked) },
            onAddFriendClick = { onEvent(CommunityEvent.OnInvitationsClicked) },
            pendingRequestsCount = uiState.pendingRequests.size
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.errorMessage != null -> {
                    ErrorState(
                        modifier = Modifier.fillMaxSize(),
                        message = uiState.errorMessage,
                        onRetry = { onEvent(CommunityEvent.OnRetryClicked) }
                    )
                }

                uiState.isLoadingInitial && uiState.friends.isEmpty() -> {
                    LoadingState(modifier = Modifier.fillMaxSize())
                }

                uiState.friends.isEmpty() -> {
                    CommunityEmptyState(
                        emoji = stringResource(id = R.string.community_empty_friends_emoji),
                        title = stringResource(id = R.string.community_empty_friends_title),
                        description = stringResource(id = R.string.community_empty_friends_description),
                        actionText = stringResource(id = R.string.community_empty_friends_action),
                        onActionClick = { onEvent(CommunityEvent.OnSearchClicked) }
                    )
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items = uiState.friends, key = { it.id }) { friend ->
                            FriendListItem(
                                friend = friend,
                                onClick = { onEvent(CommunityEvent.OnFriendClicked(friend)) },
                                onMenuClick = { selectedFriend = friend }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedFriend?.let { friend ->
        FriendOptionsDialog(
            friend = friend,
            onDismiss = { selectedFriend = null },
            onViewProfile = {
                onEvent(CommunityEvent.OnFriendClicked(friend))
                selectedFriend = null
            },
            onInvite = {
                onEvent(CommunityEvent.OnInviteFriendToPleasure(friend))
                selectedFriend = null
            },
            onRemove = {
                onEvent(CommunityEvent.OnRemoveFriend(friend))
                selectedFriend = null
            }
        )
    }
}

@LightDarkPreview
@Composable
private fun FriendsListScreenPreview() {
    FlipTheme {
        Surface {
            FriendsListScreen(
                uiState = previewCommunityUiStateFull,
                onEvent = {},
                onNavigateBack = {}
            )
        }
    }
}
