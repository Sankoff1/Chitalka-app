@file:Suppress("LongParameterList", "LongMethod")

package com.ncorti.kotlin.template.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nUiState
import com.chitalka.library.LastOpenBookPersistence
import com.chitalka.library.LibrarySessionState
import com.chitalka.navigation.DrawerScreen
import com.chitalka.storage.StorageService
import com.chitalka.storage.listFavoriteBooks
import com.chitalka.storage.listLibraryBooks
import com.chitalka.storage.listRecentlyReadBooks
import com.chitalka.theme.ThemeMode
import com.ncorti.kotlin.template.app.ui.debug.ChitalkaDebugLogsPane
import com.ncorti.kotlin.template.app.ui.library.ChitalkaLibraryListPane
import com.ncorti.kotlin.template.app.ui.library.ChitalkaTrashPane
import com.ncorti.kotlin.template.app.ui.settings.ChitalkaSettingsPane

@Composable
internal fun ChitalkaDrawerRouter(
    screen: DrawerScreen,
    controller: ChitalkaAppController,
    librarySession: LibrarySessionState,
    storage: StorageService,
    persistence: LastOpenBookPersistence,
    i18n: I18nUiState,
    locale: AppLocale,
    themeMode: ThemeMode,
    listRefreshNonce: Int,
    normalizedSearchQuery: String,
    onRequestImport: () -> Unit,
    onLocaleChange: (AppLocale) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        when (screen) {
            DrawerScreen.ReadingNow ->
                ChitalkaLibraryListPane(
                    controller = controller,
                    librarySession = librarySession,
                    storage = storage,
                    i18n = i18n,
                    listRefreshNonce = listRefreshNonce,
                    normalizedSearchQuery = normalizedSearchQuery,
                    loadBooks = { storage.listRecentlyReadBooks() },
                    showImportFab = true,
                    onImportClick = onRequestImport,
                    onOpenBook = { b -> controller.openReader(b.bookId, b.fileUri) },
                )
            DrawerScreen.BooksAndDocs ->
                ChitalkaLibraryListPane(
                    controller = controller,
                    librarySession = librarySession,
                    storage = storage,
                    i18n = i18n,
                    listRefreshNonce = listRefreshNonce,
                    normalizedSearchQuery = normalizedSearchQuery,
                    loadBooks = { storage.listLibraryBooks() },
                    showImportFab = true,
                    onImportClick = onRequestImport,
                    onOpenBook = { b -> controller.openReader(b.bookId, b.fileUri) },
                )
            DrawerScreen.Favorites ->
                ChitalkaLibraryListPane(
                    controller = controller,
                    librarySession = librarySession,
                    storage = storage,
                    i18n = i18n,
                    listRefreshNonce = listRefreshNonce,
                    normalizedSearchQuery = normalizedSearchQuery,
                    loadBooks = { storage.listFavoriteBooks() },
                    showImportFab = false,
                    onImportClick = onRequestImport,
                    onOpenBook = { b -> controller.openReader(b.bookId, b.fileUri) },
                )
            DrawerScreen.Cart ->
                ChitalkaTrashPane(
                    controller = controller,
                    librarySession = librarySession,
                    storage = storage,
                    i18n = i18n,
                    listRefreshNonce = listRefreshNonce,
                    normalizedSearchQuery = normalizedSearchQuery,
                )
            DrawerScreen.DebugLogs ->
                ChitalkaDebugLogsPane(locale = locale)
            DrawerScreen.Settings ->
                ChitalkaSettingsPane(
                    persistence = persistence,
                    i18n = i18n,
                    locale = locale,
                    themeMode = themeMode,
                    onLocaleChange = onLocaleChange,
                    onThemeModeChange = onThemeModeChange,
                )
        }
    }
}
