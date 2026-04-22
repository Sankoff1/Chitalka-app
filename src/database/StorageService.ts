import { openDatabaseAsync, type SQLiteDatabase } from 'expo-sqlite';

import type { ReadingProgress } from '../core/types';

export type { ReadingProgress };

const LOG_PREFIX = '[StorageService]';

/** Logical name of the on-device SQLite file (managed by expo-sqlite). */
const DATABASE_NAME = 'chitalka.db';

const TABLE_NAME = 'reading_progress';

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

  /**
   * Removes all persisted reading positions (used by maintenance / «Эксплуатация» flows).
   */
  async clearAllData(): Promise<void> {
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(`DELETE FROM ${TABLE_NAME};`);
    try {
      await statement.executeAsync();
    } catch (error) {
      throw wrapOperationFailure('очистка данных чтения', error);
    } finally {
      await statement.finalizeAsync();
    }
  }
}
