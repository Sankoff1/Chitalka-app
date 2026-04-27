@file:Suppress("LongParameterList", "LongMethod")

package com.ncorti.kotlin.template.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chitalka.i18n.AppLocale
import com.chitalka.screens.reader.ReaderScreenSpec
import com.chitalka.screens.reader.webViewBaseUrl
import com.chitalka.theme.ThemeColors
import com.chitalka.theme.ThemeMode

private val ERROR_CONTENT_PADDING = 24.dp
private val ERROR_TEXT_TOP_GAP = 12.dp
private val ERROR_BUTTON_TOP_GAP = 20.dp
private val LOADING_TEXT_TOP_GAP = 12.dp
private val READER_TITLE_FONT_SIZE = 17.sp
private const val EMPTY_TITLE_PLACEHOLDER = "…"

@Composable
internal fun ReaderErrorContent(
    locale: AppLocale,
    errorText: String,
    onBackToLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(ERROR_CONTENT_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = ReaderScreenSpec.errorTitle(locale),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = errorText,
            modifier = Modifier.padding(top = ERROR_TEXT_TOP_GAP),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onBackToLibrary,
            modifier = Modifier.padding(top = ERROR_BUTTON_TOP_GAP),
        ) {
            Text(ReaderScreenSpec.backToBooks(locale))
        }
    }
}

@Composable
internal fun ReaderLoadingContent(
    locale: AppLocale,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(
                ReaderScreenSpec.loading(locale),
                modifier = Modifier.padding(top = LOADING_TEXT_TOP_GAP),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReaderReadyContent(
    state: ReaderScreenState,
    distancePx: Int,
    readerFrameColor: Color,
    readerPaperColor: Color,
    themeMode: ThemeMode,
    themeColors: ThemeColors,
    locale: AppLocale,
    onBackToLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tid = state.transitionTargetLayerId
    val activeLayer = state.activeLayer()
    val isTransitioning =
        tid != null &&
            activeLayer != null &&
            (if (tid == ReaderScreenSpec.ReaderLayerId.A) state.layerA else state.layerB) != null
    val animProgress = state.transitionProgress.value

    val baseUrl = ReaderScreenSpec.webViewBaseUrl(state.unpackedRoot)
    val titleText =
        state.bookRecord?.title?.trim()?.takeIf { it.isNotEmpty() }.orEmpty()
    val currentChapterIndex = activeLayer?.chapterIndex
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (titleText.isNotEmpty()) titleText else EMPTY_TITLE_PLACEHOLDER,
                        fontSize = READER_TITLE_FONT_SIZE,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToLibrary) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription =
                                ReaderScreenSpec.backToLibrary(locale),
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (currentChapterIndex != null && state.spineLength > 0) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(readerFrameColor)
                            .padding(
                                top = ReaderScreenSpec.Layout.PAGE_INDICATOR_PADDING_TOP_DP.dp,
                                bottom = ReaderScreenSpec.Layout.PAGE_INDICATOR_PADDING_BOTTOM_MIN_DP.dp,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            ReaderScreenSpec.pageIndicatorSlash(
                                zeroBasedChapterIndex = currentChapterIndex,
                                spineLength = state.spineLength,
                            ),
                        fontSize = ReaderScreenSpec.Layout.PAGE_INDICATOR_TEXT_FONT_SP.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    ) { padding ->
        val contentMod = Modifier.padding(padding)
        if (state.unpackedRoot.isNotEmpty() && activeLayer != null) {
            Box(
                modifier =
                    contentMod
                        .fillMaxSize()
                        .background(readerFrameColor),
            ) {
                ReaderPageLayer(
                    layerId = ReaderScreenSpec.ReaderLayerId.A,
                    layer = state.layerA,
                    activeLayerId = state.activeLayerId,
                    transitionTargetLayerId = state.transitionTargetLayerId,
                    isTransitioning = isTransitioning,
                    transitionProgress = animProgress,
                    transitionDirection = state.transitionDirection,
                    distancePx = distancePx,
                    readerPaperColor = readerPaperColor,
                    baseUrl = baseUrl,
                    themeMode = themeMode,
                    themeColors = themeColors,
                    onBridge = { lid, msg -> state.handleBridge(lid, msg) },
                    modifier = Modifier.fillMaxSize(),
                )
                ReaderPageLayer(
                    layerId = ReaderScreenSpec.ReaderLayerId.B,
                    layer = state.layerB,
                    activeLayerId = state.activeLayerId,
                    transitionTargetLayerId = state.transitionTargetLayerId,
                    isTransitioning = isTransitioning,
                    transitionProgress = animProgress,
                    transitionDirection = state.transitionDirection,
                    distancePx = distancePx,
                    readerPaperColor = readerPaperColor,
                    baseUrl = baseUrl,
                    themeMode = themeMode,
                    themeColors = themeColors,
                    onBridge = { lid, msg -> state.handleBridge(lid, msg) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
