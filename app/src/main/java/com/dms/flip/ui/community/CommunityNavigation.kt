package com.dms.flip.ui.community

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.ui.community.screen.CommunityScreen
import com.dms.flip.ui.community.screen.FriendsListScreen
import com.dms.flip.ui.community.screen.InvitationsScreen
import com.dms.flip.ui.community.screen.PublicProfileScreen
import com.dms.flip.ui.community.screen.SearchFriendsScreen
import com.dms.flip.ui.navigation.navEnterTransition
import com.dms.flip.ui.navigation.navExitTransition

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
    navController: NavHostController = rememberNavController(),
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = CommunityRoute.Main.route,
        modifier = modifier
    ) {
        composable(
            CommunityRoute.Main.route,
            enterTransition = { navEnterTransition() },
            exitTransition = { navExitTransition() },
            popEnterTransition = { navEnterTransition() },
            popExitTransition = { navExitTransition() }
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

                        is CommunityEvent.OnViewProfile -> {
                            navController.navigate(
                                CommunityRoute.Profile.createRoute(event.profile.id)
                            )
                        }

                        else -> viewModel.onEvent(event)
                    }
                }
            )
        }

        composable(CommunityRoute.Invitations.route) {
            InvitationsScreen(
                uiState = uiState,
                onEvent = { event ->
                    when (event) {
                        is CommunityEvent.OnViewProfile -> {
                            navController.navigate(
                                CommunityRoute.Profile.createRoute(event.profile.id)
                            )
                        }

                        else -> viewModel.onEvent(event)
                    }
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(CommunityRoute.FriendsList.route) {
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

                        is CommunityEvent.OnViewProfile -> {
                            navController.navigate(
                                CommunityRoute.Profile.createRoute(event.profile.id)
                            )
                        }

                        else -> viewModel.onEvent(event)
                    }
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(CommunityRoute.Search.route) {
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

            if (userId.isNullOrBlank()) {
                LaunchedEffect(Unit) {
                    navController.navigateUp()
                }
                return@composable
            }

            var profile by remember { mutableStateOf<PublicProfile?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var error by remember { mutableStateOf<String?>(null) }

            // Charger le profil
            LaunchedEffect(userId) {
                isLoading = true
                error = null
                try {
                    profile = viewModel.getPublicProfileById(userId)
                    if (profile == null) {
                        error = "Profil introuvable"
                    }
                } catch (e: Exception) {
                    error = "Erreur lors du chargement du profil"
                } finally {
                    isLoading = false
                }
            }

            // TODO: Refacto to PublicProfileScreen
            when {
                isLoading -> {
                    // LoadingScreen()
                }

                error != null -> {
                    // ErrorScreen(message = error, onRetry = { navController.navigateUp() })
                }

                else -> {
                    profile?.let {
                        PublicProfileScreen(
                            profile = it,
                            isCurrentUser = it.id == uiState.currentUserId,
                            onAddFriend = {
                                viewModel.onEvent(CommunityEvent.OnAddUserFromSearch(userId))
                                navController.navigateUp()
                            },
                            onAcceptFriendRequest = {
                                viewModel.onEvent(
                                    CommunityEvent.OnAcceptFriendRequestFromProfile(userId)
                                )
                                navController.navigateUp()
                            },
                            onOptionsClick = {
                                viewModel.onEvent(CommunityEvent.OnRemoveFriendFromProfile(userId))
                                navController.navigateUp()
                            },
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }
                }
            }
        }
    }
}