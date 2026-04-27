@file:Suppress("LongMethod", "LongParameterList")

package com.ncorti.kotlin.template.app.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.i18n.I18nUiState
import com.chitalka.library.LibrarySessionState
import com.chitalka.screens.common.BookListSearchFilter
import com.chitalka.storage.StorageService
import com.chitalka.ui.bookcard.BookCardSpec
import com.ncorti.kotlin.template.app.ui.ChitalkaAppController

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
    var rawBooks by remember { mutableStateOf<List<LibraryBookWithProgress>>(emptyList()) }
    LaunchedEffect(listRefreshNonce) {
        rawBooks = loadBooks()
    }
    val books =
        remember(rawBooks, normalizedSearchQuery) {
            BookListSearchFilter.filterBooksByNormalizedSearchQuery(
                rawBooks,
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
            EmptyLibraryState(message = i18n.t("books.empty"))
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(BookCardSpec.Layout.CARD_MARGIN_BOTTOM_DP.dp),
            ) {
                items(books, key = { it.record.bookId }) { book ->
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
                shape = CircleShape,
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = i18n.t("books.addBookA11y"),
                )
            }
        }
    }
}

@Composable
private fun EmptyLibraryState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.size(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
