package com.example.modular.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.modular.ExitTimerActivity
import com.example.modular.ModularApp
import com.example.modular.ui.blocking.UninstallTimerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
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
                        } else if (session.isPaused && session.pauseEndTimeMillis != null) {
                            if (System.currentTimeMillis() >= session.pauseEndTimeMillis) {
                                // Pause expired! Resume blocking.
                                repository.updateSession(session.copy(
                                    isPaused = false,
                                    pauseEndTimeMillis = null
                                ))
                            }
                        }
                    } else {
                        // NO session running! Check if it's Night Mode time (21:00 to 07:00)
                        val calendar = java.util.Calendar.getInstance()
                        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                        
                        if (hour >= 21 || hour < 7) {
                            // Find the Night Mode preset
                            val allModes = repository.getAllModes().first()
                            val nightMode = allModes.find { it.name.equals("Night Mode", ignoreCase = true) }
                            
                            if (nightMode != null) {
                                // Calculate next 7 AM
                                val targetCalendar = java.util.Calendar.getInstance().apply {
                                    set(java.util.Calendar.HOUR_OF_DAY, 7)
                                    set(java.util.Calendar.MINUTE, 0)
                                    set(java.util.Calendar.SECOND, 0)
                                    if (hour >= 21) {
                                        add(java.util.Calendar.DAY_OF_MONTH, 1) // 7 AM tomorrow
                                    }
                                }
                                
                                // Auto-start the session!
                                repository.updateSession(com.example.modular.data.local.SessionEntity(
                                    activeModeId = nightMode.id,
                                    startTime = System.currentTimeMillis(),
                                    endTimeMillis = targetCalendar.timeInMillis,
                                    isRunning = true
                                ))
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

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            val packageName = event.packageName?.toString() ?: return
            val className = event.className?.toString() ?: ""
            
            // Ignore system notifications and toasts to prevent false-positive blocks
            if (className.contains("Notification") || className.contains("Toast") || className.contains("SystemUI")) {
                return
            }
            
            // Check for uninstall attempt
            if (checkForUninstallAttempt(packageName, event)) {
                return
            }
            
            // Check if we need to block this app
            checkAndBlockApp(packageName)
        }
    }

    private fun checkForUninstallAttempt(packageName: String, event: AccessibilityEvent): Boolean {
        // Only inspect likely packages to save performance
        val inspectPackages = listOf(
            "com.android.settings",
            "com.google.android.packageinstaller",
            "com.samsung.android.packageinstaller",
            "com.miui.securitycenter",
            "com.android.launcher",
            "com.sec.android.app.launcher",
            "com.google.android.apps.nexuslauncher"
        )
        
        if (!inspectPackages.contains(packageName)) return false
        
        val node = event.source ?: rootInActiveWindow ?: return false
        val texts = mutableListOf<String>()
        extractText(node, texts)
        val screenText = texts.joinToString(" ").lowercase()
        
        // Very basic heuristic to catch the uninstall dialog or app info page for Modular
        if (screenText.contains("uninstall") && screenText.contains("modular")) {
            // Check if unlocked
            val prefs = getSharedPreferences("modular_prefs", Context.MODE_PRIVATE)
            val unlockUntil = prefs.getLong("uninstall_unlock_until", 0L)
            
            if (System.currentTimeMillis() < unlockUntil) {
                // User is in the 5-minute unlock window, allow uninstall
                return false
            }
            
            // Trigger 10-minute timer
            val intent = Intent(this, UninstallTimerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            return true
        }
        return false
    }

    private fun extractText(node: AccessibilityNodeInfo, textList: MutableList<String>) {
        if (node.text != null) {
            textList.add(node.text.toString())
        }
        if (node.contentDescription != null) {
            textList.add(node.contentDescription.toString())
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                extractText(child, textList)
                child.recycle()
            }
        }
    }


    private fun checkAndBlockApp(packageName: String) {
        val app = application as? ModularApp ?: return
        val repository = app.modeRepository

        serviceScope.launch {
            val session = repository.getSessionSync()
            if (session != null && session.isRunning && session.activeModeId != null) {
                
                // If the session is actively on a break, allow all apps
                if (session.isPaused) {
                    return@launch
                }
                
                // Ignore our own app and system UI
                if (packageName == applicationContext.packageName || 
                    packageName == "com.android.systemui" || 
                    packageName == "com.google.android.inputmethod.latin") { // Allow keyboard
                    return@launch
                }

                val explicitlyBlockedApps = repository.getAppsForModeSync(session.activeModeId)
                val isExplicitlyBlocked = explicitlyBlockedApps.any { it.packageName == packageName }

                if (isExplicitlyBlocked) {
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
