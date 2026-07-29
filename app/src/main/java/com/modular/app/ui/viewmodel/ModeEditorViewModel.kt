package com.modular.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.modular.app.data.local.ModularDatabase
import com.modular.app.data.local.entity.ModeEntity
import com.modular.app.data.model.InstalledApp
import com.modular.app.data.repository.InstalledAppRepository
import com.modular.app.data.repository.ModeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ModeEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ModularDatabase.getInstance(application)
    private val modeRepository = ModeRepository(database.modeDao())
    private val appRepository = InstalledAppRepository(application)

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps

    private val _selectedPackages = MutableStateFlow<Set<String>>(emptySet())
    val selectedPackages: StateFlow<Set<String>> = _selectedPackages

    private val _modeName = MutableStateFlow("")
    val modeName: StateFlow<String> = _modeName

    private val _modeIcon = MutableStateFlow("book")
    val modeIcon: StateFlow<String> = _modeIcon

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing

    private var currentModeId: Long = 0L

    fun loadMode(modeId: Long) {
        currentModeId = modeId
        viewModelScope.launch {
            val allApps = appRepository.getInstalledApps()

            if (modeId > 0L) {
                _isEditing.value = true
                val modeWithApps = modeRepository.getModeByIdSync(modeId)
                if (modeWithApps != null) {
                    _modeName.value = modeWithApps.mode.name
                    _modeIcon.value = modeWithApps.mode.icon
                    val allowedPkgs = modeWithApps.allowedApps.map { it.packageName }.toSet()
                    _selectedPackages.value = allowedPkgs
                }
            } else {
                _isEditing.value = false
                _modeName.value = ""
                _modeIcon.value = "book"
                _selectedPackages.value = emptySet()
            }

            _installedApps.value = allApps
        }
    }

    fun onNameChange(name: String) {
        _modeName.value = name
    }

    fun onIconChange(icon: String) {
        _modeIcon.value = icon
    }

    fun toggleAppSelection(packageName: String) {
        val current = _selectedPackages.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        _selectedPackages.value = current
    }

    fun saveMode(onSaved: () -> Unit) {
        if (_modeName.value.isBlank()) return

        viewModelScope.launch {
            val modeEntity = ModeEntity(
                id = currentModeId,
                name = _modeName.value.trim(),
                icon = _modeIcon.value
            )

            val allowedAppsMap = _installedApps.value
                .filter { _selectedPackages.value.contains(it.packageName) }
                .map { Pair(it.packageName, it.appName) }

            modeRepository.saveMode(modeEntity, allowedAppsMap)
            onSaved()
        }
    }

    fun deleteMode(onDeleted: () -> Unit) {
        if (currentModeId <= 0L) return
        viewModelScope.launch {
            val modeWithApps = modeRepository.getModeByIdSync(currentModeId)
            if (modeWithApps != null) {
                modeRepository.deleteMode(modeWithApps.mode)
            }
            onDeleted()
        }
    }
}
