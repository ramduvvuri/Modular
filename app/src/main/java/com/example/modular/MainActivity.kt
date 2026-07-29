package com.example.modular

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.modular.ui.theme.ModularTheme
import com.example.modular.ui.permissions.PermissionsScreen
import com.example.modular.ui.permissions.isAccessibilityServiceEnabled
import com.example.modular.ui.permissions.isOverlayPermissionGranted
import com.example.modular.ui.permissions.isNotificationListenerGranted

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ModularApp
        
        setContent {
            ModularTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ModularAppNavHost(app = app)
                }
            }
        }
    }
}

@Composable
fun ModularAppNavHost(app: ModularApp) {
    val navController = rememberNavController()
    
    val session by app.modeRepository.getSession().collectAsState(initial = null)
    
    LaunchedEffect(session) {
        if (session != null && session!!.isRunning) {
            navController.navigate("active_mode/${session!!.activeModeId}") {
                popUpTo(0) // Clear back stack
            }
        } else {
            // Wait, we don't want to constantly navigate to home if we are creating a mode
            if (navController.currentDestination?.route?.startsWith("active_mode") == true) {
                 navController.navigate("home") {
                    popUpTo(0)
                 }
            }
        }
    }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val hasPermissions = remember {
        isAccessibilityServiceEnabled(context) && isOverlayPermissionGranted(context) && isNotificationListenerGranted(context)
    }

    val startDest = when {
        !hasPermissions -> "permissions"
        session?.isRunning == true -> "active_mode/${session?.activeModeId}"
        else -> "home"
    }

    NavHost(navController = navController, startDestination = startDest) {
        composable("permissions") {
            PermissionsScreen(
                onPermissionsGranted = {
                    navController.navigate("home") {
                        popUpTo(0)
                    }
                }
            )
        }
        composable("inbox") {
            com.example.modular.ui.mode.NotificationInboxScreen(
                repository = app.modeRepository,
                onBack = { navController.popBackStack() }
            )
        }
        composable("home") {
            val viewModel: com.example.modular.ui.home.HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.modular.ui.home.HomeViewModelFactory(app.modeRepository)
            )
            com.example.modular.ui.home.HomeScreen(
                viewModel = viewModel,
                onCreateModeClick = { navController.navigate("create_mode") },
                onModeClick = { modeId -> navController.navigate("mode/$modeId") }
            )
        }
        composable(
            route = "create_mode?modeId={modeId}",
            arguments = listOf(navArgument("modeId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null 
            })
        ) { backStackEntry ->
            val modeIdStr = backStackEntry.arguments?.getString("modeId")
            val modeId = modeIdStr?.toLongOrNull()
            
            val viewModel: com.example.modular.ui.mode.CreateModeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.modular.ui.mode.CreateModeViewModelFactory(modeId, app.modeRepository, app.appProvider)
            )
            com.example.modular.ui.mode.CreateModeScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("mode/{modeId}") { backStackEntry ->
            val modeIdStr = backStackEntry.arguments?.getString("modeId")
            if (modeIdStr != null) {
                val modeId = modeIdStr.toLong()
                val viewModel: com.example.modular.ui.mode.ModeDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.modular.ui.mode.ModeDetailViewModelFactory(modeId, app.modeRepository)
                )
                com.example.modular.ui.mode.ModeDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onModeStarted = { 
                        // The LaunchedEffect above should handle the navigation, but just in case
                    },
                    onEditMode = {
                        navController.navigate("create_mode?modeId=$modeId")
                    }
                )
            }
        }
        composable("active_mode/{modeId}") { backStackEntry ->
            val modeIdStr = backStackEntry.arguments?.getString("modeId")
            var mode by remember { mutableStateOf<com.example.modular.data.local.ModeEntity?>(null) }
            var totalAllowedApps by remember { mutableStateOf(0) }
            
            LaunchedEffect(modeIdStr) {
                if (modeIdStr != null) {
                    val id = modeIdStr.toLong()
                    mode = app.modeRepository.getModeById(id)
                    totalAllowedApps = app.modeRepository.getAppsForModeSync(id).size
                }
            }
            
            com.example.modular.ui.mode.ActiveModeScreen(
                mode = mode,
                session = session,
                totalAllowedApps = totalAllowedApps,
                onLeaveMode = {
                    val context = navController.context
                    val intent = android.content.Intent(context, ExitTimerActivity::class.java)
                    context.startActivity(intent)
                },
                onViewInbox = {
                    navController.navigate("inbox")
                }
            )
        }
    }
}
