package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["songNumber"]),
        Index(value = ["titleOdia"]),
        Index(value = ["odiaInitial"])
    ]
)
data class SongEntity(
    @PrimaryKey val baserowId: Int,
    val songNumber: Int,
    val orderRaw: String,
    val titleOdia: String,
    val subTitle: String,
    val lyrics: String,
    val category: String?,
    val odiaInitial: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val songId: Int,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_songs")
data class RecentSongEntity(
    @PrimaryKey val songId: Int,
    val viewedAt: Long = System.currentTimeMillis(),
    val viewCount: Int = 1
)

data class SongWithMeta(
    val baserowId: Int,
    val songNumber: Int,
    val orderRaw: String,
    val titleOdia: String,
    val subTitle: String,
    val lyrics: String,
    val category: String?,
    val odiaInitial: String,
    val isFavorite: Boolean,
    val lastViewedAt: Long?,
    val viewCount: Int?
)
