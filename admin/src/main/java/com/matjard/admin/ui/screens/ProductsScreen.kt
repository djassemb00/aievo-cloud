package com.matjard.admin.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.matjard.core.Category
import com.matjard.core.MatjardClient
import com.matjard.core.Product
import com.matjard.core.formatPrice
import com.matjard.admin.R
import com.matjard.admin.ui.ConfirmDialog
import com.matjard.admin.ui.ErrorRetry
import com.matjard.admin.ui.LoadingBox
import com.matjard.admin.ui.categoryEmoji
import androidx.compose.ui.layout.ContentScale

@Composable
fun ProductsScreen(
    client: MatjardClient,
    lang: String,
    onAdd: () -> Unit,
    onEdit: (Product) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var deleteTarget by remember { mutableStateOf<Product?>(null) }

    // منتجات غير مصنفة (بدون تصنيف أو تصنيف غير موجود)
    val uncategorizedProducts: List<Product> = products.filter { p ->
        categories.none { it.id == p.categoryId }
    }

    // المنتجات المصنفة، مجمّعة حسب التصنيف
    val categorized: Map<Category, List<Product>> = categories.associateWith { cat ->
        products.filter { it.categoryId == cat.id }
    }

    suspend fun reload() {
        loading = true
        error = null
        try {
            categories = client.categories(includeInactive = true)
            products = client.products(search = query.trim().ifBlank { null }, includeInactive = true)
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    LaunchedEffect(reloadKey) { reload() }
    LaunchedEffect(query) {
        delay(300)
        runCatching {
            products = client.products(search = query.trim().ifBlank { null }, includeInactive = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.search_products)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            )

            when {
                loading -> LoadingBox()
                error != null -> ErrorRetry(stringResource(R.string.error_generic)) { reloadKey++ }
                else -> LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // --- قسم المنتجات غير المصنفة ---
                    if (uncategorizedProducts.isNotEmpty()) {
                        item {
                            SectionHeader(title = stringResource(R.string.uncategorized), icon = "📦")
                        }
                        items(uncategorizedProducts, key = { it.id }) { product ->
                            ProductRow(
                                product = product,
                                lang = lang,
                                categoryName = stringResource(R.string.uncategorized),
                                onEdit = { onEdit(product) },
                                onToggleFeatured = {
                                    scope.launch {
                                        runCatching { client.updateProduct(product.id, mapOf("is_featured" to !product.isFeatured)) }
                                        reloadKey++
                                    }
                                },
                                onToggleActive = {
                                    scope.launch {
                                        runCatching { client.updateProduct(product.id, mapOf("is_active" to !product.isActive)) }
                                        reloadKey++
                                    }
                                },
                                onDelete = { deleteTarget = product },
                            )
                        }
                    }

                    // --- كل تصنيف مع منتجاته ---
                    categorized.entries.forEach { (cat, catProducts) ->
                        if (catProducts.isEmpty()) return@forEach

                        item {
                            SectionHeader(title = cat.name(lang), icon = categoryEmoji(cat.icon))
                        }
                        items(catProducts, key = { it.id }) { product ->
                            ProductRow(
                                product = product,
                                lang = lang,
                                categoryName = cat.name(lang),
                                onEdit = { onEdit(product) },
                                onToggleFeatured = {
                                    scope.launch {
                                        runCatching { client.updateProduct(product.id, mapOf("is_featured" to !product.isFeatured)) }
                                        reloadKey++
                                    }
                                },
                                onToggleActive = {
                                    scope.launch {
                                        runCatching { client.updateProduct(product.id, mapOf("is_active" to !product.isActive)) }
                                        reloadKey++
                                    }
                                },
                                onDelete = { deleteTarget = product },
                            )
                        }
                    }

                    item { Spacer(Modifier.height(88.dp)) }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAdd,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.add_product)) },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )

        deleteTarget?.let { target ->
            ConfirmDialog(
                title = stringResource(R.string.delete),
                message = stringResource(R.string.delete_product_confirm),
                confirmLabel = stringResource(R.string.delete),
                onConfirm = {
                    scope.launch {
                        runCatching { client.deleteProduct(target.id) }
                        reloadKey++
                    }
                },
                onDismiss = { deleteTarget = null },
            )
        }
    }
}

/** رأس قسم يحتوي على اسم التصنيف ورمز تعبيري. */
@Composable
private fun SectionHeader(title: String, icon: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ProductRow(
    product: Product,
    lang: String,
    categoryName: String,
    onEdit: () -> Unit,
    onToggleFeatured: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (product.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Thumbnail
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (product.imageUrl != null) {
                    SubcomposeAsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name(lang),
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Crop,
                        loading = {},
                        error = {},
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.name(lang),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    fontSize = 14.sp,
                )
                Text(
                    categoryName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    formatPrice(product.price),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Stock badge
                    val (stockText, stockColor) = when {
                        product.stock <= 0 -> stringResource(R.string.out_badge) to androidx.compose.ui.graphics.Color(0xFFDC2626)
                        product.stock <= 5 -> stringResource(R.string.low_stock_badge, product.stock) to androidx.compose.ui.graphics.Color(0xFFD97706)
                        else -> stringResource(R.string.in_stock, product.stock) to androidx.compose.ui.graphics.Color(0xFF16A34A)
                    }
                    Text(
                        stockText,
                        fontSize = 10.sp,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(stockColor)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                    Text(
                        if (product.isActive) stringResource(R.string.active_short) else stringResource(R.string.inactive_short),
                        fontSize = 10.sp,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (product.isActive) androidx.compose.ui.graphics.Color(0xFF475569) else androidx.compose.ui.graphics.Color(0xFF94A3B8))
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                }
            }
            Column {
                IconButton(onClick = onToggleFeatured, modifier = Modifier.size(34.dp)) {
                    Icon(
                        if (product.isFeatured) Star else StarBorder,
                        contentDescription = null,
                        tint = if (product.isFeatured) androidx.compose.ui.graphics.Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit_product))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
