package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.domain.model.GameRegistry
import com.example.presentation.games.GameContainer
import com.example.presentation.home.HomeScreen
import com.example.presentation.settings.SettingsScreen
import com.example.presentation.stats.StatsScreen
import com.example.presentation.viewmodel.HomeViewModel
import com.example.presentation.viewmodel.HomeViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MathMagicApp()
            }
        }
    }
}

@Composable
fun MathMagicApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as MathMagicApplication
    
    // Resolve ViewModel with our custom Repository Factory
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(app.repository)
    )

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onGameClick = { gameId ->
                    navController.navigate("game/$gameId")
                },
                onStatsClick = {
                    navController.navigate("stats")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }

        composable(
            route = "game/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            val gameDef = GameRegistry.getGameById(gameId)
            if (gameDef != null) {
                GameContainer(
                    gameDef = gameDef,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    homeViewModel = viewModel,
                    onNavigateToGame = { targetGameId ->
                        navController.navigate("game/$targetGameId") {
                            popUpTo("home")
                        }
                    }
                )
            }
        }

        composable("stats") {
            StatsScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
