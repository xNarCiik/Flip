package com.dms.flip.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dms.flip.domain.model.RootNavigationState
import com.dms.flip.domain.model.Theme
import com.dms.flip.ui.community.CommunityBadgeViewModel
import com.dms.flip.ui.component.BottomNavBar
import com.dms.flip.ui.component.LoadingState
import com.dms.flip.ui.navigation.DailyPleasureRoute
import com.dms.flip.ui.navigation.NavGraph
import com.dms.flip.ui.navigation.RootRoute
import com.dms.flip.ui.navigation.CommunityRoute
import com.dms.flip.ui.navigation.WeeklyRoute
import com.dms.flip.ui.settings.SettingsViewModel
import com.dms.flip.ui.theme.FlipTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            mainViewModel.rootNavigationState.value == RootNavigationState.Loading
        }

        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            val rootNavigationState by mainViewModel.rootNavigationState.collectAsStateWithLifecycle()

            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val useDarkTheme = when (settingsUiState.theme) {
                Theme.LIGHT -> false
                Theme.DARK -> true
                Theme.SYSTEM -> isSystemInDarkTheme()
            }
            MainActivityContent(
                useDarkTheme = useDarkTheme,
                rootNavigationState = rootNavigationState
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainActivityContent(useDarkTheme: Boolean, rootNavigationState: RootNavigationState) {
        FlipTheme(darkTheme = useDarkTheme) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                var contentVisible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    contentVisible = true
                }

                if (rootNavigationState == RootNavigationState.Loading) {
                    LoadingState(modifier = Modifier.fillMaxSize())
                    return@Surface
                }

                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(animationSpec = tween(durationMillis = 1000))
                ) {
                    val navController = rememberNavController()

                    LaunchedEffect(rootNavigationState) {
                        navController.navigate(RootRoute) {
                            popUpTo(navController.graph.id)
                            launchSingleTop = true
                        }
                    }
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val communityBadgeViewModel: CommunityBadgeViewModel = hiltViewModel()
                    val pendingRequestsCount by communityBadgeViewModel.pendingRequestsCount.collectAsStateWithLifecycle()

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            val bottomDestinations = remember {
                                setOf(
                                    DailyPleasureRoute::class.qualifiedName,
                                    WeeklyRoute::class.qualifiedName,
                                    CommunityRoute::class.qualifiedName
                                )
                            }
                            val visible = currentRoute in bottomDestinations

                            if (visible) {
                                BottomNavBar(
                                    navController = navController,
                                    communityBadgeCount = pendingRequestsCount
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavGraph(
                            navController = navController,
                            paddingValues = innerPadding,
                            rootNavigationState = rootNavigationState
                        )
                    }
                }
            }
        }
    }
}
