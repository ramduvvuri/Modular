package com.modular.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.provider.Telephony

object SystemApps {

    private val KNOWN_EMERGENCY_PACKAGES = setOf(
        // Phone / Dialer
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.android.dialer",
        "com.oneplus.dialer",
        "com.miui.securitycenter",
        "com.xiaomi.simactivate-service",

        // Messages / SMS
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.android.mms",
        "com.oneplus.mms",

        // Clock / Alarm
        "com.google.android.deskclock",
        "com.sec.android.app.clockpackage",
        "com.android.deskclock",
        "com.oneplus.deskclock",

        // Camera
        "com.google.android.GoogleCamera",
        "com.sec.android.app.camera",
        "com.android.camera",
        "com.android.camera2",
        "com.oneplus.camera"
    )

    /**
     * Emergency apps (Phone, Messages, Clock, Camera, System UI, launcher home, and Modular itself)
     * are NEVER blocked to guarantee safety and system stability.
     */
    fun isEmergencyApp(context: Context, packageName: String): Boolean {
        if (packageName.isBlank()) return true

        // 1. Modular itself is always allowed
        if (packageName == context.packageName) return true

        // 2. Android System UI & Keyguard are always allowed
        if (packageName == "com.android.systemui" || packageName == "android") return true

        // 3. Known hardcoded emergency package set
        if (KNOWN_EMERGENCY_PACKAGES.contains(packageName)) return true

        // 4. Dynamic default dialer & default SMS resolution
        val defaultDialer = Telephony.Sms.getDefaultSmsPackage(context)
        if (packageName == defaultDialer) return true

        val dialerIntent = Intent(Intent.ACTION_DIAL)
        val resolveDialer = context.packageManager.resolveActivity(dialerIntent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resolveDialer?.activityInfo?.packageName == packageName) return true

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val resolveCamera = context.packageManager.resolveActivity(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resolveCamera?.activityInfo?.packageName == packageName) return true

        return false
    }
}
