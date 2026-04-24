@file:Suppress("LongMethod", "LongParameterList")

package com.ncorti.kotlin.template.app.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.i18n.I18nUiState
import com.chitalka.library.LibrarySessionState
import com.chitalka.screens.common.BookListSearchFilter
import com.chitalka.storage.StorageService
import com.chitalka.ui.bookactions.BookActionsSheetSpec
import com.chitalka.ui.bookcard.BookCardSpec
import com.ncorti.kotlin.template.app.ui.ChitalkaAppController
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChitalkaLibraryListPane(
    controller: ChitalkaAppController,
    librarySession: LibrarySessionState,
    storage: StorageService,
    i18n: I18nUiState,
    listRefreshNonce: Int,
    normalizedSearchQuery: String,
    loadBooks: suspend () -> List<LibraryBookWithProgress>,
    showImportFab: Boolean,
    onImportClick: () -> Unit,
    onOpenBook: (LibraryBookWithProgress) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var books by remember { mutableStateOf<List<LibraryBookWithProgress>>(emptyList()) }
    LaunchedEffect(listRefreshNonce, normalizedSearchQuery) {
        val raw = loadBooks()
        books =
            BookListSearchFilter.filterBooksByNormalizedSearchQuery(
                raw,
                normalizedSearchQuery,
            )
    }
    var sheetBook by remember { mutableStateOf<LibraryBookWithProgress?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (sheetBook != null) {
        ModalBottomSheet(
            onDismissRequest = { sheetBook = null },
            sheetState = sheetState,
        ) {
            BookActionsContent(
                book = sheetBook!!,
                i18n = i18n,
                storage = storage,
                librarySession = librarySession,
                controller = controller,
                onDismiss = { sheetBook = null },
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (books.isEmpty()) {
            Text(
                i18n.t("books.empty"),
                modifier = Modifier.padding(24.dp).align(Alignment.Center),
            )
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(BookCardSpec.Layout.CARD_MARGIN_BOTTOM_DP.dp),
            ) {
                items(books, key = { it.bookId }) { book ->
                    BookRowCard(
                        book = book,
                        i18n = i18n,
                        onClick = { onOpenBook(book) },
                        onLongClick = { sheetBook = book },
                        onMenuClick = { sheetBook = book },
                    )
                }
            }
        }
        if (showImportFab) {
            FloatingActionButton(
                onClick = onImportClick,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = i18n.t("books.addBookA11y"))
            }
        }
    }
}

@Composable
private fun BookRowCard(
    book: LibraryBookWithProgress,
    i18n: I18nUiState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val ctx = LocalContext.current
    val coverW = BookCardSpec.Layout.COVER_WIDTH_DP.dp
    val coverH = BookCardSpec.Layout.coverHeightDp().dp
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            Modifier.padding(BookCardSpec.Layout.CARD_PADDING_DP.dp),
            horizontalArrangement = Arrangement.spacedBy(BookCardSpec.Layout.ROW_GAP_DP.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (book.coverUri != null) {
                AsyncImage(
                    model =
                        ImageRequest.Builder(ctx)
                            .data(book.coverUri)
                            .crossfade(true)
                            .build(),
                    contentDescription = null,
                    modifier = Modifier.width(coverW).height(coverH),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier.width(coverW).height(coverH),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📖", style = MaterialTheme.typography.headlineSmall)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(book.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val p = book.progressFraction
                if (p != null && p > 0) {
                    LinearProgressIndicator(
                        progress = { p.toFloat().coerceIn(0f, 1f) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                    )
                }
            }
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(BookCardSpec.Layout.MENU_BUTTON_SIZE_DP.dp),
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = i18n.t(BookCardSpec.I18nKeys.A11Y_OPEN_MENU),
                )
            }
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun BookActionsContent(
    book: LibraryBookWithProgress,
    i18n: I18nUiState,
    storage: StorageService,
    librarySession: LibrarySessionState,
    controller: ChitalkaAppController,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    Column(Modifier.padding(16.dp)) {
        Text(book.title, style = MaterialTheme.typography.titleMedium)
        Text(book.author, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(
            onClick = {
                scope.launch {
                    storage.setBookFavorite(book.bookId, !book.isFavorite)
                    librarySession.bumpLibraryEpoch()
                    controller.bumpLists()
                    onDismiss()
                }
            },
        ) {
            Text(
                if (book.isFavorite) {
                    i18n.t(BookActionsSheetSpec.I18nKeys.REMOVE_FROM_FAVORITES)
                } else {
                    i18n.t(BookActionsSheetSpec.I18nKeys.ADD_TO_FAVORITES)
                },
            )
        }
        TextButton(
            onClick = {
                scope.launch {
                    storage.moveBookToTrash(book.bookId)
                    librarySession.bumpLibraryEpoch()
                    controller.bumpLists()
                    onDismiss()
                }
            },
        ) {
            Text(i18n.t(BookActionsSheetSpec.I18nKeys.MOVE_TO_TRASH))
        }
        TextButton(onClick = onDismiss) {
            Text(i18n.t(BookActionsSheetSpec.I18nKeys.COMMON_CANCEL))
        }
    }
}
