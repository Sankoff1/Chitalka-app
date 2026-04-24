@file:Suppress("TooManyFunctions")

package com.chitalka.screens.trash

import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog
import com.chitalka.screens.common.BookListScreenLayout
import com.chitalka.screens.common.BookListSearchFilter
import java.util.Locale

/**
 * Экран корзины без React / Storage / `Alert` (`TrashScreen.tsx`).
 */
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
        const val LIST_CONTENT_PADDING_DP: Int = 16
        const val CARD_CORNER_RADIUS_DP: Int = 12
        const val CARD_PADDING_DP: Int = 12
        const val CARD_MARGIN_BOTTOM_DP: Int = 12
        const val ROW_GAP_DP: Int = 12
        const val COVER_WIDTH_DP: Int = 72
        private const val COVER_HEIGHT_NUM: Int = 145
        private const val COVER_HEIGHT_DEN: Int = 100

        fun coverHeightDp(): Int = COVER_WIDTH_DP * COVER_HEIGHT_NUM / COVER_HEIGHT_DEN

        const val COVER_CORNER_RADIUS_DP: Int = 8
        const val COVER_PLACEHOLDER_FONT_SP: Int = 28
        const val TEXT_BLOCK_GAP_DP: Int = 4
        const val TITLE_FONT_SP: Int = 17
        const val TITLE_LINE_HEIGHT_SP: Int = 22
        const val AUTHOR_FONT_SP: Int = 14
        const val AUTHOR_LINE_HEIGHT_SP: Int = 19
        const val SIZE_FONT_SP: Int = 12
        const val SIZE_MARGIN_TOP_DP: Int = 2
        const val ACTIONS_GAP_DP: Int = 8
        const val ACTIONS_MARGIN_TOP_DP: Int = 12
        const val ACTION_BUTTON_PADDING_VERTICAL_DP: Int = 10
        const val ACTION_BUTTON_CORNER_RADIUS_DP: Int = 8
        const val ACTION_LABEL_FONT_SP: Int = 14
        const val PRESSED_OPACITY: Float = 0.85f
    }

    /** Кнопка «Удалить навсегда» (фикс в RN). */
    const val DESTRUCTIVE_BUTTON_BACKGROUND_HEX: String = "#B3261E"

    const val DESTRUCTIVE_ACTION_LABEL_HEX: String = "#FFFFFF"

    /** Заглушка обложки — как в `TrashScreen`. */
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

    /** Как `(item.fileSizeBytes / (1024 * 1024)).toFixed(2) + ' ' + t('common.mb')` в RN. */
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
