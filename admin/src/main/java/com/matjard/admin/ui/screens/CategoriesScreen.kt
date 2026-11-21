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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
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
import kotlinx.coroutines.launch
import com.matjard.core.Category
import com.matjard.core.MatjardClient
import com.matjard.admin.R
import com.matjard.admin.ui.ConfirmDialog
import com.matjard.admin.ui.ErrorRetry
import com.matjard.admin.ui.LoadingBox
import com.matjard.admin.ui.categoryEmoji

private val ICON_OPTIONS = listOf(
    "smartphone" to "📱", "laptop" to "💻", "tv" to "📺", "kitchen" to "🧺",
    "audio" to "🎧", "cable" to "🔌", "camera" to "📷", "watch" to "⌚",
    "game" to "🎮", "devices" to "⚡",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(client: MatjardClient, lang: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf<Category?>(null) }   // null => closed; sentinel: use showDialog flag
    var showDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(reloadKey) {
        loading = true
        error = null
        try {
            categories = client.categories(includeInactive = true)
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            when {
                loading -> LoadingBox()
                error != null -> ErrorRetry(stringResource(R.string.error_generic)) { reloadKey++ }
                else -> LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(categories, key = { it.id }) { category ->
                        CategoryRow(
                            category = category,
                            lang = lang,
                            onEdit = { editing = category; showDialog = true },
                            onToggleActive = {
                                scope.launch {
                                    runCatching { client.updateCategory(category.id, mapOf("is_active" to !category.isActive)) }
                                    reloadKey++
                                }
                            },
                            onDelete = { deleteTarget = category },
                        )
                    }
                    item { Spacer(Modifier.height(88.dp)) }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { editing = null; showDialog = true },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.add_category)) },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
        )
    }

    if (showDialog) {
        CategoryDialog(
            client = client,
            initial = editing,
            onDismiss = { showDialog = false },
            onSaved = { showDialog = false; reloadKey++ },
        )
    }

    deleteTarget?.let { target ->
        ConfirmDialog(
            title = stringResource(R.string.delete),
            message = stringResource(R.string.delete_category_confirm),
            confirmLabel = stringResource(R.string.delete),
            onConfirm = {
                scope.launch {
                    runCatching { client.deleteCategory(target.id) }
                    reloadKey++
                }
            },
            onDismiss = { deleteTarget = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryRow(
    category: Category,
    lang: String,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (category.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(categoryEmoji(category.icon), fontSize = 24.sp)
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category.nameAr, fontWeight = FontWeight.SemiBold)
                Text(category.nameFr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("#${category.sortOrder}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = category.isActive, onCheckedChange = { onToggleActive() })
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit_category))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDialog(
    client: MatjardClient,
    initial: Category?,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var nameAr by remember { mutableStateOf(initial?.nameAr ?: "") }
    var nameFr by remember { mutableStateOf(initial?.nameFr ?: "") }
    var icon by remember { mutableStateOf(initial?.icon ?: "devices") }
    var sortOrder by remember { mutableStateOf(initial?.sortOrder?.toString() ?: "0") }
    var isActive by remember { mutableStateOf(initial?.isActive ?: true) }
    var iconExpanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (initial == null) R.string.add_category else R.string.edit_category), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text(stringResource(R.string.name_ar)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = nameFr,
                    onValueChange = { nameFr = it },
                    label = { Text(stringResource(R.string.name_fr)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = iconExpanded, onExpandedChange = { iconExpanded = !iconExpanded }) {
                    OutlinedTextField(
                        value = (ICON_OPTIONS.firstOrNull { it.first == icon }?.first ?: icon) + " " + (ICON_OPTIONS.firstOrNull { it.first == icon }?.second ?: ""),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.icon_key)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = iconExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(expanded = iconExpanded, onDismissRequest = { iconExpanded = false }) {
                        ICON_OPTIONS.forEach { (key, emoji) ->
                            DropdownMenuItem(
                                text = { Text("$emoji  $key") },
                                onClick = { icon = key; iconExpanded = false },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = sortOrder,
                    onValueChange = { sortOrder = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.sort_order)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.weight(1f))
                    Text(stringResource(R.string.active), fontSize = 13.sp)
                    Spacer(Modifier.size(8.dp))
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
                error?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !saving,
                onClick = {
                    if (nameAr.trim().isBlank() || nameFr.trim().isBlank()) {
                        error = context.getString(R.string.required_fields)
                        return@Button
                    }
                    saving = true
                    scope.launch {
                        try {
                            val fields = mapOf<String, Any?>(
                                "name_ar" to nameAr.trim(),
                                "name_fr" to nameFr.trim(),
                                "icon" to icon,
                                "sort_order" to (sortOrder.toIntOrNull() ?: 0),
                                "is_active" to isActive,
                            )
                            if (initial == null) client.createCategory(fields) else client.updateCategory(initial.id, fields)
                            onSaved()
                        } catch (e: Exception) {
                            error = e.message
                            saving = false
                        }
                    }
                },
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
