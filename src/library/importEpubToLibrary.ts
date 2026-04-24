import * as FileSystem from 'expo-file-system/legacy';
import { Alert } from 'react-native';

import {
  EpubService,
  EpubServiceError,
  readFilesystemLibraryMetadata,
} from '../api/EpubService';
import { copyFileToInternalStorage } from '../utils/epubPipelineAndroid';
import type { LibraryBookRecord } from '../core/types';
import { StorageService } from '../database/StorageService';
import { bookFallbackLabels } from '../i18n/catalog';
import type { AppLocale } from '../i18n/types';

const EPUB_SUBDIR = 'library_epubs/';
const COVERS_SUBDIR = 'library_covers/';

function logImportStage(message: string, extra?: Record<string, unknown>): void {
  if (extra) {
    console.log('[Chitalka][Импорт]', message, extra);
  } else {
    console.log('[Chitalka][Импорт]', message);
  }
}

function sanitizeFileStem(bookId: string): string {
  return bookId
    .trim()
    .replace(/[/\\?%*:|"<>.\0]/g, '_')
    .replace(/\s+/g, ' ')
    .slice(0, 120)
    .trim()
    .replace(/^_+|_+$/g, '') || 'book';
}

function shortFileSuffix(bookId: string): string {
  let h = 0;
  for (let i = 0; i < bookId.length; i++) {
    h = (Math.imul(31, h) + bookId.charCodeAt(i)) | 0;
  }
  return Math.abs(h).toString(36);
}

function coverExtensionFromUri(uri: string): string {
  const lower = uri.split('?')[0]?.toLowerCase() ?? '';
  if (lower.endsWith('.png')) return '.png';
  if (lower.endsWith('.webp')) return '.webp';
  if (lower.endsWith('.gif')) return '.gif';
  if (lower.endsWith('.jpeg')) return '.jpeg';
  if (lower.endsWith('.jpg')) return '.jpg';
  return '.jpg';
}

/**
 * Копирует EPUB в постоянный каталог приложения, извлекает метаданные и обложку через {@link EpubService},
 * сохраняет запись в {@link StorageService}.
 */
export async function importEpubToLibrary(
  sourceUri: string,
  bookId: string,
  storage: StorageService,
  locale: AppLocale,
  options?: { suppressSuccessAlert?: boolean }
): Promise<{ stableUri: string; bookId: string }> {
  try {
    const base = FileSystem.documentDirectory;
    if (!base) {
      throw new EpubServiceError(
        'documentDirectory недоступен — нельзя сохранить книгу в библиотеку.'
      );
    }

    const stem = sanitizeFileStem(bookId);
    const fileBase = `${stem}__${shortFileSuffix(bookId)}`;
    const epubDir = `${base}${EPUB_SUBDIR}`;
    const coversDir = `${base}${COVERS_SUBDIR}`;
    await FileSystem.makeDirectoryAsync(epubDir, { intermediates: true });
    await FileSystem.makeDirectoryAsync(coversDir, { intermediates: true });

    const stableUri = `${epubDir}${fileBase}.epub`;

    const tempEpubUri = await copyFileToInternalStorage(sourceUri);
    await FileSystem.copyAsync({ from: tempEpubUri, to: stableUri });
    try {
      await FileSystem.deleteAsync(tempEpubUri, { idempotent: true });
    } catch {
      /* ignore */
    }

    logImportStage('Копирование завершено', { bookId });

    const info = await FileSystem.getInfoAsync(stableUri);
    if (!info.exists || info.isDirectory) {
      throw new EpubServiceError('Не удалось сохранить файл EPUB в библиотеку.');
    }
    const fileSizeBytes =
      typeof info.size === 'number' && Number.isFinite(info.size) ? info.size : 0;

    const svc = new EpubService(stableUri);
    const labels = bookFallbackLabels(locale);
    let coverUri: string | null = null;
    try {
      await svc.unpackThroughStep5();
      const unpacked = svc.getUnpackedRootUri();
      if (!unpacked) {
        throw new EpubServiceError('Распаковка не создала каталог книги.');
      }
      const fsMeta = await readFilesystemLibraryMetadata(unpacked);
      const coverSrc = fsMeta.coverFileUri;
      if (coverSrc) {
        const coverInfo = await FileSystem.getInfoAsync(coverSrc);
        if (coverInfo.exists && !coverInfo.isDirectory) {
          const ext = coverExtensionFromUri(coverSrc);
          const destCover = `${coversDir}${fileBase}_cover${ext}`;
          try {
            await FileSystem.copyAsync({ from: coverSrc, to: destCover });
            coverUri = destCover;
          } catch {
            coverUri = null;
          }
        }
      }

      const row: LibraryBookRecord = {
        bookId,
        fileUri: stableUri,
        title: fsMeta.title.trim() ? fsMeta.title : labels.untitled,
        author: fsMeta.author.trim() ? fsMeta.author : labels.unknownAuthor,
        fileSizeBytes,
        coverUri,
        addedAt: Date.now(),
      };
      await storage.addBook(row);
      logImportStage('Книга добавлена в базу', { bookId, title: row.title });
      if (!options?.suppressSuccessAlert) {
        Alert.alert('Успех', 'Книга добавлена в базу');
      }
      return { stableUri, bookId };
    } finally {
      svc.destroy();
    }
  } catch (e) {
    const message = e instanceof Error ? e.message : String(e);
    logImportStage('Ошибка импорта', { bookId, message });
    throw e;
  }
}
