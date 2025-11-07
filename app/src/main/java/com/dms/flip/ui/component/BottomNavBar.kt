package com.dms.flip.ui.component

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dms.flip.R
import com.dms.flip.ui.navigation.CommunityRootRoute
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

    val tabBarItems = listOf(
        TabBarItem(
            title = stringResource(R.string.history_title),
            icon = Icons.Outlined.CalendarMonth,
            route = WeeklyRoute
        ),
        TabBarItem(
            title = stringResource(R.string.my_flip_title),
            icon = Icons.Outlined.Home,
            route = DailyPleasureRoute
        ),
        TabBarItem(
            title = stringResource(R.string.community_title),
            icon = Icons.Outlined.Group,
            route = CommunityRootRoute,
            badgeCount = communityBadgeCount
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabBarItems.forEach { tab ->
                val isSelected =
                    currentDestination?.route == tab.route::class.qualifiedName
                NavBarItem(
                    isSelected = isSelected,
                    icon = tab.icon,
                    label = tab.title,
                    badgeCount = tab.badgeCount,
                    onClick = {
                        if (!isSelected) {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
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
    val activeColor = MaterialTheme.colorScheme.onSurfaceVariant

    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(250),
        label = "iconColor"
    )

    val labelColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(250),
        label = "labelColor"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f)
        else
            Color.Transparent,
        label = "bgColor"
    )

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(bgColor)
                .padding(horizontal = 18.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (badgeCount > 0) {
                    BadgedBox(badge = { Badge { Text("$badgeCount") } }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(22.dp),
                            tint = iconColor
                        )
                    }
                } else {
                    Icon(
                        imageVector = icon,
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
