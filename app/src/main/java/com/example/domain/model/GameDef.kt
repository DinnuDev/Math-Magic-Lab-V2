package com.example.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class GameCategory(val displayName: String, val icon: String, val description: String) {
    MIND_READING("Mind Reading", "🧠", "Mind-bending magic based on mathematical invariants."),
    NUMBER_MAGIC("Number Magic", "🔢", "Astonishing tricks using arithmetic and number theory."),
    PROBABILITY("Probability", "🎲", "Fascinating demonstrations of chance, risk, and statistics."),
    LOGIC("Logic & Nim", "💡", "Strategic games and puzzles governed by mathematical logic."),
    PUZZLES("Puzzles", "🧩", "Classic spatial and logic puzzles with built-in mathematical solvers.")
}

enum class Difficulty(val displayName: String, val colorHex: String) {
    EASY("Easy", "#4CAF50"),
    MEDIUM("Medium", "#FF9800"),
    HARD("Hard", "#F44336")
}

data class GameDef(
    val id: String,
    val title: String,
    val category: GameCategory,
    val difficulty: Difficulty,
    val description: String,
    val instructions: String,
    val durationMinutes: Int,
    val concept: String,
    val tags: List<String>,
    val history: String = "",
    val realWorldApps: String = ""
)
