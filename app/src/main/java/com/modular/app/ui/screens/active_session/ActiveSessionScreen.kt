package com.modular.app.ui.screens.active_session

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.modular.app.ui.components.AppHeader
import com.modular.app.ui.theme.DarkBackground
import com.modular.app.ui.theme.SurfaceDark
import com.modular.app.ui.theme.TextMuted
import com.modular.app.ui.theme.TextPrimary
import com.modular.app.ui.theme.TextSecondary
import com.modular.app.ui.viewmodel.SessionViewModel
import java.util.Locale

@Composable
fun ActiveSessionScreen(
    viewModel: SessionViewModel,
    onSessionEnded: () -> Unit
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val activeModeWithApps by viewModel.activeMode.collectAsState()

    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()

    val modeName = activeModeWithApps?.mode?.name ?: "Focus"

    LaunchedEffect(activeSession) {
        if (activeSession == null || activeSession?.isRunning == false) {
            onSessionEnded()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isTimerRunning) {
            // Initial Prompt: "Leave Study Mode?"
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Leave $modeName Mode?",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "This requires waiting five uninterrupted minutes.",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2E2E2E), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Rules: Closing Modular, switching apps, turning off the screen, or locking the phone will immediately reset the timer.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { viewModel.startExitTimer() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TextPrimary,
                        contentColor = DarkBackground
                    )
                ) {
                    Text(
                        text = "Start Timer",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Live Countdown Timer Screen: "Leaving Study Mode" "04:59"
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            val formattedTime = String.format(Locale.US, "%02d:%02d", minutes, seconds)

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassBottom,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Leaving $modeName Mode",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp, lineHeight = 76.sp),
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Keep this screen active and uninterrupted.\nLocking phone or leaving app resets the timer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                OutlinedButton(
                    onClick = { viewModel.cancelExitTimer() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text(
                        text = "Cancel & Stay Focused",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
