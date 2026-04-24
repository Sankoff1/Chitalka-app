package com.chitalka.epub

/**
 * Сообщения [Throwable.message] у ошибок открытия EPUB (как в RN `EpubService` / `ReaderScreen`).
 * Android [EpubServiceError] использует те же строки.
 */
const val EPUB_EMPTY_SPINE: String = "EMPTY_SPINE"

const val EPUB_ERR_TIMEOUT_COPY: String = "TIMEOUT_COPY"

const val EPUB_ERR_TIMEOUT_UNZIP: String = "TIMEOUT_UNZIP"

const val EPUB_ERR_TIMEOUT_PREPARE_CHAPTER: String = "TIMEOUT_PREPARE_CHAPTER"
