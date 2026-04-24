package com.chitalka.navigation

/**
 * Экраны корневого нативного стека без заголовка (`RootStack.tsx`: `Stack.Screen` Main и Reader).
 *
 * Вложенный drawer (`AppDrawer`, `DrawerScreen`) живёт под [Main]; модуль `nav-drawer` подключается отдельно.
 */
sealed interface RootStackDestination {
    /** Drawer и основная навигация библиотеки. */
    data object Main : RootStackDestination

    /** Читалка; аргументы как `RootStackParamList['Reader']` в RN. */
    data class Reader(
        val params: ReaderRouteParams,
    ) : RootStackDestination
}

/** Имя маршрута в графе (Compose Navigation / кастомный роутер). */
val RootStackDestination.routeId: String
    get() =
        when (this) {
            is RootStackDestination.Main -> RootStackRoutes.MAIN
            is RootStackDestination.Reader -> RootStackRoutes.READER
        }

fun readerRootDestination(
    bookPath: String,
    bookId: String,
): RootStackDestination.Reader =
    RootStackDestination.Reader(ReaderRouteParams(bookPath = bookPath, bookId = bookId))
