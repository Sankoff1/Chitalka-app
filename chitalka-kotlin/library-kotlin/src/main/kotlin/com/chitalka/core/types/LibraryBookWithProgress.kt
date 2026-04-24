package com.chitalka.core.types

/** Запись библиотеки, дополненная прогрессом чтения для списочных экранов. */
data class LibraryBookWithProgress(
    val bookId: String,
    val fileUri: String,
    val title: String,
    val author: String,
    val fileSizeBytes: Long,
    val coverUri: String?,
    val addedAt: Long,
    val totalChapters: Int,
    val isFavorite: Boolean,
    val deletedAt: Long?,
    /** Индекс последней открытой главы (0-based) или null, если книгу ещё не открывали. */
    val lastChapterIndex: Int?,
    /** Доля прочитанного 0..1 или null, если прогресс неизвестен. */
    val progressFraction: Double?,
)
