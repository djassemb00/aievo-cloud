package com.matjard.admin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.matjard.core.AdminNotification
import com.matjard.core.MatjardClient
import com.matjard.admin.R
import com.matjard.admin.ui.ErrorRetry
import com.matjard.admin.ui.LoadingBox

@Composable
fun NotificationsScreen(
    client: MatjardClient,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var notifications by remember { mutableStateOf<List<AdminNotification>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadKey) {
        loading = true
        error = null
        try {
            notifications = client.listAdminNotifications()
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    val unreadCount = notifications.count { !it.isRead }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.notifications_title),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                )
                if (unreadCount > 0) {
                    Text(
                        "$unreadCount ${stringResource(R.string.notifications_unread)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { reloadKey++ }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.refresh))
                }
            }
        }

        when {
            loading -> LoadingBox()
            error != null -> ErrorRetry(stringResource(R.string.error_generic)) { reloadKey++ }
            notifications.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.notifications_empty), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                }
            }
            else -> LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationCard(
                        notification = notification,
                        onMarkRead = {
                            scope.launch {
                                try {
                                    client.markNotificationRead(notification.id)
                                    reloadKey++
                                } catch (e: Exception) {
                                    // فشل تحديد المقروء ليس خطأً حرجة
                                }
                            }
                        },
                    )
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }

    Spacer(Modifier.height(24.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Button(
            onClick = onClose,
        ) {
            Text(stringResource(R.string.close))
        }
    }
}

@Composable
private fun NotificationCard(
    notification: AdminNotification,
    onMarkRead: () -> Unit,
) {
    val color = when (notification.type) {
        "warning" -> 0xFFF59E0B
        "error" -> 0xFFDC2626
        "success" -> 0xFF16A34A
        else -> 0xFF2563EB
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
        ),
        elevation = CardDefaults.cardElevation(if (notification.isRead) 0.dp else 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    notification.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onMarkRead) {
                    Icon(
                        if (notification.isRead) Icons.Filled.DeleteOutline else Icons.Filled.Done,
                        contentDescription = if (notification.isRead) stringResource(R.string.mark_unread) else stringResource(R.string.mark_read),
                        tint = if (notification.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                notification.message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    notification.createdAt.take(16).replace('T', ' '),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                ChipType(notification.type, color)
            }
        }
    }
}

@Composable
private fun ChipType(type: String, color: Long) {
    val label = when (type) {
        "warning" -> stringResource(R.string.notify_warning)
        "error" -> stringResource(R.string.notify_error)
        "success" -> stringResource(R.string.notify_success)
        else -> stringResource(R.string.notify_info)
    }
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .background(androidx.compose.ui.graphics.Color(color).copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
    ) {
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.ui.graphics.Color(color),
        )
    }
}
