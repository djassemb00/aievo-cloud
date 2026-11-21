package com.matjard.customer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matjard.core.StoreSettings
import com.matjard.core.formatPrice
import com.matjard.customer.R
import com.matjard.customer.cart.CartStore
import com.matjard.customer.ui.ProductImage
import com.matjard.customer.ui.QuantitySelector

@Composable
fun CartScreen(
    cartStore: CartStore,
    lang: String,
    settings: StoreSettings?,
    cartVersion: Int,
    onBump: () -> Unit,
    onCheckout: () -> Unit,
    onBrowse: () -> Unit,
) {
    val items = remember(cartVersion) { cartStore.items() }
    val subtotal = items.sumOf { it.lineTotal() }
    val deliveryFee = settings?.deliveryFee ?: 400.0
    val freeMin = settings?.freeDeliveryMin ?: 50000.0
    val fee = if (subtotal >= freeMin || subtotal == 0.0) 0.0 else deliveryFee
    val total = subtotal + fee

    if (items.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Filled.RemoveShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(72.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.cart_empty), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.cart_empty_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            Button(onClick = onBrowse, shape = RoundedCornerShape(14.dp)) {
                Text(stringResource(R.string.browse_products))
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.items_count, items.sumOf { it.quantity }),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            items(items, key = { it.productId }) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProductImage(
                            imageUrl = item.imageUrl,
                            contentDescription = item.name(lang),
                            modifier = Modifier
                                .size(84.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.name(lang),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                fontSize = 13.sp,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                formatPrice(item.price),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                QuantitySelector(
                                    quantity = item.quantity,
                                    onDecrease = { cartStore.setQuantity(item.productId, item.quantity - 1); onBump() },
                                    onIncrease = { cartStore.setQuantity(item.productId, item.quantity + 1); onBump() },
                                )
                            }
                        }
                        IconButton(onClick = { cartStore.remove(item.productId); onBump() }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.remove), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Summary footer
        Surface(tonalElevation = 6.dp, shadowElevation = 12.dp) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                // Free delivery progress
                val remaining = (freeMin - subtotal).coerceAtLeast(0.0)
                if (freeMin > 0) {
                    Text(
                        text = if (remaining > 0) stringResource(R.string.free_delivery_progress, formatPrice(remaining))
                        else stringResource(R.string.free_delivery_reached),
                        fontSize = 12.sp,
                        color = if (remaining > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (subtotal / freeMin).coerceIn(0.0, 1.0).toFloat() },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    )
                    Spacer(Modifier.height(10.dp))
                }

                Row(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.subtotal), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    Text(formatPrice(subtotal))
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.delivery_fee), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    Text(if (fee == 0.0) stringResource(R.string.delivery_free) else formatPrice(fee))
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.total), fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    Spacer(Modifier.weight(1f))
                    Text(formatPrice(total), fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onCheckout,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(stringResource(R.string.go_checkout), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.cod_note),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}
