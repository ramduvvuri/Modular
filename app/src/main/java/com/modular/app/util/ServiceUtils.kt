package com.modular.app.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.modular.app.service.ModularAccessibilityService

object ServiceUtils {

    /**
     * Checks if Modular's Accessibility Service is currently enabled.
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        val expectedComponentName = "${context.packageName}/${ModularAccessibilityService::class.java.canonicalName}"
        val expectedShortComponentName = "${context.packageName}/${ModularAccessibilityService::class.java.simpleName}"

        for (service in enabledServices) {
            val serviceId = service.id
            if (serviceId == expectedComponentName || serviceId == expectedShortComponentName) {
                return true
            }
        }
        return false
    }

    /**
     * Checks if Display Over Other Apps (SYSTEM_ALERT_WINDOW) is granted.
     */
    fun isOverlayPermissionGranted(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Opens system accessibility settings.
     */
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Opens display over other apps settings for Modular.
     */
    fun openOverlaySettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Opens battery optimization settings page.
     */
    fun openBatteryOptimizationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
