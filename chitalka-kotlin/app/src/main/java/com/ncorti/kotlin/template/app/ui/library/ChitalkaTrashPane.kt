@file:Suppress("LongMethod")

package com.ncorti.kotlin.template.app.ui.library

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.i18n.I18nUiState
import com.chitalka.library.LibrarySessionState
import com.chitalka.screens.trash.TrashScreenSpec
import com.chitalka.storage.StorageService
import com.chitalka.storage.listTrashedBooks
import com.chitalka.storage.purgeBook
import com.chitalka.storage.restoreBookFromTrash
import com.ncorti.kotlin.template.app.ui.ChitalkaAppController
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("LongParameterList")
@Composable
fun ChitalkaTrashPane(
    controller: ChitalkaAppController,
    librarySession: LibrarySessionState,
    storage: StorageService,
    i18n: I18nUiState,
    listRefreshNonce: Int,
    normalizedSearchQuery: String,
) {
    val scope = rememberCoroutineScope()
    val locale = i18n.locale
    var rawBooks by remember { mutableStateOf<List<LibraryBookWithProgress>>(emptyList()) }
    LaunchedEffect(listRefreshNonce) {
        rawBooks = storage.listTrashedBooks()
    }
    val visibleBooks =
        remember(rawBooks, normalizedSearchQuery) {
            TrashScreenSpec.visibleBooksForSearch(rawBooks, normalizedSearchQuery)
        }
    var pendingDelete by remember { mutableStateOf<LibraryBookWithProgress?>(null) }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            icon = {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            title = { Text(TrashScreenSpec.purgeConfirmTitle(locale)) },
            text = { Text(TrashScreenSpec.purgeConfirmMessage(locale)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val book = target
                        pendingDelete = null
                        scope.launch {
                            val ok =
                                runCatching {
                                    deleteLibraryFilesQuiet(book)
                                    storage.purgeBook(book.bookId)
                                }.isSuccess
                            if (ok) {
                                librarySession.bumpLibraryEpoch()
                                controller.bumpLists()
                            }
                        }
                    },
                ) {
                    Text(
                        TrashScreenSpec.deleteForeverLabel(locale),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(TrashScreenSpec.purgeCancelLabel(locale))
                }
            },
        )
    }

    if (visibleBooks.isEmpty()) {
        EmptyTrashState(
            message = TrashScreenSpec.emptyListMessage(locale, normalizedSearchQuery.isNotEmpty()),
        )
    } else {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(visibleBooks, key = { it.bookId }) { book ->
                TrashRowCard(
                    book = book,
                    locale = locale,
                    onRestore = {
                        scope.launch {
                            storage.restoreBookFromTrash(book.bookId)
                            librarySession.bumpLibraryEpoch()
                            controller.bumpLists()
                        }
                    },
                    onDelete = { pendingDelete = book },
                )
            }
        }
    }
}

@Composable
private fun EmptyTrashState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.Info,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun TrashRowCard(
    book: LibraryBookWithProgress,
    locale: com.chitalka.i18n.AppLocale,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    book.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    TrashScreenSpec.formatFileSizeMbLine(book.fileSizeBytes, locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRestore) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = TrashScreenSpec.restoreLabel(locale),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = TrashScreenSpec.deleteForeverLabel(locale),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private suspend fun deleteLibraryFilesQuiet(book: LibraryBookWithProgress) {
    withContext(Dispatchers.IO) {
        tryDeleteFileUri(book.fileUri)
        tryDeleteFileUri(book.coverUri)
    }
}

private fun tryDeleteFileUri(uriString: String?) {
    if (uriString.isNullOrBlank()) return
    val path = Uri.parse(uriString).path ?: return
    runCatching { File(path).delete() }
}
