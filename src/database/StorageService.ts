import { openDatabaseAsync, type SQLiteDatabase } from 'expo-sqlite';

import type { LibraryBookRecord, ReadingProgress } from '../core/types';

export type { LibraryBookRecord, ReadingProgress };

const LOG_PREFIX = '[StorageService]';

/** Logical name of the on-device SQLite file (managed by expo-sqlite). */
const DATABASE_NAME = 'chitalka.db';

const TABLE_NAME = 'reading_progress';

const LIBRARY_TABLE = 'library_books';

/**
 * Thrown when the storage layer cannot open the database, run migrations,
 * or complete a requested operation in a recoverable, user-visible way.
 */
export class StorageServiceError extends Error {
  constructor(message: string, options?: { cause?: unknown }) {
    super(message, options);
    this.name = 'StorageServiceError';
  }
}

function logError(context: string, error: unknown): void {
  if (__DEV__) {
    console.error(`${LOG_PREFIX} ${context}`, error);
  } else {
    const message = error instanceof Error ? error.message : String(error);
    console.error(`${LOG_PREFIX} ${context}: ${message}`);
  }
}

function wrapOpenFailure(error: unknown): StorageServiceError {
  const cause = error instanceof Error ? error : undefined;
  return new StorageServiceError(
    'Не удалось открыть локальную базу данных читалки. Проверьте свободное место и перезапустите приложение.',
    { cause },
  );
}

function wrapOperationFailure(context: string, error: unknown): StorageServiceError {
  logError(context, error);
  const cause = error instanceof Error ? error : undefined;
  return new StorageServiceError(
    `Ошибка хранилища: ${context}. Повторите попытку или очистите данные в разделе «Эксплуатация».`,
    { cause },
  );
}

function assertNonEmptyBookId(bookId: string): void {
  if (typeof bookId !== 'string' || bookId.trim().length === 0) {
    throw new StorageServiceError('Идентификатор книги (bookId) должен быть непустой строкой.');
  }
}

function assertValidProgress(progress: ReadingProgress): void {
  assertNonEmptyBookId(progress.bookId);
  if (!Number.isFinite(progress.lastChapterIndex)) {
    throw new StorageServiceError('lastChapterIndex должен быть конечным числом.');
  }
  if (!Number.isFinite(progress.scrollOffset)) {
    throw new StorageServiceError('scrollOffset должен быть конечным числом.');
  }
  if (!Number.isFinite(progress.lastReadTimestamp)) {
    throw new StorageServiceError('lastReadTimestamp должен быть конечным числом.');
  }
}

type ProgressRow = {
  bookId: string;
  lastChapterIndex: number;
  scrollOffset: number;
  lastReadTimestamp: number;
};

/**
 * SQLite-backed persistence for per-book reading progress.
 *
 * The database is opened lazily on the first method call, so constructing
 * `StorageService` is cheap and safe on the UI thread.
 *
 * All user-controlled values are passed through prepared statements to avoid
 * SQL injection. DDL for schema creation uses static SQL only.
 */
export class StorageService {
  private db: SQLiteDatabase | null = null;

  /** In-flight or completed first-open promise (cleared on failure for retry). */
  private openPromise: Promise<SQLiteDatabase> | null = null;

  /**
   * Ensures the database file is open and the schema exists.
   * Concurrent callers share the same open operation.
   */
  private async getDatabase(): Promise<SQLiteDatabase> {
    if (this.db) {
      return this.db;
    }
    if (!this.openPromise) {
      this.openPromise = this.openAndMigrate();
    }
    try {
      this.db = await this.openPromise;
      return this.db;
    } catch (error) {
      this.openPromise = null;
      this.db = null;
      throw error;
    }
  }

  private async openAndMigrate(): Promise<SQLiteDatabase> {
    try {
      const database = await openDatabaseAsync(DATABASE_NAME);
      await database.execAsync(`
        CREATE TABLE IF NOT EXISTS ${TABLE_NAME} (
          book_id TEXT PRIMARY KEY NOT NULL,
          last_chapter_index INTEGER NOT NULL,
          scroll_offset REAL NOT NULL,
          last_read_timestamp INTEGER NOT NULL
        );
        CREATE INDEX IF NOT EXISTS idx_${TABLE_NAME}_last_read
          ON ${TABLE_NAME} (last_read_timestamp DESC);

        CREATE TABLE IF NOT EXISTS ${LIBRARY_TABLE} (
          book_id TEXT PRIMARY KEY NOT NULL,
          file_uri TEXT NOT NULL,
          title TEXT NOT NULL,
          author TEXT NOT NULL,
          file_size_bytes INTEGER NOT NULL,
          cover_uri TEXT,
          added_at INTEGER NOT NULL
        );
        CREATE INDEX IF NOT EXISTS idx_${LIBRARY_TABLE}_added
          ON ${LIBRARY_TABLE} (added_at DESC);
      `);
      return database;
    } catch (error) {
      logError('openAndMigrate', error);
      throw wrapOpenFailure(error);
    }
  }

