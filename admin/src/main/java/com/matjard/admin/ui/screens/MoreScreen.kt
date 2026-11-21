package com.matjard.admin.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
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
import com.matjard.core.MatjardClient
import com.matjard.admin.R

@Composable
fun MoreScreen(
    client: MatjardClient,
    lang: String,
    onOpenSettings: () -> Unit,
    onOpenBanners: () -> Unit,
<<<<<<< HEAD
=======
    onOpenNotifications: () -> Unit,
>>>>>>> 5425fa1 (feat: add admin notifications screen)
    isManager: Boolean = true,
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val rtl = lang == "ar"

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(stringResource(R.string.more_title), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(4.dp))
        client.tokens.userEmail?.let { email ->
            Text(
                "${stringResource(R.string.signed_in_as)}: $email",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(16.dp))

        // Store management rows — only for manager/owner
        if (isManager) {
            NavRow(
                icon = Icons.Filled.Settings,
                label = stringResource(R.string.store_settings),
                chevronRtl = rtl,
                onClick = onOpenSettings,
            )
            Spacer(Modifier.height(8.dp))
            NavRow(
                icon = Icons.Filled.Campaign,
                label = stringResource(R.string.banners_row),
                chevronRtl = rtl,
                onClick = onOpenBanners,
            )
<<<<<<< HEAD
=======
            Spacer(Modifier.height(8.dp))
            NavRow(
                icon = Icons.Filled.Notifications,
                label = stringResource(R.string.notifications_title),
                chevronRtl = rtl,
                onClick = onOpenNotifications,
            )
>>>>>>> 5425fa1 (feat: add admin notifications screen)
        }

        Spacer(Modifier.height(22.dp))
        HorizontalDivider()
        Spacer(Modifier.height(18.dp))

        // Language
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.size(10.dp))
            Text(stringResource(R.string.language), fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        Row {
            LangChoice(stringResource(R.string.lang_ar), selected = lang == "ar") {
                client.language.setLang("ar")
                activity?.recreate()
            }
            Spacer(Modifier.size(10.dp))
            LangChoice(stringResource(R.string.lang_fr), selected = lang == "fr") {
                client.language.setLang("fr")
                activity?.recreate()
            }
        }

        Spacer(Modifier.height(22.dp))
        HorizontalDivider()
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(R.string.app_version, "1.0.0"),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun NavRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    chevronRtl: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.size(12.dp))
            Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            Icon(
                if (chevronRtl) Icons.Filled.ChevronLeft else Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LangChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        androidx.compose.material3.FilledTonalButton(onClick = onClick, shape = RoundedCornerShape(20.dp)) {
            Text(label, fontWeight = FontWeight.Bold)
        }
    } else {
        androidx.compose.material3.FilledTonalButton(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
