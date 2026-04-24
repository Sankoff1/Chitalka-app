import { openDatabaseAsync, type SQLiteDatabase } from 'expo-sqlite';

import type {
  LibraryBookRecord,
  LibraryBookWithProgress,
  ReadingProgress,
} from '../core/types';

export type { LibraryBookRecord, LibraryBookWithProgress, ReadingProgress };

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

type RawBookRow = Omit<LibraryBookRecord, 'isFavorite' | 'deletedAt'> & {
  isFavorite: number | boolean | null;
  deletedAt: number | null;
};

type JoinedBookRow = RawBookRow & {
  lastChapterIndex: number | null;
};

function normalizeBookRow(row: RawBookRow): LibraryBookRecord {
  const totalChapters =
    typeof row.totalChapters === 'number' && Number.isFinite(row.totalChapters)
      ? Math.max(0, Math.trunc(row.totalChapters))
      : 0;
  const deletedAt =
    typeof row.deletedAt === 'number' && Number.isFinite(row.deletedAt)
      ? Math.trunc(row.deletedAt)
      : null;
  return {
    bookId: row.bookId,
    fileUri: row.fileUri,
    title: row.title,
    author: row.author,
    fileSizeBytes: row.fileSizeBytes,
    coverUri: row.coverUri,
    addedAt: row.addedAt,
    totalChapters,
    isFavorite: Boolean(row.isFavorite),
    deletedAt,
  };
}

