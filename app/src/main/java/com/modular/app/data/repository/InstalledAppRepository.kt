package com.modular.app.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.modular.app.data.model.InstalledApp
import com.modular.app.util.SystemApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppRepository(private val context: Context) {

    suspend fun getInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        val appList = mutableListOf<InstalledApp>()

        for (resolveInfo in resolveInfos) {
            val packageName = resolveInfo.activityInfo.packageName
            // Skip Modular itself from the selectable list as it's always permitted
            if (packageName == context.packageName) continue

            val appName = resolveInfo.loadLabel(packageManager).toString()
            val icon = resolveInfo.loadIcon(packageManager)
            val isEmergency = SystemApps.isEmergencyApp(context, packageName)

            appList.add(
                InstalledApp(
                    packageName = packageName,
                    appName = appName,
                    icon = icon,
                    isEmergency = isEmergency,
                    isAllowed = isEmergency
                )
            )
        }

        appList.sortedBy { it.appName.lowercase() }
    }
}
