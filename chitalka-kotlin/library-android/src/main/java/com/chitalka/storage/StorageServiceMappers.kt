package com.chitalka.storage

import android.database.Cursor
import com.chitalka.core.types.LibraryBookRecord
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.core.types.ReadingProgress
import kotlin.math.max
import kotlin.math.min

internal fun Cursor.mapLibraryBookRecordByOrdinal(): LibraryBookRecord {
    val totalChapters = max(0, getInt(7))
    val deletedAt = if (isNull(9)) null else getLong(9)
    return LibraryBookRecord(
        bookId = getString(0),
        fileUri = getString(1),
        title = getString(2),
        author = getString(3),
        fileSizeBytes = getLong(4),
        coverUri = if (isNull(5)) null else getString(5),
        addedAt = getLong(6),
        totalChapters = totalChapters,
        isFavorite = getInt(8) != 0,
        deletedAt = deletedAt,
    )
}

internal fun Cursor.mapJoinedRowByOrdinal(): LibraryBookWithProgress {
    val base = mapLibraryBookRecordByOrdinal()
    val lastChapterIndex: Int? = if (isNull(10)) null else max(0, getInt(10))
    val progressFraction: Double? =
        if (base.totalChapters > 0 && lastChapterIndex != null) {
            val raw = (lastChapterIndex + 1).toDouble() / base.totalChapters.toDouble()
            min(1.0, max(0.0, raw))
        } else {
            null
        }
    return LibraryBookWithProgress(
        bookId = base.bookId,
        fileUri = base.fileUri,
        title = base.title,
        author = base.author,
        fileSizeBytes = base.fileSizeBytes,
        coverUri = base.coverUri,
        addedAt = base.addedAt,
        totalChapters = base.totalChapters,
        isFavorite = base.isFavorite,
        deletedAt = base.deletedAt,
        lastChapterIndex = lastChapterIndex,
        progressFraction = progressFraction,
    )
}

internal fun assertNonEmptyBookId(bookId: String) {
    if (bookId.isBlank()) {
        throw StorageServiceError("Идентификатор книги (bookId) должен быть непустой строкой.")
    }
}

internal fun assertValidProgress(progress: ReadingProgress) {
    assertNonEmptyBookId(progress.bookId)
    if (!progress.scrollOffset.isFinite()) {
        throw StorageServiceError("scrollOffset должен быть конечным числом.")
    }
    if (progress.lastReadTimestamp < Long.MIN_VALUE / 2 || progress.lastReadTimestamp > Long.MAX_VALUE / 2) {
        throw StorageServiceError("lastReadTimestamp должен быть конечным числом.")
    }
}
