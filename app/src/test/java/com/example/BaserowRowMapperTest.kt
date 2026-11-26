package com.example

import com.example.data.remote.BaserowRowMapper
import com.example.data.repository.SongRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class BaserowRowMapperTest {

    @Test
    fun `maps actual Baserow table 484219 fields accurately`() {
        val row = mapOf<String, Any?>(
            "id" to 540.0,
            "order" to "255.00000000000000000000",
            "TITAL" to "ଧନ୍ୟବାଦ ଘେନ ପ୍ରଭୁ ଆମ ହୃଦୟର",
            "SUB TITAL" to "Dhanyvad ghena prabhu",
            "SONG" to "ଧନ୍ୟବାଦ ଘେନ ପ୍ରଭୁ\nଆମ ହୃଦୟର"
        )

        val mapped = BaserowRowMapper.mapRow(row)
        assertNotNull(mapped)
        assertEquals(540, mapped!!.baserowId)
        assertEquals(255, mapped.songNumber)
        assertEquals("ଧନ୍ୟବାଦ ଘେନ ପ୍ରଭୁ ଆମ ହୃଦୟର", mapped.titleOdia)
        assertEquals("Dhanyvad ghena prabhu", mapped.subTitle)
        assertEquals("ଧନ୍ୟବାଦ ଘେନ ପ୍ରଭୁ\nଆମ ହୃଦୟର", mapped.lyrics)
        assertNull(mapped.category)
    }

    @Test
    fun `maps optional Baserow category if added later`() {
        val row = mapOf<String, Any?>(
            "id" to 1,
            "order" to "1.00000000000000000000",
            "TITAL" to "ଆଉ ରାତ୍ରି ନାହିଁ",
            "SUB TITAL" to "Aau Raatri nahi",
            "SONG" to "ଆଉ ରାତ୍ରି ନାହିଁ",
            "Category" to mapOf("id" to 1, "value" to "Easter")
        )

        val mapped = BaserowRowMapper.mapRow(row)
        assertNotNull(mapped)
        assertEquals("Easter", mapped!!.category)
        assertEquals("ଆ", SongRepository.extractOdiaInitial(mapped.titleOdia))
    }

    @Test
    fun `moshi parses BaserowPageResponse and bundled seed file cleanly`() {
        val sampleJson = """
            {
              "count": 2,
              "next": null,
              "previous": null,
              "results": [
                {
                  "id": 1,
                  "order": "1.00000000000000000000",
                  "TITAL": "ଆଉ ରାତ୍ରି ନାହିଁ",
                  "SUB TITAL": "Aau Raatri nahi",
                  "SONG": "ଆଉ ରାତ୍ରି ନାହିଁ"
                },
                {
                  "id": 540,
                  "order": "255.00000000000000000000",
                  "TITAL": "ଧନ୍ୟବାଦ ଘେନ ପ୍ରଭୁ",
                  "SUB TITAL": "Dhanyvad ghena prabhu",
                  "SONG": "ଧନ୍ୟବାଦ ଘେନ ପ୍ରଭୁ"
                }
              ]
            }
        """.trimIndent()
        val adapter = com.example.data.remote.BaserowNetworkFactory.createMoshi()
            .adapter(com.example.data.remote.BaserowPageResponse::class.java)
        val parsed = adapter.fromJson(sampleJson)
        assertNotNull(parsed)
        assertEquals(2, parsed!!.count)
        val mapped = parsed.results.mapNotNull { BaserowRowMapper.mapRow(it) }
        assertEquals(2, mapped.size)
        assertEquals(1, mapped[0].songNumber)
        assertEquals(255, mapped[1].songNumber)

        val seedFile = java.io.File("src/main/assets/baserow_songs_seed.json")
        if (seedFile.exists()) {
            val seedParsed = adapter.fromJson(seedFile.readText())
            assertNotNull(seedParsed)
            assertEquals(255, seedParsed!!.count)
            val seedMapped = seedParsed.results.mapNotNull { BaserowRowMapper.mapRow(it) }
            assertEquals(255, seedMapped.distinctBy { it.baserowId }.size)
        }
    }
}
