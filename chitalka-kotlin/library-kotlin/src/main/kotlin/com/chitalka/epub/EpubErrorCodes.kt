package com.chitalka.epub

/**
 * Стабильные коды ошибок открытия EPUB. Android `EpubServiceError` пробрасывает их как [Throwable.message],
 * UI-слой матчит по коду и подменяет на локализованный текст.
 */
const val EPUB_EMPTY_SPINE: String = "EMPTY_SPINE"

const val EPUB_ERR_TIMEOUT_COPY: String = "TIMEOUT_COPY"

const val EPUB_ERR_TIMEOUT_UNZIP: String = "TIMEOUT_UNZIP"

const val EPUB_ERR_TIMEOUT_PREPARE_CHAPTER: String = "TIMEOUT_PREPARE_CHAPTER"
