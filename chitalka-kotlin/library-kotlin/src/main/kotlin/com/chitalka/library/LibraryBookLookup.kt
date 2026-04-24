package com.chitalka.library

import com.chitalka.core.types.LibraryBookRecord

/**
 * Минимальный контракт для автооткрытия последней книги (`LibraryContext.tsx`).
 * Реализуется [com.chitalka.storage.StorageService] на Android.
 */
fun interface LibraryBookLookup {
    suspend fun getLibraryBook(bookId: String): LibraryBookRecord?
}
