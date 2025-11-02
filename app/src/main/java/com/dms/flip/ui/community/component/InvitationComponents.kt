package com.dms.flip.ui.community.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.dms.flip.R
import com.dms.flip.ui.community.component.CommunityAvatar
import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.model.community.FriendSuggestion
import com.dms.flip.ui.component.FlipTopBar
import com.dms.flip.ui.component.TopBarIcon
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewPendingRequests
import com.dms.flip.ui.util.previewSentRequests
import com.dms.flip.ui.util.previewSuggestions

@Composable
fun InvitationsTopBar(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlipTopBar(
        modifier = modifier,
        title = stringResource(R.string.community_invitations),
        startTopBarIcon = TopBarIcon(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.navigate_back),
            onClick = onNavigateBack
        )
    )
}

@Composable
fun ReceivedRequestItem(
    request: FriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RequestRow(
        modifier = modifier.clickable(onClick = onClick),
        request = request,
        trailingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDecline,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(stringResource(R.string.button_decline))
                }
                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(stringResource(R.string.button_accept))
                }
            }
        }
    )
}

@Composable
fun SentRequestItem(
    request: FriendRequest,
    onCancel: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RequestRow(
        modifier = modifier.clickable(onClick = onClick),
        request = request,
        trailingContent = {
            TextButton(
                onClick = onCancel,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}

@Composable
private fun RequestRow(
    request: FriendRequest,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            CommunityAvatar(
                imageUrl = request.avatarUrl,
                fallbackText = request.username.firstOrNull()?.uppercase() ?: "?",
                size = 56.dp
            )

            Column {
                Text(
                    text = request.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = request.handle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatRequestTime(request.requestedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        trailingContent()
    }
}

@Composable
fun SuggestionsSection(
    suggestions: List<FriendSuggestion>,
    onAdd: (FriendSuggestion) -> Unit,
    onHide: (FriendSuggestion) -> Unit,
    onSuggestionClick: (FriendSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.suggestions_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            suggestions.forEach { suggestion ->
                SuggestionCard(
                    suggestion = suggestion,
                    onAdd = { onAdd(suggestion) },
                    onHide = { onHide(suggestion) },
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: FriendSuggestion,
    onAdd: () -> Unit,
    onHide: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Box {
            IconButton(
                onClick = onHide,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.button_hide),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CommunityAvatar(
                    imageUrl = suggestion.avatarUrl,
                    fallbackText = suggestion.username.firstOrNull()?.uppercase() ?: "?",
                    size = 64.dp
                )

                Text(
                    text = suggestion.username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (suggestion.mutualFriendsCount > 0) {
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.community_mutual_friends,
                            count = suggestion.mutualFriendsCount,
                            suggestion.mutualFriendsCount
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAdd,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.button_add),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
internal fun formatRequestTime(timestamp: Long): String {
    val days = ((System.currentTimeMillis() - timestamp) / (1_000 * 60 * 60 * 24)).toInt()
    return when (days) {
        0 -> stringResource(id = R.string.community_request_time_today)
        1 -> stringResource(id = R.string.community_request_time_yesterday)
        else -> pluralStringResource(
            id = R.plurals.community_request_time_days,
            count = days,
            days
        )
    }
}

private class InvitationsPreviewProvider : PreviewParameterProvider<List<FriendRequest>> {
    override val values: Sequence<List<FriendRequest>> = sequenceOf(previewPendingRequests)
}

@LightDarkPreview
@Composable
private fun InvitationsComponentsPreview(
    @PreviewParameter(InvitationsPreviewProvider::class)
    requests: List<FriendRequest>
) {
    FlipTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InvitationsTopBar(onNavigateBack = {})
                ReceivedRequestItem(
                    request = requests.first(),
                    onAccept = {},
                    onDecline = {},
                    onClick = {}
                )
                SentRequestItem(
                    request = previewSentRequests.first(),
                    onCancel = {},
                    onClick = {}
                )
                SuggestionsSection(
                    suggestions = previewSuggestions,
                    onAdd = {},
                    onHide = {},
                    onSuggestionClick = {}
                )
            }
        }
    }
}
