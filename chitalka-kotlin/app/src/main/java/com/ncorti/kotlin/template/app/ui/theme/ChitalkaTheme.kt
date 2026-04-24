@file:Suppress("MagicNumber")

package com.ncorti.kotlin.template.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.chitalka.theme.ThemeColors
import com.chitalka.theme.ThemeMode

private fun parseHex(hex: String): Color {
    val s = hex.trim().removePrefix("#")
    val argb: Int =
        when (s.length) {
            6 -> (0xFF shl 24) or (s.toLong(16).toInt() and 0xFFFFFF)
            8 -> (s.toLong(16) and 0xFFFFFFFFL).toInt()
            else -> 0xFF000000.toInt()
        }
    return Color(argb)
}

private fun ThemeColors.toLightScheme(): ColorScheme {
    val bg = parseHex(background)
    val surface = parseHex(menuBackground)
    val primary = parseHex(topBar)
    val onPrimary = parseHex(topBarText)
    val onBg = parseHex(text)
    val onSurfaceVariant = parseHex(textSecondary)
    val interactive = parseHex(interactive)
    return lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = interactive,
        onPrimaryContainer = onBg,
        secondary = interactive,
        onSecondary = onBg,
        background = bg,
        onBackground = onBg,
        surface = surface,
        onSurface = onBg,
        surfaceVariant = surface,
        onSurfaceVariant = onSurfaceVariant,
    )
}

private fun ThemeColors.toDarkScheme(): ColorScheme {
    val bg = parseHex(background)
    val surface = parseHex(menuBackground)
    val primary = parseHex(topBar)
    val onPrimary = parseHex(topBarText)
    val onBg = parseHex(text)
    val onSurfaceVariant = parseHex(textSecondary)
    val interactive = parseHex(interactive)
    return darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = interactive,
        onPrimaryContainer = onBg,
        secondary = interactive,
        onSecondary = onBg,
        background = bg,
        onBackground = onBg,
        surface = surface,
        onSurface = onBg,
        surfaceVariant = surface,
        onSurfaceVariant = onSurfaceVariant,
    )
}

@Composable
fun ChitalkaMaterialTheme(
    mode: ThemeMode,
    colors: ThemeColors,
    content: @Composable () -> Unit,
) {
    val scheme =
        when (mode) {
            ThemeMode.LIGHT -> colors.toLightScheme()
            ThemeMode.DARK -> colors.toDarkScheme()
        }
    MaterialTheme(
        colorScheme = scheme,
        content = content,
    )
}
