@file:Suppress(
    "TooGenericExceptionCaught",
    "ReturnCount",
    "ThrowsCount",
    "MaxLineLength",
    "MagicNumber",
)
package com.chitalka.epub

import android.content.Context
import com.chitalka.debug.ChitalkaMirrorLog
import com.chitalka.utils.WithTimeoutException
import com.chitalka.utils.withTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@Suppress("TooManyFunctions")
class EpubService(
    context: Context,
    epubFilePath: String,
) {
    private val appContext = context.applicationContext
    private val epubSourceUri = ensureFileUri(epubFilePath)

    private var unpackedRootUri: String? = null
    private var opfDirFileUrl: String = ""
    private var spineItems: List<EpubSpineItem> = emptyList()

    fun getUnpackedRootUri(): String? = unpackedRootUri

    suspend fun unpackThroughStep5() {
        if (unpackedRootUri != null) return

        logEpubOpen("Шаг 1: файл получен", epubSourceUri)
        logEpubOpen("Шаг 2: копирование во внутренний кэш (temp.epub)")

        val localEpubUri =
            try {
                withTimeout(TIMEOUT_COPY_MS, EPUB_ERR_TIMEOUT_COPY) {
                    copySourceToTempEpub(appContext, epubSourceUri)
                }
            } catch (e: WithTimeoutException) {
                throw EpubServiceError(EPUB_ERR_TIMEOUT_COPY, e)
            } catch (e: Exception) {
                throw EpubServiceError("Не удалось скопировать EPUB во внутренний кэш (temp.epub).", e)
            }

        logEpubOpen("Шаг 3: скопировано в кэш", localEpubUri)

        val baseUri = ensureDirectoryRootFileUrl(ensureFileUri(appContext.filesDir.absolutePath))
        val cacheRootUri = "${baseUri}$BOOK_CACHE_SEGMENT"
        val extractId = UUID.randomUUID().toString()
        val destUri = "${cacheRootUri}$extractId/"
        val destDir = File(fileUriToNativePath(ensureDirectoryRootFileUrl(destUri)))

        try {
            destDir.mkdirs()
        } catch (e: Exception) {
            throw EpubServiceError("Не удалось создать каталог для распаковки.", e)
        }

        logEpubOpen("Шаг 4: начинаю распаковку (unzip)")

        val epubPath = File(fileUriToNativePath(localEpubUri))
        try {
            withTimeout(TIMEOUT_UNZIP_MS, EPUB_ERR_TIMEOUT_UNZIP) {
                withContext(Dispatchers.IO) {
                    unzipArchiveToDirectory(epubPath, destDir)
                }
            }
        } catch (e: WithTimeoutException) {
            deleteDirectoryQuiet(destDir)
            throw EpubServiceError(EPUB_ERR_TIMEOUT_UNZIP, e)
        } catch (e: Exception) {
            deleteDirectoryQuiet(destDir)
            throw EpubServiceError(
                "Не удалось распаковать EPUB. Файл может быть повреждён или не являться ZIP/EPUB.",
                e,
            )
        }

        val destRootForProbe = ensureDirectoryRootFileUrl(destUri)
        val containerUri = "${destRootForProbe}META-INF/container.xml"
        val containerOk = fileExistsAsFile(containerUri)
        if (!containerOk) {
            throw EpubServiceError("Папка после распаковки пуста или нет META-INF/container.xml.")
        }

        unpackedRootUri = destUri
        logEpubOpen("Шаг 5: распаковка завершена", destRootForProbe)
    }

    suspend fun open(): EpubStructure {
        unpackThroughStep5()
        val destUri = unpackedRootUri ?: throw EpubServiceError("Внутренняя ошибка: нет каталога распаковки после unzip.")

        val epubRootFileUrl = ensureDirectoryRootFileUrl(destUri)
        logEpubOpen("Шаг 6: разбор OPF", epubRootFileUrl)

        val (opfXml, opfDir) =
            try {
                withContext(Dispatchers.IO) {
                    val r = readOpfFromUnpackedRootFiles(destUri)
                    r.opfXml to r.opfDirFileUrl
                }
            } catch (e: EpubServiceError) {
                throw e
            } catch (e: Exception) {
                throw EpubServiceError(
                    "Не удалось прочитать container.xml или OPF. Файл может быть повреждён.",
                    e,
                )
            }

        val spine =
            withContext(Dispatchers.IO) {
                buildSpineFromOpfXml(opfXml)
            }
        if (spine.isEmpty()) {
            throw EpubServiceError(EPUB_EMPTY_SPINE)
        }

        opfDirFileUrl = opfDir
        spineItems = spine

        logEpubOpen("Шаг 7: spine готов", "${spine.size} элементов")

        return EpubStructure(
            spine = spine,
            toc = emptyList(),
            unpackedRootUri = destUri,
        )
    }

    fun getSpineChapterUri(spineIndex: Int): String {
        val root = unpackedRootUri ?: throw EpubServiceError("Сначала вызовите open() для распаковки и разбора книги.")
        if (opfDirFileUrl.isEmpty()) {
            throw EpubServiceError("Сначала вызовите open() для распаковки и разбора книги.")
        }
        val item = spineItems.getOrNull(spineIndex)
            ?: throw EpubServiceError("Нет элемента spine с индексом $spineIndex.")
        return ensureFileUri(joinUnderUnpackedRoot(opfDirFileUrl, item.href))
    }

    suspend fun prepareChapter(htmlPath: String): String =
        try {
            withTimeout(TIMEOUT_PREPARE_CHAPTER_MS, EPUB_ERR_TIMEOUT_PREPARE_CHAPTER) {
                withContext(Dispatchers.IO) {
                    prepareChapterBody(htmlPath)
                }
            }
        } catch (e: WithTimeoutException) {
            throw EpubServiceError(EPUB_ERR_TIMEOUT_PREPARE_CHAPTER, e)
        }

    private fun prepareChapterBody(htmlPath: String): String {
        val root = unpackedRootUri ?: throw EpubServiceError("Сначала вызовите open() для распаковки и разбора книги.")
        val chapterUri = ensureFileUri(htmlPath)
        val html =
            try {
                readUtf8FromFileUri(chapterUri)
            } catch (err: Exception) {
                throw EpubServiceError("Не удалось прочитать главу: $chapterUri", err)
            }

        val matches = IMG_TAG_REGEX.findAll(html).toList()
        if (matches.isEmpty()) {
            ChitalkaMirrorLog.d(
                EPUB_OPEN_LOG,
                "prepareChapter: chapter=$chapterUri imgTagsFound=0 rewritten=0",
            )
            return injectReaderViewportAndReflowCss(html)
        }

        val out = StringBuilder(html.length + matches.size * IMG_REWRITE_HEADROOM)
        var cursor = 0
        var rewritten = 0
        for (match in matches) {
            val fullTag = match.value
            val srcMatch = SRC_ATTR_REGEX.find(fullTag)
            val newTag =
                if (srcMatch == null) {
                    fullTag
                } else {
                    val quote = srcMatch.groupValues[1]
                    val srcVal = srcMatch.groupValues[2].trim()
                    rewriteLocalImageSrcInTag(
                        fullTag = fullTag,
                        srcAttrFull = srcMatch.value,
                        quote = quote,
                        srcVal = srcVal,
                        chapterUri = chapterUri,
                        unpackedRootUri = root,
                    )
                }
            if (newTag !== fullTag && newTag != fullTag) rewritten++
            out.append(html, cursor, match.range.first)
            out.append(newTag)
            cursor = match.range.last + 1
        }
        out.append(html, cursor, html.length)

        ChitalkaMirrorLog.d(
            EPUB_OPEN_LOG,
            "prepareChapter: chapter=$chapterUri imgTagsFound=${matches.size} rewritten=$rewritten",
        )
        return injectReaderViewportAndReflowCss(out.toString())
    }

    @Suppress("LongParameterList")
    private fun rewriteLocalImageSrcInTag(
        fullTag: String,
        srcAttrFull: String,
        quote: String,
        srcVal: String,
        chapterUri: String,
        unpackedRootUri: String,
    ): String {
        if (srcVal.isEmpty() || srcVal.startsWith("data:")) {
            return fullTag
        }
        if (HTTP_URL_REGEX.containsMatchIn(srcVal)) {
            return fullTag
        }
        val assetUri = resolveChapterAssetUri(unpackedRootUri, chapterUri, srcVal)
        if (!assetUri.startsWith("file://")) {
            ChitalkaMirrorLog.w(EPUB_OPEN_LOG, "img src не удалось разрешить: src=$srcVal chapter=$chapterUri")
            return fullTag
        }
        if (!fileExistsAsFile(assetUri)) {
            ChitalkaMirrorLog.w(EPUB_OPEN_LOG, "img файл не найден: src=$srcVal resolved=$assetUri")
            return fullTag
        }
        val escaped = escapeHtmlAttrValue(assetUri, quote)
        val newSrcAttr = "src=$quote$escaped$quote"
        return fullTag.replace(srcAttrFull, newSrcAttr)
    }

    suspend fun getMetadata(): Pair<String, String> {
        val root = unpackedRootUri ?: throw EpubServiceError("Сначала вызовите open() для распаковки и разбора книги.")
        val m = readFilesystemLibraryMetadata(root)
        return m.title to m.author
    }

    suspend fun resolveCoverFileUri(): String? {
        val root = unpackedRootUri ?: throw EpubServiceError("Сначала вызовите open() для распаковки и разбора книги.")
        return readFilesystemLibraryMetadata(root).coverFileUri
    }

    /**
     * Fallback: извлекает первый `<img src>` из первой главы spine и возвращает существующий file:// путь.
     * Используется, когда в OPF-метаданных обложка не указана или не найдена.
     * Требует предварительного вызова [open].
     */
    suspend fun resolveFallbackCoverFromFirstSpineImage(): String? =
        withContext(Dispatchers.IO) {
            val root = unpackedRootUri ?: return@withContext null
            if (spineItems.isEmpty() || opfDirFileUrl.isEmpty()) return@withContext null
            val firstChapterUri =
                try {
                    getSpineChapterUri(0)
                } catch (e: Exception) {
                    ChitalkaMirrorLog.w(EPUB_OPEN_LOG, "fallback cover: spine[0] недоступен", e)
                    return@withContext null
                }
            val html =
                try {
                    readUtf8FromFileUri(firstChapterUri)
                } catch (e: Exception) {
                    ChitalkaMirrorLog.w(EPUB_OPEN_LOG, "fallback cover: чтение главы упало $firstChapterUri", e)
                    return@withContext null
                }
            val srcMatch =
                Regex(
                    """<img\b[^>]*\bsrc\s*=\s*["']([^"']+)["']""",
                    RegexOption.IGNORE_CASE,
                ).find(html) ?: return@withContext null
            val src = srcMatch.groupValues[1].trim()
            val resolved = resolveChapterAssetUri(root, firstChapterUri, src)
            if (resolved.startsWith("file://") && fileExistsAsFile(resolved)) resolved else null
        }

    fun destroy() {
        spineItems = emptyList()
        opfDirFileUrl = ""
        unpackedRootUri = null
    }
}

private val IMG_TAG_REGEX = Regex("""<img\b[^>]*>""", RegexOption.IGNORE_CASE)
private val SRC_ATTR_REGEX = Regex("""\bsrc\s*=\s*(["'])([\s\S]*?)\1""", RegexOption.IGNORE_CASE)
private val HTTP_URL_REGEX = Regex("^https?://", RegexOption.IGNORE_CASE)
private const val IMG_REWRITE_HEADROOM = 32

private fun logEpubOpen(step: String, detail: String? = null) {
    if (detail != null) {
        val d = if (detail.length > 900) detail.take(900) + "…" else detail
        ChitalkaMirrorLog.d(EPUB_OPEN_LOG, "$step $d")
    } else {
        ChitalkaMirrorLog.d(EPUB_OPEN_LOG, step)
    }
}
