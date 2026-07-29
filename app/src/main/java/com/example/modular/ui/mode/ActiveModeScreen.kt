package com.example.modular.ui.mode

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modular.data.local.ModeEntity
import com.example.modular.data.local.SessionEntity
import kotlinx.coroutines.delay
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveModeScreen(
    mode: ModeEntity?,
    session: SessionEntity?,
    totalAllowedApps: Int,
    onLeaveMode: () -> Unit,
    onViewInbox: () -> Unit = {}
) {
    var timeLeftMillis by remember { mutableStateOf(0L) }
    var pauseTimeLeftMillis by remember { mutableStateOf(0L) }
    
    LaunchedEffect(session) {
        if (session != null) {
            while (true) {
                if (session.endTimeMillis != null) {
                    val remaining = session.endTimeMillis - System.currentTimeMillis()
                    timeLeftMillis = if (remaining > 0) remaining else 0
                }
                if (session.isPaused && session.pauseEndTimeMillis != null) {
                    val pauseRemaining = session.pauseEndTimeMillis - System.currentTimeMillis()
                    pauseTimeLeftMillis = if (pauseRemaining > 0) pauseRemaining else 0
                }
                delay(1000)
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Mode") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (mode != null) {
                Text(
                    text = mode.icon,
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = mode.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "is currently active.",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                if (session?.isPaused == true && pauseTimeLeftMillis > 0) {
                    Text(
                        text = "Take a deep breath. You are on a break.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Break Time Remaining")
                    val totalSeconds = pauseTimeLeftMillis / 1000
                    val minutes = totalSeconds / 60
                    val seconds = totalSeconds % 60
                    
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (session != null && session.endTimeMillis != null) {
                    Text("Time Remaining")
                    val totalSeconds = timeLeftMillis / 1000
                    val hours = totalSeconds / 3600
                    val minutes = (totalSeconds % 3600) / 60
                    val seconds = totalSeconds % 60
                    
                    val timeString = if (hours > 0) {
                        String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    } else {
                        String.format("%02d:%02d", minutes, seconds)
                    }
                    
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Running Indefinitely",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Stats", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Blocked Apps: $totalAllowedApps", style = MaterialTheme.typography.bodyLarge)                  
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onViewInbox,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("View Intercepted Messages")
                }
            } else {
                Text(text = "Loading...")
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            if (session?.isPaused != true) {
                Button(
                    onClick = onLeaveMode,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Take a 15-Minute Break")
                }
            }
        }
    }
}
