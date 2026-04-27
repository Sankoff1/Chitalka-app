@file:Suppress("TooManyFunctions")

package com.chitalka.screens.settings

import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog
import kotlin.math.round

/** Контракт экрана настроек. */
object SettingsScreenSpec {

    /** Высота меню выбора языка (две строки по 48 + разделитель). */
    const val LANGUAGE_MENU_ESTIMATE_PX: Int = 97

    const val APP_VERSION_FALLBACK: String = "—"

    object I18nKeys {
        const val THEME_SECTION = "settings.themeSection"
        const val DARK_THEME = "settings.darkTheme"
        const val LANGUAGE_SECTION = "settings.languageSection"
        const val LANGUAGE_RU = "settings.languageRu"
        const val LANGUAGE_EN = "settings.languageEn"
        const val VERSION_LABEL = "settings.versionLabel"
        const val A11Y_LANGUAGE_PICKER = "a11y.languagePicker"
        const val A11Y_DISMISS_OVERLAY = "a11y.dismissOverlay"
    }

    object Layout {
        const val ROOT_PADDING_HORIZONTAL_DP: Int = 24
        const val ROOT_PADDING_TOP_DP: Int = 16
        const val CARD_CORNER_RADIUS_DP: Int = 14
        const val CARD_BORDER_WIDTH_DP: Int = 1
        const val CARD_PADDING_HORIZONTAL_DP: Int = 18
        const val CARD_PADDING_TOP_DP: Int = 16
        const val CARD_PADDING_BOTTOM_DP: Int = 18
        const val GROUP_LABEL_FONT_SP: Int = 13
        const val GROUP_LABEL_MARGIN_BOTTOM_DP: Int = 10
        const val GROUP_LABEL_SPACED_MARGIN_TOP_DP: Int = 20
        const val GROUP_LABEL_LETTER_SPACING: Float = 0.3f
        const val THEME_ROW_MIN_HEIGHT_DP: Int = 48
        const val THEME_ROW_PADDING_VERTICAL_DP: Int = 4
        const val ROW_PRIMARY_FONT_SP: Int = 17
        const val ROW_PRIMARY_MARGIN_END_DP: Int = 12
        const val DROPDOWN_HEIGHT_DP: Int = 48
        const val DROPDOWN_PADDING_HORIZONTAL_DP: Int = 16
        const val DROPDOWN_CORNER_RADIUS_DP: Int = 16
        const val DROPDOWN_BORDER_WIDTH_DP: Int = 1
        const val DROPDOWN_VALUE_FONT_SP: Int = 16
        const val DROPDOWN_VALUE_LETTER_SPACING: Float = 0.2f
        const val DROPDOWN_ICON_SIZE_DP: Int = 22
        const val DROPDOWN_PRESSED_OPACITY: Float = 0.94f
        const val VERSION_BLOCK_MARGIN_TOP_DP: Int = 28
        const val VERSION_BLOCK_PADDING_TOP_DP: Int = 20
        const val VERSION_BLOCK_BORDER_WIDTH_DP: Int = 1
        const val VERSION_LABEL_FONT_SP: Int = 14
        const val VERSION_LABEL_MARGIN_BOTTOM_DP: Int = 6
        const val VERSION_VALUE_FONT_SP: Int = 18
        const val LANGUAGE_MENU_CORNER_DP: Int = 16
        const val LANGUAGE_ROW_HEIGHT_DP: Int = 48
        const val LANGUAGE_ROW_PADDING_HORIZONTAL_DP: Int = 16
        const val LANGUAGE_ROW_LABEL_PADDING_END_DP: Int = 8
        const val LANGUAGE_ROW_TEXT_FONT_SP: Int = 16
        const val LANGUAGE_ROW_TEXT_LETTER_SPACING: Float = 0.15f
        const val LANGUAGE_ROW_TRAIL_WIDTH_DP: Int = 28
        const val LANGUAGE_CHECK_ICON_SIZE_DP: Int = 22
    }

    /**
     * Версия приложения для отображения. На Android передайте `BuildConfig.VERSION_NAME`
     * вторым аргументом; первый зарезервирован под альтернативный источник (например, конфиг).
     */
    fun resolveAppVersion(
        expoConfigVersion: String?,
        nativeApplicationVersion: String?,
    ): String =
        expoConfigVersion?.takeIf { it.isNotBlank() }
            ?: nativeApplicationVersion?.takeIf { it.isNotBlank() }
            ?: APP_VERSION_FALLBACK

    /** Подпись пункта языка в выпадающем списке. */
    fun languageOptionLabel(
        forLocale: AppLocale,
        uiLocale: AppLocale,
    ): String =
        when (forLocale) {
            AppLocale.RU -> I18nCatalog.tSync(uiLocale, I18nKeys.LANGUAGE_RU)
            AppLocale.EN -> I18nCatalog.tSync(uiLocale, I18nKeys.LANGUAGE_EN)
        }

    /** Показывать меню над якорём, если снизу не хватает места. */
    fun shouldOpenLanguageMenuAbove(
        windowHeightPx: Float,
        anchorY: Float,
        anchorHeight: Float,
    ): Boolean {
        val spaceBelow = windowHeightPx - (anchorY + anchorHeight)
        return spaceBelow < LANGUAGE_MENU_ESTIMATE_PX && anchorY > LANGUAGE_MENU_ESTIMATE_PX
    }

    /** Вертикальная позиция (top, в px) меню языка относительно якоря. */
    fun languageMenuTopPx(
        anchorY: Float,
        anchorHeight: Float,
        openAbove: Boolean,
    ): Float =
        if (openAbove) {
            round(anchorY) - LANGUAGE_MENU_ESTIMATE_PX + 1f
        } else {
            round(anchorY + anchorHeight) - 1f
        }

    fun themeSectionLabel(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.THEME_SECTION)

    fun darkThemeLabel(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.DARK_THEME)

    fun languageSectionLabel(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.LANGUAGE_SECTION)

    fun versionLabel(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.VERSION_LABEL)

    fun a11yLanguagePicker(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.A11Y_LANGUAGE_PICKER)

    fun a11yDismissOverlay(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.A11Y_DISMISS_OVERLAY)
}
