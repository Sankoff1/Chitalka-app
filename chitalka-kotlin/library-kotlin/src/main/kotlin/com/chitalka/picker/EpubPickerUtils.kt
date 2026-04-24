@file:Suppress("ReturnCount")
package com.chitalka.picker

fun deriveBookId(fileName: String): String {
    val base = fileName.replace(Regex("""^.*[/\\]"""), "").trim()
    val withoutExt = base.replace(Regex("""\.epub$""", RegexOption.IGNORE_CASE), "").trim()
    return if (withoutExt.isNotEmpty()) {
        withoutExt
    } else {
        "book_${System.currentTimeMillis()}"
    }
}

fun isEpubFileName(name: String): Boolean = name.trim().lowercase().endsWith(".epub")

fun isLikelyEpubAsset(
    name: String,
    mimeType: String?,
    uriString: String,
): Boolean {
    if (isEpubFileName(name)) {
        return true
    }
    val mime = mimeType?.trim()?.lowercase().orEmpty()
    if (mime.contains("epub")) {
        return true
    }
    val pathOnly = uriString.substringBefore('?')
    return Regex("""\.epub$""", RegexOption.IGNORE_CASE).containsMatchIn(pathOnly)
}

/** MIME-типы для `OpenDocument` на Android (аналог `EPUB_PICK_TYPES`). */
fun epubOpenDocumentMimeTypes(): Array<String> =
    arrayOf(
        "application/epub+zip",
        "application/octet-stream",
        "application/x-fictionbook+xml",
        "*/*",
    )
