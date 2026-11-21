package com.matjard.admin.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.matjard.core.MatjardClient
import com.matjard.core.StoreSettings
import com.matjard.admin.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    client: MatjardClient,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var settings by remember { mutableStateOf<StoreSettings?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // form state (null until loaded)
    var storeNameAr by remember { mutableStateOf("") }
    var storeNameFr by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var addressAr by remember { mutableStateOf("") }
    var addressFr by remember { mutableStateOf("") }
    var hoursAr by remember { mutableStateOf("") }
    var hoursFr by remember { mutableStateOf("") }
    var deliveryFee by remember { mutableStateOf("") }
    var freeDeliveryMin by remember { mutableStateOf("") }
    var facebookUrl by remember { mutableStateOf("") }
    var instagramUrl by remember { mutableStateOf("") }
    var tiktokUrl by remember { mutableStateOf("") }

    var saving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val s = client.settings()
            settings = s
            storeNameAr = s.storeNameAr; storeNameFr = s.storeNameFr
            phone = s.phone; whatsapp = s.whatsapp ?: ""; email = s.email ?: ""
            addressAr = s.addressAr ?: ""; addressFr = s.addressFr ?: ""
            hoursAr = s.hoursAr ?: ""; hoursFr = s.hoursFr ?: ""
            deliveryFee = java.math.BigDecimal(s.deliveryFee.toString()).stripTrailingZeros().toPlainString()
            freeDeliveryMin = java.math.BigDecimal(s.freeDeliveryMin.toString()).stripTrailingZeros().toPlainString()
            facebookUrl = s.facebookUrl ?: ""; instagramUrl = s.instagramUrl ?: ""; tiktokUrl = s.tiktokUrl ?: ""
        } catch (e: Exception) {
            loadError = e.message
        }
    }

    // Same BigDecimal-safe parsing trick as the product editor
    fun numberOrNull(raw: String) = raw.trim().replace(",", ".").toDoubleOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White,
                    actionIconContentColor = androidx.compose.ui.graphics.Color.White,
                ),
                title = {
                    Column {
                        Text(stringResource(R.string.settings_title), color = androidx.compose.ui.graphics.Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text(stringResource(R.string.settings_subtitle), fontSize = 10.sp, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.75f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            saving = true
                            saved = false
                            error = null
                            scope.launch {
                                try {
                                    val fields = buildMap<String, Any?> {
                                        put("store_name_ar", storeNameAr.trim())
                                        put("store_name_fr", storeNameFr.trim())
                                        put("phone", phone.trim())
                                        put("whatsapp", whatsapp.trim().ifBlank { null })
                                        put("email", email.trim().ifBlank { null })
                                        put("address_ar", addressAr.trim().ifBlank { null })
                                        put("address_fr", addressFr.trim().ifBlank { null })
                                        put("hours_ar", hoursAr.trim().ifBlank { null })
                                        put("hours_fr", hoursFr.trim().ifBlank { null })
                                        numberOrNull(deliveryFee)?.let { put("delivery_fee", it) }
                                        numberOrNull(freeDeliveryMin)?.let { put("free_delivery_min", it) }
                                        put("facebook_url", facebookUrl.trim().ifBlank { null })
                                        put("instagram_url", instagramUrl.trim().ifBlank { null })
                                        put("tiktok_url", tiktokUrl.trim().ifBlank { null })
                                    }
                                    client.updateSettings(fields)
                                    saved = true
                                } catch (e: Exception) {
                                    error = e.message
                                } finally {
                                    saving = false
                                }
                            }
                        },
                        enabled = !saving && settings != null,
                    ) {
                        if (saving) {
                            CircularProgressIndicator(modifier = androidx.compose.ui.Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp, color = androidx.compose.ui.graphics.Color.White)
                        } else {
                            Text(stringResource(R.string.save_changes), color = androidx.compose.ui.graphics.Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            loadError != null -> Column(modifier = androidx.compose.ui.Modifier.padding(padding).padding(24.dp)) {
                Text(stringResource(R.string.error_generic), color = MaterialTheme.colorScheme.error)
                Text(loadError ?: "", fontSize = 12.sp)
            }
            settings == null -> com.matjard.admin.ui.LoadingBox()
            else -> Column(
                modifier = androidx.compose.ui.Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                if (saved) {
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    ) {
                        Row(modifier = androidx.compose.ui.Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(androidx.compose.ui.Modifier.width(8.dp))
                            Text(stringResource(R.string.saved_ok), color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                    Spacer(androidx.compose.ui.Modifier.height(12.dp))
                }

                Field(stringResource(R.string.store_name_ar), storeNameAr) { storeNameAr = it }
                Field(stringResource(R.string.store_name_fr), storeNameFr) { storeNameFr = it }
                Field(stringResource(R.string.phone), phone) { phone = it }
                Field(stringResource(R.string.whatsapp), whatsapp) { whatsapp = it }
                Field(stringResource(R.string.email), email) { email = it }
                Field(stringResource(R.string.address_ar), addressAr) { addressAr = it }
                Field(stringResource(R.string.address_fr), addressFr) { addressFr = it }
                Field(stringResource(R.string.hours_ar), hoursAr) { hoursAr = it }
                Field(stringResource(R.string.hours_fr), hoursFr) { hoursFr = it }
                HorizontalDivider(modifier = androidx.compose.ui.Modifier.padding(vertical = 6.dp))
                Field(stringResource(R.string.delivery_fee_label), deliveryFee) { deliveryFee = it.filter { c -> c.isDigit() || c == '.' } }
                Field(stringResource(R.string.free_delivery_min), freeDeliveryMin) { freeDeliveryMin = it.filter { c -> c.isDigit() || c == '.' } }
                HorizontalDivider(modifier = androidx.compose.ui.Modifier.padding(vertical = 6.dp))
                Field(stringResource(R.string.facebook_url), facebookUrl) { facebookUrl = it }
                Field(stringResource(R.string.instagram_url), instagramUrl) { instagramUrl = it }
                Field(stringResource(R.string.tiktok_url), tiktokUrl) { tiktokUrl = it }

                error?.let {
                    Spacer(androidx.compose.ui.Modifier.height(10.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
                Spacer(androidx.compose.ui.Modifier.height(28.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Field(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
    )
}
