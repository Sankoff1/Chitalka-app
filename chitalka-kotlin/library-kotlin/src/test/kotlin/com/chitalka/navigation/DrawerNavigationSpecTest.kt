package com.chitalka.navigation

import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog
import org.junit.Assert.assertEquals
import org.junit.Test

class DrawerNavigationSpecTest {

    @Test
    fun targetWidth_matchesReactNative() {
        assertEquals(288, DrawerNavigationSpec.TARGET_WIDTH_DP)
    }

    @Test
    fun clampedDrawerWidthDp_matchesMinFormula() {
        assertEquals(288, DrawerNavigationSpec.clampedDrawerWidthDp(400))
        assertEquals(276, DrawerNavigationSpec.clampedDrawerWidthDp(300))
        assertEquals(0, DrawerNavigationSpec.clampedDrawerWidthDp(20))
    }

    @Test
    fun drawerScreenOrder_matchesNavigatorOrder() {
        assertEquals(
            listOf(
                "ReadingNow",
                "BooksAndDocs",
                "Favorites",
                "Cart",
                "DebugLogs",
                "Settings",
            ),
            DrawerNavigationSpec.drawerScreenOrder.map { it.routeName },
        )
    }

    @Test
    fun drawerLabelPaths_matchRuCatalog() {
        val ru = AppLocale.RU
        assertEquals("Читаю сейчас", I18nCatalog.tSync(ru, DrawerScreen.ReadingNow.drawerLabelI18nPath))
        assertEquals("Книги и документы", I18nCatalog.tSync(ru, DrawerScreen.BooksAndDocs.drawerLabelI18nPath))
        assertEquals("Избранное", I18nCatalog.tSync(ru, DrawerScreen.Favorites.drawerLabelI18nPath))
        assertEquals("Корзина", I18nCatalog.tSync(ru, DrawerScreen.Cart.drawerLabelI18nPath))
        assertEquals("Отладочные логи", I18nCatalog.tSync(ru, DrawerScreen.DebugLogs.drawerLabelI18nPath))
        assertEquals("Настройки", I18nCatalog.tSync(ru, DrawerScreen.Settings.drawerLabelI18nPath))
    }
}
