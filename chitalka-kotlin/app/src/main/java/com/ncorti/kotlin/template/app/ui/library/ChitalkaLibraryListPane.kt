@file:Suppress("LongMethod", "LongParameterList")

package com.ncorti.kotlin.template.app.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.i18n.I18nUiState
import com.chitalka.library.LibrarySessionState
import com.chitalka.screens.common.BookListSearchFilter
import com.chitalka.storage.StorageService
import com.chitalka.storage.moveBookToTrash
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
                shape = androidx.compose.foundation.shape.CircleShape,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookRowCard(
    book: LibraryBookWithProgress,
    i18n: I18nUiState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: () -> Unit,
) {
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            Modifier.padding(BookCardSpec.Layout.CARD_PADDING_DP.dp),
            horizontalArrangement = Arrangement.spacedBy(BookCardSpec.Layout.ROW_GAP_DP.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookCover(
                coverUri = book.coverUri,
                coverW = coverW,
                coverH = coverH,
            )
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    if (book.isFavorite) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Text(
                    book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ReadingProgressBlock(book = book, locale = i18n.locale)
            }
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(BookCardSpec.Layout.MENU_BUTTON_SIZE_DP.dp),
            ) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = i18n.t(BookCardSpec.I18nKeys.A11Y_OPEN_MENU),
                )
            }
        }
    }
}

@Composable
private fun ReadingProgressBlock(
    book: LibraryBookWithProgress,
    locale: com.chitalka.i18n.AppLocale,
) {
    val raw = book.progressFraction ?: return
    val fraction = BookCardSpec.clampProgressFraction(raw).toFloat()
    val percent = BookCardSpec.progressPercentRounded(raw)
    val trackHeight = 8.dp
    val trackShape = RoundedCornerShape(percent = 50)
    Column(Modifier.padding(top = BookCardSpec.Layout.PROGRESS_ROW_MARGIN_TOP_DP.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(trackShape)
                .background(MaterialTheme.colorScheme.outlineVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(trackHeight)
                    .clip(trackShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Text(
            BookCardSpec.readPercentLabel(locale, percent),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = BookCardSpec.Layout.PROGRESS_ROW_GAP_DP.dp),
        )
    }
}

@Composable
private fun BookCover(
    coverUri: String?,
    coverW: androidx.compose.ui.unit.Dp,
    coverH: androidx.compose.ui.unit.Dp,
) {
    if (coverUri != null) {
        AsyncImage(
            model = coverUri,
            contentDescription = null,
            modifier = Modifier
                .width(coverW)
                .height(coverH)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = Modifier
                .width(coverW)
                .height(coverH)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
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
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            book.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            book.author,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        ActionRow(
            icon = if (book.isFavorite) Icons.Filled.FavoriteBorder else Icons.Filled.Favorite,
            label = if (book.isFavorite) {
                i18n.t(BookActionsSheetSpec.I18nKeys.REMOVE_FROM_FAVORITES)
            } else {
                i18n.t(BookActionsSheetSpec.I18nKeys.ADD_TO_FAVORITES)
            },
            onClick = {
                scope.launch {
                    storage.setBookFavorite(book.bookId, !book.isFavorite)
                    librarySession.bumpLibraryEpoch()
                    controller.bumpLists()
                    onDismiss()
                }
            },
        )
        ActionRow(
            icon = Icons.Filled.Delete,
            label = i18n.t(BookActionsSheetSpec.I18nKeys.MOVE_TO_TRASH),
            tint = MaterialTheme.colorScheme.error,
            onClick = {
                scope.launch {
                    storage.moveBookToTrash(book.bookId)
                    librarySession.bumpLibraryEpoch()
                    controller.bumpLists()
                    onDismiss()
                }
            },
        )
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(i18n.t(BookActionsSheetSpec.I18nKeys.COMMON_CANCEL))
        }
    }
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick, onLongClick = onClick)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = tint)
    }
}
