package com.ncorti.kotlin.template.app.ui.reader

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

/**
 * Реализация `window.ReactNativeWebView.postMessage` для нативного [android.webkit.WebView]:
 * страница читалки (наследие RN) шлёт сообщения по этому имени, мы пробрасываем их в Kotlin-callback.
 */
class ReactNativeWebPolyfill(
    private val onMessage: (String) -> Unit,
) {
    private val main = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun postMessage(message: String) {
        main.post { onMessage(message) }
    }
}
