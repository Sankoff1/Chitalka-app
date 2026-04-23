import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { Alert } from 'react-native';

import { FirstLaunchModal } from '../components/FirstLaunchModal';
import { StorageService } from '../database/StorageService';
import { useI18n } from '../i18n';
import { importEpubToLibrary } from '../library/importEpubToLibrary';
import { navigationRef, navigateToReader } from '../navigation/navigationRef';
import { pickEpubAsset } from '../utils/epubPicker';

type LibraryContextValue = {
  bookCount: number;
  storageReady: boolean;
  /** Увеличивается после успешного импорта — подпишитесь, чтобы обновить список книг на экране. */
  libraryEpoch: number;
  /** Принудительно обновить подписчиков списка библиотеки (например, после импорта вне стандартного потока). */
  bumpLibraryEpoch: () => void;
  refreshBookCount: () => Promise<void>;
  pickEpubFromToolbar: () => Promise<void>;
  openBooksForSearch: () => void;
};

const LibraryContext = createContext<LibraryContextValue | null>(null);

export function LibraryProvider({ children }: { children: ReactNode }) {
  const { t, locale } = useI18n();
  const storage = useMemo(() => new StorageService(), []);
  const [bookCount, setBookCount] = useState(0);
  const [libraryEpoch, setLibraryEpoch] = useState(0);
  const [storageReady, setStorageReady] = useState(false);
  const [welcomeDismissedSession, setWelcomeDismissedSession] = useState(false);
  const [welcomePickerHint, setWelcomePickerHint] = useState<string | null>(null);
  /** На Android системный document picker часто не показывается поверх RN `Modal` — временно скрываем окно. */
  const [suppressWelcomeForPicker, setSuppressWelcomeForPicker] = useState(false);

  const refreshBookCount = useCallback(async () => {
    try {
      const n = await storage.countLibraryBooks();
      setBookCount(n);
    } catch {
      setBookCount(0);
    }
  }, [storage]);

  const bumpLibraryEpoch = useCallback(() => {
    setLibraryEpoch((n) => n + 1);
  }, []);

  useEffect(() => {
    void (async () => {
      await refreshBookCount();
      setStorageReady(true);
    })();
  }, [refreshBookCount]);

  const welcomeModalVisible =
    storageReady &&
    bookCount === 0 &&
    !welcomeDismissedSession &&
    !suppressWelcomeForPicker;

  const dismissWelcomeModal = useCallback(() => {
    setWelcomeDismissedSession(true);
    setWelcomePickerHint(null);
  }, []);

  const openReader = useCallback((uri: string, bookId: string) => {
    navigateToReader(uri, bookId);
  }, []);

  const pickEpubFromToolbar = useCallback(async () => {
    try {
      const r = await pickEpubAsset();
      if (r.kind === 'canceled') {
        return;
      }
      if (r.kind === 'error') {
        Alert.alert('', t(r.messageKey));
        return;
      }
      console.log('[Chitalka][Импорт]', 'Файл выбран', {
        bookId: r.bookId,
        uriPreview: r.uri.slice(0, 72),
      });
      const { stableUri, bookId } = await importEpubToLibrary(
        r.uri,
        r.bookId,
        storage,
        locale
      );
      setLibraryEpoch((n) => n + 1);
      await refreshBookCount();
      openReader(stableUri, bookId);
    } catch (e) {
      const msg = e instanceof Error ? e.message : t('library.importFailed');
      Alert.alert('', msg || t('library.importFailed'));
    }
  }, [locale, openReader, refreshBookCount, storage, t]);

  const pickEpubFromWelcome = useCallback(async () => {
    setWelcomePickerHint(null);
    setSuppressWelcomeForPicker(true);
    await new Promise<void>((resolve) => {
      setTimeout(resolve, 320);
    });
    try {
      const r = await pickEpubAsset();
      if (r.kind === 'canceled') {
        return;
      }
      if (r.kind === 'error') {
        setWelcomePickerHint(t(r.messageKey));
        return;
      }
      console.log('[Chitalka][Импорт]', 'Файл выбран', {
        bookId: r.bookId,
        uriPreview: r.uri.slice(0, 72),
      });
      const { stableUri, bookId } = await importEpubToLibrary(
        r.uri,
        r.bookId,
        storage,
        locale
      );
      setWelcomeDismissedSession(true);
      setLibraryEpoch((n) => n + 1);
      await refreshBookCount();
      openReader(stableUri, bookId);
    } catch (e) {
      const msg =
        e instanceof Error ? e.message || t('library.importFailed') : t('library.importFailed');
      setWelcomePickerHint(msg);
      Alert.alert('', msg);
    } finally {
      setSuppressWelcomeForPicker(false);
    }
  }, [locale, openReader, refreshBookCount, storage, t]);

  const openBooksForSearch = useCallback(() => {
    if (!navigationRef.isReady()) {
      return;
    }
    navigationRef.navigate('Main', { screen: 'BooksAndDocs' });
  }, []);

  const value = useMemo(
    () => ({
      bookCount,
      storageReady,
      libraryEpoch,
      bumpLibraryEpoch,
      refreshBookCount,
      pickEpubFromToolbar,
      openBooksForSearch,
    }),
    [
      bookCount,
      bumpLibraryEpoch,
      libraryEpoch,
      openBooksForSearch,
      pickEpubFromToolbar,
      refreshBookCount,
      storageReady,
    ]
  );

  return (
    <LibraryContext.Provider value={value}>
      {children}
      <FirstLaunchModal
        visible={welcomeModalVisible}
        hint={welcomePickerHint}
        onDismiss={dismissWelcomeModal}
        onPickEpub={() => {
          void pickEpubFromWelcome();
        }}
      />
    </LibraryContext.Provider>
  );
}

export function useLibrary(): LibraryContextValue {
  const ctx = useContext(LibraryContext);
  if (!ctx) {
    throw new Error('useLibrary must be used within LibraryProvider');
  }
  return ctx;
}
