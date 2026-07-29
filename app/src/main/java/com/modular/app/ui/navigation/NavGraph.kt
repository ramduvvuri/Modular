package com.modular.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.modular.app.ui.screens.active_session.ActiveSessionScreen
import com.modular.app.ui.screens.home.HomeScreen
import com.modular.app.ui.screens.mode_editor.ModeEditorScreen
import com.modular.app.ui.screens.permissions.PermissionSetupScreen
import com.modular.app.ui.viewmodel.HomeViewModel
import com.modular.app.ui.viewmodel.ModeEditorViewModel
import com.modular.app.ui.viewmodel.SessionViewModel
import com.modular.app.util.ServiceUtils

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    val homeViewModel: HomeViewModel = viewModel()
    val sessionViewModel: SessionViewModel = viewModel()
    val modeEditorViewModel: ModeEditorViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.PermissionSetup.route) {
            PermissionSetupScreen(
                onPermissionsGranted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.PermissionSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onCreateMode = {
                    navController.navigate(Screen.ModeEditor.createRoute(0L))
                },
                onEditMode = { modeId ->
                    navController.navigate(Screen.ModeEditor.createRoute(modeId))
                },
                onOpenActiveSession = {
                    navController.navigate(Screen.ActiveSession.route)
                }
            )
        }

        composable(
            route = Screen.ModeEditor.route,
            arguments = listOf(navArgument("modeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val modeId = backStackEntry.arguments?.getLong("modeId") ?: 0L
            ModeEditorScreen(
                modeId = modeId,
                viewModel = modeEditorViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ActiveSession.route) {
            ActiveSessionScreen(
                viewModel = sessionViewModel,
                onSessionEnded = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
