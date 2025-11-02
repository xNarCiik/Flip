package com.dms.flip.ui.community.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dms.flip.R
import com.dms.flip.ui.component.FlipTopBar
import com.dms.flip.ui.component.TopBarIcon
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview

@Composable
fun FriendsListTopBar(
    pendingRequestsCount: Int,
    onNavigateBack: () -> Unit,
    onSearchClick: () -> Unit,
    onAddFriendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlipTopBar(
        modifier = modifier,
        title = stringResource(R.string.friends_list_title),
        startTopBarIcon = TopBarIcon(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.navigate_back),
            onClick = onNavigateBack
        ),
        endTopBarIcons = listOf(
            TopBarIcon(
                icon = Icons.Default.Search,
                contentDescription = stringResource(R.string.community_search),
                onClick = onSearchClick
            ),
            TopBarIcon(
                icon = Icons.Default.PersonAdd,
                contentDescription = stringResource(R.string.community_add_friend),
                onClick = onAddFriendClick,
                badgeCount = pendingRequestsCount.takeIf { it > 0 }
            )
        )
    )
}

@LightDarkPreview
@Composable
private fun FriendsListTopBarPreview() {
    FlipTheme {
        Surface {
            FriendsListTopBar(
                onNavigateBack = {},
                onSearchClick = {},
                onAddFriendClick = {},
                pendingRequestsCount = 2
            )
        }
    }
}
