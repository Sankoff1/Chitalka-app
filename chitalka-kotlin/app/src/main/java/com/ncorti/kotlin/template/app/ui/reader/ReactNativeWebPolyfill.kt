package com.ncorti.kotlin.template.app.ui.reader

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

/**
 * Подмена `window.ReactNativeWebView.postMessage` из RN WebView для нативного [android.webkit.WebView].
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
