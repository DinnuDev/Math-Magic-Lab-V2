package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.MathMagicApplication
import com.example.data.GameRepository
import com.example.data.GameStat
import com.example.domain.model.Difficulty
import com.example.domain.model.GameCategory
import com.example.domain.model.GameDef
import com.example.domain.model.GameRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(private val repository: GameRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<GameCategory?>(null)
    val selectedCategory: StateFlow<GameCategory?> = _selectedCategory

    val allGames = GameRegistry.games

    val gameStats: StateFlow<List<GameStat>> = repository.allGameStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPlayTime: StateFlow<Int> = repository.getTotalPlayTimeMinutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dailyStreak: StateFlow<Int> = repository.getDailyStreak()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val achievements: StateFlow<List<String>> = repository.getAchievements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredGames: StateFlow<List<GameDef>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        allGames.filter { game ->
            val matchesCategory = category == null || game.category == category
            val matchesQuery = query.isEmpty() || 
                game.title.contains(query, ignoreCase = true) ||
                game.concept.contains(query, ignoreCase = true) ||
                game.description.contains(query, ignoreCase = true) ||
                game.tags.any { it.contains(query, ignoreCase = true) } ||
                game.difficulty.displayName.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allGames)

    val favoriteGames: StateFlow<List<GameDef>> = gameStats.mapCombined { statsMap ->
        allGames.filter { game -> statsMap[game.id]?.isFavorite == true }
    }

    val recentlyPlayedGames: StateFlow<List<GameDef>> = gameStats.mapCombined { statsMap ->
        allGames.filter { game -> (statsMap[game.id]?.lastPlayedTime ?: 0L) > 0L }
            .sortedByDescending { game -> statsMap[game.id]?.lastPlayedTime ?: 0L }
    }

    val featuredGame: StateFlow<GameDef> = MutableStateFlow(
        allGames[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % allGames.size]
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: GameCategory?) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(gameId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(gameId)
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAll()
        }
    }

    fun recordPlaySession(gameId: String) {
        viewModelScope.launch {
            repository.incrementPlayCount(gameId)
            repository.addPlayTime(2) // Approximate session play time increment
        }
    }

    fun recordCompletion(gameId: String) {
        viewModelScope.launch {
            repository.incrementCompletionCount(gameId)
        }
    }

    // Helper to map StateFlow combinatorically
    private fun <T> StateFlow<List<GameStat>>.mapCombined(transform: (Map<String, GameStat>) -> T): StateFlow<T> {
        val mappedFlow = MutableStateFlow(transform(emptyMap()))
        viewModelScope.launch {
            this@mapCombined.collect { stats ->
                val statsMap = stats.associateBy { it.gameId }
                mappedFlow.value = transform(statsMap)
            }
        }
        return mappedFlow
    }
}

class HomeViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
