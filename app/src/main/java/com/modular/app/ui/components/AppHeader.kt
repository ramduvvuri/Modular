package com.modular.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.modular.app.ui.theme.TextMuted
import com.modular.app.ui.theme.TextPrimary

@Composable
fun AppHeader(
    title: String = "Modular",
    subtitle: String? = "Intentionally minimal app blocking."
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp),
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
    }
}
