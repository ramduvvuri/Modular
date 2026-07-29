package com.example.modular

import android.app.Application
import com.example.modular.data.local.AppDatabase
import com.example.modular.data.repository.ModeRepositoryImpl
import com.example.modular.data.repository.AppProviderImpl
import com.example.modular.domain.repository.AppProvider
import com.example.modular.domain.repository.ModeRepository

class ModularApp : Application() {

    lateinit var modeRepository: ModeRepository
    lateinit var appProvider: AppProvider

    override fun onCreate() {
        super.onCreate()
        
        val database = AppDatabase.getDatabase(this)
        modeRepository = ModeRepositoryImpl(
            modeDao = database.modeDao(),
            sessionDao = database.sessionDao(),
            notificationDao = database.notificationDao()
        )
        appProvider = AppProviderImpl(this)
    }
}
