@file:Suppress("MatchingDeclarationName")
package com.chitalka.library

/**
 * Абстракция над key-value (аналог `AsyncStorage` в RN).
 * Реализацию даёт Android-слой (DataStore / SharedPreferences).
 */
interface LastOpenBookPersistence {
    suspend fun getItem(key: String): String?
    suspend fun setItem(key: String, value: String)
    suspend fun removeItem(key: String)
}

/**
 * Ключ последней открытой книги: выставляется при открытии читалки
 * и очищается при возврате в библиотеку.
 */
const val LAST_OPEN_BOOK_STORAGE_KEY = "chitalka_last_open_book_id"

/** Как в RN: `v && v.trim() ? v : null` — возвращаем сырое значение, если после trim не пусто. */
private fun String?.storedBookIdOrNull(): String? {
    if (this == null) return null
    val trimmed = trim()
    return if (trimmed.isEmpty()) null else this
}

suspend fun getLastOpenBookId(storage: LastOpenBookPersistence): String? =
    try {
        storage.getItem(LAST_OPEN_BOOK_STORAGE_KEY).storedBookIdOrNull()
    } catch (_: Exception) {
        null
    }

suspend fun setLastOpenBookId(storage: LastOpenBookPersistence, bookId: String) {
    if (bookId.isBlank()) return
    try {
        storage.setItem(LAST_OPEN_BOOK_STORAGE_KEY, bookId)
    } catch (_: Exception) {
        /* best-effort: отсутствие восстановления не ломает чтение */
    }
}

suspend fun clearLastOpenBookId(storage: LastOpenBookPersistence) {
    try {
        storage.removeItem(LAST_OPEN_BOOK_STORAGE_KEY)
    } catch (_: Exception) {
        /* best-effort */
    }
}
