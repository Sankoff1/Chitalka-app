@file:Suppress("LongParameterList")

package com.ncorti.kotlin.template.app.ui.reader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.chitalka.epub.ensureFileUri
import com.chitalka.epub.fileUriToNativePath
import com.chitalka.i18n.AppLocale
import com.chitalka.library.LibrarySessionState
import com.chitalka.screens.reader.ReaderScreenSpec
import com.chitalka.screens.reader.transitionDistancePx
import com.chitalka.storage.StorageService
import com.chitalka.theme.ThemeColors
import com.chitalka.theme.ThemeMode

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

    val state =
        remember(bookId, storage, librarySession, locale) {
            ReaderScreenState(
                bookId = bookId,
                storage = storage,
                librarySession = librarySession,
                locale = locale,
                scope = scope,
            )
        }

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
        state.bookRecord = storage.getLibraryBook(bookId)
    }

    LaunchedEffect(bookId, nativePath) {
        state.initialize(context, nativePath)
    }

    DisposableEffect(nativePath) {
        onDispose { state.dispose() }
    }

    when (state.phase) {
        ReaderLoadPhase.Error ->
            ReaderErrorContent(
                locale = locale,
                errorText = state.errorText.orEmpty(),
                onBackToLibrary = onBackToLibrary,
                modifier = modifier.fillMaxSize(),
            )
        ReaderLoadPhase.Loading ->
            ReaderLoadingContent(
                locale = locale,
                modifier = modifier.fillMaxSize(),
            )
        ReaderLoadPhase.Ready ->
            ReaderReadyContent(
                state = state,
                distancePx = distancePx,
                readerFrameColor = readerFrameColor,
                readerPaperColor = readerPaperColor,
                themeMode = themeMode,
                themeColors = themeColors,
                locale = locale,
                onBackToLibrary = onBackToLibrary,
                modifier = modifier,
            )
    }
}
