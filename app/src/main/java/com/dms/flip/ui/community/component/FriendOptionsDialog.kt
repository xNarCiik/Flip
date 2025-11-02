package com.dms.flip.ui.community.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dms.flip.R
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewFriends

@Composable
fun FriendOptionsDialog(
    friend: Friend,
    onDismiss: () -> Unit,
    onViewProfile: () -> Unit,
    onInvite: () -> Unit,
    onRemove: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CommunityAvatar(
                    imageUrl = friend.avatarUrl,
                    fallbackText = friend.username.firstOrNull()?.uppercase() ?: "?",
                    size = 72.dp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = friend.username,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = friend.handle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(id = R.string.view_profile),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingContent = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onViewProfile)
                )

                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(id = R.string.friends_invite_to_pleasure),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingContent = {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onInvite)
                )

                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(id = R.string.remove_friend),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.PersonRemove,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onRemove)
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.dialog_close))
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@LightDarkPreview
@Composable
private fun FriendOptionsDialogPreview() {
    FlipTheme {
        FriendOptionsDialog(
            friend = previewFriends.first(),
            onDismiss = {},
            onViewProfile = {},
            onInvite = {},
            onRemove = {}
        )
    }
}
