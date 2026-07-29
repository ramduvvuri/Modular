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
import android.view.WindowManager
import android.graphics.PixelFormat
import android.view.Gravity

class ModularAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var windowManager: WindowManager? = null
    private var currentOverlay: BlockingOverlayView? = null
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
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
                            launch(Dispatchers.Main) {
                                removeOverlay()
                            }
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
    
    private fun removeOverlay() {
        if (currentOverlay != null) {
            try {
                windowManager?.removeView(currentOverlay)
            } catch (e: Exception) {
                // Ignore if not attached
            }
            currentOverlay = null
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
                    
                    // If returning to our app or system ui, we might want to remove overlay
                    if (packageName == applicationContext.packageName) {
                        launch(Dispatchers.Main) { removeOverlay() }
                    }
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
                    launch(Dispatchers.Main) {
                        showOverlay(packageName)
                    }
                } else {
                    launch(Dispatchers.Main) {
                        removeOverlay()
                    }
                }
            } else {
                launch(Dispatchers.Main) {
                    removeOverlay()
                }
            }
        }
    }
    
    private fun showOverlay(packageName: String) {
        if (currentOverlay != null) return // Already showing
        
        val overlay = BlockingOverlayView(
            context = this,
            blockedPackage = packageName,
            onGoBack = {
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(homeIntent)
                removeOverlay()
            },
            onLeaveMode = {
                val leaveIntent = Intent(this, ExitTimerActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(leaveIntent)
                removeOverlay()
            }
        )
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or 
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }
        
        try {
            windowManager?.addView(overlay, params)
            currentOverlay = overlay
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInterrupt() {
        // Do nothing
    }
}
