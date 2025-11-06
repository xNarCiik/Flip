package com.dms.flip.ui.community.component.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dms.flip.R
import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.iconTint
import com.dms.flip.ui.component.CommunityAvatar
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.formatTimestamp
import com.dms.flip.ui.util.previewPosts

private val FireStreakColor = Color(0xFFFF6B35)

@Composable
fun PostHeader(
    post: Post,
    isOwnPost: Boolean,
    onFriendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = post.pleasureCategory?.iconTint ?: MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onFriendClick)
        ) {
            CommunityAvatar(
                imageUrl = post.author.avatarUrl,
                fallbackText = post.author.username.firstOrNull()?.uppercase() ?: "?",
                size = 40.dp,
                borderColor = categoryColor
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayName = if (isOwnPost) {
                        stringResource(id = R.string.community_comment_author_me)
                    } else post.author.username

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.95f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (post.author.currentStreak > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = FireStreakColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = post.author.currentStreak.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = FireStreakColor
                            )
                        }
                    }
                }

                Text(
                    text = formatTimestamp(post.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PostHeaderPreview() {
    FlipTheme {
        Surface {
            PostHeader(
                post = previewPosts.first(),
                isOwnPost = true,
                onFriendClick = {}
            )
        }
    }
}
