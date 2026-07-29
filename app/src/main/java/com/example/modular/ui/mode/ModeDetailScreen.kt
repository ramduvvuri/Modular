package com.example.modular.ui.mode

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.modular.data.local.ModeEntity
import com.example.modular.data.local.SessionEntity
import com.example.modular.domain.repository.ModeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ModeDetailViewModel(
    private val modeId: Long,
    private val modeRepository: ModeRepository
) : ViewModel() {

    private val _mode = MutableStateFlow<ModeEntity?>(null)
    val mode: StateFlow<ModeEntity?> = _mode.asStateFlow()
    
    private val _isModeStarted = MutableStateFlow(false)
    val isModeStarted: StateFlow<Boolean> = _isModeStarted.asStateFlow()

    init {
        viewModelScope.launch {
            _mode.value = modeRepository.getModeById(modeId)
        }
    }

    fun startMode() {
        viewModelScope.launch {
            modeRepository.updateSession(
                SessionEntity(
                    activeModeId = modeId,
                    startTime = System.currentTimeMillis(),
                    isRunning = true
                )
            )
            _isModeStarted.update { true }
        }
    }
}

class ModeDetailViewModelFactory(
    private val modeId: Long,
    private val modeRepository: ModeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModeDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ModeDetailViewModel(modeId, modeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeDetailScreen(
    viewModel: ModeDetailViewModel,
    onNavigateBack: () -> Unit,
    onModeStarted: () -> Unit
) {
    val mode by viewModel.mode.collectAsState()
    val isStarted by viewModel.isModeStarted.collectAsState()

    LaunchedEffect(isStarted) {
        if (isStarted) {
            onModeStarted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mode Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (mode == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = mode!!.icon,
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = mode!!.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(64.dp))
                Button(
                    onClick = { viewModel.startMode() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Start Mode")
                }
            }
        }
    }
}
