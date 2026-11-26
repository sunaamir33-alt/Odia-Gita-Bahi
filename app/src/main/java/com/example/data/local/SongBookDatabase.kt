package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongEntity::class,
        FavoriteEntity::class,
        RecentSongEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SongBookDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: SongBookDatabase? = null

        fun getInstance(context: Context): SongBookDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SongBookDatabase::class.java,
                    "odia_gita_bahi.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
