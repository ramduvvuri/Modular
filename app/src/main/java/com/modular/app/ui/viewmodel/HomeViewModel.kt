package com.modular.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.modular.app.data.local.ModularDatabase
import com.modular.app.data.local.entity.ModeWithApps
import com.modular.app.data.local.entity.SessionEntity
import com.modular.app.data.repository.ModeRepository
import com.modular.app.data.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ModularDatabase.getInstance(application)
    private val modeRepository = ModeRepository(database.modeDao())
    private val sessionRepository = SessionRepository(database.sessionDao())

    val allModes: StateFlow<List<ModeWithApps>> = modeRepository.getAllModes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSession: StateFlow<SessionEntity?> = sessionRepository.getActiveSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun startMode(modeId: Long) {
        viewModelScope.launch {
            sessionRepository.startSession(modeId)
        }
    }
}
