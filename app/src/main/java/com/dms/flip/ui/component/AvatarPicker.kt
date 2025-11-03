package com.dms.flip.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
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
    fallbackText: String = "",
    size: Dp = 120.dp,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(
                color = if (avatarUrl == null) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Si une URL est fournie, afficher l'image
            !avatarUrl.isNullOrBlank() -> {
                GlideImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(size),
                    contentScale = ContentScale.Crop
                )
                
                // Overlay avec icône caméra
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(size * 0.3f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Changer l'avatar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(size * 0.18f)
                    )
                }
            }
            
            // Si un texte fallback existe (initiale)
            fallbackText.isNotBlank() -> {
                Text(
                    text = fallbackText.uppercase(),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                // Overlay avec icône caméra
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(size * 0.3f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Ajouter un avatar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(size * 0.18f)
                    )
                }
            }
            
            // Sinon, afficher l'icône par défaut
            else -> {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar par défaut",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(size * 0.5f)
                )
                
                // Overlay avec icône "ajouter"
                Box(
                    modifier = Modifier
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
    }
}

@LightDarkPreview
@Composable
private fun AvatarPickerEmptyPreview() {
    FlipTheme {
        Surface {
            AvatarPicker(
                avatarUrl = null,
                fallbackText = "",
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
                fallbackText = "M",
                size = 120.dp
            )
        }
    }
}
