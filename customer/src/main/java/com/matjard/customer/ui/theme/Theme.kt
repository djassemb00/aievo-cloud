package com.matjard.customer.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TealPrimary = Color(0xFF0F766E)
private val TealDark = Color(0xFF0B4F49)
private val TealSoft = Color(0xFFCCFBF1)
private val Amber = Color(0xFFF59E0B)
private val Ink = Color(0xFF134E4A)
private val Background = Color(0xFFFAFAF9)
private val Surface = Color(0xFFFFFFFF)

private val CustomerColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealSoft,
    onPrimaryContainer = Ink,
    secondary = Amber,
    onSecondary = Color(0xFF422006),
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF422006),
    background = Background,
    onBackground = Color(0xFF1C1917),
    surface = Surface,
    onSurface = Color(0xFF1C1917),
    surfaceVariant = Color(0xFFF5F5F4),
    onSurfaceVariant = Color(0xFF57534E),
    outline = Color(0xFFE7E5E4),
    error = Color(0xFFDC2626),
)

@Composable
fun MatjarCustomerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CustomerColors,
        content = content,
    )
}
