package com.dms.flip.ui.community.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.dms.flip.R
import com.dms.flip.ui.component.FlipTopBar
import com.dms.flip.ui.component.TopBarIcon
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview

@Composable
fun CommunityTopBar(
    modifier: Modifier = Modifier,
    pendingRequestsCount: Int,
    onFriendsListClick: () -> Unit,
    onInvitationsClick: () -> Unit,
    onRefreshClick: (() -> Unit)? = null
) {
    FlipTopBar(
        modifier = modifier,
        title = stringResource(R.string.community_title),
        startTopBarIcon = TopBarIcon(
            icon = Icons.Default.People,
            contentDescription = stringResource(R.string.friends_list),
            onClick = onFriendsListClick
        ),
        endTopBarIcons = listOf(
            TopBarIcon(
                icon = Icons.Default.PersonAdd,
                contentDescription = stringResource(R.string.community_invitations),
                onClick = onInvitationsClick,
                badgeCount = pendingRequestsCount.takeIf { it > 0 }
            ),
            onRefreshClick?.let {
                TopBarIcon(
                    icon = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.retry),
                    onClick = it
                )
            }
        ).filterNotNull()
    )
}

private class CommunityTopBarPreviewParameterProvider : PreviewParameterProvider<Int> {
    override val values: Sequence<Int> = sequenceOf(0, 3)
}

@LightDarkPreview
@Composable
private fun CommunityTopBarPreview(
    @PreviewParameter(CommunityTopBarPreviewParameterProvider::class)
    pendingRequests: Int
) {
    FlipTheme {
        Surface {
            CommunityTopBar(
                pendingRequestsCount = pendingRequests,
                onFriendsListClick = {},
                onInvitationsClick = {},
                onRefreshClick = {}
            )
        }
    }
}
