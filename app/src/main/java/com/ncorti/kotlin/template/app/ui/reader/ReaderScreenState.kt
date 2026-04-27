@file:Suppress("LongParameterList")

package com.ncorti.kotlin.template.app.ui.reader

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.chitalka.core.types.LibraryBookRecord
import com.chitalka.debug.ChitalkaMirrorLog
import com.chitalka.epub.EpubService
import com.chitalka.epub.EpubServiceError
import com.chitalka.i18n.AppLocale
import com.chitalka.library.LibrarySessionState
import com.chitalka.screens.reader.ReaderScreenSpec
import com.chitalka.screens.reader.readerOpenErrorMessage
import com.chitalka.storage.StorageService
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking

internal enum class ReaderLoadPhase {
    Loading,
    Ready,
    Error,
}

@Stable
internal class ReaderScreenState(
    val bookId: String,
    internal val storage: StorageService,
    internal val librarySession: LibrarySessionState,
    internal val locale: AppLocale,
    internal val scope: CoroutineScope,
) {
    var phase by mutableStateOf(ReaderLoadPhase.Loading)
    var errorText by mutableStateOf<String?>(null)
    var epub by mutableStateOf<EpubService?>(null)
    var spineLength by mutableIntStateOf(0)
    var unpackedRoot by mutableStateOf("")

    var layerA by mutableStateOf<ReaderScreenSpec.ReaderLayerState?>(null)
    var layerB by mutableStateOf<ReaderScreenSpec.ReaderLayerState?>(null)
    var activeLayerId by mutableStateOf(ReaderScreenSpec.ReaderLayerId.A)
    var transitionTargetLayerId by mutableStateOf<ReaderScreenSpec.ReaderLayerId?>(null)
    var transitionDirection by mutableIntStateOf(1)

    var latestScroll by mutableDoubleStateOf(0.0)
    var latestScrollRangeMax by mutableDoubleStateOf(0.0)
    var busy by mutableStateOf(false)
    var bookRecord by mutableStateOf<LibraryBookRecord?>(null)

    val transitionProgress = Animatable(0f)

    internal var scrollBridgeJob: Job? = null
    internal var persistJob: Job? = null
    internal val incomingGateRef = AtomicReference<CompletableDeferred<Unit>?>(null)

    fun activeLayer(): ReaderScreenSpec.ReaderLayerState? =
        when (activeLayerId) {
            ReaderScreenSpec.ReaderLayerId.A -> layerA
            ReaderScreenSpec.ReaderLayerId.B -> layerB
        }

    fun dispose() {
        scrollBridgeJob?.cancel()
        persistJob?.cancel()
        if (phase == ReaderLoadPhase.Ready) {
            val layer = activeLayer()
            if (layer != null) {
                try {
                    runBlocking {
                        persistNow(layer.chapterIndex, latestScroll, latestScrollRangeMax)
                    }
                } catch (e: Exception) {
                    ChitalkaMirrorLog.w("Reader", "dispose flush progress failed bookId=$bookId", e)
                }
            }
        }
        epub?.destroy()
        epub = null
    }
}

internal fun openErrorText(locale: AppLocale, e: Exception): String =
    when (e) {
        is EpubServiceError ->
            ReaderScreenSpec.readerOpenErrorMessage(
                locale,
                ReaderScreenSpec.ReaderOpenErrorKind.Epub(e.message ?: ""),
            )
        else ->
            ReaderScreenSpec.readerOpenErrorMessage(
                locale,
                ReaderScreenSpec.ReaderOpenErrorKind.Other(e.message),
            )
    }
