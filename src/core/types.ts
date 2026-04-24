/**
 * Persisted reading position for a single book.
 * Stored in SQLite with `book_id` as the primary key.
 */
export interface ReadingProgress {
  bookId: string;
  lastChapterIndex: number;
  scrollOffset: number;
  lastReadTimestamp: number;
}

/** Запись о книге в локальной библиотеке (SQLite). */
export interface LibraryBookRecord {
  bookId: string;
  fileUri: string;
  title: string;
  author: string;
  fileSizeBytes: number;
  coverUri: string | null;
  addedAt: number;
  /** Число глав (длина spine). 0 пока книга ни разу не открывалась в читалке. */
  totalChapters: number;
  /** Отмечена ли книга как избранная. */
  isFavorite: boolean;
  /** Время перемещения в корзину (мс epoch) или `null`, если книга активна. */
  deletedAt: number | null;
}

/** Запись библиотеки, дополненная прогрессом чтения для списочных экранов. */
export interface LibraryBookWithProgress extends LibraryBookRecord {
  /** Индекс последней открытой главы (0-based) или null, если книгу ещё не открывали. */
  lastChapterIndex: number | null;
  /** Доля прочитанного 0..1 или null, если прогресс неизвестен. */
  progressFraction: number | null;
}
