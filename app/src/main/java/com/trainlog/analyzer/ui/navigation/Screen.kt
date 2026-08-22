package com.trainlog.analyzer.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")

    object Form : Screen("form?runId={runId}") {
        fun createRoute(runId: Long? = null): String {
            return if (runId != null) "form?runId=$runId" else "form"
        }
    }

    object Detail : Screen("detail/{runId}") {
        fun createRoute(runId: Long) = "detail/$runId"
    }
}
