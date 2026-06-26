package com.example.presentation.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Difficulty
import com.example.domain.model.GameCategory
import com.example.domain.model.GameDef
import com.example.domain.model.GameRegistry
import com.example.presentation.viewmodel.HomeViewModel
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.random.Random

@Composable
fun CustomSleekTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val tabs = listOf("📖 Instructions", "🧪 Workbench")
        tabs.forEachIndexed { index, label ->
            val isSelected = selectedTab == index
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                label = "TabBackground"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                label = "TabContent"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(backgroundColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
fun FlipCard3D(
    modifier: Modifier = Modifier,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    frontContent: @Composable () -> Unit,
    backContent: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CardFlip"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onFlip,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        if (rotation <= 90f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = 0f
                    }
            ) {
                frontContent()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = 180f
                    }
            ) {
                backContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameContainer(
    gameDef: GameDef,
    onBackClick: () -> Unit,
    homeViewModel: HomeViewModel,
    onNavigateToGame: (String) -> Unit
) {
    val isFavorite by homeViewModel.gameStats.collectAsState()
    val currentStat = isFavorite.firstOrNull { it.gameId == gameDef.id }
    val favStatus = currentStat?.isFavorite ?: false

    var selectedTab by remember(gameDef.id) { mutableIntStateOf(0) }
    var isSecretFlipped by remember(gameDef.id) { mutableStateOf(false) }

    LaunchedEffect(gameDef.id) {
        homeViewModel.recordPlaySession(gameDef.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = gameDef.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { homeViewModel.toggleFavorite(gameDef.id) }) {
                        Icon(
                            imageVector = if (favStatus) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (favStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Meta Row & Sub-header info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(gameDef.category.displayName) },
                        icon = { Text(gameDef.category.icon) }
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.parseColor(gameDef.difficulty.colorHex).copy(alpha = 0.12f))
                            .border(
                                width = 1.dp,
                                color = Color.parseColor(gameDef.difficulty.colorHex).copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .align(Alignment.CenterVertically)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.parseColor(gameDef.difficulty.colorHex), CircleShape)
                            )
                            Text(
                                text = gameDef.difficulty.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.parseColor(gameDef.difficulty.colorHex)
                            )
                        }
                    }
                }

                Text(
                    text = "⏱️ ${gameDef.durationMinutes} min",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Dynamic Custom Sliding Tabs
            CustomSleekTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Dynamic animated tab cross-navigation
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }.using(
                        SizeTransform(clip = false)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "TabContentTransition"
            ) { targetTab ->
                if (targetTab == 0) {
                    // Instructions & Secret
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Game Goal Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "🔬 The Goal",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = gameDef.description,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Normal,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }

                        // Instructions Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "How to Play",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = gameDef.instructions,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 22.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        // The 3D Secret Card
                        item {
                            Column {
                                Text(
                                    text = "The Mathematical Secret",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                FlipCard3D(
                                    isFlipped = isSecretFlipped,
                                    onFlip = { isSecretFlipped = !isSecretFlipped },
                                    frontContent = {
                                        // Front Content
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(
                                                            MaterialTheme.colorScheme.primaryContainer,
                                                            MaterialTheme.colorScheme.secondaryContainer
                                                        )
                                                    )
                                                )
                                                .padding(24.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                val infiniteTransition = rememberInfiniteTransition(label = "iconRotation")
                                                val iconRotation by infiniteTransition.animateFloat(
                                                    initialValue = 0f,
                                                    targetValue = 360f,
                                                    animationSpec = infiniteRepeatable(
                                                        animation = tween(8000, easing = LinearEasing),
                                                        repeatMode = RepeatMode.Restart
                                                    ),
                                                    label = "rotation"
                                                )

                                                Box(
                                                    modifier = Modifier
                                                        .size(56.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "✨",
                                                        fontSize = 28.sp,
                                                        modifier = Modifier.graphicsLayer {
                                                            rotationZ = iconRotation
                                                        }
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    text = "Reveal the Mathematical Secret",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Behind every trick is an elegant invariant or algorithm. Tap to flip this card in 3D to reveal the formulas and secret math.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    textAlign = TextAlign.Center,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                                    lineHeight = 18.sp
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.FlipCameraAndroid,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = "TAP TO REVEAL",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    backContent = {
                                        // Back Content
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surface)
                                                .padding(24.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "🔬 The Science Exposed",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Icon(
                                                    Icons.Default.WorkspacePremium,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = "Mathematical Invariant:",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = gameDef.concept,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    lineHeight = 20.sp
                                                )
                                            }

                                            if (gameDef.history.isNotEmpty()) {
                                                Column {
                                                    Text(
                                                        text = "How & Why It Works:",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = gameDef.history,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        lineHeight = 20.sp
                                                    )
                                                }
                                            }

                                            if (gameDef.realWorldApps.isNotEmpty()) {
                                                Column {
                                                    Text(
                                                        text = "Real-World Applications:",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = gameDef.realWorldApps,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        lineHeight = 20.sp
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "↺ Tap to flip back",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        // Big Tab Transition Button
                        item {
                            Button(
                                onClick = { selectedTab = 1 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Launch Workbench",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                } else {
                    // Lab Workbench Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Interactive Laboratory",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = (-0.5).sp
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    shadowElevation = 8f
                                    shape = RoundedCornerShape(24.dp)
                                    clip = true
                                },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                GamePlayArea(
                                    gameId = gameDef.id,
                                    onComplete = {
                                        homeViewModel.recordCompletion(gameDef.id)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Stuck or need rules? You can slide back to 'Instructions' at any time without losing game state.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Beautiful persistent footer navigator
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentIndex = GameRegistry.games.indexOf(gameDef)
                    val prevGame = if (currentIndex > 0) GameRegistry.games[currentIndex - 1] else null
                    val nextGame = if (currentIndex < GameRegistry.games.size - 1) GameRegistry.games[currentIndex + 1] else null

                    TextButton(
                        onClick = {
                            if (prevGame != null) {
                                onNavigateToGame(prevGame.id)
                            }
                        },
                        enabled = prevGame != null
                    ) {
                        Icon(Icons.Filled.ArrowLeft, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous Lab", fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = {
                            if (nextGame != null) {
                                onNavigateToGame(nextGame.id)
                            }
                        },
                        enabled = nextGame != null
                    ) {
                        Text("Next Lab", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.ArrowRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

// Helper to convert hex colors to Color
fun Color.Companion.parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}

@Composable
fun GamePlayArea(gameId: String, onComplete: () -> Unit) {
    when (gameId) {
        "symbol_reader" -> SymbolMindReaderScreen(onComplete)
        "emoji_reader" -> EmojiMindReaderScreen(onComplete)
        "binary_guess" -> BinaryNumberGuessScreen(onComplete)
        "age_guesser" -> AgeGuesserScreen(onComplete)
        "birthday_guesser" -> BirthdayGuesserScreen(onComplete)
        "calendar_prediction" -> CalendarPredictionScreen(onComplete)
        "trick_1089" -> Trick1089Screen(onComplete)
        "kaprekar_constant" -> KaprekarConstantScreen(onComplete)
        "digital_root" -> DigitalRootScreen(onComplete)
        "casting_nines" -> CastingNinesScreen(onComplete)
        "magic_square" -> MagicSquareScreen(onComplete)
        "monty_hall" -> MontyHallScreen(onComplete)
        "birthday_paradox" -> BirthdayParadoxScreen(onComplete)
        "dice_prediction" -> DicePredictionScreen(onComplete)
        "coin_probability" -> CoinProbabilityScreen(onComplete)
        "nim_game" -> NimGameScreen(onComplete)
        "hanoi_tower" -> HanoiTowerScreen(onComplete)
        "lights_out" -> LightsOutScreen(onComplete)
        "gray_code" -> GrayCodeScreen(onComplete)
        "fifteen_puzzle" -> FifteenPuzzleScreen(onComplete)
        "river_crossing" -> RiverCrossingScreen(onComplete)
        "domino_prediction" -> DominoPredictionScreen(onComplete)
        "animal_guess" -> SecretAnimalGuessScreen(onComplete)
        else -> Text("Game Screen Under Construction")
    }
}

// ==========================================
// 1. SYMBOL MIND READER
// ==========================================
@Composable
fun SymbolMindReaderScreen(onComplete: () -> Unit) {
    val symbols = listOf("🔮", "⚡", "🌟", "🔥", "🍀", "💎", "🎯", "⚓", "🎨", "🚀", "🛸", "🔑")
    var targetSymbol by remember { mutableStateOf(symbols.random()) }
    var items by remember { mutableStateOf(emptyList<Pair<Int, String>>()) }
    var mindReadRevealed by remember { mutableStateOf(false) }

    fun generateGrid() {
        targetSymbol = symbols.random()
        val randomSymbols = List(99) { symbols.filter { it != targetSymbol }.random() }
        val newItems = mutableListOf<Pair<Int, String>>()
        for (i in 1..99) {
            val sym = if (i % 9 == 0) targetSymbol else randomSymbols[i - 1]
            newItems.add(Pair(i, sym))
        }
        items = newItems
        mindReadRevealed = false
    }

    LaunchedEffect(Unit) {
        generateGrid()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!mindReadRevealed) {
            Text(
                "Scroll the grid and find the symbol for your resulting number:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(modifier = Modifier.height(250.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(60.dp),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(items) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(item.first.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(item.second, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    mindReadRevealed = true
                    onComplete()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔮 Read My Mind!")
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "The universe reveals your symbol...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Text(targetSymbol, fontSize = 64.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { generateGrid() }) {
                    Text("Play Again")
                }
            }
        }
    }
}

// ==========================================
// 2. EMOJI MIND READER
// ==========================================
@Composable
fun EmojiMindReaderScreen(onComplete: () -> Unit) {
    val emojis = listOf("🍎", "🍌", "🍒", "🍇", "🍉", "🍓", "🍍", "🥑", "🥕", "🍕", "🍔", "🍦")
    var targetEmoji by remember { mutableStateOf(emojis.random()) }
    var items by remember { mutableStateOf(emptyList<Pair<Int, String>>()) }
    var mindReadRevealed by remember { mutableStateOf(false) }

    fun generateGrid() {
        targetEmoji = emojis.random()
        val randomEmojis = List(99) { emojis.filter { it != targetEmoji }.random() }
        val newItems = mutableListOf<Pair<Int, String>>()
        for (i in 1..99) {
            val em = if (i % 9 == 0) targetEmoji else randomEmojis[i - 1]
            newItems.add(Pair(i, em))
        }
        items = newItems
        mindReadRevealed = false
    }

    LaunchedEffect(Unit) {
        generateGrid()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!mindReadRevealed) {
            Text(
                "Find your number in the grid and focus on its emoji:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(modifier = Modifier.height(250.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(60.dp),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(items) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(item.first.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(item.second, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    mindReadRevealed = true
                    onComplete()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🌀 Read Emojis!")
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "The Emoji Wizard perceives your thoughts...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(targetEmoji, fontSize = 64.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { generateGrid() }) {
                    Text("Play Again")
                }
            }
        }
    }
}

// ==========================================
// 3. BINARY NUMBER GUESS
// ==========================================
@Composable
fun BinaryNumberGuessScreen(onComplete: () -> Unit) {
    var cardIndex by remember { mutableStateOf(0) }
    var sum by remember { mutableStateOf(0) }
    var revealedResult by remember { mutableStateOf(false) }

    val cards = remember {
        List(6) { bitIndex ->
            val power = 2.0.pow(bitIndex).toInt()
            (1..63).filter { n -> (n and power) != 0 }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!revealedResult) {
            Text(
                "Analyzing Card ${cardIndex + 1} of 6",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Is your secret number on this card?",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(cards[cardIndex]) { num ->
                        Text(
                            num.toString(),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        sum += 2.0.pow(cardIndex).toInt()
                        if (cardIndex < 5) {
                            cardIndex++
                        } else {
                            revealedResult = true
                            onComplete()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("YES")
                }

                OutlinedButton(
                    onClick = {
                        if (cardIndex < 5) {
                            cardIndex++
                        } else {
                            revealedResult = true
                            onComplete()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("NO")
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Your Secret Number Is:",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        sum.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        cardIndex = 0
                        sum = 0
                        revealedResult = false
                    }
                ) {
                    Text("Reset & Guess Again")
                }
            }
        }
    }
}

// ==========================================
// 4. AGE GUESSER
// ==========================================
@Composable
fun AgeGuesserScreen(onComplete: () -> Unit) {
    var finalNumberStr by remember { mutableStateOf("") }
    var guessedAge by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Perform this formula in your head or a calculator:\n" +
                    "1. Think of your age (or any number 1-100)\n" +
                    "2. Multiply by 3\n" +
                    "3. Add 6\n" +
                    "4. Divide by 3",
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = finalNumberStr,
            onValueChange = { finalNumberStr = it },
            label = { Text("Enter the final number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val finalNum = finalNumberStr.toIntOrNull()
                if (finalNum != null) {
                    guessedAge = finalNum - 2
                    onComplete()
                }
            },
            enabled = finalNumberStr.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reveal My Age!")
        }

        guessedAge?.let { age ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Your Secret Number Is:", fontWeight = FontWeight.Bold)
                    Text(age.toString(), fontSize = 36.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "How it works: Let S be your age. The steps computed: ((3S + 6) / 3) = S + 2. Hence, subtracting 2 always yields your age perfectly!",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. BIRTHDAY GUESSER
// ==========================================
@Composable
fun BirthdayGuesserScreen(onComplete: () -> Unit) {
    var finalSumStr by remember { mutableStateOf("") }
    var resultDate by remember { mutableStateOf<String?>(null) }

    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Instructions:\n" +
                    "1. Take birth month (Jan = 1, Dec = 12), multiply by 5, add 7.\n" +
                    "2. Multiply that by 4, add 13, multiply that by 5.\n" +
                    "3. Add your birth day (1-31).",
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = finalSumStr,
            onValueChange = { finalSumStr = it },
            label = { Text("Enter final sum") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val sum = finalSumStr.toIntOrNull()
                if (sum != null && sum >= 205) {
                    val decoded = sum - 205
                    val day = decoded % 100
                    val monthIdx = (decoded / 100) - 1
                    if (monthIdx in 0..11 && day in 1..31) {
                        resultDate = "${months[monthIdx]} $day"
                        onComplete()
                    } else {
                        resultDate = "Invalid calculation. Double check your steps!"
                    }
                } else {
                    resultDate = "Please enter a valid final sum."
                }
            },
            enabled = finalSumStr.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guess Birthday!")
        }

        resultDate?.let { date ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Guessed Date:", fontWeight = FontWeight.Bold)
                    Text(date, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}

// ==========================================
// 6. CALENDAR PREDICTION
// ==========================================
@Composable
fun CalendarPredictionScreen(onComplete: () -> Unit) {
    var selectedCenter by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Select a cell to center a 3x3 block of dates:",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Simple mock calendar month
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            // Calendar Headers
            val days = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
            items(days) { day ->
                Text(
                    text = day,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Month Dates (1 to 28 representing a nice tidy block to ensure 3x3 fits nicely)
            items(28) { idx ->
                val date = idx + 1
                val isSelectable = date in 9..20 && date % 7 != 1 && date % 7 != 0
                val isHighlighted = selectedCenter?.let { center ->
                    val diff = date - center
                    val rowDiff = java.lang.Math.abs(diff / 7)
                    val colDiff = java.lang.Math.abs(diff % 7)
                    rowDiff <= 1 && colDiff <= 1
                } ?: false

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                date == selectedCenter -> MaterialTheme.colorScheme.primary
                                isHighlighted -> MaterialTheme.colorScheme.primaryContainer
                                else -> Color.Transparent
                            }
                        )
                        .clickable(enabled = isSelectable) {
                            selectedCenter = date
                            onComplete()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        date.toString(),
                        color = when {
                            date == selectedCenter -> MaterialTheme.colorScheme.onPrimary
                            !isSelectable -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        fontSize = 14.sp,
                        fontWeight = if (isSelectable) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        selectedCenter?.let { center ->
            Spacer(modifier = Modifier.height(16.dp))
            val datesInBlock = listOf(
                center - 8, center - 7, center - 6,
                center - 1, center, center + 1,
                center + 6, center + 7, center + 8
            )
            val computedSum = datesInBlock.sum()

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Predicted 3x3 Sum:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Text("$computedSum", fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Math Verification: ${datesInBlock.joinToString(" + ")} = $computedSum.\n" +
                                "Notice that center coordinate $center * 9 = $computedSum. Perfect centroid arithmetic!",
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 7. 1089 TRICK
// ==========================================
@Composable
fun Trick1089Screen(onComplete: () -> Unit) {
    var inputNumStr by remember { mutableStateOf("") }
    var stages by remember { mutableStateOf<List<String>?>(null) }
    var errorMsg by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = inputNumStr,
            onValueChange = {
                inputNumStr = it
                errorMsg = ""
            },
            label = { Text("Choose a 3-digit number (e.g. 725)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMsg.isNotEmpty()) {
            Text(errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val num = inputNumStr.toIntOrNull()
                if (num == null || num !in 100..999) {
                    errorMsg = "Must be a 3-digit number."
                    return@Button
                }
                val d1 = num / 100
                val d3 = num % 10
                if (java.lang.Math.abs(d1 - d3) < 2) {
                    errorMsg = "First and last digits must differ by at least 2."
                    return@Button
                }

                // Algorithm
                val reversed = inputNumStr.reversed().toInt()
                val diff = java.lang.Math.abs(num - reversed)
                val diffStr = String.format("%03d", diff)
                val diffReversed = diffStr.reversed().toInt()
                val finalSum = diff + diffReversed

                stages = listOf(
                    "Selected Number: $num",
                    "Reversed: $reversed",
                    "Difference: |$num - $reversed| = $diffStr",
                    "Reversed Difference: $diffReversed",
                    "Final Sum: $diffStr + $diffReversed = $finalSum"
                )
                onComplete()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Compute Magic 1089")
        }

        stages?.let { steps ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    steps.forEach { step ->
                        Text(step, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. KAPREKAR CONSTANT (6174)
// ==========================================
@Composable
fun KaprekarConstantScreen(onComplete: () -> Unit) {
    var inputNumStr by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorMsg by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = inputNumStr,
            onValueChange = {
                inputNumStr = it
                errorMsg = ""
            },
            label = { Text("Enter a 4-digit number (e.g. 3524)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMsg.isNotEmpty()) {
            Text(errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val num = inputNumStr.toIntOrNull()
                if (num == null || num !in 1000..9999) {
                    errorMsg = "Must be a 4-digit number."
                    return@Button
                }
                val digits = inputNumStr.toList()
                if (digits.distinct().size < 2) {
                    errorMsg = "Must contain at least 2 distinct digits."
                    return@Button
                }

                val list = mutableListOf<String>()
                var current = num
                var attempts = 0
                while (current != 6174 && attempts < 8) {
                    val sDigits = String.format("%04d", current).toList().map { it.toString().toInt() }
                    val desc = sDigits.sortedDescending().joinToString("").toInt()
                    val asc = sDigits.sorted().joinToString("").toInt()
                    val diff = desc - asc
                    list.add("$desc - $asc = $diff")
                    current = diff
                    attempts++
                }
                steps = list
                onComplete()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Launch Kaprekar Routine")
        }

        if (steps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Convergence Steps to 6174:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    steps.forEach { step ->
                        Text(step, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. DIGITAL ROOT EXPLORER
// ==========================================
@Composable
fun DigitalRootScreen(onComplete: () -> Unit) {
    var inputStr by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf<List<String>>(emptyList()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = inputStr,
            onValueChange = { inputStr = it },
            label = { Text("Enter any integer") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (inputStr.isEmpty()) return@Button
                val list = mutableListOf<String>()
                var current = inputStr
                while (current.length > 1) {
                    val digits = current.map { it.toString().toInt() }
                    val sum = digits.sum()
                    list.add("${digits.joinToString(" + ")} = $sum")
                    current = sum.toString()
                }
                steps = list
                onComplete()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Analyze Digital Root")
        }

        if (steps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Steps of Digits Sum:", fontWeight = FontWeight.Bold)
                    steps.forEach { step ->
                        Text(step, fontSize = 16.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// 10. CASTING OUT NINES
// ==========================================
@Composable
fun CastingNinesScreen(onComplete: () -> Unit) {
    var operandA by remember { mutableStateOf(123) }
    var operandB by remember { mutableStateOf(45) }
    var product by remember { mutableStateOf(5535) }

    fun dr(n: Int): Int {
        val s = n.toString()
        if (s.length == 1) return n
        return dr(s.map { it.toString().toInt() }.sum())
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Casting Out Nines Equation:\n$operandA × $operandB = $product",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        val drA = dr(operandA)
        val drB = dr(operandB)
        val drProd = dr(product)
        val drRule = dr(drA * drB)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Verification Process:")
                Text("• Digital Root of $operandA = $drA")
                Text("• Digital Root of $operandB = $drB")
                Text("• Product of Roots: $drA × $drB = ${drA * drB} (Digital Root = $drRule)")
                Text("• Digital Root of Product ($product) = $drProd")
                Spacer(modifier = Modifier.height(8.dp))
                if (drRule == drProd) {
                    Text("✅ EQUIVALENT ($drRule = $drProd). Arithmetic is correct!", color = Color.Green, fontWeight = FontWeight.Bold)
                } else {
                    Text("❌ MISMATCH. Equation has errors!", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                operandA = Random.nextInt(100, 999)
                operandB = Random.nextInt(10, 99)
                product = operandA * operandB
                onComplete()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate New Problem")
        }
    }
}

// ==========================================
// 11. MAGIC SQUARE GENERATOR
// ==========================================
@Composable
fun MagicSquareScreen(onComplete: () -> Unit) {
    var targetSumStr by remember { mutableStateOf("34") }
    var square by remember { mutableStateOf<Array<IntArray>?>(null) }
    var errorMsg by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = targetSumStr,
            onValueChange = {
                targetSumStr = it
                errorMsg = ""
            },
            label = { Text("Target Magic Sum (minimum 34)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMsg.isNotEmpty()) {
            Text(errorMsg, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val sum = targetSumStr.toIntOrNull()
                if (sum == null || sum < 34) {
                    errorMsg = "Sum must be 34 or greater."
                    return@Button
                }

                // Dürer's Matrix generator adjusted to custom target S
                // S = S-20 + 1 + 12 + 7
                val matrix = Array(4) { IntArray(4) }
                matrix[0] = intArrayOf(sum - 20, 1, 12, 7)
                matrix[1] = intArrayOf(11, 8, sum - 21, 2)
                matrix[2] = intArrayOf(5, 10, 3, sum - 18)
                matrix[3] = intArrayOf(4, sum - 19, 6, 9)

                square = matrix
                onComplete()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate Magic Square")
        }

        square?.let { matrix ->
            Spacer(modifier = Modifier.height(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                for (r in 0 until 4) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                        for (c in 0 until 4) {
                            Card(
                                modifier = Modifier.size(60.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = matrix[r][c].toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 12. MONTY HALL SIMULATION
// ==========================================
@Composable
fun MontyHallScreen(onComplete: () -> Unit) {
    var step by remember { mutableStateOf(1) } // 1: Pick, 2: Switch/Stay, 3: Reveal
    var carDoor by remember { mutableStateOf(Random.nextInt(3)) }
    var userPick by remember { mutableStateOf(-1) }
    var hostRevealed by remember { mutableStateOf(-1) }
    var winStatus by remember { mutableStateOf<Boolean?>(null) }

    // Simulation Stats
    var totalSimulations by remember { mutableStateOf(0) }
    var stayWins by remember { mutableStateOf(0) }
    var switchWins by remember { mutableStateOf(0) }

    fun resetGame() {
        carDoor = Random.nextInt(3)
        userPick = -1
        hostRevealed = -1
        winStatus = null
        step = 1
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Stage: " + when(step) {
            1 -> "Choose one of the 3 doors!"
            2 -> "Host opened door ${hostRevealed + 1} with a GOAT! Decide to STAY or SWITCH!"
            else -> "Results are in!"
        }, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            for (i in 0 until 3) {
                val isSelectable = step == 1 || (step == 2 && i != hostRevealed)
                
                // Animate door swinging open
                val isDoorOpen = (hostRevealed == i) || (step == 3)
                val doorAngle by animateFloatAsState(
                    targetValue = if (isDoorOpen) -95f else 0f,
                    animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
                    label = "doorAngle"
                )

                // Highlighting picked door
                val isPicked = userPick == i
                val borderBrush = if (isPicked) {
                    BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                } else if (step == 3 && carDoor == i) {
                    BorderStroke(3.dp, Color(0xFF4CAF50))
                } else {
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
                }

                Box(
                    modifier = Modifier
                        .size(90.dp, 135.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isPicked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .border(borderBrush, RoundedCornerShape(12.dp))
                        .clickable(enabled = isSelectable) {
                            if (step == 1) {
                                userPick = i
                                // Host reveals a goat
                                hostRevealed = (0..2).first { it != carDoor && it != userPick }
                                step = 2
                            } else if (step == 2) {
                                val choiceStay = (i == userPick)
                                userPick = i
                                winStatus = (userPick == carDoor)
                                if (choiceStay) {
                                    if (winStatus == true) stayWins++
                                } else {
                                    if (winStatus == true) switchWins++
                                }
                                totalSimulations++
                                step = 3
                                onComplete()
                            }
                        }
                ) {
                    // 1. Inside/Behind the Door (The Prize)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val prizeEmoji = if (carDoor == i) "🚗" else "🐐"
                        val prizeLabel = if (carDoor == i) "CAR!" else "Goat"
                        Text(prizeEmoji, fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = prizeLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (carDoor == i) Color(0xFF2E7D32) else Color(0xFF5D4037)
                        )
                    }

                    // 2. The 3D Swing Wood Door
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationY = doorAngle
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                                cameraDistance = 10f * density
                            }
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF8D6E63), Color(0xFF4E342E)) // Warm wood brown
                                ),
                                RoundedCornerShape(10.dp)
                            )
                            .border(2.dp, Color(0xFF3E2723), RoundedCornerShape(10.dp))
                    ) {
                        // Inner door panel line
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                        )

                        // Doorknob (Shiny Brass)
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .align(Alignment.CenterEnd)
                                .offset(x = (-8).dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFFFFEE58), Color(0xFFF57F17))
                                    ),
                                    shape = CircleShape
                                )
                                .border(1.dp, Color(0xFFE65100), CircleShape)
                        )

                        // Door number
                        Text(
                            text = "${i + 1}",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

        if (step == 3) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (winStatus == true) "🎉 You Won the CAR!" else "🐐 You got a Goat!",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Button(onClick = { resetGame() }, modifier = Modifier.padding(top = 8.dp)) {
                Text("Play Again")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(12.dp))

        Text("Auto-Simulator (1000 Trials)", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                // Instantly simulate 1000 games of stay and switch
                for (j in 0 until 1000) {
                    val actualCar = Random.nextInt(3)
                    val pick = Random.nextInt(3)
                    val revealed = (0..2).first { it != actualCar && it != pick }
                    // Strategy STAY
                    if (pick == actualCar) stayWins++
                    // Strategy SWITCH
                    val switched = (0..2).first { it != pick && it != revealed }
                    if (switched == actualCar) switchWins++
                    totalSimulations++
                }
            }
        ) {
            Text("Simulate 1,000 Games")
        }

        if (totalSimulations > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Switch wins: $switchWins (~${(switchWins * 100) / totalSimulations}%)")
            Text("Stay wins: $stayWins (~${(stayWins * 100) / totalSimulations}%)")
        }
    }
}

// ==========================================
// 13. BIRTHDAY PARADOX LAB
// ==========================================
@Composable
fun BirthdayParadoxScreen(onComplete: () -> Unit) {
    var peopleCount by remember { mutableFloatStateOf(23f) }
    var theoreticalProbability by remember { mutableDoubleStateOf(0.507) }
    var simulationHits by remember { mutableStateOf(0) }
    var totalRuns by remember { mutableStateOf(0) }

    // Floating people simulation state
    var simulatedPeople by remember { mutableStateOf<List<VisualPerson>>(emptyList()) }
    var detectedMatches by remember { mutableStateOf<List<Pair<VisualPerson, VisualPerson>>>(emptyList()) }
    var isSimulating by remember { mutableStateOf(false) }

    fun calculateTheoretical(n: Int): Double {
        var pUnique = 1.0
        for (i in 0 until n) {
            pUnique *= (365.0 - i) / 365.0
        }
        return 1.0 - pUnique
    }

    fun dayToMonthDay(day: Int): String {
        val months = listOf(
            Pair("Jan", 31), Pair("Feb", 28), Pair("Mar", 31), Pair("Apr", 30),
            Pair("May", 31), Pair("Jun", 30), Pair("Jul", 31), Pair("Aug", 31),
            Pair("Sep", 30), Pair("Oct", 31), Pair("Nov", 30), Pair("Dec", 31)
        )
        var remaining = day
        for (m in months) {
            if (remaining < m.second) {
                return "${m.first} ${remaining + 1}"
            }
            remaining -= m.second
        }
        return "Dec 31"
    }

    // Colors for the nodes
    val nodeColors = listOf(
        Color(0xFFE57373), Color(0xFFF06292), Color(0xFFBA68C8), Color(0xFF9575CD),
        Color(0xFF7986CB), Color(0xFF64B5F6), Color(0xFF4FC3F7), Color(0xFF4DD0E1),
        Color(0xFF4DB6AC), Color(0xFF81C784), Color(0xFFAED581), Color(0xFFFFD54F),
        Color(0xFFFFB74D), Color(0xFFFF8A65), Color(0xFFA1887F), Color(0xFF90A4AE)
    )

    // Physics Update Loop
    LaunchedEffect(isSimulating, simulatedPeople) {
        if (!isSimulating) return@LaunchedEffect
        while (isSimulating) {
            delay(16) // ~60fps
            simulatedPeople = simulatedPeople.map { person ->
                var nextX = person.x + person.vx
                var nextY = person.y + person.vy
                var nextVx = person.vx
                var nextVy = person.vy

                // Boundary collision (with radius of 18f)
                val radius = 18f
                if (nextX - radius < 0f) {
                    nextX = radius
                    nextVx = -nextVx
                } else if (nextX + radius > 800f) {
                    nextX = 800f - radius
                    nextVx = -nextVx
                }

                if (nextY - radius < 0f) {
                    nextY = radius
                    nextVy = -nextVy
                } else if (nextY + radius > 450f) {
                    nextY = 450f - radius
                    nextVy = -nextVy
                }

                person.copy(x = nextX, y = nextY, vx = nextVx, vy = nextVy)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stats Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Groups,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Theoretical Collision Probability: ${(theoreticalProbability * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "At 23 people, it's >50%. At 50, it is 97%. Math is beautiful!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // People count Slider Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "People in Room: ${peopleCount.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "N = ${peopleCount.toInt()}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Slider(
                    value = peopleCount,
                    onValueChange = {
                        peopleCount = it
                        theoreticalProbability = calculateTheoretical(it.toInt())
                        // Clear simulation when count is adjusted
                        isSimulating = false
                        simulatedPeople = emptyList()
                        detectedMatches = emptyList()
                    },
                    valueRange = 2f..80f,
                    steps = 77,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Active Simulation Arena Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E)) // Deep Space Classroom Slate
                    )
                )
                .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (simulatedPeople.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.CompareArrows,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap 'Run Simulation' to throw ${peopleCount.toInt()} people into the room!",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // 1. Draw glowing match lines
                    detectedMatches.forEach { (p1, p2) ->
                        // Pulsing neon matching line
                        drawLine(
                            color = Color(0xFFFFB74D),
                            start = Offset(p1.x / 800f * size.width, p1.y / 450f * size.height),
                            end = Offset(p2.x / 800f * size.width, p2.y / 450f * size.height),
                            strokeWidth = 3.5f
                        )
                        // Outer glowing overlay
                        drawLine(
                            color = Color(0xFFFFE082).copy(alpha = 0.3f),
                            start = Offset(p1.x / 800f * size.width, p1.y / 450f * size.height),
                            end = Offset(p2.x / 800f * size.width, p2.y / 450f * size.height),
                            strokeWidth = 8f
                        )
                    }

                    // 2. Draw Simulated People Nodes
                    simulatedPeople.forEach { person ->
                        val px = person.x / 800f * size.width
                        val py = person.y / 450f * size.height
                        val isMatched = detectedMatches.any { it.first.id == person.id || it.second.id == person.id }

                        if (isMatched) {
                            // Pulsing glowing matches
                            drawCircle(
                                color = Color(0xFFE65100).copy(alpha = 0.35f),
                                radius = 26f,
                                center = Offset(px, py)
                            )
                            drawCircle(
                                color = Color(0xFFFFB74D),
                                radius = 18f,
                                center = Offset(px, py)
                            )
                        } else {
                            // Regular node
                            drawCircle(
                                color = person.color.copy(alpha = 0.3f),
                                radius = 18f,
                                center = Offset(px, py)
                            )
                            drawCircle(
                                color = person.color,
                                radius = 14f,
                                center = Offset(px, py)
                            )
                        }
                    }
                }
            }
        }

        // Match Console Ticker
        if (detectedMatches.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE65100).copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "🎉 SUCCESS: Found ${detectedMatches.size} Birthday Collisions!",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF6C00)
                    )
                    detectedMatches.take(3).forEach { (p1, p2) ->
                        Text(
                            text = "• Person ${p1.id} and Person ${p2.id} both share birthday: ${p1.dateStr}!",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE65100),
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                    if (detectedMatches.size > 3) {
                        Text(
                            "• and ${detectedMatches.size - 3} more match pairs...",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE65100),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    totalRuns++
                    isSimulating = true
                    
                    // Generate new set of people
                    val count = peopleCount.toInt()
                    val bdays = List(count) { Random.nextInt(365) }
                    
                    val newPeople = List(count) { id ->
                        val bday = bdays[id]
                        VisualPerson(
                            id = id + 1,
                            birthday = bday,
                            dateStr = dayToMonthDay(bday),
                            x = Random.nextFloat() * 700f + 50f,
                            y = Random.nextFloat() * 350f + 50f,
                            vx = (Random.nextFloat() * 4f - 2f).let { if (it == 0f) 1.5f else it },
                            vy = (Random.nextFloat() * 4f - 2f).let { if (it == 0f) -1.5f else it },
                            color = nodeColors[Random.nextInt(nodeColors.size)]
                        )
                    }

                    // Find matches
                    val matches = mutableListOf<Pair<VisualPerson, VisualPerson>>()
                    for (i in 0 until count) {
                        for (j in i + 1 until count) {
                            if (newPeople[i].birthday == newPeople[j].birthday) {
                                matches.add(Pair(newPeople[i], newPeople[j]))
                            }
                        }
                    }

                    simulatedPeople = newPeople
                    detectedMatches = matches

                    if (matches.isNotEmpty()) {
                        simulationHits++
                    }
                    onComplete()
                },
                modifier = Modifier.weight(1.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Run Simulation", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            if (totalRuns > 0) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val percentage = if (totalRuns > 0) (simulationHits * 100) / totalRuns else 0
                        Text(
                            text = "Rate: $percentage% ($simulationHits/$totalRuns)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private data class VisualPerson(
    val id: Int,
    val birthday: Int,
    val dateStr: String,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color
)

// ==========================================
// 14. DICE PREDICTION TRICK
// ==========================================
enum class DiceStyle(
    val displayName: String,
    val bgColors: List<Color>,
    val dotsColor: Color,
    val bounciness: Float,
    val emblem: String
) {
    ROYAL_IVORY("Royal Ivory", listOf(Color(0xFFFDFBF7), Color(0xFFEFEBE9)), Color(0xFF2E2724), 0.55f, "🎲"),
    CYBER_NEON("Cyber Neon", listOf(Color(0xFF0F2027), Color(0xFF203A43)), Color(0xFF00E5FF), 0.42f, "⚡"),
    CRIMSON_RUBY("Crimson Ruby", listOf(Color(0xFFFF1744), Color(0xFFD50000)), Color(0xFFFFFFFF), 0.75f, "🔥")
}

private data class VisualDice(
    val id: Int,
    var value: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var rotVel: Float
)

@Composable
fun DicePredictionScreen(onComplete: () -> Unit) {
    var stage by remember { mutableStateOf(1) } // 1: instructions & virtual rolling, 2: input / decrypter
    var finalResultStr by remember { mutableStateOf("") }
    var decodedDice by remember { mutableStateOf<List<Int>?>(null) }
    
    var selectedStyle by remember { mutableStateOf(DiceStyle.ROYAL_IVORY) }
    var isRolling by remember { mutableStateOf(false) }
    var virtualDiceList by remember { mutableStateOf<List<VisualDice>>(emptyList()) }
    var showFormulaBreakdown by remember { mutableStateOf(false) }

    // Multi-dice physics simulation loop
    LaunchedEffect(isRolling) {
        if (!isRolling) return@LaunchedEffect
        
        // Initialize 3 dice with energetic forces
        val count = 3
        val sizeConstraint = 48f
        val diceWidth = 800f
        val diceHeight = 400f
        
        val newDice = List(count) { id ->
            VisualDice(
                id = id + 1,
                value = Random.nextInt(1, 7),
                x = 100f + id * 200f + Random.nextFloat() * 50f,
                y = 100f + Random.nextFloat() * 150f,
                vx = (Random.nextFloat() * 14f - 7f).let { if (it == 0f) 5f else it },
                vy = (Random.nextFloat() * 14f - 7f).let { if (it == 0f) -5f else it },
                rotation = Random.nextFloat() * 360f,
                rotVel = Random.nextFloat() * 20f - 10f
            )
        }
        virtualDiceList = newDice

        var ticks = 0
        while (ticks < 120) { // Limit duration of physical roll
            delay(16) // ~60fps
            ticks++

            val tempDice = virtualDiceList.map { it.copy() }
            
            // 1. Position & speed dynamics
            tempDice.forEach { d ->
                d.x += d.vx
                d.y += d.vy
                d.rotation += d.rotVel

                // Friction / deceleration
                d.vx *= 0.96f
                d.vy *= 0.96f
                d.rotVel *= 0.94f

                // Boundary collision (radius = 35f)
                val r = 35f
                if (d.x - r < 0f) {
                    d.x = r
                    d.vx = -d.vx * selectedStyle.bounciness
                } else if (d.x + r > diceWidth) {
                    d.x = diceWidth - r
                    d.vx = -d.vx * selectedStyle.bounciness
                }

                if (d.y - r < 0f) {
                    d.y = r
                    d.vy = -d.vy * selectedStyle.bounciness
                } else if (d.y + r > diceHeight) {
                    d.y = diceHeight - r
                    d.vy = -d.vy * selectedStyle.bounciness
                }
            }

            // 2. Simple Circle-to-Circle elastic collision between dice
            for (i in 0 until count) {
                for (j in i + 1 until count) {
                    val d1 = tempDice[i]
                    val d2 = tempDice[j]
                    val dx = d2.x - d1.x
                    val dy = d2.y - d1.y
                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                    val minDist = 70f // diameter limit
                    
                    if (dist < minDist && dist > 0f) {
                        // Push them apart
                        val overlap = minDist - dist
                        val pushX = (dx / dist) * overlap * 0.5f
                        val pushY = (dy / dist) * overlap * 0.5f
                        d1.x -= pushX
                        d1.y -= pushY
                        d2.x += pushX
                        d2.y += pushY

                        // Swap/rebound velocities
                        val tempVx = d1.vx
                        val tempVy = d1.vy
                        d1.vx = d2.vx * selectedStyle.bounciness
                        d1.vy = d2.vy * selectedStyle.bounciness
                        d2.vx = tempVx * selectedStyle.bounciness
                        d2.vy = tempVy * selectedStyle.bounciness
                        
                        // Add some micro angular spins
                        d1.rotVel += 5f
                        d2.rotVel -= 5f
                    }
                }
            }

            virtualDiceList = tempDice
            
            // Check if all settled
            val energy = tempDice.sumOf { (it.vx * it.vx + it.vy * it.vy).toDouble() }
            if (energy < 0.2 && ticks > 25) {
                break
            }
        }

        // Finalize values
        virtualDiceList = virtualDiceList.map { d ->
            d.copy(
                vx = 0f,
                vy = 0f,
                rotVel = 0f,
                rotation = (d.rotation % 90f).let { if (it < 45) 0f else 90f } // align nicely
            )
        }
        
        // Auto-compute mathematical total
        val a = virtualDiceList[0].value
        val b = virtualDiceList[1].value
        val c = virtualDiceList[2].value
        val mathSum = ((a * 2 + 5) * 5 + b) * 10 + c
        finalResultStr = mathSum.toString()
        
        isRolling = false
        showFormulaBreakdown = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector: Virtual / Physical Manual
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = if (stage == 1) "Roll virtual dice or use your own!" else "Reveal the magical math formula",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        stage = if (stage == 1) 2 else 1
                        decodedDice = null
                        showFormulaBreakdown = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(if (stage == 1) "Manual Decryptor" else "Virtual Tray", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        if (stage == 1) {
            // Virtual Tray Mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiceStyle.values().forEach { style ->
                    val isSelected = selectedStyle == style
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = !isRolling) {
                                selectedStyle = style
                                virtualDiceList = emptyList()
                                showFormulaBreakdown = false
                            },
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) style.bgColors[0] else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) style.bgColors[0].copy(alpha = 0.1f) else Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(style.emblem, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = style.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) style.bgColors[0] else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Felt Dice Board Canvas Surface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF303F9F), Color(0xFF1A237E)) // Blue velvet casino felt
                        )
                    )
                    .border(2.dp, Color(0xFF0F143F), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (virtualDiceList.isEmpty() && !isRolling) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Casino,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap 'Roll Virtual Dice' to trigger 2D physics!",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        virtualDiceList.forEach { dice ->
                            val px = dice.x / 800f * size.width
                            val py = dice.y / 400f * size.height
                            
                            // Draw 3D shadow offset
                            drawRoundRect(
                                color = Color.Black.copy(alpha = 0.35f),
                                topLeft = Offset(px - 28f + 6f, py - 28f + 6f),
                                size = androidx.compose.ui.geometry.Size(56f, 56f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                            )
                            
                            // Draw Dice acrylic face
                            drawRoundRect(
                                color = selectedStyle.bgColors[0],
                                topLeft = Offset(px - 28f, py - 28f),
                                size = androidx.compose.ui.geometry.Size(56f, 56f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                            )
                            
                            // Draw glossy Bevel outline
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.4f),
                                topLeft = Offset(px - 28f, py - 28f),
                                size = androidx.compose.ui.geometry.Size(56f, 56f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                                style = Stroke(2f)
                            )

                            // Render tactile dice dots based on numeric value
                            val dotsColor = selectedStyle.dotsColor
                            fun drawDot(cx: Float, cy: Float) {
                                drawCircle(color = dotsColor, radius = 4.5f, center = Offset(px + cx, py + cy))
                            }
                            
                            when (dice.value) {
                                1 -> drawDot(0f, 0f)
                                2 -> {
                                    drawDot(-14f, -14f)
                                    drawDot(14f, 14f)
                                }
                                3 -> {
                                    drawDot(-14f, -14f)
                                    drawDot(0f, 0f)
                                    drawDot(14f, 14f)
                                }
                                4 -> {
                                    drawDot(-14f, -14f)
                                    drawDot(14f, -14f)
                                    drawDot(-14f, 14f)
                                    drawDot(14f, 14f)
                                }
                                5 -> {
                                    drawDot(-14f, -14f)
                                    drawDot(14f, -14f)
                                    drawDot(0f, 0f)
                                    drawDot(-14f, 14f)
                                    drawDot(14f, 14f)
                                }
                                6 -> {
                                    drawDot(-14f, -14f)
                                    drawDot(14f, -14f)
                                    drawDot(-14f, 0f)
                                    drawDot(14f, 0f)
                                    drawDot(-14f, 14f)
                                    drawDot(14f, 14f)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Button(
                onClick = {
                    if (isRolling) return@Button
                    isRolling = true
                    showFormulaBreakdown = false
                },
                enabled = !isRolling,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Roll Virtual Dice", fontWeight = FontWeight.Bold)
            }

            // Math breakdown walk-through
            if (showFormulaBreakdown && virtualDiceList.size == 3) {
                val valA = virtualDiceList[0].value
                val valB = virtualDiceList[1].value
                val valC = virtualDiceList[2].value
                
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 Interactive Mathematical Breakdown:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Die A: $valA, Die B: $valB, Die C: $valC",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Multiply Die A ($valA) by 2, add 5:\n" +
                                   "   ($valA * 2) + 5 = ${valA * 2 + 5}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "2. Multiply by 5, add Die B ($valB):\n" +
                                   "   (${valA * 2 + 5} * 5) + $valB = ${(valA * 2 + 5) * 5 + valB}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "3. Multiply by 10, add Die C ($valC):\n" +
                                   "   (${(valA * 2 + 5) * 5 + valB} * 10) + $valC = $finalResultStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Formula Sum: $finalResultStr",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = {
                                    stage = 2
                                    decodedDice = listOf(valA, valB, valC)
                                    onComplete()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Decipher Sum", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

        } else {
            // Manual Decryptor Input Mode
            OutlinedTextField(
                value = finalResultStr,
                onValueChange = { finalResultStr = it },
                label = { Text("Enter calculated formula sum") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val r = finalResultStr.toIntOrNull()
                    if (r != null && r >= 250) {
                        val decoded = r - 250
                        val dieC = decoded % 10
                        val dieB = (decoded / 10) % 10
                        val dieA = (decoded / 100) % 10
                        
                        // Bounds checking
                        if (dieA in 1..6 && dieB in 1..6 && dieC in 1..6) {
                            decodedDice = listOf(dieA, dieB, dieC)
                            onComplete()
                        } else {
                            decodedDice = null
                        }
                    } else {
                        decodedDice = null
                    }
                },
                enabled = finalResultStr.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Decipher Dice!", fontWeight = FontWeight.Bold)
            }

            decodedDice?.let { dice ->
                Spacer(modifier = Modifier.height(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "🔮 Decoded Original Dice Values:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        dice.forEachIndexed { index, value ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Die ${'A' + index}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Card(
                                    modifier = Modifier.size(56.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = value.toString(),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 15. COIN PROBABILITY LAB
// ==========================================
enum class CoinStyle(
    val displayName: String,
    val weight: Float,
    val elasticity: Float,
    val primaryColor: Color,
    val secondaryColor: Color,
    val emblem: String,
    val description: String
) {
    GOLDEN_SOVEREIGN("Golden Sovereign", 0.7f, 0.55f, Color(0xFFFFD54F), Color(0xFFF57F17), "👑", "Standard mass, balanced bounce"),
    CARBON_CYBER("Carbon Cyber Token", 1.4f, 0.28f, Color(0xFF00E5FF), Color(0xFF00838F), "⚡", "High density, heavy thud"),
    COSMIC_ASTEROID("Cosmic Asteroid", 0.35f, 0.82f, Color(0xFFE040FB), Color(0xFF6A1B9A), "☄️", "Low gravity, hyper float")
}

@Composable
fun CoinProbabilityScreen(onComplete: () -> Unit) {
    var totalFlips by remember { mutableStateOf(0) }
    var heads by remember { mutableStateOf(0) }
    var tails by remember { mutableStateOf(0) }

    var selectedStyle by remember { mutableStateOf(CoinStyle.GOLDEN_SOVEREIGN) }
    
    // Physics simulation state
    var coinYOffset by remember { mutableFloatStateOf(0f) }
    var coinRotationX by remember { mutableFloatStateOf(0f) }
    var isFlipping by remember { mutableStateOf(false) }
    var currentResultStr by remember { mutableStateOf("Taps Flip to toss!") }

    // Run custom physics flip simulation
    LaunchedEffect(isFlipping) {
        if (!isFlipping) return@LaunchedEffect
        
        // 1. Predetermine the actual physical outcome
        val isHeads = Random.nextBoolean()
        
        // 2. Initial physics forces
        var vy = -18f * (1.2f / selectedStyle.weight).coerceAtMost(2.5f) // force offset by weight
        val gravity = 0.8f * selectedStyle.weight
        var rotSpeed = 32f
        var localY = 0f
        var localRot = coinRotationX

        while (true) {
            delay(16) // ~60 FPS
            localY += vy
            vy += gravity
            localRot += rotSpeed

            // Decay rotational speed slowly
            rotSpeed = (rotSpeed - 0.1f).coerceAtLeast(12f)

            if (localY > 0f) {
                // Collide with green felt table!
                localY = 0f
                vy = -vy * selectedStyle.elasticity // Elastic collision rebound
                rotSpeed *= 0.6f // Lose spin on impact
                
                // End condition when vertical energy decays below threshold
                if (kotlin.math.abs(vy) < 1.8f) {
                    localY = 0f
                    // Snapping final 3D angle to sit flatly on heads (0deg) or tails (180deg)
                    localRot = if (isHeads) 0f else 180f
                    coinYOffset = localY
                    coinRotationX = localRot
                    
                    // Commit statistics
                    totalFlips++
                    if (isHeads) heads++ else tails++
                    currentResultStr = if (isHeads) "Result: HEADS ${selectedStyle.emblem}" else "Result: TAILS 🦅"
                    isFlipping = false
                    onComplete()
                    break
                }
            }
            coinYOffset = localY
            coinRotationX = localRot
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stats Console
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Flips: $totalFlips",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            "Heads: $heads",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Tails: $tails",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (totalFlips > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Ratio: ${String.format("%.3f", heads.toDouble() / totalFlips)} (≈ 0.50)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Custom Coin Physics Selection / Controller Panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CoinStyle.values().forEach { style ->
                val isSelected = selectedStyle == style
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !isFlipping) {
                            selectedStyle = style
                            currentResultStr = "Ready to flip ${style.displayName}!"
                        },
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) style.primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) style.primaryColor.copy(alpha = 0.08f) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(style.emblem, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = style.displayName.split(" ")[0],
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) style.primaryColor else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // The Felt Table Surface Arena
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1B5E20), Color(0xFF0F3D13)) // Classic Green casino felt
                    )
                )
                .border(1.5.dp, Color(0xFF0C2E0F), RoundedCornerShape(20.dp))
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Shadow projection representing depth distance
            val shadowScale = (1.0f - (kotlin.math.abs(coinYOffset) / 250f)).coerceIn(0.2f, 1.0f)
            Box(
                modifier = Modifier
                    .offset(y = (-12).dp)
                    .size(width = (90 * shadowScale).dp, height = (16 * shadowScale).dp)
                    .background(Color.Black.copy(alpha = 0.4f * shadowScale), CircleShape)
            )

            // The Coin structure itself, using rotation matrix transformation
            Box(
                modifier = Modifier
                    .offset(y = coinYOffset.dp)
                    .graphicsLayer {
                        // Simulating 3D spinning by rotating around Y axis (or custom X axis)
                        rotationX = coinRotationX
                        cameraDistance = 8f * density
                    }
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(selectedStyle.primaryColor, selectedStyle.secondaryColor)
                        )
                    )
                    .border(3.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Glossy reflective coin bevel
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                        .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                ) {
                    val isHeadsFacing = (coinRotationX.toInt() / 90) % 2 == 0
                    
                    Text(
                        text = if (isHeadsFacing) selectedStyle.emblem else "🦅",
                        fontSize = 32.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live ticker status console
        Text(
            text = currentResultStr,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (currentResultStr.contains("HEADS")) selectedStyle.primaryColor else if (currentResultStr.contains("TAILS")) Color(0xFFEF5350) else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Trigger buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (isFlipping) return@Button
                    isFlipping = true
                    currentResultStr = "Flipping ${selectedStyle.displayName}..."
                },
                enabled = !isFlipping,
                modifier = Modifier.weight(1.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Flip Coin", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    if (isFlipping) return@Button
                    repeat(100) {
                        totalFlips++
                        if (Random.nextBoolean()) heads++ else tails++
                    }
                    currentResultStr = "Bulk simulated 100 fast flips!"
                    onComplete()
                },
                enabled = !isFlipping,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simulate 100", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// ==========================================
// 16. THE GAME OF NIM
// ==========================================
@Composable
fun NimGameScreen(onComplete: () -> Unit) {
    var heaps by remember { mutableStateOf(listOf(3, 4, 5)) }
    var selectedHeapIndex by remember { mutableStateOf(-1) }
    var selectedCount by remember { mutableStateOf(0) }
    var logMsg by remember { mutableStateOf("Your turn. Pick stones from any one heap.") }
    var gameOver by remember { mutableStateOf(false) }

    fun checkWinner() {
        if (heaps.all { it == 0 }) {
            gameOver = true
            onComplete()
        }
    }

    fun aiTurn() {
        if (heaps.all { it == 0 }) return
        val aiMove = GameEngine.calculateNimMove(heaps)
        val newHeaps = heaps.toMutableList()
        newHeaps[aiMove.heapIndex] -= aiMove.amountToRemove
        heaps = newHeaps
        logMsg = "AI took ${aiMove.amountToRemove} stones from Heap ${aiMove.heapIndex + 1}!"
        checkWinner()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Aesthetic Status Console
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = logMsg,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Stones and Trays Layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            for (idx in heaps.indices) {
                val stoneCount = heaps[idx]
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        text = "Heap ${idx + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedHeapIndex == idx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$stoneCount stones",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Vertical Stack of Stones
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (stone in 0 until stoneCount) {
                                val isSelected = selectedHeapIndex == idx && stone >= stoneCount - selectedCount
                                
                                // Tactile animation parameters
                                val scale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.22f else 1.0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "stoneScale"
                                )
                                
                                val stoneBgColor by animateColorAsState(
                                    targetValue = if (isSelected) {
                                        Color(0xFFFFB74D) // Bright Glowing Orange
                                    } else {
                                        Color(0xFF78909C) // Slate grey
                                    },
                                    animationSpec = tween(250),
                                    label = "stoneColor"
                                )

                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        }
                                        .size(width = 46.dp, height = 22.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.radialGradient(
                                                colors = if (isSelected) {
                                                    listOf(Color(0xFFFFE082), stoneBgColor, Color(0xFFE65100))
                                                } else {
                                                    listOf(Color(0xFFCFD8DC), stoneBgColor, Color(0xFF37474F))
                                                }
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color(0xFFEF6C00) else Color(0xFF455A64),
                                            shape = RoundedCornerShape(50)
                                        )
                                        .clickable {
                                            if (selectedHeapIndex != idx) {
                                                selectedHeapIndex = idx
                                                selectedCount = 1
                                            } else {
                                                selectedCount = (selectedCount + 1).coerceAtMost(heaps[idx])
                                            }
                                        }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Base Tray (Wooden Pedestal representation)
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(10.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF8B4513), Color(0xFF4E2506))
                                ),
                                RoundedCornerShape(5.dp)
                            )
                            .border(1.dp, Color(0xFF321401), RoundedCornerShape(5.dp))
                    )
                }
            }
        }

        // Active selection clear state
        if (selectedHeapIndex != -1) {
            Text(
                text = "Currently selected: $selectedCount stones from Heap ${selectedHeapIndex + 1}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Main controls
        Button(
            onClick = {
                if (selectedHeapIndex != -1 && selectedCount > 0) {
                    val newHeaps = heaps.toMutableList()
                    newHeaps[selectedHeapIndex] -= selectedCount
                    heaps = newHeaps
                    selectedHeapIndex = -1
                    selectedCount = 0
                    logMsg = "AI is computing..."
                    checkWinner()
                    if (!gameOver) {
                        aiTurn()
                    }
                }
            },
            enabled = selectedHeapIndex != -1 && !gameOver,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Confirm Selection & End Turn", fontWeight = FontWeight.Bold)
        }

        if (gameOver) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Game Over!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Perfect Nim Logic! You cleared the board.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            heaps = listOf(3, 4, 5)
                            selectedHeapIndex = -1
                            selectedCount = 0
                            gameOver = false
                            logMsg = "Your turn. Pick stones from any one heap."
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Play Again", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 17. TOWER OF HANOI
// ==========================================
@Composable
fun HanoiTowerScreen(onComplete: () -> Unit) {
    var pegs by remember { mutableStateOf<List<List<Int>>>(listOf(listOf(3, 2, 1), emptyList(), emptyList())) }
    var selectedPeg by remember { mutableStateOf(-1) }
    var statusText by remember { mutableStateOf("Solve in minimum 7 moves!") }
    var isSolving by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status & Instruction Console
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (pegs[2] == listOf(3, 2, 1)) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tactile Tower Base and Rods
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                .padding(12.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Main Row of 3 Pegs
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                for (i in 0 until 3) {
                    val isSelected = selectedPeg == i
                    val rodDisks = pegs[i]

                    // Column representing individual peg station
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else Color.Transparent
                            )
                            .clickable(enabled = !isSolving) {
                                if (selectedPeg == -1) {
                                    if (rodDisks.isNotEmpty()) {
                                        selectedPeg = i
                                        statusText = "Peg ${i + 1} selected. Choose destination peg."
                                    } else {
                                        statusText = "Peg ${i + 1} is empty!"
                                    }
                                } else {
                                    val sourcePegIndex = selectedPeg
                                    val sourceDisks = pegs[sourcePegIndex]
                                    val targetDisks = rodDisks
                                    
                                    if (sourcePegIndex == i) {
                                        // De-select
                                        selectedPeg = -1
                                        statusText = "Selection cleared."
                                    } else if (targetDisks.isEmpty() || sourceDisks.last() < targetDisks.last()) {
                                        // Valid move
                                        val newPegs = pegs.map { it.toMutableList() }
                                        val movedDisk = newPegs[sourcePegIndex].removeAt(newPegs[sourcePegIndex].lastIndex)
                                        newPegs[i].add(movedDisk)
                                        pegs = newPegs
                                        selectedPeg = -1
                                        statusText = "Moved disk $movedDisk successfully!"
                                        if (pegs[2] == listOf(3, 2, 1)) {
                                            statusText = "🎉 Victory! Tower solved!"
                                            onComplete()
                                        }
                                    } else {
                                        selectedPeg = -1
                                        statusText = "⚠️ Invalid! Larger disk cannot sit on a smaller disk."
                                    }
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Rod, Base Pedestal & Disk Stacks relative space
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // The Vertical Rod itself (wooden pin style)
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFD7CCC8), Color(0xFF8D6E63), Color(0xFF5D4037))
                                        )
                                    )
                                    .border(1.dp, Color(0xFF4E342E).copy(alpha = 0.5f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            )

                            // Stacked Disks on this Rod
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                rodDisks.forEachIndexed { idx, disk ->
                                    val isTopDisk = idx == rodDisks.lastIndex
                                    val isFloating = isSelected && isTopDisk

                                    // Dynamic Spring Hover Offset
                                    val floatOffset by animateDpAsState(
                                        targetValue = if (isFloating) (-45).dp else 0.dp,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "HanoiDiskFloat"
                                    )

                                    // Vibrant color configuration
                                    val diskColors = when (disk) {
                                        1 -> listOf(Color(0xFFFFD54F), Color(0xFFFFB300)) // Size 1: Golden Amber
                                        2 -> listOf(Color(0xFF4FC3F7), Color(0xFF0288D1)) // Size 2: Sky Blue
                                        else -> listOf(Color(0xFFBA68C8), Color(0xFF7B1FA2)) // Size 3: Amethyst Purple
                                    }
                                    val diskBorderColor = when (disk) {
                                        1 -> Color(0xFFFF8F00)
                                        2 -> Color(0xFF01579B)
                                        else -> Color(0xFF4A148C)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .offset(y = floatOffset)
                                            .width((disk * 36 + 40).dp)
                                            .height(24.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Brush.verticalGradient(colors = diskColors))
                                            .border(1.5.dp, diskBorderColor, RoundedCornerShape(50)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Bevel aesthetic line
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(50))
                                        ) {
                                            Text(
                                                text = disk.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Peg ${i + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // High quality Wooden Platform base spanning all pegs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF8B4513), Color(0xFF5C2E0B))
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(1.5.dp, Color(0xFF3E1F07), RoundedCornerShape(8.dp))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Auto-solve and manual restart buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (isSolving) return@Button
                    isSolving = true
                    selectedPeg = -1
                    coroutineScope.launch {
                        val moves = mutableListOf<Pair<Int, Int>>()
                        fun solve(n: Int, source: Int, target: Int, auxiliary: Int) {
                            if (n > 0) {
                                solve(n - 1, source, auxiliary, target)
                                moves.add(Pair(source, target))
                                solve(n - 1, auxiliary, target, source)
                            }
                        }
                        solve(3, 0, 2, 1)

                        pegs = listOf(listOf(3, 2, 1), emptyList(), emptyList())
                        for (move in moves) {
                            delay(900)
                            val newPegs = pegs.map { it.toMutableList() }
                            newPegs[move.second].add(newPegs[move.first].removeAt(newPegs[move.first].lastIndex))
                            pegs = newPegs
                            statusText = "Auto-solving: peg ${move.first + 1} ➔ peg ${move.second + 1}"
                        }
                        isSolving = false
                        statusText = "🎉 Auto-solve complete! Mathematical victory!"
                        onComplete()
                    }
                },
                enabled = !isSolving,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoMode, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Auto Solve Invariant", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    pegs = listOf(listOf(3, 2, 1), emptyList(), emptyList())
                    selectedPeg = -1
                    statusText = "Solve in minimum 7 moves!"
                    isSolving = false
                },
                enabled = !isSolving,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restart Game", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// ==========================================
// 18. LIGHTS OUT SOLVER
// ==========================================
@Composable
fun LightsOutScreen(onComplete: () -> Unit) {
    var grid by remember { mutableStateOf(BooleanArray(9) { Random.nextBoolean() }) }
    var hintClicks by remember { mutableStateOf<IntArray?>(null) }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Turn off all the lights!", fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.size(180.dp)
        ) {
            items(9) { idx ->
                val isOn = grid[idx]
                val r = idx / 3
                val c = idx % 3

                val hasHint = hintClicks?.let { it[idx] == 1 } ?: false

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(4.dp)
                        .background(
                            if (isOn) Color.Yellow else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = if (hasHint) 3.dp else 1.dp,
                            color = if (hasHint) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            val next = grid.clone()
                            next[idx] = !next[idx]
                            if (r > 0) next[(r - 1) * 3 + c] = !next[(r - 1) * 3 + c]
                            if (r < 2) next[(r + 1) * 3 + c] = !next[(r + 1) * 3 + c]
                            if (c > 0) next[r * 3 + (c - 1)] = !next[r * 3 + (c - 1)]
                            if (c < 2) next[r * 3 + (c + 1)] = !next[r * 3 + (c + 1)]
                            grid = next
                            hintClicks = null
                            if (grid.all { !it }) {
                                onComplete()
                            }
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = {
                hintClicks = GameEngine.solveLightsOut(grid)
            }) {
                Text("Show Hint")
            }

            Button(onClick = {
                grid = BooleanArray(9) { Random.nextBoolean() }
                hintClicks = null
            }) {
                Text("Randomize")
            }
        }
    }
}

// ==========================================
// 19. GRAY CODE GUESS
// ==========================================
@Composable
fun GrayCodeScreen(onComplete: () -> Unit) {
    var stage by remember { mutableStateOf(0) }
    var grayCodeAccumulator by remember { mutableStateOf(0) }
    var finalResult by remember { mutableStateOf<Int?>(null) }

    val cards = remember {
        List(4) { bitIndex ->
            (0..15).filter { num ->
                // Check if the bitIndex bit of Gray code is 1
                val gray = num xor (num shr 1)
                (gray and (1 shl bitIndex)) != 0
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (stage < 4) {
            Text("Think of any number from 0 to 15.", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Is your secret number on this card?")
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    LazyVerticalGrid(columns = GridCells.Fixed(4)) {
                        items(cards[stage]) { num ->
                            Text(num.toString(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = {
                    grayCodeAccumulator = grayCodeAccumulator or (1 shl stage)
                    stage++
                    if (stage == 4) {
                        // Gray to binary conversion
                        var b = 0
                        var g = grayCodeAccumulator
                        while (g > 0) {
                            b = b xor g
                            g = g shr 1
                        }
                        finalResult = b
                        onComplete()
                    }
                }) {
                    Text("YES")
                }

                Button(onClick = {
                    stage++
                    if (stage == 4) {
                        var b = 0
                        var g = grayCodeAccumulator
                        while (g > 0) {
                            b = b xor g
                            g = g shr 1
                        }
                        finalResult = b
                        onComplete()
                    }
                }) {
                    Text("NO")
                }
            }
        } else {
            Text("We guessed your number!", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    finalResult.toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                stage = 0
                grayCodeAccumulator = 0
                finalResult = null
            }) {
                Text("Restart")
            }
        }
    }
}

// ==========================================
// 20. FIFTEEN PUZZLE
// ==========================================
@Composable
fun FifteenPuzzleScreen(onComplete: () -> Unit) {
    var board by remember { mutableStateOf(GameEngine.generateSolvableFifteenPuzzle()) }
    var moveCount by remember { mutableStateOf(0) }

    // Count how many tiles are in their correct solved slots (1 to 15)
    val solvedCount = remember(board) {
        board.filterIndexed { index, tile -> tile != 0 && tile == index + 1 }.size
    }
    val isWinner = remember(board) {
        val winState = (1..15).toList() + 0
        board == winState
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stats Console / Progress Indicator
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isWinner) {
                    Color(0xFF4CAF50).copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                }
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (isWinner) Color(0xFF4CAF50).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = if (isWinner) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isWinner) "🎉 Board Solved!" else "Arrange 1 to 15 in order",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isWinner) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Live solved badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isWinner) Color(0xFF4CAF50).copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$solvedCount / 15 Solved",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isWinner) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // The Sliding Puzzle Tray Cabinet Frame
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF37474F), Color(0xFF212121)) // Carbon slate tray
                    )
                )
                .border(2.5.dp, Color(0xFF1A1A1A), RoundedCornerShape(24.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                userScrollEnabled = false
            ) {
                items(board.size) { idx ->
                    val tile = board[idx]
                    val isCorrectPosition = tile != 0 && tile == idx + 1

                    // Spring visual scale bounciness when a tile is tapped/active
                    val scale by animateFloatAsState(
                        targetValue = if (tile != 0) 1.0f else 0.95f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "TileScaleAnim"
                    )

                    val tileBgBrush = if (tile == 0) {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF121212), Color(0xFF1E1E1E)) // Recessed empty pocket style
                        )
                    } else if (isCorrectPosition) {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)) // Bright Emerald Green
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .background(tileBgBrush)
                            .border(
                                width = if (tile == 0) 1.5.dp else 1.dp,
                                color = when {
                                    tile == 0 -> Color(0xFF0F0F0F)
                                    isCorrectPosition -> Color(0xFF1B5E20)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = tile != 0 && !isWinner) {
                                val emptyIdx = board.indexOf(0)
                                val row = idx / 4
                                val col = idx % 4
                                val empRow = emptyIdx / 4
                                val empCol = emptyIdx % 4

                                val isAdjacent = (java.lang.Math.abs(row - empRow) + java.lang.Math.abs(col - empCol)) == 1
                                if (isAdjacent) {
                                    val next = board.toMutableList()
                                    next[emptyIdx] = tile
                                    next[idx] = 0
                                    board = next
                                    moveCount++

                                    // Check win condition
                                    val winState = (1..15).toList() + 0
                                    if (board == winState) {
                                        onComplete()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (tile != 0) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Glossy Bevel Accent Line on acrylic tile
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = if (isCorrectPosition) 0.3f else 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tile.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = if (isCorrectPosition) {
                                            Color.White
                                        } else {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        }
                                    )
                                }
                            }
                        } else {
                            // Subtly render a recessed dot in the empty slot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF000000), CircleShape)
                                    .border(1.dp, Color(0x22FFFFFF), CircleShape)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions & Moves Counter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Moves badge
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Moves: $moveCount",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = {
                    board = GameEngine.generateSolvableFifteenPuzzle()
                    moveCount = 0
                },
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reshuffle Board", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// ==========================================
// 21. RIVER CROSSING
// ==========================================
@Composable
fun RiverCrossingScreen(onComplete: () -> Unit) {
    var state by remember { mutableStateOf(GameEngine.RiverState(farmer = true, wolf = true, goat = true, cabbage = true)) }
    var statusText by remember { mutableStateOf("Get everyone safely across!") }
    var solvedPath by remember { mutableStateOf<List<GameEngine.RiverState>?>(null) }
    var lastErrorState by remember { mutableStateOf<String?>(null) }

    fun crossRiver(item: String?) {
        val nextState = when (item) {
            "Wolf" -> state.copy(farmer = !state.farmer, wolf = !state.wolf)
            "Goat" -> state.copy(farmer = !state.farmer, goat = !state.goat)
            "Cabbage" -> state.copy(farmer = !state.farmer, cabbage = !state.cabbage)
            else -> state.copy(farmer = !state.farmer)
        }

        if (nextState.isValid()) {
            state = nextState
            lastErrorState = null
            statusText = "Sailed across."
            if (state.isGoal()) {
                statusText = "🎉 Victory! All safely crossed!"
                onComplete()
            }
        } else {
            // Determine exact error
            lastErrorState = if (nextState.wolf == nextState.goat && nextState.farmer != nextState.wolf) {
                "🐺 🍽️ 🐐 Oh no! The Wolf ate the Goat!"
            } else {
                "🐐 🍽️ 🥬 Oh no! The Goat ate the Cabbage!"
            }
            statusText = "Failed move!"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status & Error Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (lastErrorState != null) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                } else if (state.isGoal()) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (lastErrorState != null) "⚠️ Move Blocked" else if (state.isGoal()) "👑 Triumph!" else "🔬 Simulation Status",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (lastErrorState != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lastErrorState ?: statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Gorgeous Animated River & Landscape Lab Area
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                .background(Color(0xFFE0F7FA)) // Soft aqua water color
        ) {
            val widthPx = constraints.maxWidth
            val maxWidth = this.maxWidth

            // 1. Water waves drawing using canvas and infinite transition
            val infiniteTransition = rememberInfiniteTransition(label = "RiverFlow")
            val waveOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "waveOffset"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val wavePath = androidx.compose.ui.graphics.Path()
                val flowY = 120f
                wavePath.moveTo(0f, flowY)
                // Draw wave segments
                for (x in 0..widthPx step 20) {
                    val angle = (x.toFloat() / widthPx * 360f + waveOffset) * (Math.PI / 180f)
                    val y = flowY + kotlin.math.sin(angle).toFloat() * 10f
                    wavePath.lineTo(x.toFloat(), y)
                }
                wavePath.lineTo(widthPx.toFloat(), size.height)
                wavePath.lineTo(0f, size.height)
                wavePath.close()

                drawPath(
                    path = wavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x334DD0E1), Color(0x6600ACC1))
                    )
                )

                // Draw secondary current line
                for (i in 1..3) {
                    val currentY = 80f + i * 40f
                    val startX = (waveOffset * 2f + i * 200f) % widthPx
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f),
                        radius = 4f,
                        center = Offset(startX, currentY)
                    )
                }
            }

            // 2. Left Bank (Green grass card)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(85.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF2E7D32), Color(0xFF4CAF50))
                        ),
                        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = Color(0xFF1B5E20),
                        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    "LEFT BANK",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 3. Right Bank (Green grass card)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(85.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
                        ),
                        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = Color(0xFF1B5E20),
                        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    "RIGHT BANK",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 4. Boat bobbing & horizontal movement
            val boatBobbing by infiniteTransition.animateFloat(
                initialValue = -3f,
                targetValue = 3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "boatBobbing"
            )

            val targetBoatX = if (state.farmer) 90.dp else maxWidth - 160.dp
            val boatX by animateDpAsState(
                targetValue = targetBoatX,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                label = "boatX"
            )

            // Draw Boat
            Box(
                modifier = Modifier
                    .offset(x = boatX, y = 115.dp + boatBobbing.dp)
                    .size(70.dp, 45.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF8B4513), Color(0xFF5C2E0B))
                        ),
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .border(1.5.dp, Color(0xFF3E1F07), RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🛶", fontSize = 24.sp)
            }

            // 5. Animating Farmer (always on the boat)
            val farmerX by animateDpAsState(
                targetValue = boatX + 15.dp,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                label = "farmerX"
            )
            val farmerY by animateDpAsState(
                targetValue = 75.dp + boatBobbing.dp,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                label = "farmerY"
            )

            // Render Farmer
            Box(
                modifier = Modifier
                    .offset(x = farmerX, y = farmerY)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👨‍🌾", fontSize = 16.sp)
                    Text("Farmer", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            // 6. Animating Passengers (Wolf, Goat, Cabbage)
            // Wolf Position
            val targetWolfX = if (state.wolf) 22.dp else maxWidth - 62.dp
            val targetWolfY = if (state.wolf == state.farmer && !state.wolf) {
                // If it crossed, can animate temporarily
                28.dp
            } else {
                28.dp
            }
            val wolfX by animateDpAsState(
                targetValue = targetWolfX,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                label = "wolfX"
            )
            val wolfY by animateDpAsState(
                targetValue = targetWolfY,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                label = "wolfY"
            )

            // Goat Position
            val targetGoatX = if (state.goat) 22.dp else maxWidth - 62.dp
            val targetGoatY = 95.dp
            val goatX by animateDpAsState(
                targetValue = targetGoatX,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                label = "goatX"
            )
            val goatY by animateDpAsState(
                targetValue = targetGoatY,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                label = "goatY"
            )

            // Cabbage Position
            val targetCabbageX = if (state.cabbage) 22.dp else maxWidth - 62.dp
            val targetCabbageY = 162.dp
            val cabbageX by animateDpAsState(
                targetValue = targetCabbageX,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                label = "cabbageX"
            )
            val cabbageY by animateDpAsState(
                targetValue = targetCabbageY,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                label = "cabbageY"
            )

            // Render Wolf
            Box(
                modifier = Modifier
                    .offset(x = wolfX, y = wolfY)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(2.dp, Color(0xFF78909C), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🐺", fontSize = 16.sp)
                    Text("Wolf", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF546E7A))
                }
            }

            // Render Goat
            Box(
                modifier = Modifier
                    .offset(x = goatX, y = goatY)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(2.dp, Color(0xFFFFB74D), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🐐", fontSize = 16.sp)
                    Text("Goat", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF6C00))
                }
            }

            // Render Cabbage
            Box(
                modifier = Modifier
                    .offset(x = cabbageX, y = cabbageY)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(2.dp, Color(0xFF81C784), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🥬", fontSize = 16.sp)
                    Text("Cabbage", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Cockpit (Sail Commands)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🚢 Boat Controls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sail Alone
                    Button(
                        onClick = { crossRiver(null) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sail Alone 👨‍🌾", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Sail + Wolf
                    val canTakeWolf = state.farmer == state.wolf
                    Button(
                        onClick = { crossRiver("Wolf") },
                        modifier = Modifier.weight(1f),
                        enabled = canTakeWolf,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sail + 🐺", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sail + Goat
                    val canTakeGoat = state.farmer == state.goat
                    Button(
                        onClick = { crossRiver("Goat") },
                        modifier = Modifier.weight(1f),
                        enabled = canTakeGoat,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sail + 🐐", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Sail + Cabbage
                    val canTakeCabbage = state.farmer == state.cabbage
                    Button(
                        onClick = { crossRiver("Cabbage") },
                        modifier = Modifier.weight(1f),
                        enabled = canTakeCabbage,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sail + 🥬", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hint & Restart Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    solvedPath = GameEngine.solveRiverCrossing(state)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Help, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Get BFS Hint", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    state = GameEngine.RiverState(farmer = true, wolf = true, goat = true, cabbage = true)
                    statusText = "Get everyone safely across!"
                    lastErrorState = null
                    solvedPath = null
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Restart Lab", fontWeight = FontWeight.Bold)
            }
        }

        // BFS Path Drawer
        solvedPath?.let { path ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("💡 Shortest Path to Solve (BFS Solution):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    path.take(4).forEachIndexed { index, step ->
                        Text(
                            text = "${index + 1}. Farmer=${if(step.farmer) "Left" else "Right"}, " +
                                    "Wolf=${if(step.wolf) "Left" else "Right"}, " +
                                    "Goat=${if(step.goat) "Left" else "Right"}, " +
                                    "Cabbage=${if(step.cabbage) "Left" else "Right"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 22. DOMINO PREDICTION
// ==========================================
@Composable
fun DominoPredictionScreen(onComplete: () -> Unit) {
    var stage by remember { mutableStateOf(1) } // 1: pick, 2: prediction
    var hiddenDomino by remember { mutableStateOf(Pair(3, 5)) }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (stage == 1) {
            Text("Choose one domino to hide in your pocket:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            val dominoes = listOf(Pair(3, 5), Pair(1, 4), Pair(2, 6), Pair(0, 5))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                dominoes.forEach { d ->
                    Card(
                        modifier = Modifier
                            .size(70.dp, 40.dp)
                            .clickable {
                                hiddenDomino = d
                                stage = 2
                                onComplete()
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("[${d.first} | ${d.second}]", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            Text("We will connect all other dominoes. The prediction states:", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("The endpoints of the domino line will be:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${hiddenDomino.first} and ${hiddenDomino.second}!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Verify with your hidden domino: [${hiddenDomino.first} | ${hiddenDomino.second}]!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { stage = 1 }) {
                Text("Test Another Domino")
            }
        }
    }
}

// ==========================================
// 23. SECRET ANIMAL GUESS (Binary Decision Tree)
// ==========================================
@Composable
fun SecretAnimalGuessScreen(onComplete: () -> Unit) {
    var rootNode by remember { mutableStateOf<GameEngine.DecisionNode>(GameEngine.getInitialAnimalTree()) }
    var currentNode by remember { mutableStateOf<GameEngine.DecisionNode>(rootNode) }
    var animalNameInput by remember { mutableStateOf("") }
    var questionInput by remember { mutableStateOf("") }
    var questionAnswerForNew by remember { mutableStateOf(true) } // True = Yes, False = No
    var teachMode by remember { mutableStateOf(false) }
    var prevGuessNodeName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (!teachMode) {
            when (val node = currentNode) {
                is GameEngine.DecisionNode.Question -> {
                    Text(node.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = { currentNode = node.yesNode }) {
                            Text("YES")
                        }
                        Button(onClick = { currentNode = node.noNode }) {
                            Text("NO")
                        }
                    }
                }
                is GameEngine.DecisionNode.Guess -> {
                    Text("Is it a ${node.animalName}?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = {
                            // AI won!
                            currentNode = rootNode // Reset
                            onComplete()
                        }) {
                            Text("YES, that's correct!")
                        }
                        Button(onClick = {
                            // Teach AI mode
                            prevGuessNodeName = node.animalName
                            teachMode = true
                        }) {
                            Text("NO, wrong guess")
                        }
                    }
                }
            }
        } else {
            Text("Teach the Wizard your animal!", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = animalNameInput,
                onValueChange = { animalNameInput = it },
                label = { Text("What animal were you thinking of?") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = questionInput,
                onValueChange = { questionInput = it },
                label = { Text("Write a Yes/No question to distinguish it from a $prevGuessNodeName:") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Answer for $animalNameInput:")
                Spacer(modifier = Modifier.width(8.dp))
                RadioButton(selected = questionAnswerForNew, onClick = { questionAnswerForNew = true })
                Text("YES")
                Spacer(modifier = Modifier.width(8.dp))
                RadioButton(selected = !questionAnswerForNew, onClick = { questionAnswerForNew = false })
                Text("NO")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (animalNameInput.isNotEmpty() && questionInput.isNotEmpty()) {
                        // Reconstruct decision tree node
                        val newGuess = GameEngine.DecisionNode.Guess(animalNameInput)
                        val oldGuess = GameEngine.DecisionNode.Guess(prevGuessNodeName)

                        val yes = if (questionAnswerForNew) newGuess else oldGuess
                        val no = if (questionAnswerForNew) oldGuess else newGuess

                        val newQuestionNode = GameEngine.DecisionNode.Question(
                            text = questionInput,
                            yesNode = yes,
                            noNode = no
                        )

                        // Traverse the tree from root and replace the oldGuess node with newQuestionNode
                        fun replaceNode(node: GameEngine.DecisionNode): GameEngine.DecisionNode {
                            return when (node) {
                                is GameEngine.DecisionNode.Question -> {
                                    node.copy(
                                        yesNode = replaceNode(node.yesNode),
                                        noNode = replaceNode(node.noNode)
                                    )
                                }
                                is GameEngine.DecisionNode.Guess -> {
                                    if (node.animalName == prevGuessNodeName) newQuestionNode else node
                                }
                            }
                        }

                        rootNode = replaceNode(rootNode)
                        currentNode = rootNode
                        animalNameInput = ""
                        questionInput = ""
                        teachMode = false
                        onComplete()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Teach Wizard")
            }
        }
    }
}
