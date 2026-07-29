package com.example.modular.ui.mode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.modular.data.local.AllowedAppEntity
import com.example.modular.domain.model.AppInfo
import com.example.modular.domain.repository.AppProvider
import com.example.modular.domain.repository.ModeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateModeState(
    val modeName: String = "",
    val modeIcon: String = "📚",
    val installedApps: List<AppInfo> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val isLoadingApps: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)

class CreateModeViewModel(
    private val modeRepository: ModeRepository,
    private val appProvider: AppProvider
) : ViewModel() {

    private val _state = MutableStateFlow(CreateModeState())
    val state: StateFlow<CreateModeState> = _state.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingApps = true) }
            val apps = appProvider.getInstalledApps()
            _state.update { it.copy(installedApps = apps, isLoadingApps = false) }
        }
    }

    fun updateName(name: String) {
        _state.update { it.copy(modeName = name) }
    }

    fun updateIcon(icon: String) {
        _state.update { it.copy(modeIcon = icon) }
    }

    fun toggleAppSelection(packageName: String) {
        _state.update { currentState ->
            val currentSelected = currentState.selectedPackages
            val newSelected = if (currentSelected.contains(packageName)) {
                currentSelected - packageName
            } else {
                currentSelected + packageName
            }
            currentState.copy(selectedPackages = newSelected)
        }
    }

    fun saveMode() {
        val currentState = _state.value
        if (currentState.modeName.isBlank()) return
        
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            
            val allowedApps = currentState.selectedPackages.mapNotNull { pkgName ->
                val appInfo = currentState.installedApps.find { it.packageName == pkgName }
                appInfo?.let {
                    AllowedAppEntity(
                        modeId = 0, // Will be set by repository
                        packageName = it.packageName,
                        appName = it.appName
                    )
                }
            }
            
            modeRepository.createMode(
                name = currentState.modeName,
                icon = currentState.modeIcon,
                allowedApps = allowedApps
            )
            
            _state.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}

class CreateModeViewModelFactory(
    private val modeRepository: ModeRepository,
    private val appProvider: AppProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateModeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateModeViewModel(modeRepository, appProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
