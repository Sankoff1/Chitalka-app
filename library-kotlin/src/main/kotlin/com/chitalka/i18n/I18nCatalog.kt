@file:Suppress("ReturnCount")
package com.chitalka.i18n

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/**
 * Строки каталога (аналог импортов en.json / ru.json в catalog.ts).
 * JSON в classpath: chitalka/i18n/ — копия src/i18n/locales; при правках синхронизируйте файлы.
 */
object I18nCatalog {
    private val json = Json { ignoreUnknownKeys = true }

    private val ruRoot: JsonObject = loadRoot("ru.json")
    private val enRoot: JsonObject = loadRoot("en.json")

    private fun loadRoot(fileName: String): JsonObject {
        val path = "/chitalka/i18n/$fileName"
        val text =
            checkNotNull(I18nCatalog::class.java.getResourceAsStream(path)) {
                "Missing resource $path"
            }.bufferedReader().use { it.readText() }
        return json.parseToJsonElement(text).jsonObject
    }

    private fun rootFor(locale: AppLocale): JsonObject =
        when (locale) {
            AppLocale.RU -> ruRoot
            AppLocale.EN -> enRoot
        }

    private fun getNested(root: JsonObject, parts: List<String>): String? {
        var cur: JsonElement = root
        for (key in parts) {
            val obj = cur as? JsonObject ?: return null
            cur = obj[key] ?: return null
        }
        val prim = cur as? JsonPrimitive ?: return null
        return if (prim.isString) prim.content else null
    }

    private val varPattern = Regex("""\{\{(\w+)\}\}""")

    /**
     * Синхронный перевод по вложенному пути (например drawer.settings), без UI-фреймворка.
     */
    fun tSync(locale: AppLocale, path: String, vars: Map<String, Any>? = null): String {
        val parts = path.split('.').filter { it.isNotEmpty() }
        val raw =
            getNested(rootFor(locale), parts)
                ?: getNested(ruRoot, parts)
                ?: path
        if (vars == null) {
            return raw
        }
        return varPattern.replace(raw) { m ->
            val name = m.groupValues[1]
            val v = vars[name]
            when (v) {
                null -> ""
                is Number -> v.toString()
                else -> v.toString()
            }
        }
    }

    fun bookFallbackLabels(locale: AppLocale): BookFallbackLabels =
        BookFallbackLabels(
            untitled = tSync(locale, "book.untitled"),
            unknownAuthor = tSync(locale, "book.unknownAuthor"),
        )
}

data class BookFallbackLabels(
    val untitled: String,
    val unknownAuthor: String,
)