  /**
   * Inserts a new row or updates an existing one for the same `bookId`.
   */
  async saveProgress(progress: ReadingProgress): Promise<void> {
    assertValidProgress(progress);
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(`
      INSERT INTO ${TABLE_NAME} (
        book_id,
        last_chapter_index,
        scroll_offset,
        last_read_timestamp
      ) VALUES (?, ?, ?, ?)
      ON CONFLICT(book_id) DO UPDATE SET
        last_chapter_index = excluded.last_chapter_index,
        scroll_offset = excluded.scroll_offset,
        last_read_timestamp = excluded.last_read_timestamp;
    `);
    try {
      await statement.executeAsync([
        progress.bookId,
        Math.trunc(progress.lastChapterIndex),
        progress.scrollOffset,
        Math.trunc(progress.lastReadTimestamp),
      ]);
    } catch (error) {
      throw wrapOperationFailure('сохранение прогресса чтения', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  /**
   * Returns stored progress for `bookId`, or `null` if none exists.
   */
  async getProgress(bookId: string): Promise<ReadingProgress | null> {
    assertNonEmptyBookId(bookId);
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(`
      SELECT
        book_id AS bookId,
        last_chapter_index AS lastChapterIndex,
        scroll_offset AS scrollOffset,
        last_read_timestamp AS lastReadTimestamp
      FROM ${TABLE_NAME}
      WHERE book_id = ?
      LIMIT 1;
    `);
    try {
      const result = await statement.executeAsync<ProgressRow>([bookId]);
      const row = await result.getFirstAsync();
      return row ?? null;
    } catch (error) {
      throw wrapOperationFailure('чтение прогресса', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  /** Алиас для {@link upsertLibraryBook} — добавление/обновление записи о книге. */
  async addBook(row: LibraryBookRecord): Promise<void> {
    await this.upsertLibraryBook(row);
  }

  async upsertLibraryBook(row: LibraryBookRecord): Promise<void> {
    assertNonEmptyBookId(row.bookId);
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(`
      INSERT INTO ${LIBRARY_TABLE} (
        book_id,
        file_uri,
        title,
        author,
        file_size_bytes,
        cover_uri,
        added_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?)
      ON CONFLICT(book_id) DO UPDATE SET
        file_uri = excluded.file_uri,
        title = excluded.title,
        author = excluded.author,
        file_size_bytes = excluded.file_size_bytes,
        cover_uri = excluded.cover_uri,
        added_at = excluded.added_at;
    `);
    try {
      await statement.executeAsync([
        row.bookId,
        row.fileUri,
        row.title,
        row.author,
        Math.max(0, Math.trunc(row.fileSizeBytes)),
        row.coverUri,
        Math.trunc(row.addedAt),
      ]);
    } catch (error) {
      throw wrapOperationFailure('сохранение книги в библиотеку', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  async listLibraryBooks(): Promise<LibraryBookRecord[]> {
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(`
      SELECT
        book_id AS bookId,
        file_uri AS fileUri,
        title AS title,
        author AS author,
        file_size_bytes AS fileSizeBytes,
        cover_uri AS coverUri,
        added_at AS addedAt
      FROM ${LIBRARY_TABLE}
      ORDER BY added_at DESC;
    `);
    try {
      const result = await statement.executeAsync<LibraryBookRecord>();
      return await result.getAllAsync();
    } catch (error) {
      throw wrapOperationFailure('список книг библиотеки', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  async getLibraryBook(bookId: string): Promise<LibraryBookRecord | null> {
    assertNonEmptyBookId(bookId);
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(`
      SELECT
        book_id AS bookId,
        file_uri AS fileUri,
        title AS title,
        author AS author,
        file_size_bytes AS fileSizeBytes,
        cover_uri AS coverUri,
        added_at AS addedAt
      FROM ${LIBRARY_TABLE}
      WHERE book_id = ?
      LIMIT 1;
    `);
    try {
      const result = await statement.executeAsync<LibraryBookRecord>([bookId]);
      const row = await result.getFirstAsync();
      return row ?? null;
    } catch (error) {
      throw wrapOperationFailure('чтение записи книги', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  /**
   * Количество книг в локальной библиотеке.
   */
  async countLibraryBooks(): Promise<number> {
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(`
      SELECT COUNT(*) AS cnt FROM ${LIBRARY_TABLE};
    `);
    try {
      const result = await statement.executeAsync<{ cnt: number }>();
      const row = await result.getFirstAsync();
      const raw = row?.cnt;
      const n = typeof raw === 'number' ? raw : Number(raw);
      return Number.isFinite(n) ? n : 0;
    } catch (error) {
      throw wrapOperationFailure('подсчёт книг в библиотеке', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  /**
   * Количество книг, для которых уже есть запись прогресса (то есть книга хотя бы раз открывалась).
   */
  async countBooksWithProgress(): Promise<number> {
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(`
      SELECT COUNT(*) AS cnt FROM ${TABLE_NAME};
    `);
    try {
      const result = await statement.executeAsync<{ cnt: number }>();
      const row = await result.getFirstAsync();
      const raw = row?.cnt;
      const n = typeof raw === 'number' ? raw : Number(raw);
      return Number.isFinite(n) ? n : 0;
    } catch (error) {
      throw wrapOperationFailure('подсчёт книг в хранилище', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  /**
   * Removes all persisted reading positions (used by maintenance / «Эксплуатация» flows).
   */
  async clearAllData(): Promise<void> {
    const database = await this.getDatabase();
    const progressStatement = await database.prepareAsync(
      `DELETE FROM ${TABLE_NAME};`
    );
    const libraryStatement = await database.prepareAsync(
      `DELETE FROM ${LIBRARY_TABLE};`
    );
    try {
      await progressStatement.executeAsync();
      await libraryStatement.executeAsync();
    } catch (error) {
      throw wrapOperationFailure('очистка данных чтения', error);
    } finally {
      await progressStatement.finalizeAsync();
      await libraryStatement.finalizeAsync();
    }
  }
}
