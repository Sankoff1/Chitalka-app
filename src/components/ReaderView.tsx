import { useCallback, useEffect, useRef } from 'react';
import { StyleSheet, View } from 'react-native';
import WebView from 'react-native-webview';

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
};

export function ReaderView({
  html,
  baseUrl,
  chapterKey,
  initialScrollY,
  onScrollOffsetChange,
}: ReaderViewProps) {
  const webRef = useRef<WebView>(null);
  const scrollDebounce = useRef<ReturnType<typeof setTimeout> | null>(null);
  const onScrollRef = useRef(onScrollOffsetChange);
  onScrollRef.current = onScrollOffsetChange;

  const handleMessage = useCallback((event: { nativeEvent: { data: string } }) => {
    try {
      const payload = JSON.parse(event.nativeEvent.data) as { t?: string; y?: number };
      if (payload.t !== 'scroll' || typeof payload.y !== 'number' || !Number.isFinite(payload.y)) {
        return;
      }
      if (scrollDebounce.current) {
        clearTimeout(scrollDebounce.current);
      }
      scrollDebounce.current = setTimeout(() => {
        scrollDebounce.current = null;
        onScrollRef.current(payload.y as number);
      }, 350);
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
      } catch (e) {}
      true;
    })();
  `;

  const applyInitialScroll = useCallback(() => {
    const y = Math.max(0, Math.floor(initialScrollY));
    if (!Number.isFinite(y)) {
      return;
    }
    webRef.current?.injectJavaScript(`window.scrollTo(0, ${y}); true;`);
  }, [initialScrollY]);

  return (
    <View style={styles.wrap}>
      <WebView
        ref={webRef}
        key={chapterKey}
        style={styles.webview}
        originWhitelist={['*']}
        source={{ html, baseUrl }}
        injectedJavaScript={injectedScrollBridge}
        onMessage={handleMessage}
        onLoadEnd={applyInitialScroll}
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
