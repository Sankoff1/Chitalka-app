package com.chitalka.screens.readingnow

import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.i18n.AppLocale
import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingNowScreenSpecTest {

    private fun sampleBook(
        bookId: String,
        title: String,
        author: String,
    ): LibraryBookWithProgress =
        LibraryBookWithProgress(
            bookId = bookId,
            fileUri = "file:///$bookId.epub",
            title = title,
            author = author,
            fileSizeBytes = 1,
            coverUri = null,
            addedAt = 0,
            totalChapters = 0,
            isFavorite = false,
            deletedAt = null,
            lastChapterIndex = null,
            progressFraction = null,
        )

    @Test
    fun visibleBooksForSearch_emptyQuery_returnsAll() {
        val books = listOf(sampleBook("1", "A", "B"))
        assertEquals(books, ReadingNowScreenSpec.visibleBooksForSearch(books, ""))
    }

    @Test
    fun visibleBooksForSearch_filtersTitleOrAuthor() {
        val books =
            listOf(
                sampleBook("1", "Война и мир", "Толстой"),
                sampleBook("2", "Другая", "Пушкин"),
            )
        val q = ReadingNowScreenSpec.normalizeSearchQuery("  пушкин ")
        val out = ReadingNowScreenSpec.visibleBooksForSearch(books, q)
        assertEquals(1, out.size)
        assertEquals("2", out[0].bookId)
    }

    @Test
    fun listContentBottomPadding_matchesFormula() {
        assertEquals(104, ReadingNowScreenSpec.listContentBottomPaddingDp(16))
    }

    @Test
    fun emptyListMessageRu() {
        val ru = AppLocale.RU
        assertEquals(
            "Здесь будет список текущих книг.",
            ReadingNowScreenSpec.emptyListMessage(ru, false),
        )
        assertEquals(
            "Ничего не найдено. Попробуйте изменить запрос.",
            ReadingNowScreenSpec.emptyListMessage(ru, true),
        )
    }
}
