package com.chitalka.ui.bookactions

import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog
import kotlin.math.max

/** Контракт нижнего листа действий с книгой. */
object BookActionsSheetSpec {

    object I18nKeys {
        const val SHEET_TITLE = "bookActions.title"
        const val ADD_TO_FAVORITES = "bookActions.addToFavorites"
        const val REMOVE_FROM_FAVORITES = "bookActions.removeFromFavorites"
        const val MOVE_TO_TRASH = "bookActions.moveToTrash"
        const val COMMON_CANCEL = "common.cancel"
    }

    object Animation {
        const val OPEN_DURATION_MS: Int = 220
        const val SHEET_SLIDE_FROM_DP: Int = 60
        const val BACKDROP_MAX_OPACITY: Float = 0.55f
    }

    object Layout {
        const val SHEET_TOP_RADIUS_DP: Int = 20
        const val SHEET_PADDING_TOP_DP: Int = 8
        const val SHEET_PADDING_HORIZONTAL_DP: Int = 16
        const val SHEET_BOTTOM_PADDING_MIN_INSET_DP: Int = 12
        const val SHEET_BOTTOM_PADDING_EXTRA_DP: Int = 8
        const val GRABBER_WIDTH_DP: Int = 40
        const val GRABBER_HEIGHT_DP: Int = 4
        const val GRABBER_CORNER_DP: Int = 2
        const val GRABBER_MARGIN_BOTTOM_DP: Int = 12
        const val HEADER_ROW_GAP_DP: Int = 12
        const val HEADER_PADDING_VERTICAL_DP: Int = 4
        const val COVER_WIDTH_DP: Int = 48
        const val COVER_HEIGHT_DP: Int = 68
        const val COVER_CORNER_DP: Int = 6
        const val COVER_FALLBACK_GLYPH_SP: Int = 22
        const val HEADER_TEXT_GAP_DP: Int = 2
        const val TITLE_FONT_SP: Int = 14
        const val TITLE_LINE_HEIGHT_SP: Int = 18
        const val AUTHOR_FONT_SP: Int = 13
        const val AUTHOR_LINE_HEIGHT_SP: Int = 18
        const val DIVIDER_MARGIN_TOP_DP: Int = 12
        const val DIVIDER_MARGIN_BOTTOM_DP: Int = 4
        const val DIVIDER_HEIGHT_DP: Int = 1
        const val ACTIONS_PADDING_VERTICAL_DP: Int = 4
        const val ACTION_ROW_PADDING_VERTICAL_DP: Int = 14
        const val ACTION_ROW_PADDING_HORIZONTAL_DP: Int = 8
        const val ACTION_ROW_CORNER_DP: Int = 10
        const val ACTION_ICON_SLOT_WIDTH_DP: Int = 28
        const val ACTION_ICON_SIZE_DP: Int = 22
        const val ACTION_LABEL_FONT_SP: Int = 15
        const val CANCEL_MARGIN_TOP_DP: Int = 8
        const val CANCEL_PADDING_VERTICAL_DP: Int = 14
        const val CANCEL_CORNER_DP: Int = 12
        const val CANCEL_LABEL_FONT_SP: Int = 15
        const val CANCEL_PRESSED_OPACITY: Float = 0.85f
    }

    /** Нижний отступ листа: max(insets.bottom, минимум) + хвостовой extra. */
    fun sheetBottomPaddingDp(safeInsetBottomDp: Int): Int =
        max(safeInsetBottomDp, Layout.SHEET_BOTTOM_PADDING_MIN_INSET_DP) + Layout.SHEET_BOTTOM_PADDING_EXTRA_DP

    /** Цвет деструктивного действия. */
    const val DESTRUCTIVE_ACTION_HEX: String = "#D93A3A"

    object MaterialIcons {
        const val FAVORITE = "favorite"
        const val FAVORITE_BORDER = "favorite-border"
        const val DELETE_OUTLINE = "delete-outline"
    }

    /** Заглушка обложки (эмодзи книги). */
    const val COVER_PLACEHOLDER_GLYPH: String = "\uD83D\uDCD6"

    fun sheetTitle(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.SHEET_TITLE)

    fun cancelLabel(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.COMMON_CANCEL)

    fun moveToTrashLabel(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.MOVE_TO_TRASH)

    fun favoriteActionIconName(isFavorite: Boolean): String =
        if (isFavorite) {
            MaterialIcons.FAVORITE
        } else {
            MaterialIcons.FAVORITE_BORDER
        }

    fun favoriteActionLabel(
        locale: AppLocale,
        isFavorite: Boolean,
    ): String =
        if (isFavorite) {
            I18nCatalog.tSync(locale, I18nKeys.REMOVE_FROM_FAVORITES)
        } else {
            I18nCatalog.tSync(locale, I18nKeys.ADD_TO_FAVORITES)
        }
}
