package com.modular.app.data.model

import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isEmergency: Boolean = false,
    val isAllowed: Boolean = false
)
