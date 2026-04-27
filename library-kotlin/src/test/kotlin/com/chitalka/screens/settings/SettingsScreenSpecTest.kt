package com.chitalka.screens.settings

import com.chitalka.i18n.AppLocale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsScreenSpecTest {

    @Test
    fun shouldOpenLanguageMenuAbove() {
        val anchorY = 400f
        val anchorH = 48f
        val winTall = 800f
        assertFalse(SettingsScreenSpec.shouldOpenLanguageMenuAbove(winTall, anchorY, anchorH))
        val winShort = 500f
        assertTrue(SettingsScreenSpec.shouldOpenLanguageMenuAbove(winShort, anchorY, anchorH))
    }

    @Test
    fun languageMenuTopPx() {
        assertEquals(447f, SettingsScreenSpec.languageMenuTopPx(400f, 48f, openAbove = false), 0f)
        assertEquals(304f, SettingsScreenSpec.languageMenuTopPx(400f, 48f, openAbove = true), 0f)
    }

    @Test
    fun languageOptionLabelsRu() {
        val ru = AppLocale.RU
        assertEquals("Русский", SettingsScreenSpec.languageOptionLabel(AppLocale.RU, ru))
        assertEquals("English", SettingsScreenSpec.languageOptionLabel(AppLocale.EN, ru))
    }

    @Test
    fun themeStringsRu() {
        val ru = AppLocale.RU
        assertEquals("Тема оформления", SettingsScreenSpec.themeSectionLabel(ru))
        assertEquals("Тёмная тема", SettingsScreenSpec.darkThemeLabel(ru))
        assertEquals("Язык интерфейса (Language)", SettingsScreenSpec.languageSectionLabel(ru))
        assertEquals("Версия приложения", SettingsScreenSpec.versionLabel(ru))
    }
}
