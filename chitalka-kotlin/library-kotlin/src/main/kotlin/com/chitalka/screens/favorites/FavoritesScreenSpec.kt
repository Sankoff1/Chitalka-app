package com.chitalka.screens.favorites

import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog
import com.chitalka.screens.common.BookListScreenLayout
import com.chitalka.screens.common.BookListSearchFilter

/**
 * Экран «Избранное» без React / Storage (`FavoritesScreen.tsx`).
 * FAB нет — нижний padding списка только `insets.bottom + 16`.
 */
object FavoritesScreenSpec {

    object I18nKeys {
        const val EMPTY_LIST = "screens.favorites.empty"
        const val SEARCH_NO_RESULTS = "search.noResults"
    }

    /** Как `contentContainerStyle` paddingBottom в RN (без кнопки «+»). */
    private const val LIST_BOTTOM_INSET_EXTRA_DP: Int = 16

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

    fun listContentBottomPaddingDp(safeInsetBottomDp: Int): Int =
        safeInsetBottomDp + LIST_BOTTOM_INSET_EXTRA_DP

    /** Отступы пустого состояния и списка совпадают с другими book-list экранами. */
    val listContentPaddingDp: Int = BookListScreenLayout.LIST_CONTENT_PADDING_DP
    val emptyTextMarginTopDp: Int = BookListScreenLayout.EMPTY_TEXT_MARGIN_TOP_DP
    val emptyTextPaddingHorizontalDp: Int = BookListScreenLayout.EMPTY_TEXT_PADDING_HORIZONTAL_DP
    val emptyTextFontSp: Int = BookListScreenLayout.EMPTY_TEXT_FONT_SP
    val emptyTextLineHeightSp: Int = BookListScreenLayout.EMPTY_TEXT_LINE_HEIGHT_SP
}
