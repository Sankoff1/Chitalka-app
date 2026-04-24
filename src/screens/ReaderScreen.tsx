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
import { useTheme } from '../theme';

export type ReaderScreenProps = {
  bookPath: string;
  bookId: string;
  /** Закрыть книгу и вернуться на экран библиотеки. */
  onBackToLibrary?: () => void;
  /** Вызывается после первого успешного открытия книги и сохранения прогресса. */
  onOpened?: () => void;
};

type ReaderLayerId = 'a' | 'b';

type ReaderLayerState = {
  chapterIndex: number;
  html: string;
  initialScrollY: number;
  token: string;
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
  const { colors, mode } = useTheme();
  const storage = useMemo(() => new StorageService(), []);
  const insets = useSafeAreaInsets();
  const { width: screenWidth } = useWindowDimensions();
  const transitionAnim = useRef(new Animated.Value(0)).current;
  const flippingRef = useRef(false);
  const pendingReadyResolverRef = useRef<((layerId: ReaderLayerId) => void) | null>(null);
  const pendingReadyTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const transitionResetFrameRef = useRef<number | null>(null);

  const [phase, setPhase] = useState<'loading' | 'ready' | 'error'>('loading');
  const [errorText, setErrorText] = useState<string | null>(null);

  const [spine, setSpine] = useState<EpubSpineItem[]>([]);
  const [unpackedRootUri, setUnpackedRootUri] = useState('');
  const [layerA, setLayerA] = useState<ReaderLayerState | null>(null);
  const [layerB, setLayerB] = useState<ReaderLayerState | null>(null);
  const [activeLayerId, setActiveLayerId] = useState<ReaderLayerId>('a');
  const [transitionTargetLayerId, setTransitionTargetLayerId] = useState<ReaderLayerId | null>(null);
  const [transitionDirection, setTransitionDirection] = useState<1 | -1>(1);

  const epubRef = useRef<EpubService | null>(null);
  const latestScrollRef = useRef(0);
  const scrollSaveTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const onOpenedRef = useRef(onOpened);
  onOpenedRef.current = onOpened;
  const activeLayer = activeLayerId === 'a' ? layerA : layerB;
  const inactiveLayerId: ReaderLayerId = activeLayerId === 'a' ? 'b' : 'a';
  const incomingLayer = transitionTargetLayerId
    ? transitionTargetLayerId === 'a'
      ? layerA
      : layerB
    : null;
  const activeLayerRef = useRef<ReaderLayerState | null>(null);
  activeLayerRef.current = activeLayer;
  const inactiveLayerIdRef = useRef<ReaderLayerId>(inactiveLayerId);
  inactiveLayerIdRef.current = inactiveLayerId;

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

  const clearPendingReadyWait = useCallback(() => {
    if (pendingReadyTimerRef.current) {
      clearTimeout(pendingReadyTimerRef.current);
      pendingReadyTimerRef.current = null;
    }
  }, []);

  const clearTransitionResetFrame = useCallback(() => {
    if (transitionResetFrameRef.current != null) {
      cancelAnimationFrame(transitionResetFrameRef.current);
      transitionResetFrameRef.current = null;
    }
  }, []);

  const resetTransitionAfterCommit = useCallback(() => {
    clearTransitionResetFrame();
    transitionResetFrameRef.current = requestAnimationFrame(() => {
      transitionResetFrameRef.current = null;
      transitionAnim.setValue(0);
    });
  }, [clearTransitionResetFrame, transitionAnim]);

  const handleLayerReady = useCallback(
    (layerId: ReaderLayerId) => {
      const resolve = pendingReadyResolverRef.current;
      if (!resolve) {
        return;
      }
      resolve(layerId);
      if (pendingReadyResolverRef.current == null) {
        clearPendingReadyWait();
      }
    },
    [clearPendingReadyWait]
  );

  const waitForPendingLayer = useCallback(
    (targetLayerId: ReaderLayerId) =>
      new Promise<void>((resolve) => {
        clearPendingReadyWait();
        pendingReadyResolverRef.current = (loadedLayerId) => {
          if (loadedLayerId !== targetLayerId) {
            return;
          }
          pendingReadyResolverRef.current = null;
          clearPendingReadyWait();
          resolve();
        };
        pendingReadyTimerRef.current = setTimeout(() => {
          const done = pendingReadyResolverRef.current;
          done?.(targetLayerId);
        }, 400);
      }),
    [clearPendingReadyWait]
  );

  useEffect(
    () => () => {
      if (scrollSaveTimer.current) {
        clearTimeout(scrollSaveTimer.current);
      }
      pendingReadyResolverRef.current = null;
      clearPendingReadyWait();
      clearTransitionResetFrame();
    },
    [clearPendingReadyWait, clearTransitionResetFrame]
  );

  useEffect(() => {
    let cancelled = false;

    const run = async () => {
      setPhase('loading');
      setErrorText(null);
      setSpine([]);
      setUnpackedRootUri('');
      setLayerA(null);
      setLayerB(null);
      setActiveLayerId('a');
      setTransitionTargetLayerId(null);
      setTransitionDirection(1);
      clearTransitionResetFrame();
      transitionAnim.setValue(0);
      flippingRef.current = false;
      pendingReadyResolverRef.current = null;
      clearPendingReadyWait();

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

        void storage
          .setBookTotalChapters(bookId, structure.spine.length)
          .catch(() => {
            /* не критично: прогресс-бар в списках просто останется пустым */
          });

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
        setLayerA({
          chapterIndex: savedIndex,
          html,
          initialScrollY: latestScrollRef.current,
          token: `${bookId}-${savedIndex}-${Date.now()}`,
        });
        setLayerB(null);
        setActiveLayerId('a');
        setTransitionTargetLayerId(null);
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
      pendingReadyResolverRef.current = null;
      clearPendingReadyWait();
      clearTransitionResetFrame();
      epubRef.current?.destroy();
      epubRef.current = null;
    };
  }, [
    bookPath,
    bookId,
    clearPendingReadyWait,
    clearTransitionResetFrame,
    storage,
    t,
    transitionAnim,
  ]);

  const runTransitionAnim = useCallback(
    (duration: number): Promise<void> =>
      new Promise<void>((resolve) => {
        Animated.timing(transitionAnim, {
          toValue: 1,
          duration,
          easing: Easing.inOut(Easing.cubic),
          useNativeDriver: true,
        }).start(() => resolve());
      }),
    [transitionAnim]
  );

  const goChapter = useCallback(
    async (nextIndex: number) => {
      const epub = epubRef.current;
      const currentLayer = activeLayerRef.current;
      if (!epub || !spine.length || phase !== 'ready' || flippingRef.current || !currentLayer) {
        return;
      }
      const clamped = clampChapterIndex(nextIndex, spine.length);
      if (clamped === currentLayer.chapterIndex) {
        return;
      }

      flippingRef.current = true;
      await persistProgress(currentLayer.chapterIndex, latestScrollRef.current);

      try {
        const html = await epub.prepareChapter(epub.getSpineChapterUri(clamped));
        const targetLayerId = inactiveLayerIdRef.current;
        const nextLayer: ReaderLayerState = {
          chapterIndex: clamped,
          html,
          initialScrollY: 0,
          token: `${bookId}-${clamped}-${Date.now()}`,
        };
        transitionAnim.setValue(0);
        setTransitionDirection(clamped > currentLayer.chapterIndex ? 1 : -1);
        setTransitionTargetLayerId(targetLayerId);
        if (targetLayerId === 'a') {
          setLayerA(nextLayer);
        } else {
          setLayerB(nextLayer);
        }
        await waitForPendingLayer(targetLayerId);
        await runTransitionAnim(380);

        latestScrollRef.current = 0;
        setActiveLayerId(targetLayerId);
        setTransitionTargetLayerId(null);
        resetTransitionAfterCommit();
        void persistProgress(clamped, 0);
      } catch (e) {
        setTransitionTargetLayerId(null);
        resetTransitionAfterCommit();
        setPhase('error');
        setErrorText(errorMessage(e, t));
      } finally {
        pendingReadyResolverRef.current = null;
        clearPendingReadyWait();
        flippingRef.current = false;
      }
    },
    [
      clearPendingReadyWait,
      bookId,
      persistProgress,
      phase,
      resetTransitionAfterCommit,
      runTransitionAnim,
      spine.length,
      t,
      transitionAnim,
      waitForPendingLayer,
    ]
  );

  const onScrollOffsetChange = useCallback(
    (y: number) => {
      latestScrollRef.current = y;
      const currentLayer = activeLayerRef.current;
      if (currentLayer) {
        scheduleScrollSave(currentLayer.chapterIndex, y);
      }
    },
    [scheduleScrollSave]
  );

  const onRequestPageChange = useCallback(
    (direction: ReaderPageDirection) => {
      const currentChapterIndex = activeLayerRef.current?.chapterIndex ?? 0;
      void goChapter(currentChapterIndex + (direction === 'next' ? 1 : -1));
    },
    [goChapter]
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

  const isTransitioning =
    transitionTargetLayerId != null && activeLayer != null && incomingLayer != null;
  const transitionDistance = Math.max(screenWidth || 360, 1);
  const activeTranslateX = transitionAnim.interpolate({
    inputRange: [0, 1],
    outputRange: [0, -transitionDirection * transitionDistance * 0.12],
  });
  /* Уходящая страница плавно гаснет до 0 — иначе финальный setActiveLayerId хлопает opacity 0.58 → 0. */
  const activeOpacity = transitionAnim.interpolate({
    inputRange: [0, 0.6, 1],
    outputRange: [1, 0.25, 0],
  });
  const incomingTranslateX = transitionAnim.interpolate({
    inputRange: [0, 1],
    outputRange: [transitionDirection * transitionDistance * 0.12, 0],
  });
  /* Держим новую страницу невидимой первые ~30% анимации: даём WebView
     докрасить текст после сигнала ready, чтобы не было видимого «пересбора». */
  const incomingOpacity = transitionAnim.interpolate({
    inputRange: [0, 0.3, 1],
    outputRange: [0, 0, 1],
  });
  const outgoingShadeOpacity = transitionAnim.interpolate({
    inputRange: [0, 1],
    outputRange: [0, 0.08],
  });
  const incomingShadeOpacity = transitionAnim.interpolate({
    inputRange: [0, 0.3, 1],
    outputRange: [0, 0.06, 0],
  });
  const emptyReaderHtml =
    '<!DOCTYPE html><html><head><meta charset="utf-8"></head><body></body></html>';
  const currentChapterIndex = activeLayer?.chapterIndex ?? 0;

  const renderReaderLayer = (layerId: ReaderLayerId, layer: ReaderLayerState | null) => {
    if (!layer) {
      return null;
    }

    const isActiveLayer = layerId === activeLayerId;
    const isIncomingLayer = layerId === transitionTargetLayerId;
    const isVisibleLayer = isActiveLayer || isIncomingLayer;
    const layerHtml = layer.html.trim().length > 0 ? layer.html : emptyReaderHtml;
    const layerAnimatedStyle = isTransitioning
      ? isIncomingLayer
        ? {
            opacity: incomingOpacity,
            transform: [{ translateX: incomingTranslateX }],
          }
        : isActiveLayer
          ? {
              opacity: activeOpacity,
              transform: [{ translateX: activeTranslateX }],
            }
          : {
              opacity: 0,
              transform: [{ translateX: 0 }],
            }
      : isActiveLayer
        ? {
            opacity: 1,
            transform: [{ translateX: 0 }],
          }
        : {
            opacity: 0,
            transform: [{ translateX: 0 }],
          };

    return (
      <Animated.View
        key={layerId}
        pointerEvents={isActiveLayer && !isTransitioning ? 'auto' : 'none'}
        renderToHardwareTextureAndroid={isTransitioning && isVisibleLayer}
        shouldRasterizeIOS={isTransitioning && isVisibleLayer}
        style={[
          styles.pageLayer,
          styles.pageLayerAbsolute,
          isActiveLayer ? styles.pageLayerOutgoing : styles.pageLayerIncoming,
          { backgroundColor: readerPaperBg },
          layerAnimatedStyle,
        ]}
      >
        <ReaderView
          chapterKey={layer.token}
          html={layerHtml}
          baseUrl={webViewBaseUrl}
          initialScrollY={layer.initialScrollY}
          onScrollOffsetChange={onScrollOffsetChange}
          onRequestPageChange={onRequestPageChange}
          onContentReady={() => handleLayerReady(layerId)}
        />
        {isTransitioning && isIncomingLayer ? (
          <Animated.View
            pointerEvents="none"
            style={[styles.pageShade, { opacity: incomingShadeOpacity }]}
          />
        ) : null}
        {isTransitioning && isActiveLayer ? (
          <Animated.View
            pointerEvents="none"
            style={[styles.pageShade, { opacity: outgoingShadeOpacity }]}
          />
        ) : null}
      </Animated.View>
    );
  };

  const hairlineBorder =
    mode === 'dark' ? 'rgba(255,255,255,0.12)' : 'rgba(0,0,0,0.12)';
  const readerFrameBg = mode === 'dark' ? colors.background : '#ece9e1';
  const readerPaperBg = mode === 'dark' ? colors.menuBackground : '#ffffff';

  if (phase === 'error' && errorText) {
    return (
      <View
        style={[
          styles.centered,
          { paddingTop: insets.top + 24, backgroundColor: colors.background },
        ]}
      >
        <Text style={[styles.errorTitle, { color: colors.text }]}>{t('reader.errorTitle')}</Text>
        <Text style={[styles.errorBody, { color: colors.textSecondary }]}>{errorText}</Text>
        {onBackToLibrary ? (
          <Pressable
            onPress={onBackToLibrary}
            style={({ pressed }) => [
              styles.errorBack,
              {
                backgroundColor: mode === 'dark' ? colors.interactive : '#e8e6e1',
              },
              pressed && styles.errorBackPressed,
            ]}
          >
            <Text style={[styles.errorBackText, { color: colors.text }]}>{t('reader.backToBooks')}</Text>
          </Pressable>
        ) : null}
      </View>
    );
  }

  return (
    <View style={[styles.root, { paddingTop: insets.top, backgroundColor: colors.background }]}>
      {onBackToLibrary ? (
        <View
          style={[
            styles.libraryBar,
            {
              backgroundColor: colors.menuBackground,
              borderBottomColor: hairlineBorder,
            },
          ]}
        >
          <Pressable
            onPress={onBackToLibrary}
            disabled={phase === 'loading'}
            style={({ pressed }) => [
              styles.libraryLink,
              phase === 'loading' && styles.libraryLinkDisabled,
              pressed && phase !== 'loading' && styles.libraryLinkPressed,
            ]}
          >
            <Text
              style={[
                styles.libraryLinkText,
                { color: mode === 'dark' ? colors.text : colors.topBar },
              ]}
            >
              {t('reader.backToLibrary')}
            </Text>
          </Pressable>
        </View>
      ) : null}

      {phase === 'ready' && unpackedRootUri && activeLayer ? (
        <View style={[styles.pageHost, { backgroundColor: readerFrameBg }]}>
          {renderReaderLayer('a', layerA)}
          {renderReaderLayer('b', layerB)}
        </View>
      ) : (
        <View style={styles.loaderWrap}>
          <ActivityIndicator size="large" color={colors.topBar} />
          <Text style={[styles.loaderText, { color: colors.textSecondary }]}>
            {t('reader.loading')}
          </Text>
        </View>
      )}

      {phase === 'ready' && spine.length > 0 ? (
        <View
          style={[
            styles.pageIndicator,
            {
              paddingBottom: Math.max(insets.bottom, 8),
              backgroundColor: colors.menuBackground,
              borderTopColor: hairlineBorder,
            },
          ]}
        >
          <Text style={[styles.pageIndicatorText, { color: colors.textSecondary }]}>
            {currentChapterIndex + 1}/{spine.length}
          </Text>
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
  },
  libraryBar: {
    paddingHorizontal: 8,
    paddingTop: 6,
    paddingBottom: 4,
    borderBottomWidth: StyleSheet.hairlineWidth,
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
    fontWeight: '600',
  },
  pageIndicator: {
    alignItems: 'center',
    paddingTop: 6,
    borderTopWidth: StyleSheet.hairlineWidth,
  },
  pageIndicatorText: {
    fontSize: 13,
    fontVariant: ['tabular-nums'],
  },
  pageHost: {
    flex: 1,
    overflow: 'hidden',
    position: 'relative',
  },
  pageLayer: {
    ...StyleSheet.absoluteFillObject,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.35,
    shadowRadius: 12,
    elevation: 12,
  },
  pageLayerAbsolute: {
    position: 'absolute',
  },
  pageLayerIncoming: {
    zIndex: 1,
  },
  pageLayerOutgoing: {
    zIndex: 2,
  },
  pageShade: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: '#000',
  },
  loaderWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 12,
  },
  loaderText: {
    fontSize: 15,
  },
  centered: {
    flex: 1,
    padding: 24,
    justifyContent: 'center',
    alignItems: 'center',
  },
  errorTitle: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 12,
    textAlign: 'center',
  },
  errorBody: {
    fontSize: 15,
    textAlign: 'center',
    lineHeight: 22,
  },
  errorBack: {
    marginTop: 20,
    paddingVertical: 12,
    paddingHorizontal: 18,
    borderRadius: 8,
  },
  errorBackPressed: {
    opacity: 0.88,
  },
  errorBackText: {
    fontSize: 16,
    fontWeight: '600',
  },
});
