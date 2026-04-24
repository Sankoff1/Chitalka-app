package com.chitalka.screens.common

import com.chitalka.core.types.LibraryBookWithProgress
import java.util.Locale

/** Общая фильтрация списков книг по строке поиска (`visibleBooks` в экранах библиотеки RN). */
object BookListSearchFilter {

    /** Как `searchQuery.trim().toLocaleLowerCase()` в RN. */
    fun normalizeBookListSearchQuery(raw: String): String =
        raw.trim().lowercase(Locale.getDefault())

    fun filterBooksByNormalizedSearchQuery(
        books: List<LibraryBookWithProgress>,
        normalizedQuery: String,
    ): List<LibraryBookWithProgress> {
        if (normalizedQuery.isEmpty()) {
            return books
        }
        val locale = Locale.getDefault()
        return books.filter { b ->
            b.title.lowercase(locale).contains(normalizedQuery) ||
                b.author.lowercase(locale).contains(normalizedQuery)
        }
    }
}
