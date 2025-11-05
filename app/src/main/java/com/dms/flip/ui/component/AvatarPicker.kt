package com.dms.flip.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview

/**
 * Composant pour afficher et sélectionner un avatar
 * @param avatarUrl URL de l'avatar existant (nullable)
 * @param fallbackText Texte à afficher si pas d'avatar (initiale du username)
 * @param size Taille du composant
 * @param onClick Callback lors du clic pour changer l'avatar
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AvatarPicker(
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    size: Dp = 120.dp,
    onClick: () -> Unit = {}
) {
    // Avatar
    Box(
        modifier = modifier
            .size(size)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null) {
                CommunityAvatar(
                    imageUrl = avatarUrl,
                    fallbackText = "",
                    textStyle = MaterialTheme.typography.headlineLarge,
                    size = size
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar par défaut",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .align(Alignment.BottomEnd)
                .size(size * 0.3f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = "Ajouter un avatar",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size * 0.18f)
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun AvatarPickerEmptyPreview() {
    FlipTheme {
        Surface {
            AvatarPicker(
                avatarUrl = null,
                size = 120.dp
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun AvatarPickerWithInitialPreview() {
    FlipTheme {
        Surface {
            AvatarPicker(
                avatarUrl = null,
                size = 120.dp
            )
        }
    }
}
