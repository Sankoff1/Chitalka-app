package com.chitalka.storage

import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog

/**
 * Маппинг кодов из [StorageErrorCodes] на локализованные сообщения каталога.
 * Неизвестный код возвращается как есть — лучше показать «STORAGE_GENERIC», чем пустую строку.
 */
fun storageErrorMessage(locale: AppLocale, code: String): String {
    val key =
        when (code) {
            STORAGE_ERR_OPEN_FAILED -> "storage.errors.openFailed"
            STORAGE_ERR_GENERIC -> "storage.errors.generic"
            STORAGE_ERR_INVALID_BOOK_ID -> "storage.errors.invalidBookId"
            STORAGE_ERR_INVALID_PROGRESS_OFFSET -> "storage.errors.invalidProgressOffset"
            else -> return code
        }
    return I18nCatalog.tSync(locale, key)
}
