package com.chitalka.navigation

import kotlin.math.min

/**
 * Параметры бокового меню из `AppDrawer.tsx` (без привязки к Compose / `AppTopBar`).
 */
object DrawerNavigationSpec {
    /** Целевая ширина drawer в dp (`DRAWER_TARGET_WIDTH = 288`). */
    const val TARGET_WIDTH_DP: Int = 288

    /**
     * Запас по краю окна перед ограничением ширины (`Math.min(288, windowWidth - 24)` в RN, логические px = dp).
     */
    const val WIDTH_WINDOW_EDGE_RESERVE_DP: Int = 24

    /** Порядок пунктов в навигаторе — как след экранов в `Drawer.Navigator`. */
    val drawerScreenOrder: List<DrawerScreen> =
        listOf(
            DrawerScreen.ReadingNow,
            DrawerScreen.BooksAndDocs,
            DrawerScreen.Favorites,
            DrawerScreen.Cart,
            DrawerScreen.DebugLogs,
            DrawerScreen.Settings,
        )

    /**
     * Ширина drawer в dp: не больше целевой и не шире окна с отступом по краю.
     */
    fun clampedDrawerWidthDp(windowWidthDp: Int): Int {
        val maxFromWindow = (windowWidthDp - WIDTH_WINDOW_EDGE_RESERVE_DP).coerceAtLeast(0)
        return min(TARGET_WIDTH_DP, maxFromWindow)
    }
}

/**
 * Ключи для [com.chitalka.i18n.I18nCatalog.tSync] — в RN `t('drawer.readingNow')` и т.д.
 * Заголовок экрана и подпись в меню совпадают (`title` и `drawerLabel`).
 */
val DrawerScreen.drawerLabelI18nPath: String
    get() =
        when (this) {
            DrawerScreen.ReadingNow -> "drawer.readingNow"
            DrawerScreen.BooksAndDocs -> "drawer.books"
            DrawerScreen.Favorites -> "drawer.favorites"
            DrawerScreen.Cart -> "drawer.cart"
            DrawerScreen.DebugLogs -> "drawer.debugLogs"
            DrawerScreen.Settings -> "drawer.settings"
        }
