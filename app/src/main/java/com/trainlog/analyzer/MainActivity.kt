package com.trainlog.analyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.trainlog.analyzer.ui.detail.DetailScreen
import com.trainlog.analyzer.ui.form.FormScreen
import com.trainlog.analyzer.ui.home.HomeScreen
import com.trainlog.analyzer.ui.navigation.Screen
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

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {
                    navController.navigate(Screen.Form.createRoute())
                },
                onRunClick = { id ->
                    navController.navigate(Screen.Detail.createRoute(id))
                }
            )
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
        ) { backStackEntry ->
            val runIdStr = backStackEntry.arguments?.getString("runId")
            val runId = runIdStr?.toLongOrNull()
            FormScreen(
                viewModel = viewModel,
                runId = runId,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

        // Also support plain "form" without query
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
        ) { backStackEntry ->
            val runId = backStackEntry.arguments?.getLong("runId") ?: return@composable
            DetailScreen(
                viewModel = viewModel,
                runId = runId,
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    navController.navigate(Screen.Form.createRoute(id))
                }
            )
        }
    }
}
