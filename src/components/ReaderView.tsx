import { useCallback, useEffect, useRef } from 'react';
import { StyleSheet, View } from 'react-native';
import WebView from 'react-native-webview';

export type ReaderPageDirection = 'prev' | 'next';

export type ReaderViewProps = {
  /** HTML фрагмент (или документ) для отображения. */
  html: string;
  /** Каталог для разрешения относительных ссылок во фрагменте. */
  baseUrl: string;
  /** Меняется при смене главы — сбрасывает WebView. */
  chapterKey: string;
  /** Вертикальная позиция прокрутки после загрузки. */
  initialScrollY: number;
  /** Вызывается с throttling при прокрутке (для автосохранения). */
  onScrollOffsetChange: (scrollY: number) => void;
  /** Запрос смены главы по тапу в зоне или горизонтальному свайпу. */
  onRequestPageChange?: (direction: ReaderPageDirection) => void;
  /** Документ WebView закончил загрузку и initial scroll применен. */
  onContentReady?: () => void;
};

export function ReaderView({
  html,
  baseUrl,
  chapterKey,
  initialScrollY,
  onScrollOffsetChange,
  onRequestPageChange,
  onContentReady,
}: ReaderViewProps) {
  const webRef = useRef<WebView>(null);
  const scrollDebounce = useRef<ReturnType<typeof setTimeout> | null>(null);
  const onScrollRef = useRef(onScrollOffsetChange);
  onScrollRef.current = onScrollOffsetChange;
  const onPageRef = useRef(onRequestPageChange);
  onPageRef.current = onRequestPageChange;
  const onReadyRef = useRef(onContentReady);
  onReadyRef.current = onContentReady;

  const handleMessage = useCallback((event: { nativeEvent: { data: string } }) => {
    try {
      const payload = JSON.parse(event.nativeEvent.data) as {
        t?: string;
        y?: number;
        dir?: ReaderPageDirection;
      };
      if (payload.t === 'scroll') {
        if (typeof payload.y !== 'number' || !Number.isFinite(payload.y)) {
          return;
        }
        if (scrollDebounce.current) {
          clearTimeout(scrollDebounce.current);
        }
        const y = payload.y;
        scrollDebounce.current = setTimeout(() => {
          scrollDebounce.current = null;
          onScrollRef.current(y);
        }, 350);
        return;
      }
      if (payload.t === 'page' && (payload.dir === 'prev' || payload.dir === 'next')) {
        onPageRef.current?.(payload.dir);
        return;
      }
      if (payload.t === 'ready') {
        onReadyRef.current?.();
      }
    } catch {
      /* ignore malformed messages */
    }
  }, []);

  useEffect(
    () => () => {
      if (scrollDebounce.current) {
        clearTimeout(scrollDebounce.current);
      }
    },
    []
  );

  const injectedScrollBridge = `
    (function () {
      try {
        function postY() {
          var y = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0;
          if (window.ReactNativeWebView) {
            window.ReactNativeWebView.postMessage(JSON.stringify({ t: 'scroll', y: y }));
          }
        }
        var timer;
        function onScroll() {
          clearTimeout(timer);
          timer = setTimeout(postY, 200);
        }
        window.addEventListener('scroll', onScroll, { passive: true });

        var startX = 0, startY = 0, startT = 0;
        var TAP_MAX_DELTA = 10;
        var TAP_MAX_TIME = 400;
        var SWIPE_MIN_DX = 50;
        var SWIPE_MAX_DY = 35;
        var SWIPE_MAX_TIME = 600;
        var INTERACTIVE_SELECTOR = 'a,button,input,select,textarea,label,[role="link"],[role="button"]';

        function postPage(dir) {
          if (window.ReactNativeWebView) {
            window.ReactNativeWebView.postMessage(JSON.stringify({ t: 'page', dir: dir }));
          }
        }
        function onTouchStart(e) {
          if (!e.changedTouches || e.changedTouches.length !== 1) {
            startT = 0;
            return;
          }
          var p = e.changedTouches[0];
          startX = p.clientX; startY = p.clientY; startT = Date.now();
        }
        function onTouchEnd(e) {
          if (!startT || !e.changedTouches || e.changedTouches.length !== 1) {
            startT = 0;
            return;
          }
          var p = e.changedTouches[0];
          var dx = p.clientX - startX;
          var dy = p.clientY - startY;
          var dt = Date.now() - startT;
          startT = 0;
          if (window.getSelection && String(window.getSelection()).length > 0) return;
          if (dt <= SWIPE_MAX_TIME && Math.abs(dx) >= SWIPE_MIN_DX && Math.abs(dy) <= SWIPE_MAX_DY) {
            postPage(dx < 0 ? 'next' : 'prev');
            return;
          }
          if (dt <= TAP_MAX_TIME && Math.abs(dx) <= TAP_MAX_DELTA && Math.abs(dy) <= TAP_MAX_DELTA) {
            if (e.target && e.target.closest && e.target.closest(INTERACTIVE_SELECTOR)) return;
            var w = window.innerWidth || document.documentElement.clientWidth || 1;
            var frac = p.clientX / w;
            if (frac <= 0.33) postPage('prev');
            else if (frac >= 0.67) postPage('next');
          }
        }
        window.addEventListener('touchstart', onTouchStart, { passive: true });
        window.addEventListener('touchend', onTouchEnd, { passive: true });
        window.addEventListener('touchcancel', function () { startT = 0; }, { passive: true });
      } catch (e) {}
      true;
    })();
  `;

  const handleLoadEnd = useCallback(() => {
    const raw = Math.floor(initialScrollY);
    const y = Number.isFinite(raw) ? Math.max(0, raw) : 0;
    /* Ждём два rAF, чтобы отчитаться о готовности только после первой реальной отрисовки —
       иначе анимация перелистывания стартует до paint, и текст визуально «пересобирается». */
    webRef.current?.injectJavaScript(`
      (function () {
        try { window.scrollTo(0, ${y}); } catch (e) {}
        var ping = function () {
          if (window.ReactNativeWebView) {
            window.ReactNativeWebView.postMessage(JSON.stringify({ t: 'ready' }));
          }
        };
        if (typeof requestAnimationFrame === 'function') {
          requestAnimationFrame(function () { requestAnimationFrame(ping); });
        } else {
          setTimeout(ping, 32);
        }
      })();
      true;
    `);
  }, [initialScrollY]);

  return (
    <View style={styles.wrap}>
      <WebView
        ref={webRef}
        key={chapterKey}
        style={styles.webview}
        originWhitelist={['*']}
        androidLayerType="hardware"
        source={{ html, baseUrl }}
        injectedJavaScript={injectedScrollBridge}
        onMessage={handleMessage}
        onLoadEnd={handleLoadEnd}
        scrollEnabled
        showsVerticalScrollIndicator
        setSupportMultipleWindows={false}
        domStorageEnabled
        allowFileAccess
        allowFileAccessFromFileURLs={true}
        allowUniversalAccessFromFileURLs
        mixedContentMode="compatibility"
      />
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    flex: 1,
    overflow: 'hidden',
  },
  webview: {
    flex: 1,
    backgroundColor: '#fff',
  },
});
