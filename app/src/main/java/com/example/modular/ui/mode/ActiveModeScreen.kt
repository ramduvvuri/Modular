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
    onLeaveMode: () -> Unit
) {
    var timeLeftMillis by remember { mutableStateOf(0L) }
    
    LaunchedEffect(session) {
        if (session != null && session.durationMinutes > 0) {
            val durationMillis = session.durationMinutes * 60 * 1000L
            while (true) {
                val elapsed = System.currentTimeMillis() - session.startTime
                val remaining = durationMillis - elapsed
                timeLeftMillis = if (remaining > 0) remaining else 0
                if (timeLeftMillis == 0L) break
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
                
                if (session != null && session.durationMinutes > 0) {
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
                        Text("Allowed Apps: $totalAllowedApps")
                    }
                }
            } else {
                Text(text = "Loading...")
            }
            
            Spacer(modifier = Modifier.height(64.dp))
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
                Text("Leave Mode")
            }
        }
    }
}
