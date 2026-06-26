package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_stats")
    fun getAllGameStatsFlow(): Flow<List<GameStat>>

    @Query("SELECT * FROM game_stats WHERE gameId = :gameId")
    suspend fun getGameStat(gameId: String): GameStat?

    @Query("SELECT * FROM game_stats WHERE gameId = :gameId")
    fun getGameStatFlow(gameId: String): Flow<GameStat?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameStat(stat: GameStat)

    @Query("SELECT * FROM user_progress")
    fun getAllUserProgressFlow(): Flow<List<UserProgress>>

    @Query("SELECT * FROM user_progress WHERE `key` = :key")
    suspend fun getUserProgress(key: String): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgress)

    @Query("DELETE FROM game_stats")
    suspend fun clearGameStats()

    @Query("DELETE FROM user_progress")
    suspend fun clearUserProgress()
}
