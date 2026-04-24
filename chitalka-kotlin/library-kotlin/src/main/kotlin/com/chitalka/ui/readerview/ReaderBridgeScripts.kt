package com.chitalka.ui.readerview

import kotlin.math.floor
import kotlin.math.max

private const val RESOURCE_PATH = "/chitalka/reader/injectedScrollBridge.js"

private object ReaderBridgeAssets

/** Fallback, если нет `requestAnimationFrame` (мс, близко к 2×16 ms). */
const val READER_READY_RAF_FALLBACK_MS: Int = 32

/**
 * Тот же скрипт, что `injectedJavaScript` в `ReaderView.tsx` (скролл, тап-зоны, свайпы).
 */
fun readerInjectedScrollBridge(): String {
    val stream =
        checkNotNull(ReaderBridgeAssets::class.java.getResourceAsStream(RESOURCE_PATH)) {
            "Missing resource $RESOURCE_PATH"
        }
    return stream.bufferedReader().use { it.readText() }
}

/**
 * Скрипт после `onLoadEnd`: начальный скролл и `ready` после двух rAF (`handleLoadEnd` в RN).
 */
fun readerLoadEndScrollAndReadyScript(initialScrollY: Double): String {
    val raw = floor(initialScrollY)
    val y =
        if (raw.isFinite()) {
            max(0, raw.toInt())
        } else {
            0
        }
    return """
        (function () {
          try { window.scrollTo(0, $y); } catch (e) {}
          var ping = function () {
            if (window.ReactNativeWebView) {
              window.ReactNativeWebView.postMessage(JSON.stringify({ t: 'ready' }));
            }
          };
          if (typeof requestAnimationFrame === 'function') {
            requestAnimationFrame(function () { requestAnimationFrame(ping); });
          } else {
            setTimeout(ping, $READER_READY_RAF_FALLBACK_MS);
          }
        })();
        true;
    """.trimIndent()
}
