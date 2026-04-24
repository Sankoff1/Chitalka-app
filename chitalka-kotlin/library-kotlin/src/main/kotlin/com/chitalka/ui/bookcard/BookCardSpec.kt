package com.chitalka.ui.bookcard

import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog
import kotlin.math.roundToInt

/**
 * Карточка книги без Image/Pressable (`BookCard.tsx`).
 */
object BookCardSpec {

    private const val PERCENT_SCALE: Double = 100.0

    object I18nKeys {
        const val READ_PERCENT = "books.readPercent"
        const val A11Y_OPEN_MENU = "a11y.openMenu"
    }

    object Layout {
        const val CARD_CORNER_RADIUS_DP: Int = 12
        const val CARD_MARGIN_BOTTOM_DP: Int = 12
        const val CARD_PADDING_DP: Int = 12
        const val ROW_GAP_DP: Int = 12
        const val COVER_WIDTH_DP: Int = 72
        private const val COVER_HEIGHT_TO_WIDTH_NUM: Int = 145
        private const val COVER_HEIGHT_TO_WIDTH_DEN: Int = 100

        fun coverHeightDp(): Int = COVER_WIDTH_DP * COVER_HEIGHT_TO_WIDTH_NUM / COVER_HEIGHT_TO_WIDTH_DEN

        const val COVER_CORNER_RADIUS_DP: Int = 8
        const val COVER_FALLBACK_PADDING_HORIZONTAL_DP: Int = 6
        const val COVER_FALLBACK_PADDING_VERTICAL_DP: Int = 10
        const val COVER_ACCENT_BAR_HEIGHT_DP: Int = 4
        const val COVER_TITLE_FONT_SP: Int = 11
        const val COVER_TITLE_LINE_HEIGHT_SP: Int = 13
        const val COVER_RULE_WIDTH_DP: Int = 28
        const val COVER_RULE_HEIGHT_DP: Int = 1
        const val COVER_RULE_MARGIN_VERTICAL_DP: Int = 6
        const val COVER_AUTHOR_FONT_SP: Int = 9
        const val COVER_AUTHOR_LINE_HEIGHT_SP: Int = 11
        const val FAVORITE_BADGE_SIZE_DP: Int = 22
        const val FAVORITE_BADGE_INSET_DP: Int = 4
        const val FAVORITE_GLYPH_FONT_SP: Int = 13
        const val FAVORITE_GLYPH_LINE_HEIGHT_SP: Int = 14
        const val MENU_BUTTON_SIZE_DP: Int = 32
        const val MENU_BUTTON_RIGHT_INSET_DP: Int = 6
        const val MENU_BUTTON_HIT_SLOP_DP: Int = 6
        const val TEXT_BLOCK_EXTRA_END_PADDING_WITH_MENU_DP: Int = 10
        const val TITLE_FONT_SP: Int = 17
        const val TITLE_LINE_HEIGHT_SP: Int = 22
        const val AUTHOR_FONT_SP: Int = 14
        const val AUTHOR_LINE_HEIGHT_SP: Int = 19
        const val TEXT_BLOCK_TITLE_AUTHOR_GAP_DP: Int = 4
        const val PROGRESS_ROW_MARGIN_TOP_DP: Int = 6
        const val PROGRESS_ROW_GAP_DP: Int = 4
        const val PROGRESS_TRACK_HEIGHT_DP: Int = 6
        const val PROGRESS_TRACK_CORNER_DP: Int = 3
        const val PROGRESS_LABEL_FONT_SP: Int = 12
        const val PRESSED_OPACITY: Float = 0.9f
        const val LONG_PRESS_DELAY_MS: Long = 350L
    }

    /** К `colors.textSecondary` для рамки обложки (`+ '33'` в RN). */
    const val COVER_BORDER_ALPHA_SUFFIX: String = "33"

    /** К `colors.textSecondary` для разделителя в fallback-обложке (`+ '55'`). */
    const val COVER_RULE_ALPHA_SUFFIX: String = "55"

    object Colors {
        const val FAVORITE_GLYPH_HEX: String = "#FF5A7A"
        const val MENU_ICON_ON_SCRIM_HEX: String = "#FFFFFF"
        const val OVERLAY_SCRIM_ALPHA: Float = 0.55f
    }

    object MaterialIcons {
        const val MENU_INFO_OUTLINE = "info-outline"
        const val MENU_ICON_SIZE_DP: Int = 20
    }

    /**
     * Ограничение доли прогресса 0..1 (`clampFraction` в RN).
     */
    fun clampProgressFraction(progress: Double): Double {
        if (!progress.isFinite()) {
            return 0.0
        }
        return progress.coerceIn(0.0, 1.0)
    }

    /** Показывать ли блок прогресса: в RN `typeof progress === 'number'` (в т.ч. NaN даёт 0 %). */
    fun hasProgressValue(progress: Double?): Boolean = progress != null

    fun progressPercentRounded(progress: Double): Int =
        (clampProgressFraction(progress) * PERCENT_SCALE).roundToInt()

    fun readPercentLabel(
        locale: AppLocale,
        percent: Int,
    ): String =
        I18nCatalog.tSync(
            locale,
            I18nKeys.READ_PERCENT,
            mapOf("percent" to percent),
        )

    /** Дополнительный `paddingEnd` текстового блока при кнопке меню (`COVER_MENU_BTN + 10`). */
    fun textBlockEndPaddingWithMenuDp(): Int =
        Layout.MENU_BUTTON_SIZE_DP + Layout.TEXT_BLOCK_EXTRA_END_PADDING_WITH_MENU_DP
}
