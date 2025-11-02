package com.dms.flip.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview

data class TopBarIcon(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val badgeCount: Int? = null
)

@Composable
fun FlipTopBar(
    modifier: Modifier = Modifier,
    title: String,
    startTopBarIcon: TopBarIcon? = null,
    endTopBarIcon: TopBarIcon? = null,
    endTopBarIcons: List<TopBarIcon> = emptyList()
) {
    val combinedEndIcons = buildList {
        endTopBarIcon?.let { add(it) }
        addAll(endTopBarIcons)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            startTopBarIcon?.let { icon ->
                IconButton(
                    onClick = icon.onClick,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = icon.icon,
                        contentDescription = icon.contentDescription,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )

            if (combinedEndIcons.isNotEmpty()) {
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    combinedEndIcons.forEach { icon ->
                        IconButton(
                            onClick = icon.onClick,
                            modifier = Modifier.size(48.dp)
                        ) {
                            val badgeCount = icon.badgeCount ?: 0
                            if (badgeCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text(text = badgeCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = icon.icon,
                                        contentDescription = icon.contentDescription,
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = icon.icon,
                                    contentDescription = icon.contentDescription,
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ========== PREVIEW ==========
@LightDarkPreview
@Composable
private fun FlipTopBarPreview() {
    FlipTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            FlipTopBar(
                title = "Flip",
                startTopBarIcon = TopBarIcon(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "",
                    onClick = { }
                ),
                endTopBarIcons = listOf(
                    TopBarIcon(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "",
                        onClick = {}
                    )
                )
            )
        }
    }
}
