package com.chitalka.library

import com.chitalka.storage.StorageService

/**
 * Обновляет [LibrarySessionState.bookCount] из SQLite (аналог `refreshBookCount` в `LibraryContext.tsx`).
 */
suspend fun LibrarySessionState.refreshBookCount(storage: StorageService) {
    try {
        updateBookCount(storage.countLibraryBooks())
    } catch (_: Exception) {
        updateBookCount(0)
    }
}
