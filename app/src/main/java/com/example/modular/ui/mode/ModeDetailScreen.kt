package com.example.modular.ui.mode

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.Calendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ExperimentalMaterial3Api

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
            modeRepository.getAllModes().collect { modes ->
                _mode.value = modes.find { it.id == modeId }
            }
        }
    }

    fun startMode(endTimeMillis: Long?) {
        val currentMode = _mode.value ?: return
        viewModelScope.launch {
            modeRepository.updateSession(
                SessionEntity(
                    activeModeId = modeId,
                    startTime = System.currentTimeMillis(),
                    endTimeMillis = endTimeMillis,
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
    onModeStarted: () -> Unit,
    onEditMode: () -> Unit
) {
    val mode by viewModel.mode.collectAsState()
    val isStarted by viewModel.isModeStarted.collectAsState()

    LaunchedEffect(isStarted) {
        if (isStarted) {
            onModeStarted()
        }
    }

    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        
                        // If selected time is before current time, assume it's for tomorrow
                        if (before(Calendar.getInstance())) {
                            add(Calendar.DAY_OF_MONTH, 1)
                        }
                    }
                    viewModel.startMode(selectedCalendar.timeInMillis)
                }) {
                    Text("Start")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("When should this mode end?") },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mode Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditMode) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
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
                    onClick = { viewModel.startMode(null) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Start Indefinitely")
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showTimePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Set End Time & Start")
                }
            }
        }
    }
}
