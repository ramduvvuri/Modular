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
import kotlinx.coroutines.Job
import com.example.modular.data.local.SessionEntity
import com.example.modular.data.local.AllowedAppEntity

class ModularNotificationService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentSession: SessionEntity? = null
    private var explicitlyBlockedApps: List<AllowedAppEntity> = emptyList()
    private var appsCollectionJob: Job? = null

    override fun onListenerConnected() {
        super.onListenerConnected()
        val app = application as? ModularApp ?: return
        
        serviceScope.launch {
            app.modeRepository.getSession().collect { session ->
                currentSession = session
                appsCollectionJob?.cancel()
                if (session != null && session.activeModeId != null) {
                    appsCollectionJob = launch {
                        app.modeRepository.getAppsForMode(session.activeModeId).collect { apps ->
                            explicitlyBlockedApps = apps
                        }
                    }
                } else {
                    explicitlyBlockedApps = emptyList()
                }
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        
        // Ignore system notifications
        if (packageName == "android" || packageName == "com.android.systemui") return

        val session = currentSession
        if (session != null && session.isRunning && session.activeModeId != null && !session.isPaused) {
            val isExplicitlyBlocked = explicitlyBlockedApps.any { it.packageName == packageName }

            if (isExplicitlyBlocked) {
                // Extract text
                val extras = sbn.notification.extras
                val title = extras.getString(Notification.EXTRA_TITLE) ?: "New Message"
                
                var text = ""
                // Check for MessagingStyle (like WhatsApp)
                val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
                if (messages != null && messages.isNotEmpty()) {
                    val lastMessage = messages.last()
                    if (lastMessage is android.app.Notification.MessagingStyle.Message) {
                        text = lastMessage.text?.toString() ?: ""
                    } else if (lastMessage is android.os.Bundle) {
                        text = lastMessage.getCharSequence("text")?.toString() ?: ""
                    }
                }
                
                // Fallbacks
                if (text.isBlank()) {
                    text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() 
                        ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() 
                        ?: ""
                }
                
                // Try to get app name
                val pm = packageManager
                val appName = try {
                    val ai = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(ai).toString()
                } catch (e: Exception) {
                    packageName
                }

                // Save to Database
                val app = application as? ModularApp ?: return
                serviceScope.launch {
                    app.modeRepository.insertNotification(
                        NotificationEntity(
                            packageName = packageName,
                            appName = appName,
                            title = title,
                            text = text,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }

                // Cancel the notification so it doesn't bother the user
                cancelNotification(sbn.key)
            }
        }
    }
}
