package com.dms.flip.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dms.flip.R
import com.dms.flip.ui.navigation.CommunityRoute
import com.dms.flip.ui.navigation.DailyPleasureRoute
import com.dms.flip.ui.navigation.WeeklyRoute
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview

@Immutable
data class TabBarItem(
    val title: String,
    val icon: ImageVector,
    val route: Any,
    val badgeCount: Int = 0
)

@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    communityBadgeCount: Int = 0
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val historyTitle = stringResource(R.string.history_title)
    val dailyTitle = stringResource(R.string.my_flip_title)
    val communityTitle = stringResource(R.string.community_title)

    val tabBarItems = remember(historyTitle, dailyTitle, communityTitle, communityBadgeCount) {
        listOf(
            TabBarItem(
                title = historyTitle,
                icon = Icons.Outlined.CalendarMonth,
                route = WeeklyRoute
            ),
            TabBarItem(
                title = dailyTitle,
                icon = Icons.Outlined.Home,
                route = DailyPleasureRoute
            ),
            TabBarItem(
                title = communityTitle,
                icon = Icons.Outlined.Group,
                route = CommunityRoute,
                badgeCount = communityBadgeCount
            )
        )
    }

    val currentRoute = currentDestination?.route
    val selectedIndex = remember(currentRoute, tabBarItems) {
        tabBarItems.indexOfFirst { item ->
            item.route::class.qualifiedName == currentRoute
        }.takeIf { it >= 0 } ?: 1
    }

    val navigateToItem = remember(navController) {
        { item: TabBarItem ->
            navController.navigate(item.route) {
                popUpTo(navController.graph.id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f))
        ) {
            // Top divider (visible)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    .align(Alignment.TopCenter)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabBarItems.forEachIndexed { index, tabBarItem ->
                    val isSelected = index == selectedIndex
                    NavBarItem(
                        isSelected = isSelected,
                        icon = tabBarItem.icon,
                        label = tabBarItem.title,
                        badgeCount = tabBarItem.badgeCount,
                        onClick = {
                            if (!isSelected) {
                                navigateToItem(tabBarItem)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavBarItem(
    isSelected: Boolean,
    icon: ImageVector,
    label: String,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val iconPainter = rememberVectorPainter(image = icon)
    val iconColor by animateColorAsState(
        targetValue = if (isSelected)
            Color.White
        else
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        animationSpec = tween(250),
        label = "iconColor"
    )

    val labelColor by animateColorAsState(
        targetValue = if (isSelected)
            Color.White
        else
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        animationSpec = tween(250),
        label = "labelColor"
    )

    val containerAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(250),
        label = "containerAlpha"
    )

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(
                    MaterialTheme.colorScheme.surfaceVariant
                        .copy(alpha = 0.25f * containerAlpha)
                )
                .padding(horizontal = 18.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (badgeCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge {
                                Text(text = badgeCount.toString())
                            }
                        }
                    ) {
                        Icon(
                            painter = iconPainter,
                            contentDescription = label,
                            modifier = Modifier.size(22.dp),
                            tint = iconColor
                        )
                    }
                } else {
                    Icon(
                        painter = iconPainter,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp),
                        tint = iconColor
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 0.sp
                    ),
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = labelColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
fun BottomNavBarPreview() {
    FlipTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            BottomNavBar(
                navController = rememberNavController(),
                communityBadgeCount = 3
            )
        }
    }
}