function joinedRowToBookWithProgress(row: JoinedBookRow): LibraryBookWithProgress {
  const base = normalizeBookRow(row);
  const lastChapterIndex =
    typeof row.lastChapterIndex === 'number' && Number.isFinite(row.lastChapterIndex)
      ? Math.max(0, Math.trunc(row.lastChapterIndex))
      : null;
  let progressFraction: number | null = null;
  if (base.totalChapters > 0 && lastChapterIndex != null) {
    const raw = (lastChapterIndex + 1) / base.totalChapters;
    progressFraction = Math.min(1, Math.max(0, raw));
  }
  return {
    ...base,
    lastChapterIndex,
    progressFraction,
  };
}

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

  private async addLibraryColumnIfMissing(
    database: SQLiteDatabase,
    column: string,
    typeClause: string
  ): Promise<void> {
    try {
      await database.execAsync(
        `ALTER TABLE ${LIBRARY_TABLE} ADD COLUMN ${column} ${typeClause};`
      );
    } catch {
      /* колонка уже есть — SQLite кидает "duplicate column name" */
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
          added_at INTEGER NOT NULL,
          total_chapters INTEGER NOT NULL DEFAULT 0,
          is_favorite INTEGER NOT NULL DEFAULT 0,
          deleted_at INTEGER
        );
        CREATE INDEX IF NOT EXISTS idx_${LIBRARY_TABLE}_added
          ON ${LIBRARY_TABLE} (added_at DESC);
      `);
      // Идемпотентные миграции для установок с ранними версиями таблицы:
      // колонки добавляются ДО создания индекса по deleted_at, иначе на старых БД
      // индекс падает с "no such column".
      await this.addLibraryColumnIfMissing(
        database,
        'total_chapters',
        'INTEGER NOT NULL DEFAULT 0'
      );
      await this.addLibraryColumnIfMissing(
        database,
        'is_favorite',
        'INTEGER NOT NULL DEFAULT 0'
      );
      await this.addLibraryColumnIfMissing(database, 'deleted_at', 'INTEGER');
      await database.execAsync(
        `CREATE INDEX IF NOT EXISTS idx_${LIBRARY_TABLE}_deleted
          ON ${LIBRARY_TABLE} (deleted_at);`
      );
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
    // total_chapters/is_favorite не обновляются через upsert: они живут независимо
    // от импорта. Повторный импорт восстанавливает книгу из корзины (deleted_at = NULL).
    const statement = await database.prepareAsync(`
      INSERT INTO ${LIBRARY_TABLE} (
        book_id,
        file_uri,
        title,
        author,
        file_size_bytes,
        cover_uri,
        added_at,
        total_chapters
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      ON CONFLICT(book_id) DO UPDATE SET
        file_uri = excluded.file_uri,
        title = excluded.title,
        author = excluded.author,
        file_size_bytes = excluded.file_size_bytes,
        cover_uri = excluded.cover_uri,
        added_at = excluded.added_at,
        deleted_at = NULL;
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
        Math.max(0, Math.trunc(row.totalChapters)),
      ]);
    } catch (error) {
      throw wrapOperationFailure('сохранение книги в библиотеку', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  /**
   * Обновляет число глав (длину spine) для существующей книги.
   * Вызывается читалкой после разбора EPUB, чтобы списочные экраны могли показать % прочитанного.
   */
  async setBookTotalChapters(bookId: string, totalChapters: number): Promise<void> {
    assertNonEmptyBookId(bookId);
    const normalized = Math.max(0, Math.trunc(Number(totalChapters) || 0));
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(
      `UPDATE ${LIBRARY_TABLE} SET total_chapters = ? WHERE book_id = ?;`
    );
    try {
      await statement.executeAsync([normalized, bookId]);
    } catch (error) {
      throw wrapOperationFailure('обновление числа глав книги', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  async listLibraryBooks(): Promise<LibraryBookWithProgress[]> {
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(`
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
      FROM ${LIBRARY_TABLE} AS lb
      LEFT JOIN ${TABLE_NAME} AS rp ON rp.book_id = lb.book_id
      WHERE lb.deleted_at IS NULL
      ORDER BY lb.added_at DESC;
    `);
    try {
      const result = await statement.executeAsync<JoinedBookRow>();
      const rows = await result.getAllAsync();
      return rows.map(joinedRowToBookWithProgress);
    } catch (error) {
      throw wrapOperationFailure('список книг библиотеки', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  /**
   * Книги, которые пользователь уже открывал (есть запись прогресса), отсортированные
   * по времени последнего чтения (свежие сверху). Используется экраном «Читаю сейчас».
   */
  async listRecentlyReadBooks(): Promise<LibraryBookWithProgress[]> {
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(`
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
      FROM ${LIBRARY_TABLE} AS lb
      INNER JOIN ${TABLE_NAME} AS rp ON rp.book_id = lb.book_id
      WHERE lb.deleted_at IS NULL
      ORDER BY rp.last_read_timestamp DESC;
    `);
    try {
      const result = await statement.executeAsync<JoinedBookRow>();
      const rows = await result.getAllAsync();
      return rows.map(joinedRowToBookWithProgress);
    } catch (error) {
      throw wrapOperationFailure('список читаемых книг', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  /** Книги, отмеченные как избранные. */
  async listFavoriteBooks(): Promise<LibraryBookWithProgress[]> {
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(`
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
      FROM ${LIBRARY_TABLE} AS lb
      LEFT JOIN ${TABLE_NAME} AS rp ON rp.book_id = lb.book_id
      WHERE lb.is_favorite = 1 AND lb.deleted_at IS NULL
      ORDER BY lb.added_at DESC;
    `);
    try {
      const result = await statement.executeAsync<JoinedBookRow>();
      const rows = await result.getAllAsync();
      return rows.map(joinedRowToBookWithProgress);
    } catch (error) {
      throw wrapOperationFailure('список избранных книг', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  /** Книги, перемещённые в корзину. */
  async listTrashedBooks(): Promise<LibraryBookWithProgress[]> {
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(`
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
      FROM ${LIBRARY_TABLE} AS lb
      LEFT JOIN ${TABLE_NAME} AS rp ON rp.book_id = lb.book_id
      WHERE lb.deleted_at IS NOT NULL
      ORDER BY lb.deleted_at DESC;
    `);
    try {
      const result = await statement.executeAsync<JoinedBookRow>();
      const rows = await result.getAllAsync();
      return rows.map(joinedRowToBookWithProgress);
    } catch (error) {
      throw wrapOperationFailure('список книг в корзине', error);
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
        added_at AS addedAt,
        total_chapters AS totalChapters,
        is_favorite AS isFavorite,
        deleted_at AS deletedAt
      FROM ${LIBRARY_TABLE}
      WHERE book_id = ?
      LIMIT 1;
    `);
    try {
      const result = await statement.executeAsync<RawBookRow>([bookId]);
      const row = await result.getFirstAsync();
      return row ? normalizeBookRow(row) : null;
    } catch (error) {
      throw wrapOperationFailure('чтение записи книги', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  /** Установить/снять отметку «Избранное». */
  async setBookFavorite(bookId: string, isFavorite: boolean): Promise<void> {
    assertNonEmptyBookId(bookId);
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(
      `UPDATE ${LIBRARY_TABLE} SET is_favorite = ? WHERE book_id = ?;`
    );
    try {
      await statement.executeAsync([isFavorite ? 1 : 0, bookId]);
    } catch (error) {
      throw wrapOperationFailure('обновление флага избранного', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  /** Переместить книгу в корзину (soft-delete). */
  async moveBookToTrash(bookId: string): Promise<void> {
    assertNonEmptyBookId(bookId);
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(
      `UPDATE ${LIBRARY_TABLE} SET deleted_at = ? WHERE book_id = ?;`
    );
    try {
      await statement.executeAsync([Date.now(), bookId]);
    } catch (error) {
      throw wrapOperationFailure('перемещение книги в корзину', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  /** Восстановить книгу из корзины. */
  async restoreBookFromTrash(bookId: string): Promise<void> {
    assertNonEmptyBookId(bookId);
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(
      `UPDATE ${LIBRARY_TABLE} SET deleted_at = NULL WHERE book_id = ?;`
    );
    try {
      await statement.executeAsync([bookId]);
    } catch (error) {
      throw wrapOperationFailure('восстановление книги из корзины', error);
    } finally {
      await statement.finalizeAsync();
    }
  }

  /** Полностью удалить запись из библиотеки и прогресс чтения. */
  async purgeBook(bookId: string): Promise<void> {
    assertNonEmptyBookId(bookId);
    const database = await this.getDatabase();
    const libStatement = await database.prepareAsync(
      `DELETE FROM ${LIBRARY_TABLE} WHERE book_id = ?;`
    );
    const progressStatement = await database.prepareAsync(
      `DELETE FROM ${TABLE_NAME} WHERE book_id = ?;`
    );
    try {
      await libStatement.executeAsync([bookId]);
      await progressStatement.executeAsync([bookId]);
    } catch (error) {
      throw wrapOperationFailure('окончательное удаление книги', error);
    } finally {
      await libStatement.finalizeAsync();
      await progressStatement.finalizeAsync();
    }
  }

  /**
   * Количество активных книг в локальной библиотеке (без корзины).
   */
  async countLibraryBooks(): Promise<number> {
    const database = await this.getDatabase();
    const statement = await database.prepareAsync(`
      SELECT COUNT(*) AS cnt FROM ${LIBRARY_TABLE} WHERE deleted_at IS NULL;
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
