package com.example.presentation.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: HomeViewModel,
    onBackClick: () -> Unit
) {
    val gameStats by viewModel.gameStats.collectAsState()
    val totalTime by viewModel.totalPlayTime.collectAsState()
    val streak by viewModel.dailyStreak.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    val totalPlays = gameStats.sumOf { it.playCount }
    val totalCompletions = gameStats.sumOf { it.completionCount }

    val allAchievementsList = listOf(
        Pair("First Breakthrough", "Complete any math game or trick for the first time."),
        Pair("Time Explorer", "Invest a total of 10 minutes playing Math Magic Lab."),
        Pair("Grandmaster of Time", "Accumulate 1 hour or more of focused math magic play."),
        Pair("Streak Starter", "Keep an active daily streak of 3 days."),
        Pair("Unstoppable Thinker", "Maintain a perfect 7-day daily streak."),
        Pair("Math Apprentice", "Register 5 total game completions."),
        Pair("Magic Prodigy", "Complete 15 games/simulations successfully."),
        Pair("Archmage of Numbers", "Reach a master count of 30 total game completions."),
        Pair("Mind Reader Extraordinaire", "Complete 3 distinct games in the Mind Reading category."),
        Pair("Probability Master", "Complete 3 distinct games in the Probability category."),
        Pair("Tactician", "Complete 3 distinct games in the Logic category."),
        Pair("Puzzle Solver", "Complete 3 distinct games in the Puzzles category.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance & Badges", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Stats Panel
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Mathematical Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Plays", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$totalPlays", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                            }
                            Column {
                                Text("Completions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$totalCompletions", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                            }
                            Column {
                                Text("Play Time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$totalTime m", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                            }
                            Column {
                                Text("Streak", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$streak d", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            // Achievements Headers
            item {
                Text("Unlocked Achievements (${achievements.size} / ${allAchievementsList.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            // Achievements List
            items(allAchievementsList) { ach ->
                val isUnlocked = achievements.contains(ach.first)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = null,
                            tint = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(ach.first, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(ach.second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
