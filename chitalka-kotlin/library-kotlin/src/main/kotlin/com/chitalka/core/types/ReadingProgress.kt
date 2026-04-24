package com.chitalka.core.types

/**
 * Persisted reading position for a single book.
 * Stored in SQLite with `book_id` as the primary key.
 */
data class ReadingProgress(
    val bookId: String,
    val lastChapterIndex: Int,
    val scrollOffset: Double,
    val lastReadTimestamp: Long,
)
