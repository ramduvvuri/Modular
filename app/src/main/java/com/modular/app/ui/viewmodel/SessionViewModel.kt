package com.modular.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.modular.app.data.local.ModularDatabase
import com.modular.app.data.local.entity.ModeWithApps
import com.modular.app.data.local.entity.SessionEntity
import com.modular.app.data.repository.ModeRepository
import com.modular.app.data.repository.SessionRepository
import com.modular.app.service.ExitTimerManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ModularDatabase.getInstance(application)
    private val sessionRepository = SessionRepository(database.sessionDao())
    private val modeRepository = ModeRepository(database.modeDao())

    val exitTimerManager = ExitTimerManager(application)

    val activeSession: StateFlow<SessionEntity?> = sessionRepository.getActiveSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeMode: StateFlow<ModeWithApps?> = activeSession
        .flatMapLatest { session ->
            if (session != null && session.isRunning) {
                modeRepository.getModeById(session.modeId)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val remainingSeconds: StateFlow<Int> = exitTimerManager.remainingSeconds
    val isTimerRunning: StateFlow<Boolean> = exitTimerManager.isTimerRunning

    fun startExitTimer() {
        exitTimerManager.startTimer()
    }

    fun cancelExitTimer() {
        exitTimerManager.resetTimer("User canceled exit")
    }

    override fun onCleared() {
        super.onCleared()
        exitTimerManager.cleanup()
    }
}
