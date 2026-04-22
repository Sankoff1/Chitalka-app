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
