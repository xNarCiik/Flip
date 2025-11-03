package com.dms.flip.ui.community.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dms.flip.R
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.model.community.RecentActivity
import com.dms.flip.domain.model.community.RelationshipStatus
import com.dms.flip.ui.component.FlipTopBar
import com.dms.flip.ui.component.TopBarIcon
import com.dms.flip.ui.community.component.CommunityAvatar
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewPublicProfile

@Composable
fun PublicProfileScreen(
    modifier: Modifier = Modifier,
    profile: PublicProfile,
    isCurrentUser: Boolean,
    onAddFriend: () -> Unit,
    onAcceptFriendRequest: () -> Unit,
    onOptionsClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        FlipTopBar(
            title = profile.username,
            startTopBarIcon = TopBarIcon(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.navigate_back),
                onClick = onNavigateBack
            ),
            endTopBarIcons = if (!isCurrentUser && profile.relationshipStatus == RelationshipStatus.FRIEND) {
                listOf(
                    TopBarIcon(
                        icon = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.friend_options),
                        onClick = onOptionsClick
                    )
                )
            } else {
                emptyList()
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Profile Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CommunityAvatar(
                        imageUrl = profile.avatarUrl,
                        fallbackText = profile.username.firstOrNull()?.uppercase() ?: "?",
                        size = 120.dp,
                        textStyle = MaterialTheme.typography.headlineLarge
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = profile.username,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = profile.handle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (profile.bio != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = profile.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Add Friend Button
            if (!isCurrentUser) {
                item {
                    val (buttonText, enabled, action) = when (profile.relationshipStatus) {
                        RelationshipStatus.FRIEND -> Triple(
                            stringResource(R.string.community_already_friends),
                            false,
                            null
                        )

                        RelationshipStatus.PENDING_SENT -> Triple(
                            stringResource(R.string.status_pending),
                            false,
                            null
                        )

                        RelationshipStatus.PENDING_RECEIVED -> Triple(
                            stringResource(R.string.button_accept),
                            true,
                            onAcceptFriendRequest
                        )

                        else -> Triple(
                            stringResource(R.string.button_add_friend),
                            true,
                            onAddFriend
                        )
                    }

                    Button(
                        onClick = { action?.invoke() },
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        value = profile.friendsCount.toString(),
                        label = stringResource(R.string.stat_friends)
                    )
                    StatItem(
                        value = profile.daysCompleted.toString(),
                        label = stringResource(R.string.stat_days_completed)
                    )
                    StatItem(
                        value = profile.currentStreak.toString(),
                        label = stringResource(R.string.stat_streak)
                    )
                }
            }

            // Recent Activity
            item {
                Text(
                    text = stringResource(R.string.recent_activity_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(profile.recentActivities) { activity ->
                RecentActivityItem(activity)
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecentActivityItem(activity: RecentActivity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(activity.category.iconTint.copy(0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = activity.category.icon,
                contentDescription = null,
                tint = activity.category.iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.pleasureTitle,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (activity.isCompleted) stringResource(R.string.activity_completed)
                else stringResource(R.string.activity_in_progress),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = formatActivityTime(activity.completedAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatActivityTime(timestamp: Long): String {
    val days = ((System.currentTimeMillis() - timestamp) / (1000 * 60 * 60 * 24)).toInt()
    return when (days) {
        0 -> "Aujourd'hui"
        1 -> "Il y a 1 jour"
        else -> "Il y a ${days} jours"
    }
}

@LightDarkPreview
@Composable
private fun PublicProfileScreenPreview() {
    FlipTheme {
        Surface {
            PublicProfileScreen(
                profile = previewPublicProfile,
                isCurrentUser = false,
                onAddFriend = {},
                onAcceptFriendRequest = {},
                onOptionsClick = {},
                onNavigateBack = {}
            )
        }
    }
}
