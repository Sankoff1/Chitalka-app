package com.chitalka.navigation

import kotlin.math.min

/**
 * Параметры бокового меню без привязки к Compose.
 */
object DrawerNavigationSpec {
    /** Целевая ширина drawer в dp. */
    const val TARGET_WIDTH_DP: Int = 288

    /** Запас по краю окна перед ограничением ширины. */
    const val WIDTH_WINDOW_EDGE_RESERVE_DP: Int = 24

    /** Порядок пунктов в меню. */
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

/** Ключ строки в [com.chitalka.i18n.I18nCatalog] для подписи пункта меню (он же заголовок экрана). */
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
