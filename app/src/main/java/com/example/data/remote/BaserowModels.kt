package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Matches the actual Baserow API response for Table ID 484219 (?user_field_names=true).
 * Actual table columns inspected:
 * - id: Baserow row ID
 * - order: Decimal string representing sequential order (1..255+)
 * - TITAL: Odia song title
 * - SUB TITAL: Romanized / English transliteration subtitle
 * - SONG: Complete Odia Unicode lyrics
 * - Category / category: Optional if added in Baserow later
 */
@JsonClass(generateAdapter = true)
data class BaserowPageResponse(
    val count: Int = 0,
    val next: String? = null,
    val previous: String? = null,
    val results: List<Map<String, Any?>> = emptyList()
)

data class BaserowSongMapped(
    val baserowId: Int,
    val songNumber: Int,
    val orderRaw: String,
    val titleOdia: String,
    val subTitle: String,
    val lyrics: String,
    val category: String?
)

object BaserowRowMapper {
    fun mapRow(row: Map<String, Any?>): BaserowSongMapped? {
        val rawId = row["id"]
        val baserowId = when (rawId) {
            is Number -> rawId.toInt()
            is String -> rawId.toIntOrNull()
            else -> null
        } ?: return null

        val orderRaw = row["order"]?.toString()?.trim().orEmpty()
        val parsedOrderNumber = orderRaw.toDoubleOrNull()?.let { kotlin.math.round(it).toInt() }
        val explicitNumber = extractString(row, "Song Number", "SONG NUMBER", "Number", "NUMBER")?.toIntOrNull()
        val songNumber = explicitNumber ?: parsedOrderNumber ?: baserowId

        // Exact inspected Baserow field names: "TITAL", "SUB TITAL", "SONG"
        // Also gracefully checks standard variants if Baserow table evolves
        val titleOdia = extractString(row, "TITAL", "Title", "TITLE", "Song Title") ?: ""
        val subTitle = extractString(row, "SUB TITAL", "Sub Title", "SUB TITLE", "Subtitle") ?: ""
        val lyrics = extractString(row, "SONG", "Song", "Lyrics", "LYRICS") ?: ""
        val category = extractCategory(row)

        if (titleOdia.isBlank() && lyrics.isBlank()) return null

        return BaserowSongMapped(
            baserowId = baserowId,
            songNumber = songNumber,
            orderRaw = orderRaw.ifBlank { songNumber.toString() },
            titleOdia = titleOdia.trim(),
            subTitle = subTitle.trim(),
            lyrics = lyrics.trimEnd(),
            category = category?.takeIf { it.isNotBlank() }
        )
    }

    private fun extractString(row: Map<String, Any?>, vararg keys: String): String? {
        for (key in keys) {
            val value = row[key] ?: continue
            val text = value.toString().trim()
            if (text.isNotEmpty() && text != "null") return text
        }
        return null
    }

    private fun extractCategory(row: Map<String, Any?>): String? {
        val candidates = listOf("Category", "CATEGORY", "category", "Catagory")
        for (key in candidates) {
            val raw = row[key] ?: continue
            when (raw) {
                is String -> if (raw.isNotBlank()) return raw.trim()
                is Map<*, *> -> {
                    val v = raw["value"]?.toString()?.trim()
                    if (!v.isNullOrBlank()) return v
                }
                is List<*> -> {
                    val joined = raw.mapNotNull { item ->
                        when (item) {
                            is Map<*, *> -> item["value"]?.toString()?.trim()
                            else -> item?.toString()?.trim()
                        }
                    }.filter { it.isNotBlank() }.joinToString(", ")
                    if (joined.isNotBlank()) return joined
                }
            }
        }
        return null
    }
}
