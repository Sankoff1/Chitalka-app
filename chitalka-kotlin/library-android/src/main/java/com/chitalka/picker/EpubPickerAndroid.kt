@file:Suppress("ReturnCount")
package com.chitalka.picker

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.contract.ActivityResultContracts
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Связка с системным выбором документа (аналог `pickEpubAsset` из RN).
 *
 * Регистрация в Activity:
 * ```
 * val openEpub = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
 *   val result = EpubPickerAndroid.mapOpenDocumentUri(uri, contentResolver)
 *   ...
 * }
 * // затем:
 * openEpub.launch(EpubPickerAndroid.openDocumentMimeTypes())
 * ```
 */
object EpubPickerAndroid {

    /** Те же MIME, что `EPUB_PICK_TYPES` на Android в TS. */
    fun openDocumentMimeTypes(): Array<String> = epubOpenDocumentMimeTypes()

    /** Контракт для `registerForActivityResult` с нужными MIME. */
    fun openDocumentContract(): ActivityResultContracts.OpenDocument = ActivityResultContracts.OpenDocument()

    /**
     * @param uri результат [ActivityResultContracts.OpenDocument]; `null` — отмена или пустой URI.
     */
    fun mapOpenDocumentUri(
        uri: Uri?,
        contentResolver: ContentResolver,
    ): EpubPickResult {
        if (uri == null) {
            return EpubPickResult.Canceled
        }
        val uriStr = uri.toString().trim()
        if (uriStr.isEmpty()) {
            return EpubPickResult.Canceled
        }

        val displayName = contentResolver.queryDisplayName(uri)?.trim().orEmpty()
        val mime = contentResolver.getType(uri)?.trim()
        val fallbackName =
            runCatching {
                URLDecoder.decode(
                    uriStr.substringAfterLast('/').substringBefore('?'),
                    StandardCharsets.UTF_8.name(),
                )
            }.getOrDefault("")
        val nameForId =
            displayName.ifEmpty {
                fallbackName.trim().ifEmpty { "book.epub" }
            }

        val nameForLikely = displayName.ifEmpty { nameForId }
        if (!isLikelyEpubAsset(nameForLikely, mime, uriStr)) {
            return EpubPickResult.Error("picker.invalidExtension")
        }
        return EpubPickResult.Ok(uri = uriStr, bookId = deriveBookId(nameForId))
    }

    /** Если лаунчер или `ContentResolver` выбросили исключение — как `catch` в RN `pickEpubAsset`. */
    fun pickFailedResult(): EpubPickResult = EpubPickResult.Error("picker.openFailed")
}

private fun ContentResolver.queryDisplayName(uri: Uri): String? =
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
        if (!c.moveToFirst()) {
            return@use null
        }
        val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx < 0) null else c.getString(idx)
    }
