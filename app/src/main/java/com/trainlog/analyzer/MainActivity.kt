package com.trainlog.analyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.trainlog.analyzer.ui.compare.CompareScreen
import com.trainlog.analyzer.ui.detail.DetailScreen
import com.trainlog.analyzer.ui.form.FormScreen
import com.trainlog.analyzer.ui.home.HomeScreen
import com.trainlog.analyzer.ui.importlog.ImportLogScreen
import com.trainlog.analyzer.ui.lab.LabScreen
import com.trainlog.analyzer.ui.navigation.Screen
import com.trainlog.analyzer.ui.navigation.defaultEnter
import com.trainlog.analyzer.ui.navigation.defaultExit
import com.trainlog.analyzer.ui.navigation.defaultPopEnter
import com.trainlog.analyzer.ui.navigation.defaultPopExit
import com.trainlog.analyzer.ui.navigation.homeFromSplashEnter
import com.trainlog.analyzer.ui.navigation.modalEnter
import com.trainlog.analyzer.ui.navigation.modalExit
import com.trainlog.analyzer.ui.navigation.modalPopEnter
import com.trainlog.analyzer.ui.navigation.modalPopExit
import com.trainlog.analyzer.ui.navigation.slideEnter
import com.trainlog.analyzer.ui.navigation.slideExit
import com.trainlog.analyzer.ui.navigation.slidePopEnter
import com.trainlog.analyzer.ui.navigation.slidePopExit
import com.trainlog.analyzer.ui.navigation.splashExit
import com.trainlog.analyzer.ui.navigation.tabEnter
import com.trainlog.analyzer.ui.navigation.tabExit
import com.trainlog.analyzer.ui.splash.SplashScreen
import com.trainlog.analyzer.ui.theme.TrainLogTheme
import com.trainlog.analyzer.viewmodel.TrainingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrainLogTheme {
                TrainLogApp()
            }
        }
    }
}

@Composable
fun TrainLogApp() {
    val navController = rememberNavController()
    val viewModel: TrainingViewModel = viewModel()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: Screen.Splash.route
    val showBottom = route == Screen.Home.route || route == Screen.Lab.route
    val showFab = route == Screen.Home.route

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottom,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(280)
                ) + fadeIn(tween(280)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(220)
                ) + fadeOut(tween(220))
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    NavigationBarItem(
                        selected = route == Screen.Home.route,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Runs") },
                        label = { Text("Runs") }
                    )
                    NavigationBarItem(
                        selected = route == Screen.Lab.route,
                        onClick = {
                            navController.navigate(Screen.Lab.route) { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Default.Build, contentDescription = "Lab") },
                        label = { Text("Lab") }
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = fadeIn(tween(250)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(280)
                ),
                exit = fadeOut(tween(180)) + slideOutVertically(
                    targetOffsetY = { it / 2 },
                    animationSpec = tween(200)
                )
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.Form.createRoute()) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Run baru")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(padding),
            enterTransition = defaultEnter,
            exitTransition = defaultExit,
            popEnterTransition = defaultPopEnter,
            popExitTransition = defaultPopExit
        ) {
            // Splash → soft scale fade out
            composable(
                route = Screen.Splash.route,
                exitTransition = { splashExit() },
                popExitTransition = { splashExit() }
            ) {
                SplashScreen {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }

            // Home: from splash special enter; tab fade with Lab
            composable(
                route = Screen.Home.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Screen.Splash.route -> homeFromSplashEnter()
                        Screen.Lab.route -> tabEnter()
                        else -> slideEnter()
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Screen.Lab.route -> tabExit()
                        else -> slideExit()
                    }
                },
                popEnterTransition = {
                    when (initialState.destination.route) {
                        Screen.Lab.route -> tabEnter()
                        else -> slidePopEnter()
                    }
                },
                popExitTransition = { slidePopExit() }
            ) {
                HomeScreen(
                    viewModel = viewModel,
                    onAddClick = { navController.navigate(Screen.Form.createRoute()) },
                    onRunClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                    onLabClick = { navController.navigate(Screen.Lab.route) },
                    onImportClick = { navController.navigate(Screen.ImportLog.route) },
                    onCompareClick = { navController.navigate(Screen.Compare.route) }
                )
            }

            composable(
                route = Screen.Lab.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Screen.Home.route -> tabEnter()
                        else -> slideEnter()
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Screen.Home.route -> tabExit()
                        else -> slideExit()
                    }
                },
                popEnterTransition = { tabEnter() },
                popExitTransition = { tabExit() }
            ) {
                LabScreen()
            }

            // Modal-style screens (slide up)
            composable(
                route = Screen.ImportLog.route,
                enterTransition = { modalEnter() },
                exitTransition = { modalExit() },
                popEnterTransition = { modalPopEnter() },
                popExitTransition = { modalPopExit() }
            ) {
                ImportLogScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { id ->
                        navController.navigate(Screen.Detail.createRoute(id)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            composable(
                route = Screen.Compare.route,
                enterTransition = { modalEnter() },
                exitTransition = { modalExit() },
                popEnterTransition = { modalPopEnter() },
                popExitTransition = { modalPopExit() }
            ) {
                CompareScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }

            composable(
                route = "form?runId={runId}",
                arguments = listOf(
                    navArgument("runId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
                enterTransition = { modalEnter() },
                exitTransition = { modalExit() },
                popEnterTransition = { modalPopEnter() },
                popExitTransition = { modalPopExit() }
            ) { entry ->
                val runId = entry.arguments?.getString("runId")?.toLongOrNull()
                FormScreen(
                    viewModel = viewModel,
                    runId = runId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(
                route = "form",
                enterTransition = { modalEnter() },
                exitTransition = { modalExit() },
                popEnterTransition = { modalPopEnter() },
                popExitTransition = { modalPopExit() }
            ) {
                FormScreen(
                    viewModel = viewModel,
                    runId = null,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            // Detail: horizontal slide
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("runId") { type = NavType.LongType }),
                enterTransition = { slideEnter() },
                exitTransition = { slideExit() },
                popEnterTransition = { slidePopEnter() },
                popExitTransition = { slidePopExit() }
            ) { entry ->
                val runId = entry.arguments?.getLong("runId") ?: return@composable
                DetailScreen(
                    viewModel = viewModel,
                    runId = runId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Screen.Form.createRoute(id)) }
                )
            }
        }
    }
}
