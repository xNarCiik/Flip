package com.dms.flip.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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

private val navRouteOrder = listOf(
    WeeklyRoute::class.qualifiedName,
    DailyPleasureRoute::class.qualifiedName,
    CommunityRoute::class.qualifiedName
).mapIndexed { index, route -> route to index }
    .toMap()

private enum class NavDirection { Forward, Backward }

private fun navDirection(
    initialRoute: String?,
    targetRoute: String?
): NavDirection? {
    val from = initialRoute?.let(navRouteOrder::get)
    val to = targetRoute?.let(navRouteOrder::get)

    if (from == null || to == null || from == to) {
        return null
    }

    return if (to > from) NavDirection.Forward else NavDirection.Backward
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.navEnterTransition(): EnterTransition {
    val direction = navDirection(
        initialRoute = initialState.destination.route,
        targetRoute = targetState.destination.route
    )

    val offset = when (direction) {
        NavDirection.Forward -> { fullWidth: Int -> fullWidth / 3 }
        NavDirection.Backward -> { fullWidth: Int -> -fullWidth / 3 }
        null -> { fullWidth: Int -> fullWidth / 4 }
    }

    return fadeIn(animationSpec = tween(250)) +
            slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = offset
            )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.navExitTransition(): ExitTransition {
    val direction = navDirection(
        initialRoute = initialState.destination.route,
        targetRoute = targetState.destination.route
    )

    val offset = when (direction) {
        NavDirection.Forward -> { fullWidth: Int -> -fullWidth / 4 }
        NavDirection.Backward -> { fullWidth: Int -> fullWidth / 4 }
        null -> { fullWidth: Int -> -fullWidth / 4 }
    }

    return fadeOut(animationSpec = tween(200)) +
            slideOutHorizontally(
                animationSpec = tween(250),
                targetOffsetX = offset
            )
}

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

    NavHost(
        navController = navController,
        startDestination = RootRoute,
        enterTransition = { navEnterTransition() },
        exitTransition = { navExitTransition() },
        popEnterTransition = { navEnterTransition() },
        popExitTransition = { navExitTransition() }
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

        composable<DailyPleasureRoute> {
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

        composable<WeeklyRoute> {
            val viewModel: HistoryViewModel = hiltViewModel()
            val historyState by viewModel.uiState.collectAsState()

            HistoryScreen(
                modifier = modifierWithPaddingValues,
                uiState = historyState,
                onEvent = viewModel::onEvent,
                navigateToDailyFlip = { navigateSingleTop(DailyPleasureRoute) }
            )
        }

        composable<CommunityRoute> {
            CommunityNavHost(modifier = modifierWithPaddingValues)
        }

        composable<SettingsRoute> {
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

        composable<ManagePleasuresRoute> {
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
