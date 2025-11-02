package com.dms.flip.ui.community.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewFriends

private val FireStreakColor = Color(0xFFFF6B35)
private val OnlineColor = Color(0xFF4CAF50)

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun FriendListItem(
    friend: Friend,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    onQuickAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            CommunityAvatar(
                imageUrl = friend.avatarUrl,
                fallbackText = friend.username.firstOrNull()?.uppercase() ?: "?",
                textStyle = MaterialTheme.typography.titleLarge,
                size = 56.dp
            )

            if (friend.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(OnlineColor)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = friend.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (friend.streak > 0) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(FireStreakColor.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = FireStreakColor,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = friend.streak.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FireStreakColor
                        )
                    }
                }
            }

            Text(
                text = friend.handle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onMenuClick, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun FriendListItemPreview() {
    FlipTheme {
        Surface {
            FriendListItem(
                friend = previewFriends.first(),
                onClick = {},
                onMenuClick = {},
                onQuickAction = {}
            )
        }
    }
}
