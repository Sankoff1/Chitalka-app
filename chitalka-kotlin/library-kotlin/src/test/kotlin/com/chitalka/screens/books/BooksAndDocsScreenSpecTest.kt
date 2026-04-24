package com.chitalka.screens.books

import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.i18n.AppLocale
import org.junit.Assert.assertEquals
import org.junit.Test

class BooksAndDocsScreenSpecTest {

    private fun book(
        id: String,
        title: String,
        author: String,
    ): LibraryBookWithProgress =
        LibraryBookWithProgress(
            bookId = id,
            fileUri = "file:///$id.epub",
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
    fun emptyListMessageRu() {
        val ru = AppLocale.RU
        assertEquals(
            "Пока нет добавленных книг. Нажмите «+», чтобы выбрать EPUB.",
            BooksAndDocsScreenSpec.emptyListMessage(ru, false),
        )
        assertEquals(
            "Ничего не найдено. Попробуйте изменить запрос.",
            BooksAndDocsScreenSpec.emptyListMessage(ru, true),
        )
    }

    @Test
    fun listPadding_matchesReadingNowFormula() {
        assertEquals(104, BooksAndDocsScreenSpec.listContentBottomPaddingDp(16))
    }

    @Test
    fun searchFilter_sameAsReadingNow() {
        val books = listOf(book("1", "Alpha", "Beta"), book("2", "Gamma", "Delta"))
        val q = BooksAndDocsScreenSpec.normalizeSearchQuery("gamma")
        assertEquals(1, BooksAndDocsScreenSpec.visibleBooksForSearch(books, q).size)
    }
}
