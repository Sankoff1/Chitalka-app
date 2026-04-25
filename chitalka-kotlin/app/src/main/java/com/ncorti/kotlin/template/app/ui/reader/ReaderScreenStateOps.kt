@file:Suppress(
    "TooManyFunctions",
    "LongMethod",
    "MagicNumber",
    "CyclomaticComplexMethod",
    "LongParameterList",
    "TooGenericExceptionCaught",
    "ReturnCount",
    "NestedBlockDepth",
)

package com.ncorti.kotlin.template.app.ui.reader

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import com.chitalka.core.types.ReadingProgress
import com.chitalka.epub.EpubService
import com.chitalka.epub.EpubServiceError
import com.chitalka.library.refreshBookCount
import com.chitalka.screens.reader.ReaderScreenSpec
import com.chitalka.screens.reader.canAttemptChapterChange
import com.chitalka.screens.reader.clampChapterIndex
import com.chitalka.screens.reader.inactiveLayerId
import com.chitalka.screens.reader.layerToken
import com.chitalka.screens.reader.normalizeSavedScrollOffset
import com.chitalka.screens.reader.shouldSkipChapterNavigation
import com.chitalka.screens.reader.transitionDirectionSign
import com.chitalka.ui.readerview.READER_BRIDGE_SCROLL_DEBOUNCE_MS
import com.chitalka.ui.readerview.ReaderBridgeInboundMessage
import com.chitalka.ui.readerview.ReaderPageDirection
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

internal fun ReaderScreenState.schedulePersist(index: Int, scrollY: Double) {
    persistJob?.cancel()
    persistJob =
        scope.launch {
            delay(ReaderScreenSpec.Timing.SCROLL_PERSIST_DEBOUNCE_MS)
            saveProgressSafely(index, scrollY)
        }
}

internal suspend fun ReaderScreenState.persistNow(index: Int, scrollY: Double) {
    saveProgressSafely(index, scrollY)
}

private suspend fun ReaderScreenState.saveProgressSafely(index: Int, scrollY: Double) {
    try {
        storage.saveProgress(
            ReadingProgress(
                bookId = bookId,
                lastChapterIndex = index,
                scrollOffset = scrollY,
                lastReadTimestamp = System.currentTimeMillis(),
            ),
        )
    } catch (_: Exception) {
    }
}

internal fun ReaderScreenState.launchGoToChapter(targetIndex: Int) {
    scope.launch { goToChapter(targetIndex) }
}

internal suspend fun ReaderScreenState.goToChapter(targetIndex: Int) {
    val svc = epub ?: return
    val currentLayer = activeLayer()
    if (
        !ReaderScreenSpec.canAttemptChapterChange(
            epubNonNull = true,
            spineLength = spineLength,
            phaseReady = phase == ReaderLoadPhase.Ready,
            flipping = busy,
            currentLayerNonNull = currentLayer != null,
        )
    ) {
        return
    }
    val clamped = ReaderScreenSpec.clampChapterIndex(targetIndex, spineLength)
    if (ReaderScreenSpec.shouldSkipChapterNavigation(clamped, currentLayer!!.chapterIndex)) {
        return
    }

    busy = true
    try {
        persistNow(currentLayer.chapterIndex, latestScroll)
        val html = svc.prepareChapter(svc.getSpineChapterUri(clamped))
        val targetLayerId = ReaderScreenSpec.inactiveLayerId(activeLayerId)
        val nextLayer =
            ReaderScreenSpec.ReaderLayerState(
                chapterIndex = clamped,
                html = html,
                initialScrollY = 0.0,
                token = ReaderScreenSpec.layerToken(bookId, clamped, System.currentTimeMillis()),
            )

        val gate = CompletableDeferred<Unit>()
        incomingGateRef.set(gate)
        transitionProgress.snapTo(0f)
        transitionDirection =
            ReaderScreenSpec.transitionDirectionSign(clamped, currentLayer.chapterIndex)
        transitionTargetLayerId = targetLayerId
        when (targetLayerId) {
            ReaderScreenSpec.ReaderLayerId.A -> layerA = nextLayer
            ReaderScreenSpec.ReaderLayerId.B -> layerB = nextLayer
        }

        withTimeoutOrNull(ReaderScreenSpec.Timing.PENDING_LAYER_READY_TIMEOUT_MS) {
            gate.await()
        }
        incomingGateRef.set(null)

        transitionProgress.animateTo(
            targetValue = 1f,
            animationSpec =
                tween(
                    durationMillis = ReaderScreenSpec.Timing.CHAPTER_TRANSITION_DURATION_MS.toInt(),
                    easing = FastOutSlowInEasing,
                ),
        )

        latestScroll = 0.0
        activeLayerId = targetLayerId
        transitionTargetLayerId = null
        transitionProgress.snapTo(0f)

        persistNow(clamped, 0.0)
    } catch (e: Exception) {
        transitionTargetLayerId = null
        incomingGateRef.set(null)
        transitionProgress.snapTo(0f)
        phase = ReaderLoadPhase.Error
        errorText = openErrorText(locale, e)
    } finally {
        busy = false
    }
}

