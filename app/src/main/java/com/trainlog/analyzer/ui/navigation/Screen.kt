package com.trainlog.analyzer.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Lab : Screen("lab")
    object ImportLog : Screen("import")
    object Compare : Screen("compare")
    object Form : Screen("form?runId={runId}") {
        fun createRoute(runId: Long? = null): String =
            if (runId != null) "form?runId=$runId" else "form"
    }
    object Detail : Screen("detail/{runId}") {
        fun createRoute(runId: Long) = "detail/$runId"
    }
}
