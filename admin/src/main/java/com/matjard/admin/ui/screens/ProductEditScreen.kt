package com.matjard.admin.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import com.matjard.core.Category
import com.matjard.core.MatjardClient
import com.matjard.core.Product
import com.matjard.admin.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    client: MatjardClient,
    product: Product?,
    lang: String,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var nameAr by remember { mutableStateOf(product?.nameAr ?: "") }
    var nameFr by remember { mutableStateOf(product?.nameFr ?: "") }
    var categoryId by remember { mutableStateOf(product?.categoryId ?: "") }
    var brand by remember { mutableStateOf(product?.brand ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var price by remember { mutableStateOf(product?.price?.let { java.math.BigDecimal(it.toString()).stripTrailingZeros().toPlainString() } ?: "") }
    var oldPrice by remember { mutableStateOf(product?.oldPrice?.let { java.math.BigDecimal(it.toString()).stripTrailingZeros().toPlainString() } ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "0") }
    var descAr by remember { mutableStateOf(product?.descriptionAr ?: "") }
    var descFr by remember { mutableStateOf(product?.descriptionFr ?: "") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }
    var isFeatured by remember { mutableStateOf(product?.isFeatured ?: false) }
    var isActive by remember { mutableStateOf(product?.isActive ?: true) }

    var uploading by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        categories = runCatching { client.categories(includeInactive = true) }.getOrDefault(emptyList())
        if (categoryId.isBlank()) categoryId = categories.firstOrNull()?.id ?: ""
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            uploading = true
            error = null
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    ?: throw java.io.IOException("تعذر قراءة الصورة")
                if (bytes.size > 5 * 1024 * 1024) {
                    error = context.getString(R.string.image_too_big)
                    return@launch
                }
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                imageUrl = client.uploadProductImage(bytes, mime)
            } catch (e: Exception) {
                error = e.message
            } finally {
                uploading = false
            }
        }
    }

    fun save() {
        val priceValue = price.replace(",", ".").toDoubleOrNull()
        val oldPriceValue = oldPrice.trim().takeIf { it.isNotBlank() }?.replace(",", ".")?.toDoubleOrNull()
        val stockValue = stock.toIntOrNull() ?: 0
        if (nameAr.trim().isBlank() || nameFr.trim().isBlank() || categoryId.isBlank() || priceValue == null) {
            error = context.getString(R.string.required_fields)
            return
        }
        saving = true
        error = null
        scope.launch {
            try {
                val fields = mapOf<String, Any?>(
                    "name_ar" to nameAr.trim(),
                    "name_fr" to nameFr.trim(),
                    "category_id" to categoryId,
                    "description_ar" to descAr.trim().ifBlank { null },
                    "description_fr" to descFr.trim().ifBlank { null },
                    "brand" to brand.trim().ifBlank { null },
                    "sku" to sku.trim().ifBlank { null },
                    "price" to priceValue,
                    "old_price" to oldPriceValue,
                    "stock" to stockValue,
                    "image_url" to imageUrl.trim().ifBlank { null },
                    "is_featured" to isFeatured,
                    "is_active" to isActive,
                )
                if (product == null) {
                    client.createProduct(fields)
                } else {
                    client.updateProduct(product.id, fields)
                }
                onClose()
            } catch (e: Exception) {
                error = e.message
                saving = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White,
                    actionIconContentColor = androidx.compose.ui.graphics.Color.White,
                ),
                title = { Text(stringResource(if (product == null) R.string.new_product else R.string.edit_product), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(onClick = { save() }, enabled = !saving && !uploading) {
                        if (saving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = androidx.compose.ui.graphics.Color.White)
                        } else {
                            Text(stringResource(R.string.save), color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = nameAr,
                onValueChange = { nameAr = it },
                label = { Text(stringResource(R.string.name_ar)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = nameFr,
                onValueChange = { nameFr = it },
                label = { Text(stringResource(R.string.name_fr)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(10.dp))

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = categories.firstOrNull { it.id == categoryId }?.name(lang) ?: if (categoryId.isBlank()) "" else "…",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.category)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                    shape = RoundedCornerShape(14.dp),
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name(lang)) },
                            onClick = { categoryId = category.id; categoryExpanded = false },
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            Row {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text(stringResource(R.string.brand)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.width(10.dp))
                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text(stringResource(R.string.sku)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                )
            }
            Spacer(Modifier.height(10.dp))

            Row {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text(stringResource(R.string.price)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.width(10.dp))
                OutlinedTextField(
                    value = oldPrice,
                    onValueChange = { oldPrice = it },
                    label = { Text(stringResource(R.string.old_price)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                )
            }
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it.filter { c -> c.isDigit() } },
                label = { Text(stringResource(R.string.stock)) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Warehouse, contentDescription = null) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = descAr,
                onValueChange = { descAr = it },
                label = { Text(stringResource(R.string.description_ar)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = descFr,
                onValueChange = { descFr = it },
                label = { Text(stringResource(R.string.description_fr)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(16.dp))

            // Image section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (imageUrl.isNotBlank()) {
                            SubcomposeAsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                error = {},
                                loading = {},
                            )
                        } else {
                            Icon(
                                Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp),
                            )
                        }
                        if (uploading) {
                            CircularProgressIndicator(modifier = Modifier.size(40.dp))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        enabled = !uploading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(if (uploading) stringResource(R.string.uploading_image) else stringResource(R.string.pick_from_gallery))
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text(stringResource(R.string.image_url)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            // Toggles
            ToggleRow(stringResource(R.string.featured), isFeatured) { isFeatured = it }
            Spacer(Modifier.height(6.dp))
            ToggleRow(stringResource(R.string.active), isActive) { isActive = it }

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
