package com.dms.flip.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun CommunityAvatar(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    fallbackText: String,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    size: Dp = 48.dp,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 1.dp
) {
    val context = LocalContext.current

    // ✅ Memorize fallback for stability
    val fallback = remember(fallbackText) { fallbackText.ifBlank { "?" } }

    // ✅ Coil image request with full caching
    val imageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier
                        .border(
                            width = borderWidth,
                            color = borderColor.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .padding(borderWidth)
                } else Modifier
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "Avatar of $fallback",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = fallback,
                style = textStyle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
