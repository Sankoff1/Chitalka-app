@file:Suppress("NestedBlockDepth", "ThrowsCount")
package com.chitalka.epub

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream

/** Копия входного URI в `cacheDir/temp.epub` (как `copyFileToInternalStorage` в RN). */
suspend fun copySourceToTempEpub(context: Context, sourceUriString: String): String =
    withContext(Dispatchers.IO) {
        val dest = File(context.cacheDir, "temp.epub")
        val uri = Uri.parse(sourceUriString.trim())
        openSourceStream(context, uri).use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
        if (!dest.isFile) {
            throw EpubServiceError("После копирования temp.epub не найден или это каталог.")
        }
        ensureFileUri(dest.absolutePath)
    }

private fun openSourceStream(context: Context, uri: Uri): java.io.InputStream {
    return when (uri.scheme?.lowercase()) {
        "file" -> {
            val path = uri.path ?: throw EpubServiceError("Пустой путь file://")
            File(path).inputStream()
        }
        "content" ->
            context.contentResolver.openInputStream(uri)
                ?: throw EpubServiceError("Не удалось открыть content:// источник.")
        else -> throw EpubServiceError("Неподдерживаемая схема URI: ${uri.scheme}")
    }
}

internal fun unzipArchiveToDirectory(zipFile: File, destDir: File) {
    destDir.mkdirs()
    val destCanonical = destDir.canonicalFile
    ZipInputStream(FileInputStream(zipFile)).use { zis ->
        var entry = zis.nextEntry
        while (entry != null) {
            if (entry.name.isEmpty()) {
                zis.closeEntry()
                entry = zis.nextEntry
                continue
            }
            val outFile = File(destDir, entry.name).canonicalFile
            val prefix = destCanonical.path + File.separator
            if (outFile.path != destCanonical.path && !outFile.path.startsWith(prefix)) {
                throw EpubServiceError("Недопустимая запись в ZIP: ${entry.name}")
            }
            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile?.mkdirs()
                FileOutputStream(outFile).use { fos -> zis.copyTo(fos) }
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }
    }
}

internal fun deleteDirectoryQuiet(dir: File) {
    if (!dir.exists()) return
    dir.walkBottomUp().forEach { it.delete() }
}

internal fun readUtf8FromFileUri(fileUri: String): String {
    val f = File(fileUriToNativePath(ensureFileUri(fileUri)))
    return f.readText(Charsets.UTF_8)
}

internal fun fileExistsAsFile(fileUri: String): Boolean {
    val f = File(fileUriToNativePath(ensureFileUri(fileUri)))
    return f.isFile
}

/** UTF-8 с заменой невалидных последовательностей — чтобы не падать на BOM/битой кодировке в OCF XML. */
private fun File.readXmlTextLenient(): String {
    FileInputStream(this).use { ins ->
        val bytes = ins.readBytes()
        return try {
            StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .decode(java.nio.ByteBuffer.wrap(bytes))
                .toString()
        } catch (_: Exception) {
            bytes.toString(Charsets.ISO_8859_1)
        }
    }
}

internal data class OpfReadResult(
    val opfXml: String,
    val opfDirFileUrl: String,
)

internal fun readOpfFromUnpackedRootFiles(rootUriString: String): OpfReadResult {
    val root = ensureDirectoryRootFileUrl(rootUriString)
    val containerFile = File(fileUriToNativePath("${root}META-INF/container.xml"))
    if (!containerFile.isFile) {
        throw EpubServiceError("Нет META-INF/container.xml в распакованной книге.")
    }
    val containerXml = containerFile.readXmlTextLenient().trimStart('\uFEFF')
    val fp =
        Regex("""\bfull-path\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(containerXml)
            ?.groupValues?.get(1)?.trim()
            ?: Regex("""\bfull-path\s*=\s*"([^"]+)"""").find(containerXml)?.groupValues?.get(1)?.trim()
            ?: Regex("""\bfull-path\s*=\s*'([^']+)'""", RegexOption.IGNORE_CASE).find(containerXml)
                ?.groupValues?.get(1)?.trim()
            ?: throw EpubServiceError("В container.xml не найден full-path к OPF.")
    val opfUri = joinUnderUnpackedRoot(root, fp)
    val opfFile = File(fileUriToNativePath(opfUri))
    if (!opfFile.isFile) {
        throw EpubServiceError("OPF не найден по пути: $opfUri")
    }
    val opfXml = opfFile.readXmlTextLenient().trimStart('\uFEFF')
    val opfDirUri =
        if (opfUri.contains("/")) {
            opfUri.substring(0, opfUri.lastIndexOf('/') + 1)
        } else {
            root
        }
    return OpfReadResult(opfXml = opfXml, opfDirFileUrl = ensureDirectoryRootFileUrl(opfDirUri))
}
