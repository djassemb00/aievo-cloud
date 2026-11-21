package com.matjard.admin.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SlateInk = Color(0xFF0F172A)
private val SlatePanel = Color(0xFF1E293B)
private val SlateSoft = Color(0xFFE2E8F0)
private val Amber = Color(0xFFF59E0B)

private val AdminColors = lightColorScheme(
    primary = SlateInk,
    onPrimary = Color.White,
    primaryContainer = SlateSoft,
    onPrimaryContainer = SlateInk,
    secondary = Amber,
    onSecondary = Color(0xFF422006),
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF422006),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0),
    error = Color(0xFFDC2626),
)

@Composable
fun MatjarAdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AdminColors,
        content = content,
    )
}
