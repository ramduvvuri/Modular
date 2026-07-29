package com.example.modular.ui.blocking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.modular.ui.theme.ModularTheme
import kotlinx.coroutines.delay

class UninstallTimerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ModularTheme {
                // Intercept back button to prevent escaping the timer
                BackHandler {
                    goBackToHome()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UninstallTimerScreen(
                        onTimerFinished = {
                            grantUnlockWindow()
                            finish()
                        },
                        onCancel = {
                            goBackToHome()
                        }
                    )
                }
            }
        }
    }

    private fun grantUnlockWindow() {
        val prefs = getSharedPreferences("modular_prefs", Context.MODE_PRIVATE)
        // Grant a 5-minute window to uninstall
        val unlockUntil = System.currentTimeMillis() + (5 * 60 * 1000)
        prefs.edit().putLong("uninstall_unlock_until", unlockUntil).apply()
    }

    private fun goBackToHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}

@Composable
fun UninstallTimerScreen(
    onTimerFinished: () -> Unit,
    onCancel: () -> Unit
) {
    // 10 minutes in seconds
    var timeLeft by remember { mutableStateOf(600) }

    LaunchedEffect(key1 = true) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        onTimerFinished()
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Uninstall Protected",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "To uninstall this app, you must wait for the timer to finish. Please take a deep breath and think if you really want to do this.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(64.dp))
        
        Text(
            text = timeFormatted,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(64.dp))
        
        Button(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Cancel & Go Back")
        }
    }
}
