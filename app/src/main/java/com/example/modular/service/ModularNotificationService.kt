package com.example.modular.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.modular.ModularApp
import com.example.modular.data.local.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ModularNotificationService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        
        // Ignore system notifications
        if (packageName == "android" || packageName == "com.android.systemui") return

        val app = application as? ModularApp ?: return
        val repository = app.modeRepository

        serviceScope.launch {
            val session = repository.getSessionSync()
            // If session is active and NOT paused
            if (session != null && session.isRunning && session.activeModeId != null && !session.isPaused) {
                val explicitlyBlockedApps = repository.getAppsForModeSync(session.activeModeId)
                val isExplicitlyBlocked = explicitlyBlockedApps.any { it.packageName == packageName }

                if (isExplicitlyBlocked) {
                    // Extract text
                    val extras = sbn.notification.extras
                    val title = extras.getString(Notification.EXTRA_TITLE) ?: "New Message"
                    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                    
                    // Try to get app name
                    val pm = packageManager
                    val appName = try {
                        val ai = pm.getApplicationInfo(packageName, 0)
                        pm.getApplicationLabel(ai).toString()
                    } catch (e: Exception) {
                        packageName
                    }

                    // Save to Database
                    repository.insertNotification(
                        NotificationEntity(
                            packageName = packageName,
                            appName = appName,
                            title = title,
                            text = text,
                            timestamp = System.currentTimeMillis()
                        )
                    )

                    // Cancel the notification so it doesn't bother the user
                    cancelNotification(sbn.key)
                }
            }
        }
    }
}
