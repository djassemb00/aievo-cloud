package com.matjard.admin.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.ShoppingBasket
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matjard.core.MatjardClient
import com.matjard.core.Order
import com.matjard.core.formatPrice
import com.matjard.admin.R
import com.matjard.admin.ui.ErrorRetry
import com.matjard.admin.ui.LoadingBox
import com.matjard.admin.ui.StatusChip
import com.matjard.core.orderCode

@Composable
fun DashboardScreen(
    client: MatjardClient,
    onViewAllOrders: () -> Unit,
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var lowStock by remember { mutableIntStateOf(0) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadKey) {
        loading = true
        error = null
        try {
            orders = client.orders()
            lowStock = client.products(includeInactive = true).count { it.stock <= 5 }
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    val today = remember { java.time.LocalDate.now().toString() }
    val todayOrders = orders.filter { it.createdAt.startsWith(today) }
    val salesToday = todayOrders.filter { it.status != "cancelled" }.sumOf { it.total }
    val newCount = orders.count { it.status == "new" }

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.dashboard_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { reloadKey++ }) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                }
            }
        }

        when {
            loading -> item { LoadingBox() }
            error != null -> item { ErrorRetry(stringResource(R.string.error_generic)) { reloadKey++ } }
            else -> {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard(Icons.Filled.ShoppingBasket, stringResource(R.string.stat_today_orders), todayOrders.size.toString(), tint = 0xFF2563EB, modifier = Modifier.weight(1f))
                        StatCard(Icons.Filled.AttachMoney, stringResource(R.string.stat_today_sales), formatPrice(salesToday), tint = 0xFF16A34A, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard(Icons.Filled.Notifications, stringResource(R.string.stat_new_orders), newCount.toString(), tint = 0xFFD97706, modifier = Modifier.weight(1f))
                        StatCard(Icons.Filled.ReportProblem, stringResource(R.string.stat_low_stock), lowStock.toString(), tint = 0xFFDC2626, modifier = Modifier.weight(1f))
                    }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.recent_orders),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = onViewAllOrders) {
                            Text(stringResource(R.string.view_all_orders))
                        }
                    }
                }

                if (orders.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.no_orders_yet),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                } else {
                    items(orders.take(6), key = { it.id }) { order ->
                        RecentOrderRow(order)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Long,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .padding(0.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = androidx.compose.ui.graphics.Color(tint).copy(alpha = 0.14f),
                    modifier = Modifier.size(38.dp),
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color(tint),
                        modifier = Modifier.padding(9.dp),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}

@Composable
private fun RecentOrderRow(order: Order) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(orderCode(order.orderNumber), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    Spacer(Modifier.width(8.dp))
                    StatusChip(order.status)
                }
                Spacer(Modifier.height(3.dp))
                Text(order.customerName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    order.wilaya + " — " + order.createdAt.take(16).replace('T', ' '),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                formatPrice(order.total),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
            )
        }
    }
}
