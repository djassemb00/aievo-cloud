package com.matjard.admin.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import com.matjard.core.Banner
import com.matjard.core.MatjardClient
import com.matjard.admin.R
import com.matjard.admin.ui.ConfirmDialog
import com.matjard.admin.ui.ErrorRetry
import com.matjard.admin.ui.LoadingBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BannersScreen(
    client: MatjardClient,
    lang: String,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var banners by remember { mutableStateOf<List<Banner>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf<Banner?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Banner?>(null) }

    LaunchedEffect(reloadKey) {
        loading = true
        error = null
        try {
            banners = client.banners(includeInactive = true)
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White,
                ),
                title = {
                    Column {
                        Text(stringResource(R.string.banners_title), fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                        Text(stringResource(R.string.banners_subtitle), fontSize = 11.sp, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.75f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; showDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_banner)) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                loading -> LoadingBox()
                error != null -> ErrorRetry(stringResource(R.string.error_generic)) { reloadKey++ }
                else -> LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(banners, key = { it.id }) { banner ->
                        BannerRow(
                            banner = banner,
                            lang = lang,
                            onEdit = { editing = banner; showDialog = true },
                            onToggleActive = {
                                scope.launch {
                                    runCatching { client.updateBanner(banner.id, mapOf("is_active" to !banner.isActive)) }
                                    reloadKey++
                                }
                            },
                            onDelete = { deleteTarget = banner },
                        )
                    }
                    item { Spacer(Modifier.height(88.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        BannerDialog(
            client = client,
            initial = editing,
            onDismiss = { showDialog = false },
            onSaved = { showDialog = false; reloadKey++ },
        )
    }

    deleteTarget?.let { target ->
        ConfirmDialog(
            title = stringResource(R.string.delete),
            message = stringResource(R.string.delete_banner_confirm),
            confirmLabel = stringResource(R.string.delete),
            onConfirm = {
                scope.launch {
                    runCatching { client.deleteBanner(target.id) }
                    reloadKey++
                }
            },
            onDismiss = { deleteTarget = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BannerRow(
    banner: Banner,
    lang: String,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (banner.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (banner.imageUrl != null) {
                    SubcomposeAsyncImage(
                        model = banner.imageUrl,
                        contentDescription = banner.title(lang),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {},
                        error = {},
                    )
                } else {
                    Icon(Icons.Filled.Image, contentDescription = null)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(banner.titleAr, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(banner.titleFr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Text("#${banner.sortOrder}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = banner.isActive, onCheckedChange = { onToggleActive() })
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit_banner)) }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BannerDialog(
    client: MatjardClient,
    initial: Banner?,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var titleAr by remember { mutableStateOf(initial?.titleAr ?: "") }
    var titleFr by remember { mutableStateOf(initial?.titleFr ?: "") }
    var subtitleAr by remember { mutableStateOf(initial?.subtitleAr ?: "") }
    var subtitleFr by remember { mutableStateOf(initial?.subtitleFr ?: "") }
    var imageUrl by remember { mutableStateOf(initial?.imageUrl ?: "") }
    var sortOrder by remember { mutableStateOf(initial?.sortOrder?.toString() ?: "0") }
    var isActive by remember { mutableStateOf(initial?.isActive ?: true) }
    var uploading by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            uploading = true
            error = null
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    ?: throw java.io.IOException("read error")
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (initial == null) R.string.add_banner else R.string.edit_banner), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(titleAr, { titleAr = it }, label = { Text(stringResource(R.string.title_ar)) }, singleLine = true, shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(titleFr, { titleFr = it }, label = { Text(stringResource(R.string.title_fr)) }, singleLine = true, shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(subtitleAr, { subtitleAr = it }, label = { Text(stringResource(R.string.subtitle_ar)) }, singleLine = true, shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(subtitleFr, { subtitleFr = it }, label = { Text(stringResource(R.string.subtitle_fr)) }, singleLine = true, shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(8.dp))
                if (imageUrl.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        loading = {},
                        error = {},
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        enabled = !uploading,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(if (uploading) stringResource(R.string.uploading_image) else stringResource(R.string.pick_from_gallery))
                    }
                    if (uploading) {
                        Spacer(Modifier.size(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(imageUrl, { imageUrl = it }, label = { Text(stringResource(R.string.image_url)) }, singleLine = true, shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    sortOrder,
                    { sortOrder = it.filter { c -> c.isDigit() } },
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
                enabled = !saving && !uploading,
                onClick = {
                    if (titleAr.trim().isBlank() || titleFr.trim().isBlank()) {
                        error = context.getString(R.string.required_fields)
                        return@Button
                    }
                    saving = true
                    scope.launch {
                        try {
                            val fields = mapOf<String, Any?>(
                                "title_ar" to titleAr.trim(),
                                "title_fr" to titleFr.trim(),
                                "subtitle_ar" to subtitleAr.trim().ifBlank { null },
                                "subtitle_fr" to subtitleFr.trim().ifBlank { null },
                                "image_url" to imageUrl.trim().ifBlank { null },
                                "sort_order" to (sortOrder.toIntOrNull() ?: 0),
                                "is_active" to isActive,
                            )
                            if (initial == null) client.createBanner(fields) else client.updateBanner(initial.id, fields)
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
