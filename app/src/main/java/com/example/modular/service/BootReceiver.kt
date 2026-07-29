package com.example.modular.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.modular.ModularApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = context.applicationContext as ModularApp
            // We just need to initialize the app, the Room database will keep the session state.
            // If the session is active, the AccessibilityService (if enabled) will automatically
            // resume blocking when apps are opened because it reads from the repository on every window change.
            
            // Optionally, we could launch a foreground service to show an ongoing notification
            // that a mode is active, but that's beyond the MVP scope.
            
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                val session = app.modeRepository.getSessionSync()
                if (session != null && session.isRunning) {
                    // Log or handle boot resumption
                }
            }
        }
    }
}
