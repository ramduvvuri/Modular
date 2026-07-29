package com.example.modular

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.addCallback
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.modular.ui.theme.ModularTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExitTimerActivity : ComponentActivity() {

    private val waitTimeSeconds = 5 * 60 // 5 minutes
    private var isTimerRunning = false
    private var isLeaving = false
    
    // We will use Compose state to drive the UI
    private val timeLeft = mutableStateOf(waitTimeSeconds)
    private val isStarted = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        onBackPressedDispatcher.addCallback(this) {
            // Block back button
        }
        
        setContent {
            ModularTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExitTimerScreen(
                        timeLeft = timeLeft.value,
                        isStarted = isStarted.value,
                        onStartTimer = { startTimer() }
                    )
                }
            }
        }
    }

    private fun startTimer() {
        if (isStarted.value) return
        isStarted.value = true
        isTimerRunning = true
        timeLeft.value = waitTimeSeconds
        
        lifecycleScope.launch {
            while (isTimerRunning && timeLeft.value > 0) {
                delay(1000)
                timeLeft.value -= 1
            }
            
            if (timeLeft.value <= 0 && isTimerRunning) {
                finishTimerAndExitMode()
            }
        }
    }

    private fun finishTimerAndExitMode() {
        isTimerRunning = false
        isLeaving = true
        
        lifecycleScope.launch {
            val app = application as ModularApp
            val session = app.modeRepository.getSessionSync()
            if (session != null) {
                // Grant a 15-minute pause instead of completely leaving the mode
                val pauseEnd = System.currentTimeMillis() + (15 * 60 * 1000)
                val updatedSession = session.copy(
                    isPaused = true,
                    pauseEndTimeMillis = pauseEnd
                )
                app.modeRepository.updateSession(updatedSession)
            }
            
            // Go back to home screen
            val homeIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                addCategory(android.content.Intent.CATEGORY_HOME)
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
            finish()
        }
    }

    private fun resetTimer() {
        if (isStarted.value && isTimerRunning && !isLeaving) {
            // Reset if interrupted
            timeLeft.value = waitTimeSeconds
        }
    }

    override fun onPause() {
        super.onPause()
        // If the activity loses focus (e.g. user goes to home screen or screen turns off)
        resetTimer()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            // Further enforcement of uninterrupted rule
            resetTimer()
        }
    }
}

@Composable
fun ExitTimerScreen(
    timeLeft: Int,
    isStarted: Boolean,
    onStartTimer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isStarted) {
            Text(
                text = "Take a Break?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Wait 5 uninterrupted minutes to earn a 15-minute break.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 64.dp)
            )
            
            Button(
                onClick = onStartTimer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Start Timer")
            }
        } else {
            Text(
                text = "Unlocking Break...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            val minutes = timeLeft / 60
            val seconds = timeLeft % 60
            val timeString = String.format("%02d:%02d", minutes, seconds)
            
            Text(
                text = timeString,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
