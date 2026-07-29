package com.modular.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.modular.app.ui.navigation.NavGraph
import com.modular.app.ui.navigation.Screen
import com.modular.app.ui.theme.ModularTheme
import com.modular.app.util.ServiceUtils

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ModularTheme {
                val navController = rememberNavController()

                val hasAccessibility = ServiceUtils.isAccessibilityServiceEnabled(this)
                val hasOverlay = ServiceUtils.isOverlayPermissionGranted(this)

                val navigateToExit = intent.getBooleanExtra("navigate_to_exit", false)

                val startDestination = when {
                    !hasAccessibility || !hasOverlay -> Screen.PermissionSetup.route
                    navigateToExit -> Screen.ActiveSession.route
                    else -> Screen.Home.route
                }

                NavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
