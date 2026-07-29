package com.modular.app.ui.screens.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.modular.app.ui.components.AppHeader
import com.modular.app.ui.theme.DarkBackground
import com.modular.app.ui.theme.SurfaceDark
import com.modular.app.ui.theme.TextMuted
import com.modular.app.ui.theme.TextPrimary
import com.modular.app.ui.theme.TextSecondary
import com.modular.app.util.ServiceUtils

@Composable
fun PermissionSetupScreen(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasAccessibility by remember { mutableStateOf(ServiceUtils.isAccessibilityServiceEnabled(context)) }
    var hasOverlay by remember { mutableStateOf(ServiceUtils.isOverlayPermissionGranted(context)) }

    fun refreshPermissions() {
        hasAccessibility = ServiceUtils.isAccessibilityServiceEnabled(context)
        hasOverlay = ServiceUtils.isOverlayPermissionGranted(context)

        if (hasAccessibility && hasOverlay) {
            onPermissionsGranted()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                AppHeader(
                    title = "Required Setup",
                    subtitle = "Modular needs system permissions to reliably enforce focus modes."
                )

                Spacer(modifier = Modifier.height(24.dp))

                PermissionCard(
                    title = "1. Accessibility Service",
                    description = "Required to detect foreground app launches and enforce your mode's allowed app whitelist.",
                    isGranted = hasAccessibility,
                    onRequestPermission = { ServiceUtils.openAccessibilitySettings(context) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PermissionCard(
                    title = "2. Display Over Other Apps",
                    description = "Required to instantly present the blocking screen over unauthorized applications.",
                    isGranted = hasOverlay,
                    onRequestPermission = { ServiceUtils.openOverlaySettings(context) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PermissionCard(
                    title = "3. Disable Battery Optimization (Optional)",
                    description = "Prevents Android background process killers from interrupting Modular's monitoring.",
                    isGranted = true,
                    isOptional = true,
                    onRequestPermission = { ServiceUtils.openBatteryOptimizationSettings(context) }
                )
            }

            Button(
                onClick = {
                    refreshPermissions()
                },
                enabled = hasAccessibility && hasOverlay,
                modifier = Modifier
                    .fillMaxWidth()
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
                    text = if (hasAccessibility && hasOverlay) "Continue to Modular" else "Grant Permissions Above",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    isOptional: Boolean = false,
    onRequestPermission: () -> Unit
) {
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (isGranted && !isOptional) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Granted",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (!isGranted || isOptional) {
                OutlinedButton(
                    onClick = onRequestPermission,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Text(
                        text = if (isOptional) "Configure" else "Grant",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
