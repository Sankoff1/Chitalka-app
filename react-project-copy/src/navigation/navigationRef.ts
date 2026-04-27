import { createNavigationContainerRef } from '@react-navigation/native';

import type { RootStackParamList } from './types';

export const navigationRef = createNavigationContainerRef<RootStackParamList>();

type ReaderParams = { bookPath: string; bookId: string };

let pendingReader: ReaderParams | null = null;

/**
 * Вызывать из `NavigationContainer` `onReady`, чтобы не терять переход,
 * если `navigateToReader` успел выполниться до готовности контейнера.
 */
export function flushReaderNavigationIfPending(): void {
  if (!pendingReader || !navigationRef.isReady()) {
    return;
  }
  const p = pendingReader;
  pendingReader = null;
  navigationRef.navigate('Reader', p);
}

/**
 * Переход к экрану читалки с повторными попытками: после импорта/пикера
 * `navigationRef.isReady()` иногда ещё ложь — без этого переход молча не выполняется.
 */
export function navigateToReader(bookPath: string, bookId: string): void {
  pendingReader = { bookPath, bookId };
  flushReaderNavigationIfPending();
  if (!pendingReader) {
    return;
  }
  let attempts = 0;
  const tick = () => {
    flushReaderNavigationIfPending();
    if (!pendingReader) {
      return;
    }
    attempts += 1;
    if (attempts >= 50) {
      console.warn('[Chitalka] navigateToReader: navigation не стал ready за отведённое время');
      pendingReader = null;
      return;
    }
    setTimeout(tick, 50);
  };
  setTimeout(tick, 50);
}
