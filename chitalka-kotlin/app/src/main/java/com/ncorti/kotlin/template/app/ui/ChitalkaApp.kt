@file:Suppress("LongMethod")

package com.ncorti.kotlin.template.app.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.loadPersistedLocale
import com.chitalka.library.LibrarySessionState
import com.chitalka.library.importEpubToLibrary
import com.chitalka.library.refreshBookCount
import com.chitalka.library.restoreLastOpenReaderIfNeeded
import com.chitalka.picker.EpubPickResult
import com.chitalka.picker.EpubPickerAndroid
import com.chitalka.prefs.SharedPreferencesKeyValueStore
import com.chitalka.storage.StorageService
import com.chitalka.theme.ThemeMode
import com.chitalka.theme.ThemeUiState
import com.chitalka.theme.loadPersistedThemeMode
import com.ncorti.kotlin.template.app.ui.theme.ChitalkaMaterialTheme
import kotlinx.coroutines.launch

@Composable
fun ChitalkaApp(activity: ComponentActivity) {
    val context = LocalContext.current
    val persistence = remember { SharedPreferencesKeyValueStore(context) }
    val storage = remember { StorageService(context) }
    val librarySession = remember { LibrarySessionState(initialStorageReady = false) }
    val navController = rememberNavController()

    var themeMode by remember { mutableStateOf(ThemeMode.LIGHT) }
    var locale by remember { mutableStateOf(AppLocale.RU) }
    val listNonce = remember { mutableIntStateOf(0) }
    var liveBookCount by remember { mutableIntStateOf(0) }
    var importSessionEpoch by remember { mutableIntStateOf(0) }

    val readerCoordinator = rememberReaderNavCoordinator(activity, navController)
    ReaderNavCoordinatorSideEffects(navController, readerCoordinator)

    val controller = remember(readerCoordinator) {
        ChitalkaAppController(readerCoordinator) { listNonce.intValue++ }
    }

    LaunchedEffect(persistence) {
        themeMode = loadPersistedThemeMode(persistence) ?: ThemeMode.LIGHT
        locale = loadPersistedLocale(persistence) ?: AppLocale.RU
    }

    LaunchedEffect(storage, persistence, controller) {
        librarySession.refreshBookCount(storage)
        liveBookCount = librarySession.bookCount
        librarySession.markStorageReady(true)
        restoreLastOpenReaderIfNeeded(
            lookup = storage,
            lastOpenPersistence = persistence,
            openReader = { fileUri, bookId -> controller.openReader(bookId, fileUri) },
        )
    }

    LaunchedEffect(storage, listNonce.intValue) {
        if (listNonce.intValue > 0) {
            librarySession.refreshBookCount(storage)
            liveBookCount = librarySession.bookCount
        }
    }

    val importLauncher =
        rememberLauncherForActivityResult(EpubPickerAndroid.openDocumentContract()) { uri ->
            importSessionEpoch++
            activity.lifecycleScope.launch {
                val pick =
                    try {
                        EpubPickerAndroid.mapOpenDocumentUri(uri, activity.contentResolver)
                    } catch (_: Exception) {
                        EpubPickerAndroid.pickFailedResult()
                    }
                librarySession.setSuppressWelcomeForPicker(false)
                when (pick) {
                    is EpubPickResult.Ok -> {
                        try {
                            importEpubToLibrary(
                                context = activity,
                                sourceUri = pick.uri,
                                bookId = pick.bookId,
                                storage = storage,
                                locale = locale,
                            )
                            librarySession.setWelcomePickerHint(null)
                            listNonce.intValue++
                        } catch (_: Exception) {
                            librarySession.setWelcomePickerHint("library.importFailed")
                        }
                    }
                    is EpubPickResult.Error -> {
                        librarySession.setWelcomePickerHint(pick.messageKey)
                    }
                    EpubPickResult.Canceled -> {
                    }
                }
            }
        }

    SideEffect {
        activity.enableEdgeToEdge()
    }

    val themeUi = remember(themeMode) { ThemeUiState(mode = themeMode) }

    ChitalkaMaterialTheme(
        mode = themeUi.mode,
        colors = themeUi.colors,
    ) {
        CompositionLocalProvider(
            LocalChitalkaLocale provides locale,
            LocalChitalkaThemeMode provides themeMode,
            LocalChitalkaThemeColors provides themeUi.colors,
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
            ) {
                ChitalkaNavHost(
                    navController = navController,
                    persistence = persistence,
                    librarySession = librarySession,
                    storage = storage,
                ) {
                    ChitalkaMainShell(
                        controller = controller,
                        librarySession = librarySession,
                        storage = storage,
                        persistence = persistence,
                        locale = locale,
                        themeMode = themeMode,
                        liveBookCount = liveBookCount,
                        listRefreshNonce = listNonce.intValue,
                        importSessionEpoch = importSessionEpoch,
                        onRequestImport = {
                            importLauncher.launch(EpubPickerAndroid.openDocumentMimeTypes())
                        },
                        onLocaleChange = { locale = it },
                        onThemeModeChange = { themeMode = it },
                    )
                }
            }
        }
    }
}
