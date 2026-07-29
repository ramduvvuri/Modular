package com.example.modular.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.modular.ModularApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ModularAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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

                val allowedApps = repository.getAppsForModeSync(session.activeModeId)
                val isAllowed = allowedApps.any { it.packageName == packageName }

                // Define emergency apps that are always allowed
                val emergencyApps = listOf(
                    "com.android.dialer", "com.google.android.dialer", "com.samsung.android.dialer", // Phone
                    "com.android.mms", "com.google.android.apps.messaging", "com.samsung.android.messaging", // Messages
                    "com.android.deskclock", "com.google.android.deskclock", "com.samsung.android.app.clockpack", // Clock
                    "com.android.camera2", "com.google.android.GoogleCamera", "com.sec.android.app.camera" // Camera
                )

                if (!isAllowed && !emergencyApps.contains(packageName)) {
                    val intent = Intent(applicationContext, BlockingActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("BLOCKED_PACKAGE", packageName)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    override fun onInterrupt() {
        // Do nothing
    }
}
