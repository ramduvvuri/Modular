package com.modular.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.modular.app.data.local.ModularDatabase
import com.modular.app.data.repository.SessionRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExitTimerManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val sessionRepository = SessionRepository(ModularDatabase.getInstance(context).sessionDao())

    private val _remainingSeconds = MutableStateFlow(300)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning

    private var countdownJob: Job? = null

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF,
                Intent.ACTION_USER_PRESENT -> {
                    // Any screen toggle or interruption IMMEDIATELY resets the friction exit timer
                    resetTimer("Screen state changed / Phone locked")
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        context.registerReceiver(screenReceiver, filter)
    }

    fun startTimer() {
        scope.launch {
            sessionRepository.startExitTimer()
            _isTimerRunning.value = true
            _remainingSeconds.value = 300

            countdownJob?.cancel()
            countdownJob = scope.launch {
                var seconds = 300
                while (seconds > 0 && isActive) {
                    delay(1000)
                    seconds--
                    _remainingSeconds.value = seconds
                }

                if (seconds <= 0) {
                    // Exit successfully completed after 5 uninterrupted minutes!
                    sessionRepository.stopSession()
                    _isTimerRunning.value = false
                }
            }
        }
    }

    fun resetTimer(reason: String? = null) {
        scope.launch {
            countdownJob?.cancel()
            _isTimerRunning.value = false
            _remainingSeconds.value = 300
            sessionRepository.resetExitTimer()
        }
    }

    fun cleanup() {
        try {
            context.unregisterReceiver(screenReceiver)
        } catch (_: Exception) {}
        countdownJob?.cancel()
        scope.cancel()
    }
}
