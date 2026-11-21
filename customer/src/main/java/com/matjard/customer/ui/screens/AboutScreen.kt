package com.matjard.customer.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity
import com.matjard.core.MatjardClient
import com.matjard.core.StoreSettings
import com.matjard.core.formatPrice
import com.matjard.customer.R

@Composable
fun AboutScreen(
    client: MatjardClient,
    settings: StoreSettings?,
    lang: String,
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // Store identity card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(84.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "D",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = androidx.compose.ui.graphics.Color.White,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = settings?.storeName(lang) ?: stringResource(R.string.app_name),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.cash_on_delivery),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(stringResource(R.string.contact_us), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        settings?.let { s ->
            // Phone
            InfoRow(Icons.Filled.Call, s.phone) {
                runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${s.phone}"))) }
            }
            // WhatsApp
            s.whatsapp?.let { wa ->
                val digits = wa.filter { it.isDigit() }.let { d -> if (d.startsWith("0")) "213${d.drop(1)}" else d }
                InfoRow(Icons.Filled.Chat, "+$digits") {
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits"))) }
                }
            }
            // Email
            s.email?.let { email ->
                InfoRow(Icons.Filled.Email, email) {
                    runCatching { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))) }
                }
            }
            // Address
            s.address(lang)?.let { address ->
                InfoRow(Icons.Filled.LocationOn, address) {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(address)}")))
                    }
                }
            }
            // Hours
            s.hours(lang)?.let { hours ->
                InfoRow(Icons.Filled.AccessTime, hours) {}
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(
                    R.string.delivery_info,
                    formatPrice(s.deliveryFee),
                    formatPrice(s.freeDeliveryMin),
                ),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))

            // Social links
            Row {
                s.facebookUrl?.let { url -> SmallLink("Facebook") { openUrl(context, url) } }
                s.instagramUrl?.let { url -> SmallLink("Instagram") { openUrl(context, url) } }
                s.tiktokUrl?.let { url -> SmallLink("TikTok") { openUrl(context, url) } }
            }
        } ?: Text(stringResource(R.string.loading), color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(18.dp))
        HorizontalDivider()
        Spacer(Modifier.height(18.dp))

        // Language switcher
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.size(10.dp))
            Text(stringResource(R.string.language), fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        Row {
            LanguageButton(
                label = stringResource(R.string.lang_ar),
                selected = lang == "ar",
            ) {
                client.language.setLang("ar")
                activity?.recreate()
            }
            Spacer(Modifier.size(10.dp))
            LanguageButton(
                label = stringResource(R.string.lang_fr),
                selected = lang == "fr",
            ) {
                client.language.setLang("fr")
                activity?.recreate()
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.app_version, "1.0.0"),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.size(12.dp))
            Text(text, modifier = Modifier.weight(1f), fontSize = 14.sp)
        }
    }
}

@Composable
private fun SmallLink(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(label, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun LanguageButton(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        FilledTonalButton(onClick = onClick, shape = RoundedCornerShape(20.dp)) {
            Text(label, fontWeight = FontWeight.Bold)
        }
    } else {
        FilledTonalButton(onClick = onClick, shape = RoundedCornerShape(20.dp), colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    val normalized = if (url.startsWith("http")) url else "https://$url"
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalized))) }
}
