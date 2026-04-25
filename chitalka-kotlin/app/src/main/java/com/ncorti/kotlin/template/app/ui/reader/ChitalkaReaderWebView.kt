@file:Suppress("LongParameterList", "LongMethod")

package com.ncorti.kotlin.template.app.ui.reader

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.chitalka.debug.DebugLogLevel
import com.chitalka.debug.debugLogAppend
import com.chitalka.theme.ThemeColors
import com.chitalka.theme.ThemeMode
import com.chitalka.ui.readerview.ReaderBridgeInboundMessage
import com.chitalka.ui.readerview.injectDarkReaderHead
import com.chitalka.ui.readerview.parseReaderBridgeInboundMessage
import com.chitalka.ui.readerview.readerInjectedScrollBridge
import com.chitalka.ui.readerview.readerLoadEndScrollAndReadyScript

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ChitalkaReaderWebView(
    chapterKey: String,
    html: String,
    baseUrl: String,
    initialScrollY: Double,
    themeMode: ThemeMode,
    themeColors: ThemeColors,
    /** Как `pointerEvents: 'none'` в RN во время перелистывания и на неактивном слое. */
    interceptAllTouches: Boolean,
    onBridgeMessage: (ReaderBridgeInboundMessage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bridgeHandler = rememberUpdatedState(onBridgeMessage)
    val displayHtml =
        remember(html, themeMode, themeColors) {
            if (themeMode == ThemeMode.DARK) {
                injectDarkReaderHead(html, themeColors)
            } else {
                html
            }
        }
    val bridgeScript = remember { readerInjectedScrollBridge() }
    val scrollReadyJs = remember(initialScrollY) { readerLoadEndScrollAndReadyScript(initialScrollY) }

    val touchBlocker =
        remember(interceptAllTouches) {
            if (interceptAllTouches) {
                View.OnTouchListener { _, _ -> true }
            } else {
                null
            }
        }

    key(chapterKey) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                WebView(context).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    @Suppress("DEPRECATION")
                    settings.allowFileAccess = true
                    addJavascriptInterface(
                        ReactNativeWebPolyfill { json ->
                            parseReaderBridgeInboundMessage(json)?.let { msg ->
                                bridgeHandler.value.invoke(msg)
                            }
                        },
                        "ReactNativeWebView",
                    )
                    webChromeClient =
                        object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                val cm = consoleMessage ?: return false
                                val src = cm.sourceId().orEmpty()
                                val loc =
                                    if (cm.lineNumber() > 0) {
                                        "${if (src.isNotEmpty()) "$src:" else ""}${cm.lineNumber()} "
                                    } else {
                                        if (src.isNotEmpty()) "$src " else ""
                                    }
                                val line = "${loc}${cm.message()}".trim()
                                val level =
                                    when (cm.messageLevel()) {
                                        ConsoleMessage.MessageLevel.ERROR -> DebugLogLevel.Error
                                        ConsoleMessage.MessageLevel.WARNING -> DebugLogLevel.Warn
                                        ConsoleMessage.MessageLevel.DEBUG,
                                        ConsoleMessage.MessageLevel.TIP,
                                        -> DebugLogLevel.Debug
                                        else -> DebugLogLevel.Log
                                    }
                                debugLogAppend(level, "[WebView] $line")
                                return true
                            }
                        }
                    webViewClient =
                        object : WebViewClient() {
                            override fun onPageFinished(
                                view: WebView?,
                                url: String?,
                            ) {
                                view?.evaluateJavascript(bridgeScript, null)
                                view?.evaluateJavascript(scrollReadyJs, null)
                            }
                        }
                    loadDataWithBaseURL(
                        baseUrl,
                        displayHtml,
                        "text/html",
                        Charsets.UTF_8.name(),
                        null,
                    )
                    setOnTouchListener(touchBlocker)
                }
            },
            update = { webView ->
                @Suppress("ClickableViewAccessibility")
                webView.setOnTouchListener(touchBlocker)
            },
            onRelease = { it.destroy() },
        )
    }
}
