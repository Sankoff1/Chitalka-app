@file:Suppress("LongParameterList")

package com.ncorti.kotlin.template.app.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.i18n.I18nUiState
import com.chitalka.library.LibrarySessionState
import com.chitalka.storage.StorageService
import com.chitalka.storage.moveBookToTrash
import com.chitalka.ui.bookactions.BookActionsSheetSpec
import com.ncorti.kotlin.template.app.ui.ChitalkaAppController
import kotlinx.coroutines.launch

@Composable
internal fun BookActionsContent(
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
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
