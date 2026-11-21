package com.matjard.customer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.matjard.core.MatjardClient
import com.matjard.core.TrackOrderResult
import com.matjard.core.formatPrice
import com.matjard.core.orderCode
import com.matjard.customer.R
import com.matjard.customer.cart.CartStore

@Composable
fun TrackScreen(client: MatjardClient, cartStore: CartStore) {
    val (savedName, savedPhone) = remember { cartStore.savedCustomer() }
    val scope = rememberCoroutineScope()

    var number by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(savedPhone) }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<TrackOrderResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var searched by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.track_title),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.track_hint), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = number,
            onValueChange = { number = it.filter { c -> c.isDigit() } },
            label = { Text(stringResource(R.string.order_number)) },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(R.string.phone)) },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = {
                val orderNumber = number.toLongOrNull()
                if (orderNumber == null || phone.filter { it.isDigit() }.length < 9) {
                    error = "invalid"
                    result = null
                    searched = true
                    return@Button
                }
                loading = true
                error = null
                searched = true
                scope.launch {
                    try {
                        result = client.trackOrder(orderNumber, phone)
                    } catch (e: Exception) {
                        result = null
                        error = e.message
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = !loading,
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.size(10.dp))
                Text(stringResource(R.string.track_searching))
            } else {
                Text(stringResource(R.string.track), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(18.dp))

        val trackResult = result
        if (trackResult != null) {
            TrackResultCard(trackResult)
        } else if (searched && !loading) {
            Text(
                text = stringResource(R.string.track_not_found),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@Composable
private fun TrackResultCard(result: TrackOrderResult) {
    val statusText = when (result.status) {
        "new" -> stringResource(R.string.status_new)
        "preparing" -> stringResource(R.string.status_preparing)
        "out_for_delivery" -> stringResource(R.string.status_out_for_delivery)
        "delivered" -> stringResource(R.string.status_delivered)
        "cancelled" -> stringResource(R.string.status_cancelled)
        else -> result.status
    }
    val statusColor = when (result.status) {
        "new" -> Color(0xFFD97706)
        "preparing" -> Color(0xFF2563EB)
        "out_for_delivery" -> Color(0xFF7C3AED)
        "delivered" -> Color(0xFF16A34A)
        "cancelled" -> Color(0xFFDC2626)
        else -> Color(0xFF6B7280)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.track_result, orderCode(result.orderNumber)),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = statusText,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(statusColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = result.createdAt.take(10) + " — " + result.wilaya,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        result.items.forEach { item ->
            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                Text("${item.qty} ×", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(8.dp))
                Text(item.name, modifier = Modifier.weight(1f))
                Text(formatPrice(item.unitPrice * item.qty))
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
        Row {
            Text(stringResource(R.string.delivery_fee), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text(if (result.deliveryFee == 0.0) stringResource(R.string.delivery_free) else formatPrice(result.deliveryFee))
        }
        Spacer(Modifier.height(2.dp))
        Row {
            Text(stringResource(R.string.total), fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.weight(1f))
            Text(formatPrice(result.total), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
