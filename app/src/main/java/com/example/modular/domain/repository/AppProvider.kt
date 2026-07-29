package com.example.modular.domain.repository

import com.example.modular.domain.model.AppInfo

interface AppProvider {
    fun getInstalledApps(): List<AppInfo>
}
