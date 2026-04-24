package com.chitalka.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LibrarySessionStateTest {

    @Test
    fun bumpLibraryEpoch_increments() {
        val s = LibrarySessionState(initialLibraryEpoch = 2)
        s.bumpLibraryEpoch()
        assertEquals(3, s.libraryEpoch)
    }

    @Test
    fun search_openCloseClearQuery() {
        val s = LibrarySessionState()
        s.openSearch()
        assertTrue(s.isSearchOpen)
        s.setSearchQuery("foo")
        assertEquals("foo", s.searchQuery)
        s.closeSearch()
        assertFalse(s.isSearchOpen)
        assertEquals("", s.searchQuery)
    }

    @Test
    fun welcomeVisible_matchesReactFormula() {
        val s = LibrarySessionState(initialBookCount = 0)
        assertFalse(s.isFirstLaunchWelcomeVisible())
        s.markStorageReady(true)
        assertTrue(s.isFirstLaunchWelcomeVisible())
        s.dismissWelcomeModal()
        assertFalse(s.isFirstLaunchWelcomeVisible())
    }

    @Test
    fun welcomeHiddenWhenBooksOrSuppressed() {
        val s = LibrarySessionState(initialBookCount = 1)
        s.markStorageReady(true)
        assertFalse(s.isFirstLaunchWelcomeVisible())

        val empty = LibrarySessionState()
        empty.markStorageReady(true)
        empty.setSuppressWelcomeForPicker(true)
        assertFalse(empty.isFirstLaunchWelcomeVisible())
    }

    @Test
    fun dismissWelcome_clearsHint() {
        val s = LibrarySessionState()
        s.setWelcomePickerHint("err")
        assertEquals("err", s.welcomePickerHint)
        s.dismissWelcomeModal()
        assertNull(s.welcomePickerHint)
    }

    @Test
    fun updateBookCount_clampsToIntRange() {
        val s = LibrarySessionState()
        s.updateBookCount(Long.MAX_VALUE)
        assertEquals(Int.MAX_VALUE, s.bookCount)
        s.updateBookCount(-1)
        assertEquals(0, s.bookCount)
    }
}
