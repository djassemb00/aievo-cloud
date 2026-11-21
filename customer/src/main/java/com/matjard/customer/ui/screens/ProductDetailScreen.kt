package com.matjard.customer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.launch
import com.matjard.core.MatjardClient
import com.matjard.core.Product
import com.matjard.core.StoreSettings
import com.matjard.core.formatPrice
import com.matjard.customer.R
import com.matjard.customer.ui.ErrorRetry
import com.matjard.customer.ui.LoadingBox
import com.matjard.customer.ui.ProductImage
import com.matjard.customer.ui.QuantitySelector

@Composable
fun ProductDetailScreen(
    client: MatjardClient,
    productId: String,
    lang: String,
    settings: StoreSettings?,
    onAddToCart: (Product, Int) -> Unit,
    onGoToCart: () -> Unit,
) {
    val context = LocalContext.current
    var product by remember { mutableStateOf<Product?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableIntStateOf(1) }
    var reloadKey by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(productId, reloadKey) {
        loading = true
        error = null
        try {
            product = client.product(productId)
            if (product == null) error = "not_found"
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val p = product
        when {
            loading -> LoadingBox(Modifier.padding(innerPadding))
            p == null -> ErrorRetry(
                message = stringResource(R.string.error_generic),
                onRetry = { reloadKey++ },
                modifier = Modifier.padding(innerPadding),
            )
            else -> Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            ) {
                // Image header
                Box {
                    ProductImage(
                        imageUrl = p.imageUrl,
                        contentDescription = p.name(lang),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                    )
                    p.discountPercent()?.let { pct ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.secondary)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.discount_off, pct),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSecondary,
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // Name + brand
                    Text(p.name(lang), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                    p.brand?.let {
                        Spacer(Modifier.size(2.dp))
                        Text(
                            text = stringResource(R.string.brand) + " : " + it,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                        )
                    }
                    p.sku?.let {
                        Text(
                            text = stringResource(R.string.sku) + " : " + it,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                        )
                    }

                    Spacer(Modifier.size(12.dp))

                    // Price
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatPrice(p.price),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        p.oldPrice?.takeIf { it > p.price }?.let { old ->
                            Spacer(Modifier.size(10.dp))
                            Text(
                                text = formatPrice(old),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = TextDecoration.LineThrough,
                            )
                        }
                    }

                    Spacer(Modifier.size(8.dp))

                    // Stock
                    val stockText: String
                    val stockColor: Color
                    when {
                        p.stock <= 0 -> { stockText = stringResource(R.string.out_of_stock); stockColor = Color(0xFFDC2626) }
                        p.stock <= 5 -> { stockText = stringResource(R.string.low_stock, p.stock); stockColor = Color(0xFFD97706) }
                        else -> { stockText = stringResource(R.string.in_stock); stockColor = Color(0xFF16A34A) }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(stockColor))
                        Spacer(Modifier.size(6.dp))
                        Text(stockText, color = stockColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    // Description
                    p.description(lang)?.takeIf { it.isNotBlank() }?.let { desc ->
                        Spacer(Modifier.size(14.dp))
                        Text(desc, fontSize = 14.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(Modifier.size(18.dp))

                    // Quantity + CTA
                    if (p.stock > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.quantity), fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            QuantitySelector(
                                quantity = quantity,
                                onDecrease = { if (quantity > 1) quantity-- },
                                onIncrease = { if (quantity < p.stock) quantity++ },
                            )
                        }
                        Spacer(Modifier.size(12.dp))
                        Button(
                            onClick = {
                                onAddToCart(p, quantity)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.added_to_cart),
                                        actionLabel = context.getString(R.string.cart_title),
                                    ).let { result ->
                                        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) onGoToCart()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                            Spacer(Modifier.size(10.dp))
                            Text(stringResource(R.string.add_to_cart), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text(stringResource(R.string.out_of_stock), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.size(12.dp))

                    // Contact shortcuts
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                settings?.phone?.let { phone ->
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(8.dp))
                            Text(stringResource(R.string.call_store), fontSize = 13.sp)
                        }
                        Spacer(Modifier.size(10.dp))
                        FilledTonalButton(
                            onClick = {
                                settings?.let { s ->
                                    val raw = (s.whatsapp ?: s.phone).filter { it.isDigit() }
                                        .let { d -> if (d.startsWith("0")) "213${d.drop(1)}" else d }
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$raw")))
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(8.dp))
                            Text(stringResource(R.string.whatsapp), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
