package com.chitalka.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.chitalka.core.types.LibraryBookRecord
import com.chitalka.debug.ChitalkaMirrorLog
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.core.types.ReadingProgress
import com.chitalka.library.LibraryBookLookup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

private const val LOG_PREFIX = "[StorageService]"

/**
 * SQLite-хранилище прогресса чтения и библиотеки (аналог RN StorageService).
 */
@Suppress("TooManyFunctions")
class StorageService internal constructor(
    private val helper: ChitalkaSqliteOpenHelper,
) : LibraryBookLookup {
    constructor(context: Context) : this(ChitalkaSqliteOpenHelper(context))

    private val mutex = Mutex()

    private suspend fun <T> withDb(block: (SQLiteDatabase) -> T): T =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                try {
                    block(helper.writableDatabase)
                } catch (e: SQLException) {
                    throw mapDbException(e)
                }
            }
        }

    private fun mapDbException(e: SQLException): StorageServiceError {
        ChitalkaMirrorLog.e(TAG, "$LOG_PREFIX ${e.message}", e)
        val msg = e.message.orEmpty()
        val openHints = listOf("unable to open", "Could not open", "disk I/O", "SQLITE_CANTOPEN")
        val isLikelyOpen = openHints.any { msg.contains(it, ignoreCase = true) }
        return if (isLikelyOpen) {
            StorageServiceError(
                "Не удалось открыть локальную базу данных читалки. " +
                    "Проверьте свободное место и перезапустите приложение.",
                e,
            )
        } else {
            StorageServiceError(
                "Ошибка хранилища: ${e.message}. " +
                    "Повторите попытку или очистите данные в разделе «Эксплуатация».",
                e,
            )
        }
    }

    suspend fun saveProgress(progress: ReadingProgress) {
        assertValidProgress(progress)
        withDb { db ->
            val cv =
                ContentValues().apply {
                    put("book_id", progress.bookId)
                    put("last_chapter_index", progress.lastChapterIndex)
                    put("scroll_offset", progress.scrollOffset)
                    put("last_read_timestamp", progress.lastReadTimestamp)
                }
            db.insertWithOnConflict(
                ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS,
                null,
                cv,
                SQLiteDatabase.CONFLICT_REPLACE,
            )
        }
    }

    suspend fun getProgress(bookId: String): ReadingProgress? {
        assertNonEmptyBookId(bookId)
        return withDb { db ->
            db.rawQuery(
                """
                SELECT
                  book_id AS bookId,
                  last_chapter_index AS lastChapterIndex,
                  scroll_offset AS scrollOffset,
                  last_read_timestamp AS lastReadTimestamp
                FROM ${ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS}
                WHERE book_id = ?
                LIMIT 1;
                """.trimIndent(),
                arrayOf(bookId),
            ).use { c ->
                if (!c.moveToFirst()) null
                else {
                    ReadingProgress(
                        bookId = c.reqString("bookId"),
                        lastChapterIndex = c.getInt(c.col("lastChapterIndex")),
                        scrollOffset = c.getDouble(c.col("scrollOffset")),
                        lastReadTimestamp = c.getLong(c.col("lastReadTimestamp")),
                    )
                }
            }
        }
    }

    suspend fun addBook(row: LibraryBookRecord) {
        upsertLibraryBook(row)
    }

    suspend fun upsertLibraryBook(row: LibraryBookRecord) {
        assertNonEmptyBookId(row.bookId)
        withDb { db ->
            db.beginTransactionNonExclusive()
            try {
                val updateCv =
                    ContentValues().apply {
                        put("file_uri", row.fileUri)
                        put("title", row.title)
                        put("author", row.author)
                        put("file_size_bytes", max(0L, row.fileSizeBytes))
                        if (row.coverUri != null) {
                            put("cover_uri", row.coverUri)
                        } else {
                            putNull("cover_uri")
                        }
                        put("added_at", row.addedAt)
                        putNull("deleted_at")
                    }
                val updated =
                    db.update(
                        ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS,
                        updateCv,
                        "book_id = ?",
                        arrayOf(row.bookId),
                    )
                if (updated == 0) {
                    val insertCv =
                        ContentValues().apply {
                            put("book_id", row.bookId)
                            put("file_uri", row.fileUri)
                            put("title", row.title)
                            put("author", row.author)
                            put("file_size_bytes", max(0L, row.fileSizeBytes))
                            if (row.coverUri != null) {
                                put("cover_uri", row.coverUri)
                            } else {
                                putNull("cover_uri")
                            }
                            put("added_at", row.addedAt)
                            put("total_chapters", max(0, row.totalChapters))
                            put("is_favorite", if (row.isFavorite) 1 else 0)
                            if (row.deletedAt != null) {
                                put("deleted_at", row.deletedAt)
                            } else {
                                putNull("deleted_at")
                            }
                        }
                    db.insert(ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS, null, insertCv)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    suspend fun setBookTotalChapters(bookId: String, totalChapters: Int) {
        assertNonEmptyBookId(bookId)
        val normalized = max(0, totalChapters)
        withDb { db ->
            val cv = ContentValues().apply { put("total_chapters", normalized) }
            db.update(
                ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS,
                cv,
                "book_id = ?",
                arrayOf(bookId),
            )
        }
    }

    suspend fun listLibraryBooks(): List<LibraryBookWithProgress> =
        withDb { db ->
            db.rawQuery(
                """
                ${joinedSelectColumns()}
                FROM ${ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS} AS lb
                LEFT JOIN ${ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS} AS rp ON rp.book_id = lb.book_id
                WHERE lb.deleted_at IS NULL
                ORDER BY lb.added_at DESC;
                """.trimIndent(),
                null,
            ).use { c ->
                buildList {
                    while (c.moveToNext()) {
                        add(c.mapJoinedRow())
                    }
                }
            }
        }

    suspend fun listRecentlyReadBooks(): List<LibraryBookWithProgress> =
        withDb { db ->
            db.rawQuery(
                """
                ${joinedSelectColumns()}
                FROM ${ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS} AS lb
                INNER JOIN ${ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS} AS rp ON rp.book_id = lb.book_id
                WHERE lb.deleted_at IS NULL
                ORDER BY rp.last_read_timestamp DESC;
                """.trimIndent(),
                null,
            ).use { c ->
                buildList {
                    while (c.moveToNext()) {
                        add(c.mapJoinedRow())
                    }
                }
            }
        }

    suspend fun listFavoriteBooks(): List<LibraryBookWithProgress> =
        withDb { db ->
            db.rawQuery(
                """
                ${joinedSelectColumns()}
                FROM ${ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS} AS lb
                LEFT JOIN ${ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS} AS rp ON rp.book_id = lb.book_id
                WHERE lb.is_favorite = 1 AND lb.deleted_at IS NULL
                ORDER BY lb.added_at DESC;
                """.trimIndent(),
                null,
            ).use { c ->
                buildList {
                    while (c.moveToNext()) {
                        add(c.mapJoinedRow())
                    }
                }
            }
        }

    suspend fun listTrashedBooks(): List<LibraryBookWithProgress> =
        withDb { db ->
            db.rawQuery(
                """
                ${joinedSelectColumns()}
                FROM ${ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS} AS lb
                LEFT JOIN ${ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS} AS rp ON rp.book_id = lb.book_id
                WHERE lb.deleted_at IS NOT NULL
                ORDER BY lb.deleted_at DESC;
                """.trimIndent(),
                null,
            ).use { c ->
                buildList {
                    while (c.moveToNext()) {
                        add(c.mapJoinedRow())
                    }
                }
            }
        }

    override suspend fun getLibraryBook(bookId: String): LibraryBookRecord? {
        assertNonEmptyBookId(bookId)
        return withDb { db ->
            db.rawQuery(
                """
                SELECT
                  book_id AS bookId,
                  file_uri AS fileUri,
                  title AS title,
                  author AS author,
                  file_size_bytes AS fileSizeBytes,
                  cover_uri AS coverUri,
                  added_at AS addedAt,
                  total_chapters AS totalChapters,
                  is_favorite AS isFavorite,
                  deleted_at AS deletedAt
                FROM ${ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS}
                WHERE book_id = ?
                LIMIT 1;
                """.trimIndent(),
                arrayOf(bookId),
            ).use { c ->
                if (!c.moveToFirst()) null else c.mapLibraryBookRecord()
            }
        }
    }

    suspend fun setBookFavorite(bookId: String, isFavorite: Boolean) {
        assertNonEmptyBookId(bookId)
        withDb { db ->
            val cv = ContentValues().apply { put("is_favorite", if (isFavorite) 1 else 0) }
            db.update(
                ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS,
                cv,
                "book_id = ?",
                arrayOf(bookId),
            )
        }
    }

    suspend fun moveBookToTrash(bookId: String) {
        assertNonEmptyBookId(bookId)
        withDb { db ->
            val cv = ContentValues().apply { put("deleted_at", System.currentTimeMillis()) }
            db.update(
                ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS,
                cv,
                "book_id = ?",
                arrayOf(bookId),
            )
        }
    }

    suspend fun restoreBookFromTrash(bookId: String) {
        assertNonEmptyBookId(bookId)
        withDb { db ->
            val cv = ContentValues().apply { putNull("deleted_at") }
            db.update(
                ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS,
                cv,
                "book_id = ?",
                arrayOf(bookId),
            )
        }
    }

    suspend fun purgeBook(bookId: String) {
        assertNonEmptyBookId(bookId)
        withDb { db ->
            db.delete(
                ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS,
                "book_id = ?",
                arrayOf(bookId),
            )
            db.delete(
                ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS,
                "book_id = ?",
                arrayOf(bookId),
            )
        }
    }

    suspend fun countLibraryBooks(): Long =
        withDb { db ->
            db.rawQuery(
                """
                SELECT COUNT(*) AS cnt FROM ${ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS}
                WHERE deleted_at IS NULL;
                """.trimIndent(),
                null,
            ).use { c ->
                if (!c.moveToFirst()) 0L else c.getLong(c.col("cnt"))
            }
        }

    suspend fun countBooksWithProgress(): Long =
        withDb { db ->
            db.rawQuery(
                """
                SELECT COUNT(*) AS cnt FROM ${ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS};
                """.trimIndent(),
                null,
            ).use { c ->
                if (!c.moveToFirst()) 0L else c.getLong(c.col("cnt"))
            }
        }

    suspend fun clearAllData() {
        withDb { db ->
            db.delete(ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS, null, null)
            db.delete(ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS, null, null)
        }
    }

    companion object {
        private const val TAG = "ChitalkaStorage"
    }
}

private fun joinedSelectColumns(): String =
    """
    SELECT
      lb.book_id AS bookId,
      lb.file_uri AS fileUri,
      lb.title AS title,
      lb.author AS author,
      lb.file_size_bytes AS fileSizeBytes,
      lb.cover_uri AS coverUri,
      lb.added_at AS addedAt,
      lb.total_chapters AS totalChapters,
      lb.is_favorite AS isFavorite,
      lb.deleted_at AS deletedAt,
      rp.last_chapter_index AS lastChapterIndex
    """.trimIndent()

private fun Cursor.col(name: String): Int {
    val i = getColumnIndex(name)
    require(i >= 0) { "Missing column $name" }
    return i
}

private fun Cursor.reqString(name: String): String = getString(col(name))

private fun Cursor.mapLibraryBookRecord(): LibraryBookRecord {
    val totalChapters = max(0, getInt(col("totalChapters")))
    val deletedAt =
        if (isNull(col("deletedAt"))) {
            null
        } else {
            getLong(col("deletedAt"))
        }
    return LibraryBookRecord(
        bookId = reqString("bookId"),
        fileUri = reqString("fileUri"),
        title = reqString("title"),
        author = reqString("author"),
        fileSizeBytes = getLong(col("fileSizeBytes")),
        coverUri = if (isNull(col("coverUri"))) null else getString(col("coverUri")),
        addedAt = getLong(col("addedAt")),
        totalChapters = totalChapters,
        isFavorite = getInt(col("isFavorite")) != 0,
        deletedAt = deletedAt,
    )
}

private fun Cursor.mapJoinedRow(): LibraryBookWithProgress {
    val base = mapLibraryBookRecord()
    val lastChapterIndex: Int? =
        if (isNull(col("lastChapterIndex"))) {
            null
        } else {
            max(0, getInt(col("lastChapterIndex")))
        }
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

private fun assertNonEmptyBookId(bookId: String) {
    if (bookId.isBlank()) {
        throw StorageServiceError("Идентификатор книги (bookId) должен быть непустой строкой.")
    }
}

private fun assertValidProgress(progress: ReadingProgress) {
    assertNonEmptyBookId(progress.bookId)
    if (!progress.scrollOffset.isFinite()) {
        throw StorageServiceError("scrollOffset должен быть конечным числом.")
    }
    if (progress.lastReadTimestamp < Long.MIN_VALUE / 2 || progress.lastReadTimestamp > Long.MAX_VALUE / 2) {
        throw StorageServiceError("lastReadTimestamp должен быть конечным числом.")
    }
}
