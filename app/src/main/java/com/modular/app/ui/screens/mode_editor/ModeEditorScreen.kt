package com.modular.app.ui.screens.mode_editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.modular.app.data.model.InstalledApp
import com.modular.app.ui.theme.DarkBackground
import com.modular.app.ui.theme.ErrorAccent
import com.modular.app.ui.theme.SurfaceDark
import com.modular.app.ui.theme.SurfaceVariantDark
import com.modular.app.ui.theme.TextMuted
import com.modular.app.ui.theme.TextPrimary
import com.modular.app.ui.theme.TextSecondary
import com.modular.app.ui.viewmodel.ModeEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeEditorScreen(
    modeId: Long,
    viewModel: ModeEditorViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(modeId) {
        viewModel.loadMode(modeId)
    }

    val modeName by viewModel.modeName.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val selectedPackages by viewModel.selectedPackages.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Mode" else "New Mode",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { viewModel.deleteMode(onBack) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = ErrorAccent
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = modeName,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Mode Name (e.g. Study, Class, Night)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TextPrimary,
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedLabelColor = TextPrimary,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Allowed Applications Whitelist",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Select apps that are allowed during this mode. All other apps will be blocked.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search installed apps...", color = TextMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TextPrimary,
                            unfocusedBorderColor = Color(0xFF333333)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(filteredApps, key = { it.packageName }) { app ->
                    val isSelected = selectedPackages.contains(app.packageName) || app.isEmergency
                    AppCheckRow(
                        app = app,
                        isSelected = isSelected,
                        onToggle = {
                            if (!app.isEmergency) {
                                viewModel.toggleAppSelection(app.packageName)
                            }
                        }
                    )
                }
            }

            Button(
                onClick = { viewModel.saveMode(onBack) },
                enabled = modeName.isNotBlank(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TextPrimary,
                    contentColor = DarkBackground,
                    disabledContainerColor = Color(0xFF222222),
                    disabledContentColor = TextMuted
                )
            ) {
                Text(
                    text = if (isEditing) "Save Changes" else "Create Mode",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AppCheckRow(
    app: InstalledApp,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = !app.isEmergency, onClick = onToggle)
            .border(
                1.dp,
                if (isSelected) TextPrimary else Color(0xFF222222),
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceVariantDark else SurfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (app.isEmergency) "Emergency App - Always Allowed" else app.packageName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (app.isEmergency) TextSecondary else TextMuted
                )
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = { if (!app.isEmergency) onToggle() },
                enabled = !app.isEmergency,
                colors = CheckboxDefaults.colors(
                    checkedColor = TextPrimary,
                    checkmarkColor = DarkBackground,
                    disabledCheckedColor = TextMuted
                )
            )
        }
    }
}
