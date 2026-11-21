package com.matjard.admin.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.matjard.core.MatjardClient
import com.matjard.core.Order
import com.matjard.core.OrderItem
import com.matjard.core.formatPrice
import com.matjard.core.nextStatus
import com.matjard.core.orderCode
import com.matjard.admin.R
import com.matjard.admin.ui.ConfirmDialog
import com.matjard.admin.ui.ErrorRetry
import com.matjard.admin.ui.LoadingBox
import com.matjard.admin.ui.StatusChip

@Composable
fun OrdersScreen(client: MatjardClient) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedFilter by remember { mutableStateOf<String?>(null) }
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var detailOrder by remember { mutableStateOf<Order?>(null) }
    var statusError by remember { mutableStateOf<String?>(null) }
    var statusUpdating by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFilter, reloadKey) {
        loading = true
        error = null
        try {
            orders = client.orders(selectedFilter)
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier) {
        // Filter chips
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChipItem(stringResource(R.string.filter_all), selectedFilter == null) { selectedFilter = null }
            FilterChipItem(stringResource(R.string.status_new), selectedFilter == "new") { selectedFilter = "new" }
            FilterChipItem(stringResource(R.string.status_preparing), selectedFilter == "preparing") { selectedFilter = "preparing" }
            FilterChipItem(stringResource(R.string.status_out_for_delivery), selectedFilter == "out_for_delivery") { selectedFilter = "out_for_delivery" }
            FilterChipItem(stringResource(R.string.status_delivered), selectedFilter == "delivered") { selectedFilter = "delivered" }
            FilterChipItem(stringResource(R.string.status_cancelled), selectedFilter == "cancelled") { selectedFilter = "cancelled" }
        }

        when {
            loading -> LoadingBox()
            error != null -> ErrorRetry(stringResource(R.string.error_generic)) { reloadKey++ }
            orders.isEmpty() -> Text(
                stringResource(R.string.no_orders_yet),
                modifier = Modifier.padding(40.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(orders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onClick = { detailOrder = order; statusError = null },
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    // Detail dialog
    detailOrder?.let { order ->
        OrderDetailDialog(
            client = client,
            order = order,
            updating = statusUpdating,
            error = statusError,
            onDismiss = { detailOrder = null },
            onCall = {
                runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerPhone}"))) }
            },
            onWhatsApp = {
                val digits = order.customerPhone.filter { it.isDigit() }
                    .let { d -> if (d.startsWith("0")) "213${d.drop(1)}" else d }
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits"))) }
            },
            onAdvance = {
                val next = nextStatus(order.status)
                if (next != null) {
                    statusUpdating = true
                    statusError = null
                    scope.launch {
                        try {
                            client.setOrderStatus(order.id, next)
                            detailOrder = order.copy(status = next)
                            reloadKey++
                        } catch (e: Exception) {
                            statusError = e.message
                        } finally {
                            statusUpdating = false
                        }
                    }
                }
            },
            onCancel = {
                statusUpdating = true
                statusError = null
                scope.launch {
                    try {
                        client.setOrderStatus(order.id, "cancelled")
                        detailOrder = order.copy(status = "cancelled")
                        reloadKey++
                    } catch (e: Exception) {
                        statusError = e.message
                    } finally {
                        statusUpdating = false
                    }
                }
            },
        )
    }
}

@Composable
private fun FilterChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
        ),
    )
}

@Composable
private fun OrderCard(order: Order, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(orderCode(order.orderNumber), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                StatusChip(order.status)
                Spacer(Modifier.weight(1f))
                Text(formatPrice(order.total), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(6.dp))
            Text(order.customerName, fontWeight = FontWeight.SemiBold)
            Text(
                order.wilaya + " — " + order.customerPhone,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                order.createdAt.take(16).replace('T', ' '),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OrderDetailDialog(
    client: MatjardClient,
    order: Order,
    updating: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onAdvance: () -> Unit,
    onCancel: () -> Unit,
) {
    var items by remember(order.id) { mutableStateOf<List<OrderItem>>(emptyList()) }
    var itemsLoading by remember(order.id) { mutableStateOf(true) }
    var confirmCancel by remember { mutableStateOf(false) }

    LaunchedEffect(order.id, order.status) {
        itemsLoading = true
        items = runCatching { client.orderItems(order.id) }.getOrDefault(emptyList())
        itemsLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.order_n, orderCode(order.orderNumber)),
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                )
                StatusChip(order.status)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
            ) {
                // Customer block
                Text(stringResource(R.string.customer), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(order.customerName, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(order.customerPhone, modifier = Modifier.weight(1f))
                    IconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Call, contentDescription = stringResource(R.string.call_customer), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onWhatsApp, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Chat, contentDescription = stringResource(R.string.whatsapp_customer), tint = androidx.compose.ui.graphics.Color(0xFF16A34A))
                    }
                }
                Text(stringResource(R.string.wilaya) + " : " + order.wilaya, fontSize = 13.sp)
                Text(stringResource(R.string.address) + " : " + order.address, fontSize = 13.sp)
                order.note?.let { Text(stringResource(R.string.note) + " : " + it, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary) }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))

                Text(stringResource(R.string.items), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (itemsLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp).padding(4.dp), strokeWidth = 2.dp)
                } else {
                    items.forEach { item ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("${item.quantity} ×", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.size(6.dp))
                            Text(item.productName, modifier = Modifier.weight(1f), fontSize = 13.sp)
                            Text(formatPrice(item.unitPrice * item.quantity), fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))
                Row { Text(stringResource(R.string.subtotal), color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.weight(1f)); Text(formatPrice(order.total - order.deliveryFee)) }
                Row { Text(stringResource(R.string.delivery_fee), color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.weight(1f)); Text(if (order.deliveryFee == 0.0) stringResource(R.string.free) else formatPrice(order.deliveryFee)) }
                Row { Text(stringResource(R.string.total), fontWeight = FontWeight.ExtraBold); Spacer(Modifier.weight(1f)); Text(formatPrice(order.total), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary) }

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            val next = nextStatus(order.status)
            when {
                updating -> CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                next == "preparing" -> Button(onClick = onAdvance) { Text(stringResource(R.string.start_preparing)) }
                next == "out_for_delivery" -> Button(onClick = onAdvance) { Text(stringResource(R.string.mark_out)) }
                next == "delivered" -> Button(onClick = onAdvance) { Text(stringResource(R.string.mark_delivered)) }
                else -> TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok)) }
            }
        },
        dismissButton = {
            if (order.status == "new" || order.status == "preparing") {
                TextButton(onClick = { confirmCancel = true }) {
                    Text(stringResource(R.string.cancel_order), color = MaterialTheme.colorScheme.error)
                }
            } else {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok)) }
            }
        },
    )

    if (confirmCancel) {
        ConfirmDialog(
            title = stringResource(R.string.cancel_order),
            message = stringResource(R.string.cancel_order_confirm),
            confirmLabel = stringResource(R.string.confirm),
            onConfirm = onCancel,
            onDismiss = { confirmCancel = false },
        )
    }
}
