package com.dms.flip.ui.navigation

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.AnimatedNavHost
import com.dms.flip.domain.model.RootNavigationState
import com.dms.flip.ui.community.CommunityNavHost
import com.dms.flip.ui.dailyflip.DailyFlipScreen
import com.dms.flip.ui.dailyflip.DailyFlipViewModel
import com.dms.flip.ui.history.HistoryScreen
import com.dms.flip.ui.history.HistoryViewModel
import com.dms.flip.ui.login.LoginScreen
import com.dms.flip.ui.onboarding.OnboardingScreen
import com.dms.flip.ui.settings.SettingsScreen
import com.dms.flip.ui.settings.SettingsViewModel
import com.dms.flip.ui.settings.manage.ManagePleasuresScreen
import com.dms.flip.ui.settings.manage.ManagePleasuresViewModel
import com.dms.flip.ui.settings.statistics.StatisticsScreen
import com.dms.flip.ui.settings.statistics.StatisticsViewModel
import kotlinx.serialization.Serializable

@Serializable
object RootRoute

@Serializable
object DailyPleasureRoute

@Serializable
object WeeklyRoute

@Serializable
object CommunityRoute

@Serializable
object SettingsRoute

@Serializable
object ManagePleasuresRoute

@Serializable
object StatisticsRoute

private const val navigationAnimationDuration = 900

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    rootNavigationState: RootNavigationState
) {
    val modifierWithPaddingValues = Modifier.padding(paddingValues)

    val navigateSingleTop: (Any) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.id)
            {
                saveState = true
                inclusive = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    AnimatedNavHost(
        navController = navController,
        startDestination = RootRoute
    ) {
        composable<RootRoute> {
            when (rootNavigationState) {
                RootNavigationState.NotAuthenticated -> {
                    LoginScreen(
                        navigateToTerms = {},
                        navigateToPolicy = {}
                    )// TODO
                }

                RootNavigationState.AuthenticatedButNotOnboarded -> {
                    OnboardingScreen(modifier = modifierWithPaddingValues)
                }

                RootNavigationState.AuthenticatedAndOnboarded -> {
                    LaunchedEffect(Unit) {
                        navigateSingleTop(DailyPleasureRoute)
                    }
                }

                else -> Unit
            }
        }

        composable<DailyPleasureRoute>(
            enterTransition = {
                fadeIn(animationSpec = tween(250)) +
                        slideInHorizontally(
                            animationSpec = tween(300),
                            initialOffsetX = { it / 3 }
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200)) +
                        slideOutHorizontally(
                            animationSpec = tween(250),
                            targetOffsetX = { -it / 4 }
                        )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250)) +
                        slideInHorizontally(
                            animationSpec = tween(300),
                            initialOffsetX = { -it / 3 }
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) +
                        slideOutHorizontally(
                            animationSpec = tween(250),
                            targetOffsetX = { it / 4 }
                        )
            }
        ) {
            val viewModel: DailyFlipViewModel = hiltViewModel()
            val dailyPleasureUiState by viewModel.uiState.collectAsState()

            DailyFlipScreen(
                modifier = modifierWithPaddingValues,
                uiState = dailyPleasureUiState,
                onEvent = viewModel::onEvent,
                navigateToManagePleasures = { navController.navigate(ManagePleasuresRoute) },
                navigateToSettings = { navController.navigate(SettingsRoute) }
            )
        }

        composable<WeeklyRoute>(
            enterTransition = {
                fadeIn(animationSpec = tween(250)) +
                        slideInHorizontally(
                            animationSpec = tween(300),
                            initialOffsetX = { it / 3 }
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200)) +
                        slideOutHorizontally(
                            animationSpec = tween(250),
                            targetOffsetX = { -it / 4 }
                        )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250)) +
                        slideInHorizontally(
                            animationSpec = tween(300),
                            initialOffsetX = { -it / 3 }
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) +
                        slideOutHorizontally(
                            animationSpec = tween(250),
                            targetOffsetX = { it / 4 }
                        )
            }
        ) {
            val viewModel: HistoryViewModel = hiltViewModel()
            val historyState by viewModel.uiState.collectAsState()

            HistoryScreen(
                modifier = modifierWithPaddingValues,
                uiState = historyState,
                onEvent = viewModel::onEvent,
                navigateToDailyFlip = { navigateSingleTop(DailyPleasureRoute) }
            )
        }

        composable<CommunityRoute>(
            enterTransition = {
                fadeIn(animationSpec = tween(250)) +
                        slideInHorizontally(
                            animationSpec = tween(300),
                            initialOffsetX = { it / 3 }
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200)) +
                        slideOutHorizontally(
                            animationSpec = tween(250),
                            targetOffsetX = { -it / 4 }
                        )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250)) +
                        slideInHorizontally(
                            animationSpec = tween(300),
                            initialOffsetX = { -it / 3 }
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) +
                        slideOutHorizontally(
                            animationSpec = tween(250),
                            targetOffsetX = { it / 4 }
                        )
            }
        ) {
            CommunityNavHost(modifier = modifierWithPaddingValues)
        }

        composable<SettingsRoute>(
            enterTransition = {
                fadeIn(animationSpec = tween(250)) +
                        slideInHorizontally(
                            animationSpec = tween(300),
                            initialOffsetX = { it / 3 }
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200)) +
                        slideOutHorizontally(
                            animationSpec = tween(250),
                            targetOffsetX = { -it / 4 }
                        )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250)) +
                        slideInHorizontally(
                            animationSpec = tween(300),
                            initialOffsetX = { -it / 3 }
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) +
                        slideOutHorizontally(
                            animationSpec = tween(250),
                            targetOffsetX = { it / 4 }
                        )
            }
        ) {
            val viewModel: SettingsViewModel = hiltViewModel()
            val settingsState by viewModel.uiState.collectAsState()

            SettingsScreen(
                modifier = modifierWithPaddingValues,
                uiState = settingsState,
                onEvent = viewModel::onEvent,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToManagePleasures = { navController.navigate(ManagePleasuresRoute) },
                onNavigateToStatistics = { navController.navigate(StatisticsRoute) }
            )
        }

        composable<ManagePleasuresRoute>(
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(
                        durationMillis = navigationAnimationDuration,
                        easing = LinearOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(
                        durationMillis = navigationAnimationDuration,
                        easing = LinearOutSlowInEasing
                    )
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(
                        durationMillis = navigationAnimationDuration,
                        easing = LinearOutSlowInEasing
                    )
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(
                        durationMillis = navigationAnimationDuration,
                        easing = LinearOutSlowInEasing
                    )
                )
            }
        ) {
            val viewModel: ManagePleasuresViewModel = hiltViewModel()
            val managePleasuresUiState by viewModel.uiState.collectAsState()

            ManagePleasuresScreen(
                modifier = modifierWithPaddingValues,
                uiState = managePleasuresUiState,
                onEvent = viewModel::onEvent,
                navigateBack = navController::popBackStack
            )
        }

        composable<StatisticsRoute> {
            val viewModel: StatisticsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            StatisticsScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateBack = navController::popBackStack
            )
        }
    }
}
