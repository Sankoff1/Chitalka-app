@file:Suppress("LongMethod")

package com.ncorti.kotlin.template.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nUiState
import com.chitalka.library.LastOpenBookPersistence
import com.chitalka.library.LibrarySessionState
import com.chitalka.navigation.DrawerNavigationSpec
import com.chitalka.navigation.DrawerScreen
import com.chitalka.navigation.drawerLabelI18nPath
import com.chitalka.screens.common.BookListSearchFilter
import com.chitalka.storage.StorageService
import com.chitalka.theme.ThemeMode
import com.chitalka.ui.topbar.AppTopBarSpec
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
internal fun ChitalkaMainShell(
    controller: ChitalkaAppController,
    librarySession: LibrarySessionState,
    storage: StorageService,
    persistence: LastOpenBookPersistence,
    locale: AppLocale,
    themeMode: ThemeMode,
    liveBookCount: Int,
    listRefreshNonce: Int,
    importSessionEpoch: Int,
    onRequestImport: () -> Unit,
    onLocaleChange: (AppLocale) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf(DrawerScreen.ReadingNow) }
    val i18n = remember(locale) { I18nUiState(locale) }

    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val normalizedSearchQuery =
        remember(searchQuery) {
            BookListSearchFilter.normalizeBookListSearchQuery(searchQuery)
        }

    var welcomeDismissedLocal by remember { mutableStateOf(false) }
    var pickerSuppressWelcome by remember { mutableStateOf(false) }

    LaunchedEffect(importSessionEpoch) {
        if (importSessionEpoch > 0) {
            pickerSuppressWelcome = false
        }
    }

    LaunchedEffect(selected) {
        if (AppTopBarSpec.shouldAutoCloseSearchForRoute(selected.routeName, isSearchOpen)) {
            isSearchOpen = false
            searchQuery = ""
        }
    }

    val searchChrome =
        remember(liveBookCount, isSearchOpen, searchQuery) {
            AppTopBarSpec.SearchChromeState(
                bookCount = liveBookCount,
                isSearchOpen = isSearchOpen,
                searchQuery = searchQuery,
            )
        }

    val firstLaunchWelcomeVisible =
        librarySession.storageReady &&
            liveBookCount == 0 &&
            !welcomeDismissedLocal &&
            !pickerSuppressWelcome

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                DrawerNavigationSpec.drawerScreenOrder.forEach { screen ->
                    NavigationDrawerItem(
                        selected = screen == selected,
                        onClick = {
                            selected = screen
                            scope.launch { drawerState.close() }
                        },
                        label = { Text(i18n.t(screen.drawerLabelI18nPath)) },
                    )
                }
            }
        },
    ) {
        Scaffold(
            modifier = Modifier.testTag("chitalka_root"),
            topBar = {
                TopAppBar(
                    title = {
                        if (AppTopBarSpec.shouldShowSearchInput(selected.routeName, searchChrome)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(i18n.t(AppTopBarSpec.I18nKeys.SEARCH_PLACEHOLDER)) },
                                singleLine = true,
                            )
                        } else {
                            Text(i18n.t(selected.drawerLabelI18nPath))
                        }
                    },
                    navigationIcon = {
                        if (AppTopBarSpec.shouldShowSearchInput(selected.routeName, searchChrome)) {
                            IconButton(
                                onClick = {
                                    isSearchOpen = false
                                    searchQuery = ""
                                },
                            ) {
                                Text("←")
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription =
                                        i18n.t(AppTopBarSpec.I18nKeys.A11Y_OPEN_MENU),
                                )
                            }
                        }
                    },
                    actions = {
                        if (AppTopBarSpec.shouldShowClearQueryButton(selected.routeName, searchChrome)) {
                            TextButton(onClick = { searchQuery = "" }) {
                                Text("×")
                            }
                        } else if (AppTopBarSpec.shouldShowSearchButton(selected.routeName, searchChrome)) {
                            TextButton(onClick = { isSearchOpen = true }) {
                                Text("⌕")
                            }
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                ChitalkaDrawerRouter(
                    screen = selected,
                    controller = controller,
                    librarySession = librarySession,
                    storage = storage,
                    persistence = persistence,
                    i18n = i18n,
                    locale = locale,
                    themeMode = themeMode,
                    listRefreshNonce = listRefreshNonce,
                    normalizedSearchQuery = normalizedSearchQuery,
                    onRequestImport = onRequestImport,
                    onLocaleChange = onLocaleChange,
                    onThemeModeChange = onThemeModeChange,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    if (firstLaunchWelcomeVisible) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(i18n.t("firstLaunch.message")) },
            text = {
                librarySession.welcomePickerHint?.let { key ->
                    Text(
                        i18n.t(key),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerSuppressWelcome = true
                        librarySession.setSuppressWelcomeForPicker(true)
                        onRequestImport()
                    },
                ) {
                    Text(i18n.t("firstLaunch.pickEpub"))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        welcomeDismissedLocal = true
                        librarySession.setWelcomePickerHint(null)
                        librarySession.dismissWelcomeModal()
                    },
                ) {
                    Text(i18n.t("firstLaunch.cancel"))
                }
            },
        )
    }
}
