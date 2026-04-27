@file:Suppress("MatchingDeclarationName")

package com.chitalka.i18n

import com.chitalka.library.LastOpenBookPersistence

/** Снимок для UI: текущая локаль и синхронный перевод. */
data class I18nUiState(
    val locale: AppLocale,
) {
    fun t(path: String, vars: Map<String, Any>? = null): String =
        I18nCatalog.tSync(locale, path, vars)
}

/**
 * Чтение сохранённой локали. Невалидное или отсутствующее значение → `null`
 * (вызывающий применит дефолт).
 */
suspend fun loadPersistedLocale(storage: LastOpenBookPersistence): AppLocale? =
    try {
        val stored = storage.getItem(LOCALE_STORAGE_KEY) ?: return null
        AppLocale.fromCode(stored)
    } catch (_: Exception) {
        null
    }

/** Сохранение локали; ошибки глотаются — выбор языка не критичен для работы. */
suspend fun persistLocale(storage: LastOpenBookPersistence, locale: AppLocale) {
    try {
        storage.setItem(LOCALE_STORAGE_KEY, locale.code)
    } catch (_: Exception) {
        // best-effort
    }
}
