package com.chitalka.storage

import com.chitalka.i18n.AppLocale
import org.junit.Assert.assertEquals
import org.junit.Test

class StorageErrorMessagesTest {

    @Test
    fun knownCodes_returnLocalizedRussian() {
        assertEquals(
            "Не удалось открыть локальную базу данных. Проверьте свободное место и перезапустите приложение.",
            storageErrorMessage(AppLocale.RU, STORAGE_ERR_OPEN_FAILED),
        )
        assertEquals(
            "Идентификатор книги пустой.",
            storageErrorMessage(AppLocale.RU, STORAGE_ERR_INVALID_BOOK_ID),
        )
    }

    @Test
    fun knownCodes_returnLocalizedEnglish() {
        assertEquals(
            "Storage error. Please retry or clear app data.",
            storageErrorMessage(AppLocale.EN, STORAGE_ERR_GENERIC),
        )
        assertEquals(
            "Invalid scroll offset value.",
            storageErrorMessage(AppLocale.EN, STORAGE_ERR_INVALID_PROGRESS_OFFSET),
        )
    }

    @Test
    fun unknownCode_returnedAsIs() {
        assertEquals("FOO_BAR", storageErrorMessage(AppLocale.RU, "FOO_BAR"))
    }
}
