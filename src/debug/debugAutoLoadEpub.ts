import { Asset } from 'expo-asset';
import { Platform } from 'react-native';

import type { StorageService } from '../database/StorageService';
import type { AppLocale } from '../i18n/types';
import { importEpubToLibrary } from '../library/importEpubToLibrary';

import debugBundledEpubAsset from '../../assets/debug/ebook.demo.epub';

/**
 * Включить автозагрузку демо-EPUB из бандла в __DEV__ (Android/iOS).
 * Выключите здесь, не трогая `LibraryContext`, если нужно временно отключить дебаг.
 */
export const DEBUG_AUTO_LOAD_EPUB_ENABLED = true;

/** Стабильный id записи в SQLite — одна «дебаг-книга» на устройстве. */
export const DEBUG_DEMO_BOOK_ID = 'debug-ebook-demo';

const LOG = '[Chitalka][debug-autoload]';

export type DebugAutoLoadEpubDeps = {
  storage: StorageService;
  locale: AppLocale;
  openReader: (uri: string, bookId: string) => void;
  onImported: () => void;
};

export function isDebugAutoLoadEpubActive(): boolean {
  return (
    __DEV__ &&
    DEBUG_AUTO_LOAD_EPUB_ENABLED &&
    (Platform.OS === 'android' || Platform.OS === 'ios')
  );
}

/** Ненулевое значение, если автозагрузка может выполняться (флаг + платформа). */
export function getDebugEpubImportSpec(): { bookId: string } | null {
  return isDebugAutoLoadEpubActive() ? { bookId: DEBUG_DEMO_BOOK_ID } : null;
}

/**
 * Копирует EPUB из ассета в кэш, импортирует в библиотеку (или открывает уже импортированный),
 * переходит в читалку. Обход системного проводника — URI всегда `file://` после Asset.
 */
export async function runDebugAutoLoadEpubIfNeeded(deps: DebugAutoLoadEpubDeps): Promise<void> {
  if (!isDebugAutoLoadEpubActive()) {
    return;
  }
  const { storage, locale, openReader, onImported } = deps;
  const bookId = DEBUG_DEMO_BOOK_ID;

  const existing = await storage.getLibraryBook(bookId);
  if (existing) {
    if (__DEV__) {
      console.log(LOG, 'уже в библиотеке, открываем', { bookId });
    }
    openReader(existing.fileUri, bookId);
    return;
  }

  const asset = Asset.fromModule(debugBundledEpubAsset);
  await asset.downloadAsync();
  const sourceUri = asset.localUri ?? asset.uri;
  if (!sourceUri) {
    throw new Error('Asset EPUB: нет localUri после downloadAsync');
  }
  if (__DEV__) {
    console.log(LOG, 'импорт из бандла', { bookId, uriPreview: sourceUri.slice(0, 72) });
  }

  const { stableUri } = await importEpubToLibrary(sourceUri, bookId, storage, locale, {
    suppressSuccessAlert: true,
  });
  onImported();
  openReader(stableUri, bookId);
}