internal fun ReaderScreenState.handleBridge(
    layerId: ReaderScreenSpec.ReaderLayerId,
    msg: ReaderBridgeInboundMessage,
) {
    when (msg) {
        ReaderBridgeInboundMessage.Ready -> {
            if (layerId == transitionTargetLayerId) {
                incomingGateRef.get()?.let { gate ->
                    if (!gate.isCompleted) {
                        gate.complete(Unit)
                    }
                }
            }
        }
        is ReaderBridgeInboundMessage.Scroll -> {
            val transitioning = transitionTargetLayerId != null
            val isActive = layerId == activeLayerId
            if (isActive && !transitioning) {
                scrollBridgeJob?.cancel()
                scrollBridgeJob =
                    scope.launch {
                        delay(READER_BRIDGE_SCROLL_DEBOUNCE_MS)
                        latestScroll = msg.y
                        val layer = activeLayer()
                        if (layer != null) {
                            schedulePersist(layer.chapterIndex, msg.y)
                        }
                    }
            }
        }
        is ReaderBridgeInboundMessage.Page -> {
            val transitioning = transitionTargetLayerId != null
            val isActive = layerId == activeLayerId
            if (isActive && !transitioning && !busy) {
                val delta =
                    when (msg.direction) {
                        ReaderPageDirection.NEXT -> 1
                        ReaderPageDirection.PREV -> -1
                    }
                val cur = activeLayer()?.chapterIndex ?: return
                launchGoToChapter(cur + delta)
            }
        }
    }
}

internal suspend fun ReaderScreenState.initialize(context: Context, nativePath: String) {
    phase = ReaderLoadPhase.Loading
    errorText = null
    layerA = null
    layerB = null
    activeLayerId = ReaderScreenSpec.ReaderLayerId.A
    transitionTargetLayerId = null
    transitionProgress.snapTo(0f)
    busy = false
    incomingGateRef.set(null)

    epub?.destroy()
    val service = EpubService(context, nativePath)
    epub = service
    try {
        val progress = storage.getProgress(bookId)
        val structure = service.open()
        if (structure.spine.isEmpty()) {
            throw EpubServiceError(com.chitalka.epub.EPUB_EMPTY_SPINE)
        }
        spineLength = structure.spine.size
        unpackedRoot = structure.unpackedRootUri
        try {
            storage.setBookTotalChapters(bookId, structure.spine.size)
        } catch (_: Exception) {
        }
        val savedIndex =
            if (progress != null) {
                ReaderScreenSpec.clampChapterIndex(progress.lastChapterIndex, spineLength)
            } else {
                0
            }
        val scroll = ReaderScreenSpec.normalizeSavedScrollOffset(progress?.scrollOffset)
        val uri = service.getSpineChapterUri(savedIndex)
        val html = service.prepareChapter(uri)
        latestScroll = scroll
        layerA =
            ReaderScreenSpec.ReaderLayerState(
                chapterIndex = savedIndex,
                html = html,
                initialScrollY = scroll,
                token = ReaderScreenSpec.layerToken(bookId, savedIndex, System.currentTimeMillis()),
            )
        layerB = null
        activeLayerId = ReaderScreenSpec.ReaderLayerId.A
        transitionTargetLayerId = null
        phase = ReaderLoadPhase.Ready
        try {
            storage.saveProgress(
                ReadingProgress(
                    bookId = bookId,
                    lastChapterIndex = savedIndex,
                    scrollOffset = scroll,
                    lastReadTimestamp = System.currentTimeMillis(),
                ),
            )
            librarySession.refreshBookCount(storage)
        } catch (_: Exception) {
        }
    } catch (e: Exception) {
        service.destroy()
        epub = null
        phase = ReaderLoadPhase.Error
        errorText = openErrorText(locale, e)
    }
}
