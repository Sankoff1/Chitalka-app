package com.chitalka.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RootStackDestinationTest {

    @Test
    fun routeId_matchesRootStackRoutes() {
        assertEquals(RootStackRoutes.MAIN, RootStackDestination.Main.routeId)
        val reader =
            readerRootDestination(
                bookPath = "file:///a.epub",
                bookId = "id1",
            )
        assertEquals(RootStackRoutes.READER, reader.routeId)
        assertEquals("file:///a.epub", reader.params.bookPath)
        assertEquals("id1", reader.params.bookId)
    }
}
