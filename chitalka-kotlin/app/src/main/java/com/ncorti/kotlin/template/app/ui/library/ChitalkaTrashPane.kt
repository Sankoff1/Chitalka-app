@file:Suppress("LongMethod")

package com.ncorti.kotlin.template.app.ui.library

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.i18n.I18nUiState
import com.chitalka.library.LibrarySessionState
import com.chitalka.screens.trash.TrashScreenSpec
import com.chitalka.storage.StorageService
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
                    Text(TrashScreenSpec.deleteForeverLabel(locale))
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
        Text(
            TrashScreenSpec.emptyListMessage(locale, normalizedSearchQuery.isNotEmpty()),
            modifier = Modifier.padding(24.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(visibleBooks, key = { it.bookId }) { book ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(book.title, style = MaterialTheme.typography.titleSmall)
                        Text(book.author, style = MaterialTheme.typography.bodySmall)
                        Text(
                            TrashScreenSpec.formatFileSizeMbLine(book.fileSizeBytes, locale),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(
                        onClick = {
                            scope.launch {
                                storage.restoreBookFromTrash(book.bookId)
                                librarySession.bumpLibraryEpoch()
                                controller.bumpLists()
                            }
                        },
                    ) {
                        Text(TrashScreenSpec.restoreLabel(locale))
                    }
                    TextButton(
                        onClick = { pendingDelete = book },
                    ) {
                        Text(
                            TrashScreenSpec.deleteForeverLabel(locale),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
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
