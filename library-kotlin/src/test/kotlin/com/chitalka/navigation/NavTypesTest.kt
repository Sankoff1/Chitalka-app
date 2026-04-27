package com.chitalka.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NavTypesTest {

    @Test
    fun drawerRouteNames_matchTypeScriptDrawerParamList() {
        assertEquals(
            listOf(
                "ReadingNow",
                "BooksAndDocs",
                "Favorites",
                "Cart",
                "DebugLogs",
                "Settings",
            ),
            DrawerScreen.entries.map { it.routeName },
        )
    }

    @Test
    fun fromRouteName() {
        assertEquals(DrawerScreen.Cart, DrawerScreen.fromRouteName("Cart"))
        assertNull(DrawerScreen.fromRouteName("Unknown"))
    }

    @Test
    fun readerRouteParams() {
        val p = ReaderRouteParams(bookPath = "file:///book.epub", bookId = "abc")
        assertEquals("file:///book.epub", p.bookPath)
        assertEquals("abc", p.bookId)
    }

    @Test
    fun rootStackRouteConstants() {
        assertEquals("Main", RootStackRoutes.MAIN)
        assertEquals("Reader", RootStackRoutes.READER)
    }
}
