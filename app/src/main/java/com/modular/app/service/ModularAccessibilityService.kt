package com.modular.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.modular.app.data.local.ModularDatabase
import com.modular.app.data.repository.ModeRepository
import com.modular.app.data.repository.SessionRepository
import com.modular.app.util.SystemApps
import kotlinx.coroutines.*

class ModularAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var sessionRepository: SessionRepository
    private lateinit var modeRepository: ModeRepository

    private var lastBlockedPackage: String? = null
    private var lastBlockTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        val db = ModularDatabase.getInstance(applicationContext)
        sessionRepository = SessionRepository(db.sessionDao())
        modeRepository = ModeRepository(db.modeDao())
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        serviceScope.launch {
            checkAndEnforceBlocking(packageName)
        }
    }

    private suspend fun checkAndEnforceBlocking(packageName: String) {
        // 1. Never block emergency apps or Modular itself
        if (SystemApps.isEmergencyApp(applicationContext, packageName)) return

        // 2. Fetch active focus session
        val activeSession = sessionRepository.getActiveSessionSync() ?: return
        if (!activeSession.isRunning) return

        // 3. Fetch allowed apps for the active mode
        val activeModeWithApps = modeRepository.getModeByIdSync(activeSession.modeId) ?: return
        val allowedPackageNames = activeModeWithApps.allowedApps.map { it.packageName }.toSet()

        // 4. If current app is allowed, pass through
        if (allowedPackageNames.contains(packageName)) return

        // 5. App is BLOCKED! Launch full-screen blocking overlay
        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && (now - lastBlockTime) < 800) {
            // Avoid duplicate launch spam within 800ms
            return
        }
        lastBlockedPackage = packageName
        lastBlockTime = now

        val blockingIntent = Intent(applicationContext, BlockingOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
            putExtra(BlockingOverlayActivity.EXTRA_BLOCKED_PACKAGE, packageName)
            putExtra(BlockingOverlayActivity.EXTRA_MODE_NAME, activeModeWithApps.mode.name)
            putExtra(BlockingOverlayActivity.EXTRA_MODE_ID, activeModeWithApps.mode.id)
        }
        startActivity(blockingIntent)
    }

    override fun onInterrupt() {
        Log.w("ModularAccessibility", "Service interrupted")
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
