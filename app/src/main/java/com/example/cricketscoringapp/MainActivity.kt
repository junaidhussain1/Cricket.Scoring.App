package com.example.cricketscoringapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cricketscoringapp.ui.theme.CricketScoringAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") { AuthScreen(navController = navController) }
    }
}

@Composable
fun AuthScreen(navController: NavHostController) {
    val context = LocalContext.current
    var authCode by remember { mutableStateOf("") }
    val googleSheetsService = GoogleSheetsService(context)

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
                    //Toast.makeText(context, "Auth Code Submitted: $authCode", Toast.LENGTH_SHORT).show()

                    // Exchange the auth code for tokens and navigate to the main page
                    googleSheetsService.exchangeAuthorizationCodeForTokens(authCode)

                } else {
                    Toast.makeText(context, "Authorization code cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text("Submit Authorization Code")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            //val data = googleSheetsService.readData()
            CoroutineScope(Dispatchers.Main).launch {
                val data = googleSheetsService.readData()  // readData() is now a suspend function

                // After fetching the data, show a Toast message with the size of the list
                val listSize = data.size
                val values = data.joinToString(separator = "\n") { it.joinToString(", ") }

                Toast.makeText(
                    context, // Replace with your actual context
                    "List size: $listSize\nValues:\n$values",
                    Toast.LENGTH_LONG
                ).show()
            }
        }) {
            Text(text = "Get Data from Google Sheet")
        }
    }
}