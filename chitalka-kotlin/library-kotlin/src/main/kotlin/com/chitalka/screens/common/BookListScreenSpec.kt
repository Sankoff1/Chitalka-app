package com.chitalka.screens.common

import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog

/**
 * Контракт списка книг для экранов «Сейчас читаю», «Книги и документы», «Избранное».
 * Различия между экранами — только i18n-ключ пустого состояния и наличие FAB.
 */
data class BookListScreenSpec(
    val emptyI18nKey: String,
    val hasFab: Boolean,
) {
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
            I18nCatalog.tSync(locale, SEARCH_NO_RESULTS_KEY)
        } else {
            I18nCatalog.tSync(locale, emptyI18nKey)
        }

    /**
     * Нижний padding списка. Для экранов с FAB резервирует место под кнопку и зазор;
     * для экранов без FAB — только safe-area + базовый отступ.
     */
    fun listContentBottomPaddingDp(safeInsetBottomDp: Int): Int =
        if (hasFab) {
            BookListScreenLayout.listContentBottomPaddingDp(safeInsetBottomDp)
        } else {
            safeInsetBottomDp + LIST_BOTTOM_INSET_NO_FAB_DP
        }

    companion object {
        const val SEARCH_NO_RESULTS_KEY: String = "search.noResults"
        const val ADD_BOOK_A11Y_KEY: String = "books.addBookA11y"

        private const val LIST_BOTTOM_INSET_NO_FAB_DP: Int = 16

        val ReadingNow: BookListScreenSpec =
            BookListScreenSpec(emptyI18nKey = "screens.readingNow.subtitle", hasFab = true)

        val BooksAndDocs: BookListScreenSpec =
            BookListScreenSpec(emptyI18nKey = "books.empty", hasFab = true)

        val Favorites: BookListScreenSpec =
            BookListScreenSpec(emptyI18nKey = "screens.favorites.empty", hasFab = false)

        fun fabAccessibilityLabel(locale: AppLocale): String =
            I18nCatalog.tSync(locale, ADD_BOOK_A11Y_KEY)
    }
}
