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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CommunityAvatar(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    fallbackText: String,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    size: Dp = 48.dp,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 2.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier
                        .border(
                            width = borderWidth,
                            color = borderColor.copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                        .padding(borderWidth)
                } else {
                    Modifier
                }
            )
            .clip(CircleShape)
            .shadow(4.dp, CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            GlideImage(
                model = imageUrl,
                contentDescription = fallbackText,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = fallbackText.ifBlank { "?" },
                style = textStyle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}