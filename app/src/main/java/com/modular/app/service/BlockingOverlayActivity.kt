package com.modular.app.service

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.modular.app.MainActivity
import com.modular.app.ui.screens.blocking.BlockingScreen
import com.modular.app.ui.theme.ModularTheme

class BlockingOverlayActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
        const val EXTRA_MODE_NAME = "extra_mode_name"
        const val EXTRA_MODE_ID = "extra_mode_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: "App"
        val modeName = intent.getStringExtra(EXTRA_MODE_NAME) ?: "Focus"
        val modeId = intent.getLongExtra(EXTRA_MODE_ID, 0L)

        setContent {
            ModularTheme {
                BlockingScreen(
                    modeName = modeName,
                    blockedPackageName = blockedPackage,
                    onGoHome = {
                        goHomeLauncher()
                    },
                    onStartLeaveMode = {
                        openModularExitScreen(modeId)
                    }
                )
            }
        }
    }

    private fun goHomeLauncher() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    private fun openModularExitScreen(modeId: Long) {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_exit", true)
            putExtra("mode_id", modeId)
        }
        startActivity(mainIntent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Intercept back button to return user to home launcher safely
        goHomeLauncher()
    }
}
