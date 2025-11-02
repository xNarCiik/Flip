package com.dms.flip.ui.community

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.AnimatedNavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberAnimatedNavController
import androidx.navigation.navArgument
import com.dms.flip.ui.community.screen.CommunityScreen
import com.dms.flip.ui.community.screen.FriendsListScreen
import com.dms.flip.ui.community.screen.InvitationsScreen
import com.dms.flip.ui.community.screen.PublicProfileScreen
import com.dms.flip.ui.community.screen.SearchFriendsScreen

sealed class CommunityRoute(val route: String) {
    data object Main : CommunityRoute("community_main")
    data object Invitations : CommunityRoute("community_invitations")
    data object FriendsList : CommunityRoute("community_friends_list")
    data object Search : CommunityRoute("community_search")
    data object Profile : CommunityRoute("community_profile/{userId}") {
        fun createRoute(userId: String) = "community_profile/$userId"
    }
}

@Composable
fun CommunityNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberAnimatedNavController(),
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AnimatedNavHost(
        navController = navController,
        startDestination = CommunityRoute.Main.route,
        modifier = modifier
    ) {
        composable(
            CommunityRoute.Main.route,
            enterTransition = {
                fadeIn(animationSpec = tween(200)) +
                        slideInHorizontally(
                            animationSpec = tween(250),
                            initialOffsetX = { it / 4 }
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(180)) +
                        slideOutHorizontally(
                            animationSpec = tween(220),
                            targetOffsetX = { -it / 4 }
                        )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200)) +
                        slideInHorizontally(
                            animationSpec = tween(250),
                            initialOffsetX = { -it / 4 }
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(180)) +
                        slideOutHorizontally(
                            animationSpec = tween(220),
                            targetOffsetX = { it / 4 }
                        )
            }
        ) {
            CommunityScreen(
                uiState = uiState,
                onEvent = { event ->
                    when (event) {
                        is CommunityEvent.OnSearchClicked -> {
                            navController.navigate(CommunityRoute.Search.route)
                        }

                        is CommunityEvent.OnFriendsListClicked -> {
                            navController.navigate(CommunityRoute.FriendsList.route)
                        }

                        is CommunityEvent.OnInvitationsClicked -> {
                            navController.navigate(CommunityRoute.Invitations.route)
                        }

                        is CommunityEvent.OnFriendClicked -> {
                            navController.navigate(
                                CommunityRoute.Profile.createRoute(event.friend.id)
                            )
                        }

                        else -> viewModel.onEvent(event)
                    }
                }
            )
        }

        composable(
            CommunityRoute.Invitations.route,
            enterTransition = {
                fadeIn(animationSpec = tween(200)) +
                        slideInHorizontally(
                            animationSpec = tween(250),
                            initialOffsetX = { it / 4 }
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(180)) +
                        slideOutHorizontally(
                            animationSpec = tween(220),
                            targetOffsetX = { -it / 4 }
                        )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200)) +
                        slideInHorizontally(
                            animationSpec = tween(250),
                            initialOffsetX = { -it / 4 }
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(180)) +
                        slideOutHorizontally(
                            animationSpec = tween(220),
                            targetOffsetX = { it / 4 }
                        )
            }
        ) {
            InvitationsScreen(
                uiState = uiState,
                onEvent = { event ->
                    when (event) {
                        is CommunityEvent.OnViewProfile -> {
                            navController.navigate(
                                CommunityRoute.Profile.createRoute(event.userId)
                            )
                        }

                        else -> viewModel.onEvent(event)
                    }
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(
            CommunityRoute.FriendsList.route,
            enterTransition = {
                fadeIn(animationSpec = tween(200)) +
                        slideInHorizontally(
                            animationSpec = tween(250),
                            initialOffsetX = { it / 4 }
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(180)) +
                        slideOutHorizontally(
                            animationSpec = tween(220),
                            targetOffsetX = { -it / 4 }
                        )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200)) +
                        slideInHorizontally(
                            animationSpec = tween(250),
                            initialOffsetX = { -it / 4 }
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(180)) +
                        slideOutHorizontally(
                            animationSpec = tween(220),
                            targetOffsetX = { it / 4 }
                        )
            }
        ) {
            FriendsListScreen(
                uiState = uiState,
                onEvent = { event ->
                    when (event) {
                        is CommunityEvent.OnSearchClicked -> {
                            navController.navigate(CommunityRoute.Search.route)
                        }

                        is CommunityEvent.OnInvitationsClicked -> {
                            navController.navigate(CommunityRoute.Invitations.route)
                        }

                        is CommunityEvent.OnFriendClicked -> {
                            navController.navigate(
                                CommunityRoute.Profile.createRoute(event.friend.id)
                            )
                        }

                        else -> viewModel.onEvent(event)
                    }
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(
            CommunityRoute.Search.route,
            enterTransition = {
                fadeIn(animationSpec = tween(200)) +
                        slideInHorizontally(
                            animationSpec = tween(250),
                            initialOffsetX = { it / 4 }
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(180)) +
                        slideOutHorizontally(
                            animationSpec = tween(220),
                            targetOffsetX = { -it / 4 }
                        )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200)) +
                        slideInHorizontally(
                            animationSpec = tween(250),
                            initialOffsetX = { -it / 4 }
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(180)) +
                        slideOutHorizontally(
                            animationSpec = tween(220),
                            targetOffsetX = { it / 4 }
                        )
            }
        ) {
            SearchFriendsScreen(
                uiState = uiState,
                onEvent = { event ->
                    when (event) {
                        is CommunityEvent.OnSearchResultClicked -> {
                            navController.navigate(
                                CommunityRoute.Profile.createRoute(event.result.id)
                            )
                        }

                        else -> viewModel.onEvent(event)
                    }
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(
            route = CommunityRoute.Profile.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")

            if (userId != null) {
                val profiles by viewModel.publicProfiles.collectAsState()
                LaunchedEffect(userId) { viewModel.loadPublicProfile(userId) }
                val profile = profiles[userId]

                if (profile != null) {
                    PublicProfileScreen(
                        profile = profile,
                        onAddFriend = {
                            viewModel.onEvent(CommunityEvent.OnAddUserFromSearch(userId))
                            navController.navigateUp()
                        },
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
            }
        }
    }
}
