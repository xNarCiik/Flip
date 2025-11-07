package com.dms.flip.ui.community.component.feed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dms.flip.R
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.ui.component.CommunityAvatar
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.formatTimestamp
import com.dms.flip.ui.util.previewPosts

/**
 * Composant pour afficher un commentaire individuel.
 * Support du long press pour la suppression si c'est le commentaire de l'utilisateur.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentItem(
    comment: PostComment,
    isOwnComment: Boolean,
    onUserClick: () -> Unit,
    onLongPress: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onLongPress != null) {
                    Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {},
                        onLongClick = onLongPress
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar
        CommunityAvatar(
            imageUrl = comment.avatarUrl,
            fallbackText = comment.username.firstOrNull()?.uppercase() ?: "?",
            size = 34.dp,
            textStyle = MaterialTheme.typography.labelSmall,
            modifier = Modifier.clickable(onClick = onUserClick)
        )

        // Contenu avec fond
        Column(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // En-tête avec nom et timestamp
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOwnComment) {
                        stringResource(id = R.string.community_comment_author_me)
                    } else {
                        comment.username
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable(onClick = onUserClick)
                )

                Text(
                    text = "·",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )

                Text(
                    text = formatTimestamp(comment.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            // Contenu du commentaire
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun CommentItemPreview() {
    FlipTheme {
        Surface {
            Column {
                CommentItem(
                    comment = previewPosts.first().comments.first(),
                    isOwnComment = false,
                    onUserClick = {},
                    onLongPress = null
                )

                CommentItem(
                    comment = previewPosts.first().comments.first(),
                    isOwnComment = true,
                    onUserClick = {},
                    onLongPress = {}
                )
            }
        }
    }
}
