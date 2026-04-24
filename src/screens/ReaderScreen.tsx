import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Animated,
  Easing,
  Pressable,
  StyleSheet,
  Text,
  View,
  useWindowDimensions,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import * as FileSystem from 'expo-file-system/legacy';

import {
  EPUB_EMPTY_SPINE,
  EPUB_ERR_TIMEOUT_COPY,
  EPUB_ERR_TIMEOUT_PREPARE_CHAPTER,
  EPUB_ERR_TIMEOUT_UNZIP,
  EpubService,
  EpubServiceError,
  type EpubSpineItem,
} from '../api/EpubService';
import { ReaderView, type ReaderPageDirection } from '../components/ReaderView';
import { StorageService } from '../database/StorageService';
import { useI18n } from '../i18n';

export type ReaderScreenProps = {
  bookPath: string;
  bookId: string;
  /** Закрыть книгу и вернуться на экран библиотеки. */
  onBackToLibrary?: () => void;
  /** Вызывается после первого успешного открытия книги и сохранения прогресса. */
  onOpened?: () => void;
};

function clampChapterIndex(index: number, spineLength: number): number {
  if (spineLength <= 0) {
    return 0;
  }
  return Math.min(Math.max(0, Math.floor(index)), spineLength - 1);
}

function errorMessage(
  error: unknown,
  t: (path: string, vars?: Record<string, string | number>) => string
): string {
  if (error instanceof EpubServiceError) {
    if (error.message === EPUB_EMPTY_SPINE) {
      return t('reader.errors.emptySpine');
    }
    if (error.message === EPUB_ERR_TIMEOUT_COPY) {
      return t('reader.errors.timeoutCopy');
    }
    if (error.message === EPUB_ERR_TIMEOUT_UNZIP) {
      return t('reader.errors.timeoutUnzip');
    }
    if (error.message === EPUB_ERR_TIMEOUT_PREPARE_CHAPTER) {
      return t('reader.errors.timeoutPrepareChapter');
    }
    return error.message.trim() ? error.message : t('reader.errors.openFailed');
  }
  if (error instanceof Error) {
    return error.message;
  }
  return t('reader.errors.unknown');
}

