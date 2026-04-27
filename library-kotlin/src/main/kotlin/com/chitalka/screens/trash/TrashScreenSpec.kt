@file:Suppress("TooManyFunctions")

package com.chitalka.screens.trash

import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog
import com.chitalka.screens.common.BookListScreenLayout
import com.chitalka.screens.common.BookListSearchFilter
import java.util.Locale

/** Контракт экрана корзины. */
object TrashScreenSpec {

    object I18nKeys {
        const val EMPTY_LIST = "screens.cart.empty"
        const val SEARCH_NO_RESULTS = "search.noResults"
        const val RESTORE = "trash.restore"
        const val DELETE_FOREVER = "trash.deleteForever"
        const val CONFIRM_DELETE_TITLE = "trash.confirmDeleteTitle"
        const val CONFIRM_DELETE_MESSAGE = "trash.confirmDeleteMessage"
        const val DELETE_FAILED = "trash.deleteFailed"
        const val COMMON_CANCEL = "common.cancel"
        const val COMMON_MB = "common.mb"
    }

    object Layout {
        const val LIST_BOTTOM_INSET_EXTRA_DP: Int = 16
    }

    /** Фон кнопки «Удалить навсегда». */
    const val DESTRUCTIVE_BUTTON_BACKGROUND_HEX: String = "#B3261E"

    const val DESTRUCTIVE_ACTION_LABEL_HEX: String = "#FFFFFF"

    /** Заглушка обложки (эмодзи книги). */
    const val COVER_PLACEHOLDER_GLYPH: String = "\uD83D\uDCD6"

    private const val BYTES_PER_MB: Double = 1024.0 * 1024.0

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
        safeInsetBottomDp + Layout.LIST_BOTTOM_INSET_EXTRA_DP

    val emptyTextMarginTopDp: Int = BookListScreenLayout.EMPTY_TEXT_MARGIN_TOP_DP
    val emptyTextPaddingHorizontalDp: Int = BookListScreenLayout.EMPTY_TEXT_PADDING_HORIZONTAL_DP
    val emptyTextFontSp: Int = BookListScreenLayout.EMPTY_TEXT_FONT_SP
    val emptyTextLineHeightSp: Int = BookListScreenLayout.EMPTY_TEXT_LINE_HEIGHT_SP

    /** Размер файла в мегабайтах с двумя знаками после запятой и единицей измерения. */
    fun formatFileSizeMbLine(
        fileSizeBytes: Long,
        locale: AppLocale,
    ): String {
        val mb = fileSizeBytes.toDouble() / BYTES_PER_MB
        val number = String.format(Locale.US, "%.2f", mb)
        val unit = I18nCatalog.tSync(locale, I18nKeys.COMMON_MB)
        return "$number $unit"
    }

    fun restoreLabel(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.RESTORE)

    fun deleteForeverLabel(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.DELETE_FOREVER)

    fun purgeConfirmTitle(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.CONFIRM_DELETE_TITLE)

    fun purgeConfirmMessage(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.CONFIRM_DELETE_MESSAGE)

    fun purgeCancelLabel(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.COMMON_CANCEL)

    fun deleteFailedMessage(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.DELETE_FAILED)
}
