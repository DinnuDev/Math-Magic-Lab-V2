package com.example.presentation.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GameCategory
import com.example.domain.model.GameDef
import com.example.presentation.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onGameClick: (String) -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredGames by viewModel.filteredGames.collectAsState()
    val favoriteGames by viewModel.favoriteGames.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayedGames.collectAsState()
    val featuredGame by viewModel.featuredGame.collectAsState()

    val dailyStreak by viewModel.dailyStreak.collectAsState()
    val totalTime by viewModel.totalPlayTime.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Math Magic Lab",
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Amaze first, teach the math second.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onStatsClick) {
                        Icon(Icons.Filled.Leaderboard, contentDescription = "Statistics")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search games, formulas, tags, difficulty...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            // Stats Quick Summary Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔥 Daily Streak", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("$dailyStreak Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⏱️ Total Play", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("$totalTime Min", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏆 Badges", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("${achievements.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // Category filter row
            item {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("All Games") }
                        )
                    }
                    items(GameCategory.values()) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category.displayName) },
                            leadingIcon = { Text(category.icon) }
                        )
                    }
                }
            }

            // Featured Game
            if (searchQuery.isEmpty() && selectedCategory == null) {
                item {
                    Text(
                        text = "Featured Game of the Day",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        onClick = { onGameClick(featuredGame.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF4F378B), Color(0xFF381E72))
                                )
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "⭐️ FEATURED MAGIC",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFD0BCFF),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = featuredGame.title,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                
                                // Decorative vertical bars from Sleek Interface HTML mock
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(16.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFFD0BCFF).copy(alpha = 0.4f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(24.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFFD0BCFF))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFFD0BCFF).copy(alpha = 0.2f))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = featuredGame.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE6DDF2).copy(alpha = 0.9f),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onGameClick(featuredGame.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD0BCFF),
                                    contentColor = Color(0xFF381E72)
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Begin Workbench", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Recently Played
            if (recentlyPlayed.isNotEmpty() && searchQuery.isEmpty() && selectedCategory == null) {
                item {
                    Text(
                        text = "Recently Played",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(recentlyPlayed) { game ->
                            Card(
                                onClick = { onGameClick(game.id) },
                                modifier = Modifier.width(180.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(game.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(game.category.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Main Games List
            item {
                Text(
                    text = if (selectedCategory != null) "${selectedCategory?.displayName} Lab" else "Mathematical Workbenches",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (filteredGames.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No games matched your selection or search query.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredGames) { game ->
                    Card(
                        onClick = { onGameClick(game.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(game.category.icon, fontSize = 24.sp)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = game.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = game.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}
