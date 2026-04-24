@file:Suppress("LongParameterList", "MagicNumber")

package com.ncorti.kotlin.template.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.chitalka.screens.reader.ReaderScreenSpec
import com.chitalka.theme.ThemeColors
import com.chitalka.theme.ThemeMode
import com.chitalka.ui.readerview.ReaderBridgeInboundMessage

internal fun parseThemeColor(hex: String): Color {
    val s = hex.trim().removePrefix("#")
    val v = s.toLong(16)
    val argb: Int =
        when (s.length) {
            6 -> (0xFF shl 24) or (v.toInt() and 0xFFFFFF)
            8 -> (v and 0xFFFFFFFFL).toInt()
            else -> 0xFF000000.toInt()
        }
    return Color(argb)
}

@Composable
internal fun ReaderPageLayer(
    layerId: ReaderScreenSpec.ReaderLayerId,
    layer: ReaderScreenSpec.ReaderLayerState?,
    activeLayerId: ReaderScreenSpec.ReaderLayerId,
    transitionTargetLayerId: ReaderScreenSpec.ReaderLayerId?,
    isTransitioning: Boolean,
    transitionProgress: Float,
    transitionDirection: Int,
    distancePx: Int,
    readerPaperColor: Color,
    baseUrl: String,
    themeMode: ThemeMode,
    themeColors: ThemeColors,
    onBridge: (ReaderScreenSpec.ReaderLayerId, ReaderBridgeInboundMessage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val l = layer ?: return
    val isActive = layerId == activeLayerId
    val isIncoming = layerId == transitionTargetLayerId
    val isOutgoing = isActive && isTransitioning
    val isIncomingLayer = isIncoming && isTransitioning

    val dir = transitionDirection
    val dist = distancePx

    val (alpha, tx) =
        if (isTransitioning) {
            when {
                isIncomingLayer ->
                    ReaderScreenSpec.incomingPageOpacity(transitionProgress) to
                        ReaderScreenSpec.incomingPageTranslateXPx(transitionProgress, dir, dist)
                isOutgoing ->
                    ReaderScreenSpec.outgoingPageOpacity(transitionProgress) to
                        ReaderScreenSpec.outgoingPageTranslateXPx(transitionProgress, dir, dist)
                else ->
                    0f to 0f
            }
        } else if (isActive) {
            1f to 0f
        } else {
            0f to 0f
        }

    val outgoingShade =
        if (isTransitioning && isOutgoing) {
            ReaderScreenSpec.outgoingShadeOpacity(transitionProgress)
        } else {
            0f
        }
    val incomingShade =
        if (isTransitioning && isIncomingLayer) {
            ReaderScreenSpec.incomingShadeOpacity(transitionProgress)
        } else {
            0f
        }

    val z =
        when {
            isTransitioning && isOutgoing -> 2f
            isTransitioning && isIncomingLayer -> 1f
            isActive -> 1f
            else -> 0f
        }

    val interceptTouches = !(isActive && transitionTargetLayerId == null)

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .zIndex(z)
                .graphicsLayer {
                    this.alpha = alpha
                    translationX = tx
                }
                .background(readerPaperColor),
    ) {
        ChitalkaReaderWebView(
            chapterKey = l.token,
            html = ReaderScreenSpec.layerHtmlForWebView(l.html),
            baseUrl = baseUrl,
            initialScrollY = l.initialScrollY,
            themeMode = themeMode,
            themeColors = themeColors,
            interceptAllTouches = interceptTouches,
            onBridgeMessage = { msg -> onBridge(layerId, msg) },
            modifier = Modifier.fillMaxSize(),
        )
        if (incomingShade > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = incomingShade)),
            )
        }
        if (outgoingShade > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = outgoingShade)),
            )
        }
    }
}
