package com.chitalka.screens.reader

import com.chitalka.epub.EPUB_EMPTY_SPINE
import com.chitalka.epub.EPUB_ERR_TIMEOUT_COPY
import com.chitalka.epub.EPUB_ERR_TIMEOUT_PREPARE_CHAPTER
import com.chitalka.epub.EPUB_ERR_TIMEOUT_UNZIP
import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog

fun ReaderScreenSpec.readerOpenErrorMessage(
    locale: AppLocale,
    kind: ReaderScreenSpec.ReaderOpenErrorKind,
): String =
    when (kind) {
        is ReaderScreenSpec.ReaderOpenErrorKind.Epub -> epubOpenErrorMessage(locale, kind.message)
        is ReaderScreenSpec.ReaderOpenErrorKind.Other ->
            kind.message?.trim()?.takeIf { it.isNotEmpty() }
                ?: I18nCatalog.tSync(locale, ReaderScreenSpec.I18nKeys.ERR_UNKNOWN)
        ReaderScreenSpec.ReaderOpenErrorKind.Unknown ->
            I18nCatalog.tSync(locale, ReaderScreenSpec.I18nKeys.ERR_UNKNOWN)
    }

private fun epubOpenErrorMessage(
    locale: AppLocale,
    rawMessage: String,
): String {
    val m = rawMessage.trim()
    return when (m) {
        EPUB_EMPTY_SPINE -> I18nCatalog.tSync(locale, ReaderScreenSpec.I18nKeys.ERR_EMPTY_SPINE)
        EPUB_ERR_TIMEOUT_COPY -> I18nCatalog.tSync(locale, ReaderScreenSpec.I18nKeys.ERR_TIMEOUT_COPY)
        EPUB_ERR_TIMEOUT_UNZIP -> I18nCatalog.tSync(locale, ReaderScreenSpec.I18nKeys.ERR_TIMEOUT_UNZIP)
        EPUB_ERR_TIMEOUT_PREPARE_CHAPTER ->
            I18nCatalog.tSync(locale, ReaderScreenSpec.I18nKeys.ERR_TIMEOUT_PREPARE_CHAPTER)
        else ->
            if (m.isNotEmpty()) {
                m
            } else {
                I18nCatalog.tSync(locale, ReaderScreenSpec.I18nKeys.ERR_OPEN_FAILED)
            }
    }
}
