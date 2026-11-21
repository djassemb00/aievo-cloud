package com.matjard.customer.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.matjard.core.CartItemDto
import com.matjard.core.MatjardClient
import com.matjard.core.StoreSettings
import com.matjard.core.WILAYAS
import com.matjard.core.formatPrice
import com.matjard.customer.R
import com.matjard.customer.cart.CartStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    client: MatjardClient,
    cartStore: CartStore,
    lang: String,
    settings: StoreSettings?,
    onOrderPlaced: (orderNumber: Long, total: Double) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val (savedName, savedPhone) = remember { cartStore.savedCustomer() }

    var name by remember { mutableStateOf(savedName) }
    var phone by remember { mutableStateOf(savedPhone) }
    var wilaya by remember { mutableStateOf("وهران") }
    var wilayaExpanded by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var wilayaError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }

    var placing by remember { mutableStateOf(false) }
    var placeError by remember { mutableStateOf<String?>(null) }

    val items = cartStore.items()
    val subtotal = items.sumOf { it.lineTotal() }
    val deliveryFee = settings?.deliveryFee ?: 400.0
    val freeMin = settings?.freeDeliveryMin ?: 50000.0
    val fee = if (subtotal >= freeMin) 0.0 else deliveryFee
    val total = subtotal + fee

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(stringResource(R.string.your_info), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; nameError = false },
            label = { Text(stringResource(R.string.full_name)) },
            isError = nameError,
            supportingText = if (nameError) ({ Text(stringResource(R.string.full_name_required)) }) else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it; phoneError = false },
            label = { Text(stringResource(R.string.phone)) },
            isError = phoneError,
            supportingText = if (phoneError) ({ Text(stringResource(R.string.phone_invalid)) }) else null,
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(10.dp))

        ExposedDropdownMenuBox(
            expanded = wilayaExpanded,
            onExpandedChange = { wilayaExpanded = !wilayaExpanded },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = wilaya,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.wilaya)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wilayaExpanded) },
                isError = wilayaError,
                supportingText = if (wilayaError) ({ Text(stringResource(R.string.wilaya_required)) }) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                shape = RoundedCornerShape(14.dp),
            )
            ExposedDropdownMenu(
                expanded = wilayaExpanded,
                onDismissRequest = { wilayaExpanded = false },
            ) {
                WILAYAS.forEach { w ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(w) },
                        onClick = { wilaya = w; wilayaError = false; wilayaExpanded = false },
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it; addressError = false },
            label = { Text(stringResource(R.string.address)) },
            isError = addressError,
            supportingText = if (addressError) ({ Text(stringResource(R.string.address_required)) }) else null,
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(stringResource(R.string.note)) },
            minLines = 1,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )

        Spacer(Modifier.height(18.dp))

        // Order summary
        Text(stringResource(R.string.order_summary), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
                Row {
                    Text(stringResource(R.string.order_items, items.size), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(6.dp))
                Row {
                    Text(stringResource(R.string.subtotal), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.weight(1f))
                    Text(formatPrice(subtotal), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.height(4.dp))
                Row {
                    Text(stringResource(R.string.delivery_fee), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.weight(1f))
                    Text(if (fee == 0.0) stringResource(R.string.delivery_free) else formatPrice(fee), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row {
                    Text(stringResource(R.string.total), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.weight(1f))
                    Text(formatPrice(total), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        placeError?.let { message ->
            Spacer(Modifier.height(10.dp))
            Text(
                text = "${stringResource(R.string.place_order_error)}\n$message",
                color = MaterialTheme.colorScheme.error,
                fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp),
            )
        }

        Spacer(Modifier.height(14.dp))
        Button(
            onClick = {
                val nameOk = name.trim().length >= 2
                val phoneDigits = phone.filter { it.isDigit() }
                val phoneOk = phoneDigits.length in 9..13
                val wilayaOk = wilaya.isNotBlank()
                val addressOk = address.trim().length >= 4
                nameError = !nameOk; phoneError = !phoneOk; wilayaError = !wilayaOk; addressError = !addressOk
                if (!nameOk || !phoneOk || !wilayaOk || !addressOk) return@Button

                placing = true
                placeError = null
                scope.launch {
                    try {
                        val result = client.placeOrder(
                            customerName = name.trim(),
                            customerPhone = phone.trim(),
                            wilaya = wilaya,
                            address = address.trim(),
                            note = note.trim().ifBlank { null },
                            items = items.map { CartItemDto(it.productId, it.quantity) },
                        )
                        cartStore.saveCustomer(name.trim(), phone.trim())
                        cartStore.clear()
                        onOrderPlaced(result.orderNumber, result.total)
                    } catch (e: Exception) {
                        placeError = e.message
                        placing = false
                    }
                }
            },
            enabled = !placing && items.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (placing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.size(10.dp))
                Text(stringResource(R.string.placing_order), fontWeight = FontWeight.Bold)
            } else {
                Text(
                    text = "${stringResource(R.string.confirm_order)} — ${formatPrice(total)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = androidx.compose.ui.unit.TextUnit(16f, androidx.compose.ui.unit.TextUnitType.Sp),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
