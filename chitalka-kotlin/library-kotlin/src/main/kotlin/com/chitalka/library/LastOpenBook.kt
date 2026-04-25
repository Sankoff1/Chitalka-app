@file:Suppress("MatchingDeclarationName")
package com.chitalka.library

/**
 * Абстракция над key-value хранилищем. Реализация на Android — поверх SharedPreferences.
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

/** Возвращает исходную строку, если она не пуста после trim; иначе `null`. Trim применяется только для проверки. */
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
        // best-effort: потеря ключа не ломает текущее чтение, только автооткрытие в следующий запуск
    }
}

suspend fun clearLastOpenBookId(storage: LastOpenBookPersistence) {
    try {
        storage.removeItem(LAST_OPEN_BOOK_STORAGE_KEY)
    } catch (_: Exception) {
        // best-effort
    }
}
