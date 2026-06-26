package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_stats")
data class GameStat(
    @PrimaryKey val gameId: String,
    val playCount: Int = 0,
    val completionCount: Int = 0,
    val isFavorite: Boolean = false,
    val lastPlayedTime: Long = 0L
)

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val key: String,
    val value: String
)
