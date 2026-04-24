package com.chitalka.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ThemeColorsTest {

    @Test
    fun getColorsForMode_matchesTypeScript() {
        assertEquals(lightThemeColors, getColorsForMode(ThemeMode.LIGHT))
        assertEquals(darkThemeColors, getColorsForMode(ThemeMode.DARK))
    }

    @Test
    fun lightPalette_sampleHex() {
        assertEquals("#2A7833", lightThemeColors.topBar)
        assertEquals("#EBFADD", lightThemeColors.background)
    }

    @Test
    fun darkPalette_sampleHex() {
        assertEquals("#00480A", darkThemeColors.topBar)
        assertEquals("#E6F0E2", darkThemeColors.text)
    }

    @Test
    fun themeMode_fromCode() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromCode("light"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromCode("dark"))
        assertNull(ThemeMode.fromCode("LIGHT"))
    }
}
