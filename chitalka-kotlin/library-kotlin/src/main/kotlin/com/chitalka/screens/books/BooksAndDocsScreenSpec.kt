package com.chitalka.screens.books

import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog
import com.chitalka.screens.common.BookListScreenLayout
import com.chitalka.screens.common.BookListSearchFilter

/**
 * Экран «Книги и документы» без React / Storage (`BooksAndDocsScreen.tsx`).
 */
object BooksAndDocsScreenSpec {

    object I18nKeys {
        const val EMPTY_LIST = "books.empty"
        const val SEARCH_NO_RESULTS = "search.noResults"
        const val ADD_BOOK_A11Y = "books.addBookA11y"
    }

    fun normalizeSearchQuery(raw: String): String =
        BookListSearchFilter.normalizeBookListSearchQuery(raw)

    fun visibleBooksForSearch(
        books: List<LibraryBookWithProgress>,
        normalizedQuery: String,
    ): List<LibraryBookWithProgress> =
        BookListSearchFilter.filterBooksByNormalizedSearchQuery(books, normalizedQuery)

    fun emptyListMessage(
        locale: AppLocale,
        hasActiveSearch: Boolean,
    ): String =
        if (hasActiveSearch) {
            I18nCatalog.tSync(locale, I18nKeys.SEARCH_NO_RESULTS)
        } else {
            I18nCatalog.tSync(locale, I18nKeys.EMPTY_LIST)
        }

    fun fabAccessibilityLabel(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.ADD_BOOK_A11Y)

    fun fabBottomOffsetDp(safeInsetBottomDp: Int): Int =
        BookListScreenLayout.fabBottomOffsetDp(safeInsetBottomDp)

    fun listContentBottomPaddingDp(safeInsetBottomDp: Int): Int =
        BookListScreenLayout.listContentBottomPaddingDp(safeInsetBottomDp)
}
