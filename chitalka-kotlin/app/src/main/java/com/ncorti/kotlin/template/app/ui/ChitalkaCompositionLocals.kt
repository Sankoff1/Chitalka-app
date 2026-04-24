package com.ncorti.kotlin.template.app.ui

import androidx.compose.runtime.compositionLocalOf
import com.chitalka.i18n.AppLocale
import com.chitalka.theme.ThemeColors
import com.chitalka.theme.ThemeMode

val LocalChitalkaLocale = compositionLocalOf { AppLocale.RU }

val LocalChitalkaThemeMode = compositionLocalOf { ThemeMode.LIGHT }

val LocalChitalkaThemeColors = compositionLocalOf { com.chitalka.theme.lightThemeColors }
