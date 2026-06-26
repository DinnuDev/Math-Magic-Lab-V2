package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameRepository(private val gameDao: GameDao) {

    val allGameStats: Flow<List<GameStat>> = gameDao.getAllGameStatsFlow()

    fun getGameStatFlow(gameId: String): Flow<GameStat?> = gameDao.getGameStatFlow(gameId)

    suspend fun incrementPlayCount(gameId: String) {
        val existing = gameDao.getGameStat(gameId)
        if (existing != null) {
            gameDao.insertGameStat(existing.copy(playCount = existing.playCount + 1, lastPlayedTime = System.currentTimeMillis()))
        } else {
            gameDao.insertGameStat(GameStat(gameId = gameId, playCount = 1, lastPlayedTime = System.currentTimeMillis()))
        }
        updateDailyStreak()
    }

    suspend fun incrementCompletionCount(gameId: String) {
        val existing = gameDao.getGameStat(gameId)
        if (existing != null) {
            gameDao.insertGameStat(existing.copy(completionCount = existing.completionCount + 1))
        } else {
            gameDao.insertGameStat(GameStat(gameId = gameId, completionCount = 1, lastPlayedTime = System.currentTimeMillis()))
        }
        // Check for specific achievements
        checkAchievementsOnCompletion(gameId)
    }

    suspend fun toggleFavorite(gameId: String) {
        val existing = gameDao.getGameStat(gameId)
        if (existing != null) {
            gameDao.insertGameStat(existing.copy(isFavorite = !existing.isFavorite))
        } else {
            gameDao.insertGameStat(GameStat(gameId = gameId, isFavorite = true))
        }
    }

    // Play Time in minutes
    fun getTotalPlayTimeMinutes(): Flow<Int> {
        return gameDao.getAllUserProgressFlow().map { list ->
            list.firstOrNull { it.key == "total_play_time" }?.value?.toIntOrNull() ?: 0
        }
    }

    suspend fun addPlayTime(minutes: Int) {
        val current = gameDao.getUserProgress("total_play_time")?.value?.toIntOrNull() ?: 0
        val newTotal = current + minutes
        gameDao.insertUserProgress(UserProgress("total_play_time", newTotal.toString()))

        if (newTotal >= 10) unlockAchievement("Time Explorer")
        if (newTotal >= 60) unlockAchievement("Grandmaster of Time")
    }

    // Daily Streak
    fun getDailyStreak(): Flow<Int> {
        return gameDao.getAllUserProgressFlow().map { list ->
            list.firstOrNull { it.key == "daily_streak" }?.value?.toIntOrNull() ?: 0
        }
    }

    private suspend fun updateDailyStreak() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastPlayDate = gameDao.getUserProgress("last_play_date")?.value
        val streakValue = gameDao.getUserProgress("daily_streak")?.value?.toIntOrNull() ?: 0

        if (lastPlayDate == todayStr) {
            // Already played today, do nothing to streak
            return
        }

        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
        val newStreak = if (lastPlayDate == yesterdayStr) {
            streakValue + 1
        } else {
            1
        }

        gameDao.insertUserProgress(UserProgress("daily_streak", newStreak.toString()))
        gameDao.insertUserProgress(UserProgress("last_play_date", todayStr))

        if (newStreak >= 3) unlockAchievement("Streak Starter")
        if (newStreak >= 7) unlockAchievement("Unstoppable Thinker")
    }

    // Achievements: comma separated strings
    fun getAchievements(): Flow<List<String>> {
        return gameDao.getAllUserProgressFlow().map { list ->
            val achievementsStr = list.firstOrNull { it.key == "achievements" }?.value ?: ""
            if (achievementsStr.isEmpty()) emptyList() else achievementsStr.split(",")
        }
    }

    suspend fun unlockAchievement(id: String) {
        val existingProgress = gameDao.getUserProgress("achievements")
        val list = existingProgress?.value?.split(",")?.toMutableList() ?: mutableListOf()
        if (!list.contains(id)) {
            list.add(id)
            gameDao.insertUserProgress(UserProgress("achievements", list.joinToString(",")))
        }
    }

    private suspend fun checkAchievementsOnCompletion(gameId: String) {
        unlockAchievement("First Breakthrough") // Completing any game
        
        // Count completions across all games
        var totalCompletions = 0
        val allStats = mutableListOf<GameStat>()
        // Wait, let's look at all games completed
        for (g in com.example.domain.model.GameRegistry.games) {
            val stat = gameDao.getGameStat(g.id)
            if (stat != null) {
                totalCompletions += stat.completionCount
                allStats.add(stat)
            }
        }

        if (totalCompletions >= 5) unlockAchievement("Math Apprentice")
        if (totalCompletions >= 15) unlockAchievement("Magic Prodigy")
        if (totalCompletions >= 30) unlockAchievement("Archmage of Numbers")

        // Specific category achievements
        val mindReadingCompleted = allStats.filter { stat ->
            val def = com.example.domain.model.GameRegistry.getGameById(stat.gameId)
            def?.category == com.example.domain.model.GameCategory.MIND_READING && stat.completionCount > 0
        }.size
        if (mindReadingCompleted >= 3) unlockAchievement("Mind Reader Extraordinaire")

        val probabilityCompleted = allStats.filter { stat ->
            val def = com.example.domain.model.GameRegistry.getGameById(stat.gameId)
            def?.category == com.example.domain.model.GameCategory.PROBABILITY && stat.completionCount > 0
        }.size
        if (probabilityCompleted >= 3) unlockAchievement("Probability Master")

        val logicCompleted = allStats.filter { stat ->
            val def = com.example.domain.model.GameRegistry.getGameById(stat.gameId)
            def?.category == com.example.domain.model.GameCategory.LOGIC && stat.completionCount > 0
        }.size
        if (logicCompleted >= 3) unlockAchievement("Tactician")

        val puzzlesCompleted = allStats.filter { stat ->
            val def = com.example.domain.model.GameRegistry.getGameById(stat.gameId)
            def?.category == com.example.domain.model.GameCategory.PUZZLES && stat.completionCount > 0
        }.size
        if (puzzlesCompleted >= 3) unlockAchievement("Puzzle Solver")
    }

    suspend fun resetAll() {
        gameDao.clearGameStats()
        gameDao.clearUserProgress()
    }
}
