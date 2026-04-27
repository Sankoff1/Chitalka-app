package com.chitalka.ui.readerview

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Throttle обработки scroll-сообщений в нативе после получения из WebView. */
const val READER_BRIDGE_SCROLL_DEBOUNCE_MS: Long = 350L

/** Throttle отправки scroll-сообщений со страницы (внутри injected JS). */
const val READER_WEB_SCROLL_POST_DELAY_MS: Long = 200L

private val bridgeJson = Json { ignoreUnknownKeys = true }

sealed class ReaderBridgeInboundMessage {
    data class Scroll(
        val y: Double,
    ) : ReaderBridgeInboundMessage()

    data class Page(
        val direction: ReaderPageDirection,
    ) : ReaderBridgeInboundMessage()

    data object Ready : ReaderBridgeInboundMessage()
}

/**
 * Разбор JSON-сообщения из WebView в типизированное событие моста.
 * Возвращает `null` на любое не-наше или повреждённое сообщение — это часть контракта,
 * вызывающий не должен ронять WebView из-за случайного `postMessage` от стороннего скрипта.
 */
fun parseReaderBridgeInboundMessage(json: String): ReaderBridgeInboundMessage? =
    try {
        val obj = bridgeJson.parseToJsonElement(json).jsonObject
        val t = obj["t"]?.jsonPrimitive?.content ?: return null
        when (t) {
            "scroll" -> {
                val prim = obj["y"]?.jsonPrimitive ?: return null
                val y = prim.doubleFromBridge() ?: return null
                if (!y.isFinite()) return null
                ReaderBridgeInboundMessage.Scroll(y)
            }
            "page" -> {
                val dir = obj["dir"]?.jsonPrimitive?.content ?: return null
                val d = ReaderPageDirection.fromWire(dir) ?: return null
                ReaderBridgeInboundMessage.Page(d)
            }
            "ready" -> ReaderBridgeInboundMessage.Ready
            else -> null
        }
    } catch (_: Exception) {
        null
    }

private fun JsonPrimitive.doubleFromBridge(): Double? =
    content.toDoubleOrNull()
