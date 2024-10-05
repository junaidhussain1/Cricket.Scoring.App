package com.example.cricketscoringapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cricketscoringapp.ui.theme.CricketScoringAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the layout using Jetpack Compose
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
    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") { AuthScreen(navController = navController) }
        composable("homepage") { Homepage(navController = navController) }
        composable("newmatch") { NewMatchPage() }
    }
}

@Composable
fun AuthScreen(navController: NavHostController) {
    val context = LocalContext.current
    var authCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Button to trigger Google login
        Button(
            onClick = {
                val googleSheetsService = GoogleSheetsService(context)
                googleSheetsService.authorize() // Trigger Google login
            }
        ) {
            Text("Login with Google")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input field for the authentication code
        OutlinedTextField(
            value = authCode,
            onValueChange = { authCode = it },
            label = { Text("Enter Authorization Code") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Button to submit the authentication code
        Button(
            onClick = {
                if (authCode.isNotEmpty()) {
                    Toast.makeText(context, "Auth Code Submitted: $authCode", Toast.LENGTH_SHORT).show()

                    // Exchange the auth code for tokens and navigate to the main page
                    val googleSheetsService = GoogleSheetsService(context)
                    googleSheetsService.exchangeAuthorizationCodeForTokens(authCode)

                    // Navigate to the homepage
                    navController.navigate("homepage") {
                        popUpTo("auth") { inclusive = true }
                    }
                } else {
                    Toast.makeText(context, "Authorization code cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text("Submit Authorization Code")
        }
    }
}

@Composable
fun Homepage(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome to Cricket Scoring App")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("newmatch") }) {
            Text(text = "New Match")
        }
    }
}

@Composable
fun NewMatchPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Your new match page content here...
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MainScreenContent()
}