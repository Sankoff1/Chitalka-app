package com.chitalka.navigation

/**
 * Экраны drawer (`DrawerParamList` в TS). У всех маршрутов в RN параметры `undefined`.
 */
enum class DrawerScreen(val routeName: String) {
    ReadingNow("ReadingNow"),
    BooksAndDocs("BooksAndDocs"),
    Favorites("Favorites"),
    Cart("Cart"),
    DebugLogs("DebugLogs"),
    Settings("Settings"),
    ;

    companion object {
        fun fromRouteName(routeName: String): DrawerScreen? =
            entries.find { it.routeName == routeName }
    }
}

/** Имена корневого нативного стека (`RootStackParamList` в TS). */
object RootStackRoutes {
    const val MAIN = "Main"
    const val READER = "Reader"
}

/**
 * Аргументы экрана читалки: `RootStackParamList['Reader']`.
 */
data class ReaderRouteParams(
    val bookPath: String,
    val bookId: String,
)
