package com.example.cricketscoringapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cricketscoringapp.ui.theme.CricketScoringAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainScreenContent()
        }
    }
}

@Composable
fun MainScreenContent() {
    CricketScoringAppTheme {
        val navController = rememberNavController()
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppNavHost(navController = navController)

            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
        NavHost(navController = navController, startDestination = "homepage") {
        composable("homepage") { HomePage(navController = navController) }
        composable("playermgt") { PlayerMgtPage() }
        composable("newmatch") { NewMatchPage(navController = navController) }
        composable("team1PlayerSelection") { Team1PlayerSelectionPage()}
        composable("team2PlayerSelection") { Team2PlayerSelectionPage()}
        composable("startnewmatch") { StartNewMatchPage() }
        composable("existingmatches") { ExistingMatchesPage() }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MainScreenContent()
}


