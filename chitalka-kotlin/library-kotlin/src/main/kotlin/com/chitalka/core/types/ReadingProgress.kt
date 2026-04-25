package com.chitalka.core.types

/**
 * Сохранённая позиция чтения одной книги. Хранится в SQLite, ключ — `book_id`.
 */
data class ReadingProgress(
    val bookId: String,
    val lastChapterIndex: Int,
    val scrollOffset: Double,
    val lastReadTimestamp: Long,
)
