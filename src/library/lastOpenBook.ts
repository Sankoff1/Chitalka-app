import AsyncStorage from '@react-native-async-storage/async-storage';

/**
 * Ключ последней открытой книги: выставляется при монтировании читалки
 * и очищается при возврате в библиотеку. Если приложение убивают во
 * время чтения, ключ остаётся — и следующий запуск открывает ту же книгу.
 */
const LAST_OPEN_BOOK_STORAGE_KEY = 'chitalka_last_open_book_id';

export async function getLastOpenBookId(): Promise<string | null> {
  try {
    const v = await AsyncStorage.getItem(LAST_OPEN_BOOK_STORAGE_KEY);
    return v && v.trim() ? v : null;
  } catch {
    return null;
  }
}

export async function setLastOpenBookId(bookId: string): Promise<void> {
  if (!bookId || !bookId.trim()) {
    return;
  }
  try {
    await AsyncStorage.setItem(LAST_OPEN_BOOK_STORAGE_KEY, bookId);
  } catch {
    /* best-effort: отсутствие восстановления не ломает чтение */
  }
}

export async function clearLastOpenBookId(): Promise<void> {
  try {
    await AsyncStorage.removeItem(LAST_OPEN_BOOK_STORAGE_KEY);
  } catch {
    /* best-effort */
  }
}
