package com.chitalka.i18n

import org.junit.Assert.assertEquals
import org.junit.Test

class I18nCatalogTest {

    @Test
    fun tSync_nestedPath_ru() {
        assertEquals("Настройки", I18nCatalog.tSync(AppLocale.RU, "drawer.settings"))
    }

    @Test
    fun tSync_nestedPath_en() {
        assertEquals("Settings", I18nCatalog.tSync(AppLocale.EN, "drawer.settings"))
    }

    @Test
    fun tSync_unknownPath_returnsPath() {
        assertEquals("no.such.key", I18nCatalog.tSync(AppLocale.EN, "no.such.key"))
    }

    @Test
    fun tSync_interpolation() {
        assertEquals(
            "50% read",
            I18nCatalog.tSync(AppLocale.EN, "books.readPercent", mapOf("percent" to 50)),
        )
        assertEquals(
            "50% прочитано",
            I18nCatalog.tSync(AppLocale.RU, "books.readPercent", mapOf("percent" to 50)),
        )
    }

    @Test
    fun tSync_interpolation_missingVar_becomesEmpty() {
        assertEquals(
            "% read",
            I18nCatalog.tSync(AppLocale.EN, "books.readPercent", emptyMap()),
        )
    }

    @Test
    fun bookFallbackLabels() {
        val en = I18nCatalog.bookFallbackLabels(AppLocale.EN)
        assertEquals("Untitled", en.untitled)
        assertEquals("Unknown author", en.unknownAuthor)
        val ru = I18nCatalog.bookFallbackLabels(AppLocale.RU)
        assertEquals("Без названия", ru.untitled)
        assertEquals("Неизвестный автор", ru.unknownAuthor)
    }
}
