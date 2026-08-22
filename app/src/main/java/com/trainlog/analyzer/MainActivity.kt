package com.trainlog.analyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

    Scaffold(
        bottomBar = {
            if (showBottom) {
                NavigationBar {
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
            if (route == Screen.Home.route) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.Form.createRoute()) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Run baru")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onAddClick = { navController.navigate(Screen.Form.createRoute()) },
                    onRunClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                    onLabClick = { navController.navigate(Screen.Lab.route) },
                    onImportClick = { navController.navigate(Screen.ImportLog.route) },
                    onCompareClick = { navController.navigate(Screen.Compare.route) }
                )
            }
            composable(Screen.Lab.route) { LabScreen() }
            composable(Screen.ImportLog.route) {
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
            composable(Screen.Compare.route) {
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
                )
            ) { entry ->
                val runId = entry.arguments?.getString("runId")?.toLongOrNull()
                FormScreen(
                    viewModel = viewModel,
                    runId = runId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable("form") {
                FormScreen(
                    viewModel = viewModel,
                    runId = null,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("runId") { type = NavType.LongType })
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
