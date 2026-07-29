package com.example.modular.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.modular.ExitTimerActivity
import com.example.modular.ModularApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.example.modular.ui.blocking.BlockingActivity

class ModularAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        startSessionMonitor()
    }
    
    private fun startSessionMonitor() {
        serviceScope.launch {
            val app = application as? ModularApp ?: return@launch
            val repository = app.modeRepository
            
            while (true) {
                try {
                    val session = repository.getSessionSync()
                    if (session != null && session.isRunning && session.endTimeMillis != null) {
                        if (System.currentTimeMillis() >= session.endTimeMillis) {
                            // Session expired naturally
                            repository.clearSession()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                kotlinx.coroutines.delay(10000) // Check every 10 seconds
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Check if we need to block this app
            checkAndBlockApp(packageName)
        }
    }


    private fun checkAndBlockApp(packageName: String) {
        val app = application as? ModularApp ?: return
        val repository = app.modeRepository

        serviceScope.launch {
            val session = repository.getSessionSync()
            if (session != null && session.isRunning && session.activeModeId != null) {
                // Ignore our own app and system UI
                if (packageName == applicationContext.packageName || 
                    packageName == "com.android.systemui" || 
                    packageName == "com.google.android.inputmethod.latin") { // Allow keyboard
                    return@launch
                }

                val explicitlyBlockedApps = repository.getAppsForModeSync(session.activeModeId)
                val isExplicitlyBlocked = explicitlyBlockedApps.any { it.packageName == packageName }

                // Apps that are ALWAYS blocked during an active mode to prevent easy uninstalls
                val preventUninstallApps = listOf(
                    "com.android.settings",
                    "com.android.vending", // Google Play Store
                    "com.google.android.packageinstaller" // System Installer
                )

                if (isExplicitlyBlocked || preventUninstallApps.contains(packageName)) {
                    launch(Dispatchers.Main) {
                        showOverlay(packageName)
                    }
                }
            }
        }
    }
    
    private fun showOverlay(packageName: String) {
        val intent = Intent(this, BlockingActivity::class.java).apply {
            putExtra("blocked_package", packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        // Do nothing
    }
}
