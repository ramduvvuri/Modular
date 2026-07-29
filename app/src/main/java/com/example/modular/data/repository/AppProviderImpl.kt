package com.example.modular.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.modular.domain.model.AppInfo
import com.example.modular.domain.repository.AppProvider

class AppProviderImpl(private val context: Context) : AppProvider {

    override fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        
        return resolveInfos.mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val appName = resolveInfo.loadLabel(pm).toString()
            val icon = resolveInfo.loadIcon(pm)
            val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            
            // Exclude our own app
            if (packageName == context.packageName) {
                null
            } else {
                AppInfo(
                    packageName = packageName,
                    appName = appName,
                    icon = icon,
                    isSystem = isSystem
                )
            }
        }.sortedBy { it.appName.lowercase() }
    }
}
