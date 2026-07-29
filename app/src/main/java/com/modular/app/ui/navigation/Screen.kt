package com.modular.app.ui.navigation

sealed class Screen(val route: String) {
    object PermissionSetup : Screen("permission_setup")
    object Home : Screen("home")
    object ModeEditor : Screen("mode_editor/{modeId}") {
        fun createRoute(modeId: Long) = "mode_editor/$modeId"
    }
    object ActiveSession : Screen("active_session")
}
