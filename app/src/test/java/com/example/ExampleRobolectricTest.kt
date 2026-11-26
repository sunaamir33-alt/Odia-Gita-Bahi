package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.FavoriteEntity
import com.example.data.local.RecentSongEntity
import com.example.data.local.SongBookDatabase
import com.example.data.local.SongEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: SongBookDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SongBookDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `app name matches Odia Gita Bahi`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Odia Gita Bahi", appName)
    }

    @Test
    fun `atomic sync preserves favorites and prevents duplicates`() = runTest {
        val dao = database.songDao()
        val initialSongs = listOf(
            SongEntity(
                baserowId = 1,
                songNumber = 1,
                orderRaw = "1.0000",
                titleOdia = "ଆଉ ରାତ୍ରି ନାହିଁ ଆଉ କ୍ଲାନ୍ତ ନାହିଁ",
                subTitle = "Aau Raatri nahi",
                lyrics = "ଆଉ ରାତ୍ରି ନାହିଁ ଆଉ କ୍ଲାନ୍ତ ନାହିଁ\nବିଜୟୀ ଯୀଶୁ ଉଠିଛି ହସି",
                category = null,
                odiaInitial = "ଆ"
            ),
            SongEntity(
                baserowId = 2,
                songNumber = 2,
                orderRaw = "2.0000",
                titleOdia = "ଅନୁତାପ ନୀର ନେଇ",
                subTitle = "ANUTAPA NIRA",
                lyrics = "ଅନୁତାପ ନୀର ନେଇ କାନ୍ଦେ ମୋ ଅନୁର",
                category = null,
                odiaInitial = "ଅ"
            )
        )

        dao.syncAllSongsAtomically(initialSongs)
        dao.addFavorite(FavoriteEntity(songId = 1))
        dao.upsertRecent(RecentSongEntity(songId = 2, viewedAt = 1000L, viewCount = 1))

        // Re-sync updated title without creating duplicates or losing favorite/recent state
        val updatedSongs = listOf(
            initialSongs[0].copy(subTitle = "Aau Raatri Nahi Updated"),
            initialSongs[1]
        )
        dao.syncAllSongsAtomically(updatedSongs)

        val all = dao.observeAllSongs().first()
        assertEquals(2, all.size)
        assertTrue(all.first { it.baserowId == 1 }.isFavorite)
        assertFalse(all.first { it.baserowId == 2 }.isFavorite)
        assertEquals(1000L, all.first { it.baserowId == 2 }.lastViewedAt)
        assertEquals("Aau Raatri Nahi Updated", all.first { it.baserowId == 1 }.subTitle)
    }

    @Test
    fun `offline search matches song number odia title and lyrics`() = runTest {
        val dao = database.songDao()
        dao.syncAllSongsAtomically(
            listOf(
                SongEntity(
                    baserowId = 540,
                    songNumber = 255,
                    orderRaw = "255.0000",
                    titleOdia = "ଧନ୍ୟବାଦ ଘେନ ପ୍ରଭୁ ଆମ ହୃଦୟର",
                    subTitle = "Dhanyvad ghena prabhu",
                    lyrics = "ଧନ୍ୟବାଦ ଘେନ ପ୍ରଭୁ ଆମ ହୃଦୟର\nଅନନ୍ତ ପ୍ରେମ ତୁମର",
                    category = null,
                    odiaInitial = "ଧ"
                )
            )
        )

        val byNum = dao.searchSongsOffline(query = "255", exactNum = "255", includeLyrics = false).first()
        assertEquals(1, byNum.size)
        assertEquals(540, byNum.first().baserowId)

        val byOdia = dao.searchSongsOffline(query = "ଧନ୍ୟବାଦ", exactNum = "ଧନ୍ୟବାଦ", includeLyrics = false).first()
        assertEquals(1, byOdia.size)

        val byLyrics = dao.searchSongsOffline(query = "ଅନନ୍ତ ପ୍ରେମ", exactNum = "ଅନନ୍ତ ପ୍ରେମ", includeLyrics = true).first()
        assertEquals(1, byLyrics.size)
    }

    @Test
    fun `bundled baserow seed loads all 255 real songs without duplicates`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val json = context.assets.open("baserow_songs_seed.json").bufferedReader().use { it.readText() }
        val adapter = com.example.data.remote.BaserowNetworkFactory.createMoshi()
            .adapter(com.example.data.remote.BaserowPageResponse::class.java)
        val parsed = adapter.fromJson(json)
        assertEquals(255, parsed?.count)
        val mapped = parsed!!.results.mapNotNull { com.example.data.remote.BaserowRowMapper.mapRow(it) }
        assertEquals(255, mapped.distinctBy { it.baserowId }.size)
        assertEquals(1, mapped.first().songNumber)
        assertEquals(255, mapped.last().songNumber)
    }
}
