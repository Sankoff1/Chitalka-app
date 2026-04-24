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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.chitalka.core.types.LibraryBookRecord
import com.chitalka.core.types.ReadingProgress
import com.chitalka.epub.EpubService
import com.chitalka.epub.EpubServiceError
import com.chitalka.epub.ensureFileUri
import com.chitalka.epub.fileUriToNativePath
import com.chitalka.i18n.AppLocale
import com.chitalka.library.LibrarySessionState
import com.chitalka.library.refreshBookCount
import com.chitalka.screens.reader.ReaderScreenSpec
import com.chitalka.storage.StorageService
import com.chitalka.theme.ThemeColors
import com.chitalka.theme.ThemeMode
import com.chitalka.ui.readerview.READER_BRIDGE_SCROLL_DEBOUNCE_MS
import com.chitalka.ui.readerview.ReaderBridgeInboundMessage
import com.chitalka.ui.readerview.ReaderPageDirection
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private enum class ReaderLoadPhase {
    Loading,
    Ready,
    Error,
}

private fun parseThemeColor(hex: String): Color {
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

@Suppress("LongParameterList")
@Composable
private fun ReaderPageLayer(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChitalkaReaderScreen(
    bookId: String,
    bookFileUri: String,
    storage: StorageService,
    librarySession: LibrarySessionState,
    locale: AppLocale,
    themeMode: ThemeMode,
    themeColors: ThemeColors,
    onBackToLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val nativePath =
        remember(bookFileUri) {
            fileUriToNativePath(ensureFileUri(bookFileUri))
        }

    var phase by remember { mutableStateOf(ReaderLoadPhase.Loading) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var epub by remember { mutableStateOf<EpubService?>(null) }
    var spineLength by remember { mutableStateOf(0) }
    var unpackedRoot by remember { mutableStateOf("") }

    var layerA by remember { mutableStateOf<ReaderScreenSpec.ReaderLayerState?>(null) }
    var layerB by remember { mutableStateOf<ReaderScreenSpec.ReaderLayerState?>(null) }
    var activeLayerId by remember { mutableStateOf(ReaderScreenSpec.ReaderLayerId.A) }
    var transitionTargetLayerId by remember { mutableStateOf<ReaderScreenSpec.ReaderLayerId?>(null) }
    var transitionDirection by remember { mutableStateOf(1) }

    var latestScroll by remember { mutableDoubleStateOf(0.0) }
    var busy by remember { mutableStateOf(false) }
    var scrollBridgeJob by remember { mutableStateOf<Job?>(null) }
    var persistJob by remember { mutableStateOf<Job?>(null) }
    var bookRecord by remember { mutableStateOf<LibraryBookRecord?>(null) }

    val transitionProgress = remember { Animatable(0f) }
    val incomingGateRef = remember { AtomicReference<CompletableDeferred<Unit>?>(null) }

    val activeLayerIdRef = rememberUpdatedState(activeLayerId)
    val transitionTargetRef = rememberUpdatedState(transitionTargetLayerId)
    val busyRef = rememberUpdatedState(busy)

    val distancePx =
        remember(configuration.screenWidthDp, density) {
            with(density) {
                val wPx = configuration.screenWidthDp.dp.roundToPx()
                ReaderScreenSpec.transitionDistancePx(wPx)
            }
        }

    val (readerFrameColor, readerPaperColor) =
        remember(themeMode, themeColors) {
            if (themeMode == ThemeMode.DARK) {
                parseThemeColor(themeColors.background) to parseThemeColor(themeColors.menuBackground)
            } else {
                parseThemeColor(ReaderScreenSpec.Colors.READER_FRAME_BACKGROUND_LIGHT_HEX) to
                    parseThemeColor(ReaderScreenSpec.Colors.READER_PAPER_BACKGROUND_LIGHT_HEX)
            }
        }

    LaunchedEffect(bookId) {
        bookRecord = storage.getLibraryBook(bookId)
    }

    fun activeLayer(): ReaderScreenSpec.ReaderLayerState? =
        when (activeLayerId) {
            ReaderScreenSpec.ReaderLayerId.A -> layerA
            ReaderScreenSpec.ReaderLayerId.B -> layerB
        }

    fun schedulePersist(
        index: Int,
        scrollY: Double,
    ) {
        persistJob?.cancel()
        persistJob =
            scope.launch {
                delay(ReaderScreenSpec.Timing.SCROLL_PERSIST_DEBOUNCE_MS)
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
    }

    suspend fun persistNow(
        index: Int,
        scrollY: Double,
    ) {
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

    suspend fun goToChapter(targetIndex: Int) {
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
            errorText =
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
        } finally {
            busy = false
        }
    }

    fun handleReaderBridge(
        layerId: ReaderScreenSpec.ReaderLayerId,
        msg: ReaderBridgeInboundMessage,
    ) {
        when (msg) {
            ReaderBridgeInboundMessage.Ready -> {
                if (layerId == transitionTargetRef.value) {
                    incomingGateRef.get()?.let { gate ->
                        if (!gate.isCompleted) {
                            gate.complete(Unit)
                        }
                    }
                }
            }
            is ReaderBridgeInboundMessage.Scroll -> {
                val transitioning = transitionTargetRef.value != null
                val isActive = layerId == activeLayerIdRef.value
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
                val transitioning = transitionTargetRef.value != null
                val isActive = layerId == activeLayerIdRef.value
                if (isActive && !transitioning && !busyRef.value) {
                    val delta =
                        when (msg.direction) {
                            ReaderPageDirection.NEXT -> 1
                            ReaderPageDirection.PREV -> -1
                        }
                    val cur = activeLayer()?.chapterIndex ?: return
                    scope.launch { goToChapter(cur + delta) }
                }
            }
        }
    }

    DisposableEffect(nativePath) {
        onDispose {
            scrollBridgeJob?.cancel()
            persistJob?.cancel()
            epub?.destroy()
            epub = null
        }
    }

    LaunchedEffect(bookId, nativePath) {
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
            errorText =
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
        }
    }

    when (phase) {
        ReaderLoadPhase.Error -> {
            Column(
                modifier =
                    modifier
                        .fillMaxSize()
                        .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = ReaderScreenSpec.errorTitle(locale),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = errorText.orEmpty(),
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onBackToLibrary,
                    modifier = Modifier.padding(top = 20.dp),
                ) {
                    Text(ReaderScreenSpec.backToBooks(locale))
                }
            }
        }
        ReaderLoadPhase.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text(
                        ReaderScreenSpec.loading(locale),
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
        ReaderLoadPhase.Ready -> {
            val chapterIndex = activeLayer()?.chapterIndex ?: 0
            val tid = transitionTargetLayerId
            val isTransitioning =
                tid != null &&
                    activeLayer() != null &&
                    (if (tid == ReaderScreenSpec.ReaderLayerId.A) layerA else layerB) != null
            val animProgress = transitionProgress.value

            val baseUrl = ReaderScreenSpec.webViewBaseUrl(unpackedRoot)
            val titleText =
                bookRecord?.title?.trim()?.takeIf { it.isNotEmpty() }.orEmpty()
            Scaffold(
                modifier = modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text(if (titleText.isNotEmpty()) titleText else "…") },
                        navigationIcon = {
                            IconButton(onClick = onBackToLibrary) {
                                Icon(
                                    Icons.Filled.ArrowBack,
                                    contentDescription =
                                        ReaderScreenSpec.backToLibrary(locale),
                                )
                            }
                        },
                    )
                },
                bottomBar = {
                    Column(Modifier.fillMaxWidth()) {
                        if (spineLength > 1) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                TextButton(
                                    onClick = {
                                        scope.launch { goToChapter(chapterIndex - 1) }
                                    },
                                    enabled = !busy && chapterIndex > 0,
                                ) {
                                    Text("‹")
                                }
                                Text(
                                    ReaderScreenSpec.chapterProgressLabel(
                                        locale,
                                        chapterIndex + 1,
                                        spineLength,
                                    ),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                                TextButton(
                                    onClick = {
                                        scope.launch { goToChapter(chapterIndex + 1) }
                                    },
                                    enabled = !busy && chapterIndex < spineLength - 1,
                                ) {
                                    Text("›")
                                }
                            }
                        }
                        Text(
                            text = ReaderScreenSpec.pageIndicatorSlash(chapterIndex, spineLength),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                },
            ) { padding ->
                val contentMod = Modifier.padding(padding)
                if (unpackedRoot.isNotEmpty() && activeLayer() != null) {
                    Box(
                        modifier =
                            contentMod
                                .fillMaxSize()
                                .background(readerFrameColor),
                    ) {
                        ReaderPageLayer(
                            layerId = ReaderScreenSpec.ReaderLayerId.A,
                            layer = layerA,
                            activeLayerId = activeLayerId,
                            transitionTargetLayerId = transitionTargetLayerId,
                            isTransitioning = isTransitioning,
                            transitionProgress = animProgress,
                            transitionDirection = transitionDirection,
                            distancePx = distancePx,
                            readerPaperColor = readerPaperColor,
                            baseUrl = baseUrl,
                            themeMode = themeMode,
                            themeColors = themeColors,
                            onBridge = { lid, msg -> handleReaderBridge(lid, msg) },
                            modifier = Modifier.fillMaxSize(),
                        )
                        ReaderPageLayer(
                            layerId = ReaderScreenSpec.ReaderLayerId.B,
                            layer = layerB,
                            activeLayerId = activeLayerId,
                            transitionTargetLayerId = transitionTargetLayerId,
                            isTransitioning = isTransitioning,
                            transitionProgress = animProgress,
                            transitionDirection = transitionDirection,
                            distancePx = distancePx,
                            readerPaperColor = readerPaperColor,
                            baseUrl = baseUrl,
                            themeMode = themeMode,
                            themeColors = themeColors,
                            onBridge = { lid, msg -> handleReaderBridge(lid, msg) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}
