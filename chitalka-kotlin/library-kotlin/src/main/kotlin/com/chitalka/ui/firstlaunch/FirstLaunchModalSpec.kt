package com.chitalka.ui.firstlaunch

import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog

/** Спецификация модалки первого запуска / пустой библиотеки. */
object FirstLaunchModalSpec {

    /** i18n-ключи модалки. */
    object I18nKeys {
        const val MESSAGE = "firstLaunch.message"
        const val CANCEL = "firstLaunch.cancel"
        const val PICK_EPUB = "firstLaunch.pickEpub"
    }

    /** Геометрия модалки в dp/sp. */
    object Layout {
        const val OVERLAY_HORIZONTAL_PADDING_DP: Int = 28
        const val CARD_CORNER_RADIUS_DP: Int = 14
        const val CARD_PADDING_DP: Int = 22
        const val CARD_BORDER_WIDTH_DP: Int = 1
        const val MESSAGE_TEXT_SIZE_SP: Int = 16
        const val MESSAGE_LINE_HEIGHT_SP: Int = 23
        const val HINT_MARGIN_TOP_DP: Int = 12
        const val HINT_TEXT_SIZE_SP: Int = 14
        const val HINT_LINE_HEIGHT_SP: Int = 20
        const val BUTTON_ROW_MARGIN_TOP_DP: Int = 22
        const val BUTTON_ROW_GAP_DP: Int = 12
        const val BUTTON_PADDING_VERTICAL_DP: Int = 14
        const val BUTTON_CORNER_RADIUS_DP: Int = 10
        const val PRESSED_OPACITY: Float = 0.88f
    }

    /** Фиксированные цвета модалки; остальное берётся из темы. */
    object Colors {
        /** Подсказка ошибки. */
        const val HINT_HEX: String = "#aa3333"

        /** Затемнение под модалкой (~45 %). */
        const val OVERLAY_SCRIM_ALPHA: Float = 0.45f
    }

    data class Strings(
        val message: String,
        val cancel: String,
        val pickEpub: String,
    )

    fun strings(locale: AppLocale): Strings =
        Strings(
            message = I18nCatalog.tSync(locale, I18nKeys.MESSAGE),
            cancel = I18nCatalog.tSync(locale, I18nKeys.CANCEL),
            pickEpub = I18nCatalog.tSync(locale, I18nKeys.PICK_EPUB),
        )
}
