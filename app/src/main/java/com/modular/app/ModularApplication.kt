package com.modular.app

import android.app.Application
import com.modular.app.data.local.ModularDatabase

class ModularApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Room Database instance
        ModularDatabase.getInstance(this)
    }
}
