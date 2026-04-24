package com.chitalka.epub

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Метаданные из OPF после распаковки — только файловая система, без WebView.
 * Аналог RN `readFilesystemLibraryMetadata`.
 */
suspend fun readFilesystemLibraryMetadata(unpackedRootUri: String): FilesystemLibraryMetadata =
    withContext(Dispatchers.IO) {
        try {
            val (opfXml, opfDirFileUrl) = readOpfFromUnpackedRootFiles(unpackedRootUri)
            val title = pickDcText(opfXml, "title")
            val author = pickDcText(opfXml, "creator")
            val coverRel = extractCoverHrefFromOpf(opfXml)
            if (coverRel == null) {
                return@withContext FilesystemLibraryMetadata(title, author, null)
            }
            val coverAbs = joinUnderUnpackedRoot(opfDirFileUrl, coverRel)
            if (!fileExistsAsFile(coverAbs)) {
                FilesystemLibraryMetadata(title, author, null)
            } else {
                FilesystemLibraryMetadata(title, author, coverAbs)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            FilesystemLibraryMetadata("", "", null)
        }
    }
