package com.matjard.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matjard.admin.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@Composable
fun LoadingBox() {
    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ErrorRetry(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.padding(6.dp))
        OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = stringResource(R.string.confirm),
    destructive: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = { onConfirm(); onDismiss() }) {
                Text(confirmLabel, color = if (destructive) Color.White else MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

fun orderStatusColor(status: String): Color = when (status) {
    "new" -> Color(0xFFD97706)
    "preparing" -> Color(0xFF2563EB)
    "out_for_delivery" -> Color(0xFF7C3AED)
    "delivered" -> Color(0xFF16A34A)
    "cancelled" -> Color(0xFFDC2626)
    else -> Color(0xFF6B7280)
}

@Composable
fun StatusChip(status: String) {
    val label = when (status) {
        "new" -> stringResource(R.string.status_new)
        "preparing" -> stringResource(R.string.status_preparing)
        "out_for_delivery" -> stringResource(R.string.status_out_for_delivery)
        "delivered" -> stringResource(R.string.status_delivered)
        "cancelled" -> stringResource(R.string.status_cancelled)
        else -> status
    }
    Text(
        text = label,
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(orderStatusColor(status))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** Emoji for a category icon key — mirrors the customer app. */
fun categoryEmoji(key: String): String = when (key.lowercase()) {
    "smartphone", "phone" -> "📱"
    "laptop", "computer" -> "💻"
    "tv", "screen" -> "📺"
    "kitchen", "appliance", "home" -> "🧺"
    "audio", "headphones" -> "🎧"
    "cable", "accessory" -> "🔌"
    "camera" -> "📷"
    "watch" -> "⌚"
    "game" -> "🎮"
    else -> "⚡"
}
