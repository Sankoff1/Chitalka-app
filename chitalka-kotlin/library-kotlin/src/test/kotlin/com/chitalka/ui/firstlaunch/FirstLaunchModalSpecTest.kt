package com.chitalka.ui.firstlaunch

import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog
import org.junit.Assert.assertEquals
import org.junit.Test

class FirstLaunchModalSpecTest {

    @Test
    fun i18nKeys_resolveInCatalogRu() {
        val ru = AppLocale.RU
        assertEquals(
            I18nCatalog.tSync(ru, FirstLaunchModalSpec.I18nKeys.MESSAGE),
            FirstLaunchModalSpec.strings(ru).message,
        )
        assertEquals("Откройте книгу в формате EPUB с устройства.", FirstLaunchModalSpec.strings(ru).message)
        assertEquals("Отмена", FirstLaunchModalSpec.strings(ru).cancel)
        assertEquals("Выбрать .epub", FirstLaunchModalSpec.strings(ru).pickEpub)
    }

    @Test
    fun layoutTokens_matchReactNativeStyleSheet() {
        assertEquals(28, FirstLaunchModalSpec.Layout.OVERLAY_HORIZONTAL_PADDING_DP)
        assertEquals(14, FirstLaunchModalSpec.Layout.CARD_CORNER_RADIUS_DP)
        assertEquals(0.88f, FirstLaunchModalSpec.Layout.PRESSED_OPACITY, 0f)
    }

    @Test
    fun hintColor_normalizedFromShortHex() {
        assertEquals("#aa3333", FirstLaunchModalSpec.Colors.HINT_HEX)
    }
}
