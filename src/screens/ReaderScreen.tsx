import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import { EpubService, EpubServiceError, type EpubSpineItem } from '../api/EpubService';
import { ReaderView } from '../components/ReaderView';
import { StorageService } from '../database/StorageService';

export type ReaderScreenProps = {
  bookPath: string;
  bookId: string;
  /** Закрыть книгу и вернуться на экран библиотеки. */
  onBackToLibrary?: () => void;
};

function clampChapterIndex(index: number, spineLength: number): number {
  if (spineLength <= 0) {
    return 0;
  }
  return Math.min(Math.max(0, Math.floor(index)), spineLength - 1);
}

function errorMessage(error: unknown): string {
  if (error instanceof EpubServiceError) {
    return error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'Произошла неизвестная ошибка при открытии книги.';
}

export function ReaderScreen({ bookPath, bookId, onBackToLibrary }: ReaderScreenProps) {
  const storage = useMemo(() => new StorageService(), []);

  const [phase, setPhase] = useState<'loading' | 'ready' | 'error'>('loading');
  const [errorText, setErrorText] = useState<string | null>(null);

  const [spine, setSpine] = useState<EpubSpineItem[]>([]);
  const [unpackedRootUri, setUnpackedRootUri] = useState('');
  const [chapterIndex, setChapterIndex] = useState(0);
  const [chapterHtml, setChapterHtml] = useState('');
  const [initialScrollY, setInitialScrollY] = useState(0);

  const epubRef = useRef<EpubService | null>(null);
  const latestScrollRef = useRef(0);
  const scrollSaveTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const persistProgress = useCallback(
    async (index: number, scrollY: number) => {
      try {
        await storage.saveProgress({
          bookId,
          lastChapterIndex: index,
          scrollOffset: scrollY,
          lastReadTimestamp: Date.now(),
        });
      } catch {
        /* не блокируем чтение при сбое автосохранения */
      }
    },
    [bookId, storage]
  );

  const scheduleScrollSave = useCallback(
    (index: number, scrollY: number) => {
      latestScrollRef.current = scrollY;
      if (scrollSaveTimer.current) {
        clearTimeout(scrollSaveTimer.current);
      }
      scrollSaveTimer.current = setTimeout(() => {
        scrollSaveTimer.current = null;
        void persistProgress(index, scrollY);
      }, 500);
    },
    [persistProgress]
  );

  useEffect(
    () => () => {
      if (scrollSaveTimer.current) {
        clearTimeout(scrollSaveTimer.current);
      }
    },
    []
  );

  useEffect(() => {
    let cancelled = false;

    const run = async () => {
      setPhase('loading');
      setErrorText(null);
      setSpine([]);
      setChapterHtml('');
      setUnpackedRootUri('');

      epubRef.current?.destroy();
      epubRef.current = new EpubService(bookPath);

      try {
        const progress = await storage.getProgress(bookId);
        const structure = await epubRef.current.open();

        if (cancelled) {
          return;
        }

        if (!structure.spine.length) {
          throw new EpubServiceError('В книге нет ни одной главы (spine пуст).');
        }

        setSpine(structure.spine);
        setUnpackedRootUri(structure.unpackedRootUri);

        const savedIndex =
          progress != null
            ? clampChapterIndex(progress.lastChapterIndex, structure.spine.length)
            : 0;
        const rawScroll = progress?.scrollOffset;
        const scroll =
          typeof rawScroll === 'number' && Number.isFinite(rawScroll) ? rawScroll : 0;

        const chapterUri = epubRef.current.getSpineChapterUri(savedIndex);
        const html = await epubRef.current.prepareChapter(chapterUri);

        if (cancelled) {
          return;
        }

        latestScrollRef.current = Number.isFinite(scroll) ? scroll : 0;
        setChapterIndex(savedIndex);
        setInitialScrollY(latestScrollRef.current);
        setChapterHtml(html);
        setPhase('ready');

        try {
          await storage.saveProgress({
            bookId,
            lastChapterIndex: savedIndex,
            scrollOffset: latestScrollRef.current,
            lastReadTimestamp: Date.now(),
          });
        } catch {
          /* автосохранение не должно ломать открытие */
        }
      } catch (e) {
        if (!cancelled) {
          epubRef.current?.destroy();
          epubRef.current = null;
          setPhase('error');
          setErrorText(errorMessage(e));
        }
      }
    };

    void run();

    return () => {
      cancelled = true;
      epubRef.current?.destroy();
      epubRef.current = null;
    };
  }, [bookPath, bookId, storage]);

  const goChapter = useCallback(
    async (nextIndex: number) => {
      const epub = epubRef.current;
      if (!epub || !spine.length || phase !== 'ready') {
        return;
      }
      const clamped = clampChapterIndex(nextIndex, spine.length);
      if (clamped === chapterIndex) {
        return;
      }

      await persistProgress(chapterIndex, latestScrollRef.current);

      setPhase('loading');
      try {
        const uri = epub.getSpineChapterUri(clamped);
        const html = await epub.prepareChapter(uri);
        latestScrollRef.current = 0;
        setChapterIndex(clamped);
        setInitialScrollY(0);
        setChapterHtml(html);
        setPhase('ready');
        void persistProgress(clamped, 0);
      } catch (e) {
        setPhase('error');
        setErrorText(errorMessage(e));
      }
    },
    [chapterIndex, persistProgress, phase, spine.length]
  );

  const onBack = useCallback(() => {
    void goChapter(chapterIndex - 1);
  }, [chapterIndex, goChapter]);

  const onForward = useCallback(() => {
    void goChapter(chapterIndex + 1);
  }, [chapterIndex, goChapter]);

  const onScrollOffsetChange = useCallback(
    (y: number) => {
      latestScrollRef.current = y;
      scheduleScrollSave(chapterIndex, y);
    },
    [chapterIndex, scheduleScrollSave]
  );

  if (phase === 'error' && errorText) {
    return (
      <View style={styles.centered}>
        <Text style={styles.errorTitle}>Не удалось открыть книгу</Text>
        <Text style={styles.errorBody}>{errorText}</Text>
        {onBackToLibrary ? (
          <Pressable
            onPress={onBackToLibrary}
            style={({ pressed }) => [styles.errorBack, pressed && styles.errorBackPressed]}
          >
            <Text style={styles.errorBackText}>В библиотеку</Text>
          </Pressable>
        ) : null}
      </View>
    );
  }

  const canBack = chapterIndex > 0;
  const canForward = spine.length > 0 && chapterIndex < spine.length - 1;

  return (
    <View style={styles.root}>
      {onBackToLibrary ? (
        <View style={styles.libraryBar}>
          <Pressable
            onPress={onBackToLibrary}
            disabled={phase === 'loading'}
            style={({ pressed }) => [
              styles.libraryLink,
              phase === 'loading' && styles.libraryLinkDisabled,
              pressed && phase !== 'loading' && styles.libraryLinkPressed,
            ]}
          >
            <Text style={styles.libraryLinkText}>← В библиотеку</Text>
          </Pressable>
        </View>
      ) : null}
      <View style={styles.toolbar}>
        <Pressable
          onPress={onBack}
          disabled={!canBack || phase === 'loading'}
          style={({ pressed }) => [
            styles.navButton,
            (!canBack || phase === 'loading') && styles.navButtonDisabled,
            pressed && canBack && phase === 'ready' && styles.navButtonPressed,
          ]}
        >
          <Text style={styles.navButtonText}>Назад</Text>
        </Pressable>
        <Text style={styles.chapterHint} numberOfLines={1}>
          {spine.length ? `Глава ${chapterIndex + 1} / ${spine.length}` : ''}
        </Text>
        <Pressable
          onPress={onForward}
          disabled={!canForward || phase === 'loading'}
          style={({ pressed }) => [
            styles.navButton,
            (!canForward || phase === 'loading') && styles.navButtonDisabled,
            pressed && canForward && phase === 'ready' && styles.navButtonPressed,
          ]}
        >
          <Text style={styles.navButtonText}>Вперед</Text>
        </Pressable>
      </View>

      {phase === 'ready' && chapterHtml ? (
        <ReaderView
          chapterKey={`${bookId}-${chapterIndex}`}
          html={chapterHtml}
          baseUrl={unpackedRootUri.endsWith('/') ? unpackedRootUri : `${unpackedRootUri}/`}
          initialScrollY={initialScrollY}
          onScrollOffsetChange={onScrollOffsetChange}
        />
      ) : (
        <View style={styles.loaderWrap}>
          <ActivityIndicator size="large" />
          <Text style={styles.loaderText}>Загрузка книги…</Text>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: '#f6f6f4',
  },
  libraryBar: {
    paddingHorizontal: 8,
    paddingTop: 6,
    paddingBottom: 4,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: '#ddd',
    backgroundColor: '#fafaf8',
  },
  libraryLink: {
    alignSelf: 'flex-start',
    paddingVertical: 8,
    paddingHorizontal: 8,
    borderRadius: 8,
  },
  libraryLinkPressed: {
    opacity: 0.85,
  },
  libraryLinkDisabled: {
    opacity: 0.45,
  },
  libraryLinkText: {
    fontSize: 16,
    color: '#1a5f9e',
    fontWeight: '600',
  },
  toolbar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 8,
    paddingVertical: 10,
    gap: 8,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: '#ccc',
    backgroundColor: '#fafaf8',
  },
  navButton: {
    paddingVertical: 10,
    paddingHorizontal: 14,
    borderRadius: 8,
    backgroundColor: '#e8e6e1',
  },
  navButtonPressed: {
    opacity: 0.85,
  },
  navButtonDisabled: {
    opacity: 0.45,
  },
  navButtonText: {
    fontSize: 16,
    color: '#222',
  },
  chapterHint: {
    flex: 1,
    textAlign: 'center',
    fontSize: 14,
    color: '#555',
  },
  loaderWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 12,
  },
  loaderText: {
    fontSize: 15,
    color: '#666',
  },
  centered: {
    flex: 1,
    padding: 24,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#fff',
  },
  errorTitle: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 12,
    color: '#111',
    textAlign: 'center',
  },
  errorBody: {
    fontSize: 15,
    color: '#444',
    textAlign: 'center',
    lineHeight: 22,
  },
  errorBack: {
    marginTop: 20,
    paddingVertical: 12,
    paddingHorizontal: 18,
    borderRadius: 8,
    backgroundColor: '#e8e6e1',
  },
  errorBackPressed: {
    opacity: 0.88,
  },
  errorBackText: {
    fontSize: 16,
    color: '#222',
    fontWeight: '600',
  },
});
