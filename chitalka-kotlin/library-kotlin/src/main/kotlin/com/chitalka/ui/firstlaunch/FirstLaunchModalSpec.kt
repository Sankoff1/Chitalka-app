package com.chitalka.ui.firstlaunch

import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog

/**
 * Спецификация модалки первого запуска / пустой библиотеки (`FirstLaunchModal.tsx` без Modal/Pressable).
 */
object FirstLaunchModalSpec {

    /** Ключи `t('firstLaunch.*')` в RN. */
    object I18nKeys {
        const val MESSAGE = "firstLaunch.message"
        const val CANCEL = "firstLaunch.cancel"
        const val PICK_EPUB = "firstLaunch.pickEpub"
    }

    /**
     * Числа из `StyleSheet` в RN (логические единицы ≈ dp/sp на Android).
     * Граница карточки — `hairline`; в спецификации 1 dp, в UI использовать `Divider` / `hairline`.
     */
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

    /** Фиксированные цвета из RN (остальное из темы: `menuBackground`, `text`, `interactive`, …). */
    object Colors {
        /** Подсказка ошибки (`#a33` в TS). */
        const val HINT_HEX: String = "#aa3333"

        /** `rgba(0,0,0,0.45)` для затемнения под модалкой. */
        const val OVERLAY_SCRIM_ALPHA: Float = 0.45f
    }

    /**
     * Суффикс альфы к `colors.textSecondary` для рамки карточки (`textSecondary + '44'` в RN, ~27 %).
     */
    const val CARD_BORDER_ALPHA_SUFFIX: String = "44"

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
