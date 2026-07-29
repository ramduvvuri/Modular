package com.modular.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.modular.app.ui.components.AppHeader
import com.modular.app.ui.components.ModeCard
import com.modular.app.ui.theme.DarkBackground
import com.modular.app.ui.theme.SurfaceDark
import com.modular.app.ui.theme.TextMuted
import com.modular.app.ui.theme.TextPrimary
import com.modular.app.ui.theme.TextSecondary
import com.modular.app.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateMode: () -> Unit,
    onEditMode: (Long) -> Unit,
    onOpenActiveSession: () -> Unit
) {
    val modes by viewModel.allModes.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()

    val isSessionActive = activeSession != null && activeSession?.isRunning == true
    val activeModeWithApps = modes.find { it.mode.id == activeSession?.modeId }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            if (!isSessionActive) {
                FloatingActionButton(
                    onClick = onCreateMode,
                    containerColor = TextPrimary,
                    contentColor = DarkBackground,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Mode",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                item {
                    AppHeader(
                        title = "Modular",
                        subtitle = "What are you doing right now?"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isSessionActive && activeModeWithApps != null) {
                    item {
                        ActiveBanner(
                            modeName = activeModeWithApps.mode.name,
                            onOpenSession = onOpenActiveSession
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                item {
                    Text(
                        text = "Modes",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (modes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No modes created yet.\nTap + to create your first focus mode.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(modes, key = { it.mode.id }) { modeWithApps ->
                        ModeCard(
                            modeWithApps = modeWithApps,
                            onActivate = {
                                viewModel.startMode(modeWithApps.mode.id)
                            },
                            onEdit = {
                                onEditMode(modeWithApps.mode.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveBanner(
    modeName: String,
    onOpenSession: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TextPrimary, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "ACTIVE FOCUS",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$modeName Mode is currently active.",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onOpenSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E2E2E),
                    contentColor = TextPrimary
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Leave $modeName Mode",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