export function ReaderScreen({
  bookPath,
  bookId,
  onBackToLibrary,
  onOpened,
}: ReaderScreenProps) {
  const { t } = useI18n();
  const storage = useMemo(() => new StorageService(), []);
  const insets = useSafeAreaInsets();
  const { width: screenWidth } = useWindowDimensions();
  const pageAnim = useRef(new Animated.Value(0)).current;
  const flippingRef = useRef(false);

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
  const onOpenedRef = useRef(onOpened);
  onOpenedRef.current = onOpened;

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
          throw new EpubServiceError(EPUB_EMPTY_SPINE);
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
          onOpenedRef.current?.();
        } catch {
          /* автосохранение не должно ломать открытие */
        }
      } catch (e) {
        if (!cancelled) {
          epubRef.current?.destroy();
          epubRef.current = null;
          setPhase('error');
          setErrorText(errorMessage(e, t));
        }
      }
    };

    void run();

    return () => {
      cancelled = true;
      epubRef.current?.destroy();
      epubRef.current = null;
    };
  }, [bookPath, bookId, storage, t]);

  const runFlipAnim = useCallback(
    (toValue: number, duration: number): Promise<void> =>
      new Promise<void>((resolve) => {
        Animated.timing(pageAnim, {
          toValue,
          duration,
          easing: Easing.out(Easing.cubic),
          useNativeDriver: true,
        }).start(() => resolve());
      }),
    [pageAnim]
  );

  const goChapter = useCallback(
    async (nextIndex: number) => {
      const epub = epubRef.current;
      if (!epub || !spine.length || phase !== 'ready' || flippingRef.current) {
        return;
      }
      const clamped = clampChapterIndex(nextIndex, spine.length);
      if (clamped === chapterIndex) {
        return;
      }

      flippingRef.current = true;
      await persistProgress(chapterIndex, latestScrollRef.current);

      const direction = clamped > chapterIndex ? 1 : -1;
      const width = screenWidth || 360;

      try {
        const htmlPromise = epub.prepareChapter(epub.getSpineChapterUri(clamped));
        await runFlipAnim(-direction * width, 200);
        const html = await htmlPromise;

        latestScrollRef.current = 0;
        setChapterIndex(clamped);
        setInitialScrollY(0);
        setChapterHtml(html);
        pageAnim.setValue(direction * width);

        await runFlipAnim(0, 220);
        void persistProgress(clamped, 0);
      } catch (e) {
        pageAnim.setValue(0);
        setPhase('error');
        setErrorText(errorMessage(e, t));
      } finally {
        flippingRef.current = false;
      }
    },
    [chapterIndex, pageAnim, persistProgress, phase, runFlipAnim, screenWidth, spine.length, t]
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

  const onRequestPageChange = useCallback(
    (direction: ReaderPageDirection) => {
      void goChapter(chapterIndex + (direction === 'next' ? 1 : -1));
    },
    [chapterIndex, goChapter]
  );

  /** Корень распакованной книги всегда под `documentDirectory`; WebView резолвит ресурсы от этого `file://` baseUrl. */
  const webViewBaseUrl = useMemo(() => {
    if (!unpackedRootUri) {
      return '';
    }
    const u = unpackedRootUri.endsWith('/') ? unpackedRootUri : `${unpackedRootUri}/`;
    const doc = FileSystem.documentDirectory;
    if (doc) {
      const prefix = doc.endsWith('/') ? doc : `${doc}/`;
      if (!u.startsWith(prefix)) {
        console.warn('[Chitalka][Reader] baseUrl не внутри documentDirectory', u.slice(0, 200));
      }
    }
    return u;
  }, [unpackedRootUri]);

  if (phase === 'error' && errorText) {
    return (
      <View style={[styles.centered, { paddingTop: insets.top + 24 }]}>
        <Text style={styles.errorTitle}>{t('reader.errorTitle')}</Text>
        <Text style={styles.errorBody}>{errorText}</Text>
        {onBackToLibrary ? (
          <Pressable
            onPress={onBackToLibrary}
            style={({ pressed }) => [styles.errorBack, pressed && styles.errorBackPressed]}
          >
            <Text style={styles.errorBackText}>{t('reader.backToBooks')}</Text>
          </Pressable>
        ) : null}
      </View>
    );
  }

  const canBack = chapterIndex > 0;
  const canForward = spine.length > 0 && chapterIndex < spine.length - 1;

  return (
    <View style={[styles.root, { paddingTop: insets.top }]}>
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
            <Text style={styles.libraryLinkText}>{t('reader.backToLibrary')}</Text>
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
          <Text style={styles.navButtonText}>{t('reader.back')}</Text>
        </Pressable>
        <Text style={styles.chapterHint} numberOfLines={1}>
          {spine.length
            ? t('reader.chapterProgress', {
                current: chapterIndex + 1,
                total: spine.length,
              })
            : ''}
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
          <Text style={styles.navButtonText}>{t('reader.forward')}</Text>
        </Pressable>
      </View>

      {phase === 'ready' && unpackedRootUri ? (
        <View style={styles.pageHost}>
          <Animated.View
            style={[styles.pageLayer, { transform: [{ translateX: pageAnim }] }]}
          >
            <ReaderView
              chapterKey={`${bookId}-${chapterIndex}`}
              html={
                chapterHtml.trim().length > 0
                  ? chapterHtml
                  : '<!DOCTYPE html><html><head><meta charset="utf-8"></head><body></body></html>'
              }
              baseUrl={webViewBaseUrl}
              initialScrollY={initialScrollY}
              onScrollOffsetChange={onScrollOffsetChange}
              onRequestPageChange={onRequestPageChange}
            />
          </Animated.View>
        </View>
      ) : (
        <View style={styles.loaderWrap}>
          <ActivityIndicator size="large" />
          <Text style={styles.loaderText}>{t('reader.loading')}</Text>
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
  pageHost: {
    flex: 1,
    overflow: 'hidden',
    backgroundColor: '#ece9e1',
  },
  pageLayer: {
    flex: 1,
    backgroundColor: '#fff',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.35,
    shadowRadius: 12,
    elevation: 12,
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
