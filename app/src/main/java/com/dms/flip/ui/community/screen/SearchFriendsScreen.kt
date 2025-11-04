package com.dms.flip.ui.community.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dms.flip.R
import com.dms.flip.ui.component.ErrorState
import com.dms.flip.ui.component.LoadingState
import com.dms.flip.ui.community.CommunityEvent
import com.dms.flip.ui.community.CommunityUiState
import com.dms.flip.ui.community.component.CommunityEmptyState
import com.dms.flip.ui.community.component.SearchResultItem
import com.dms.flip.ui.community.component.SearchTopBar
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewCommunityUiStateSearching

@Composable
fun SearchFriendsScreen(
    modifier: Modifier = Modifier,
    uiState: CommunityUiState,
    onEvent: (CommunityEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SearchTopBar(
            searchQuery = uiState.searchQuery,
            onQueryChange = { onEvent(CommunityEvent.OnSearchQueryChanged(it)) },
            onNavigateBack = onNavigateBack
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

                uiState.isSearching -> {
                    LoadingState(modifier = Modifier.fillMaxSize())
                }

                uiState.searchQuery.isBlank() -> {
                    CommunityEmptyState(
                        emoji = stringResource(id = R.string.community_empty_search_initial_emoji),
                        title = stringResource(id = R.string.community_empty_search_initial_title),
                        description = stringResource(id = R.string.community_empty_search_initial_description)
                    )
                }

                uiState.searchResults.isEmpty() -> {
                    CommunityEmptyState(
                        emoji = stringResource(id = R.string.community_empty_search_results_emoji),
                        title = stringResource(id = R.string.community_empty_search_results_title),
                        description = stringResource(id = R.string.community_empty_search_results_description)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(uiState.searchResults, key = { it.id }) { result ->
                            SearchResultItem(
                                result = result,
                                onAdd = { onEvent(CommunityEvent.OnAddUserFromSearch(result.id)) },
                                onClick = { onEvent(CommunityEvent.OnSearchResultClicked(result)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun SearchFriendsScreenPreview() {
    FlipTheme {
        Surface {
            SearchFriendsScreen(
                uiState = previewCommunityUiStateSearching,
                onEvent = {},
                onNavigateBack = {}
            )
        }
    }
}
