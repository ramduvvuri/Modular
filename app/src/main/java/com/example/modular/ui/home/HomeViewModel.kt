package com.example.modular.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.modular.data.local.ModeEntity
import com.example.modular.domain.repository.ModeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

import kotlinx.coroutines.launch

class HomeViewModel(
    private val modeRepository: ModeRepository
) : ViewModel() {

    init {
        // Pre-populate "Night Mode" if it doesn't exist
        viewModelScope.launch {
            val currentModes = modeRepository.getAllModes().first()
            val hasNightMode = currentModes.any { it.name.equals("Night Mode", ignoreCase = true) }
            if (!hasNightMode) {
                modeRepository.createMode("Night Mode", "🌙", emptyList())
            }
        }
    }

    val modes: StateFlow<List<ModeEntity>> = modeRepository.getAllModes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

class HomeViewModelFactory(
    private val modeRepository: ModeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(modeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
