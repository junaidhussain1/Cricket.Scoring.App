package com.example.cricketscoringapp

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun ImportDataPage() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    // Check screen size and adjust the layout accordingly
    val isTablet = configuration.screenWidthDp >= 600

    val dbHelper = CricketDatabaseHelper(context)

    // Create separate launchers for each import type
    val matchesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { dbHelper.importCSVToDatabase(it, context, "matches") }
    }

    val playersLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { dbHelper.importCSVToDatabase(it, context, "players") }
    }

    val teamsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { dbHelper.importCSVToDatabase(it, context, "teams") }
    }

    val bowlingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { dbHelper.importCSVToDatabase(it, context, "bowling") }
    }

    val battingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { dbHelper.importCSVToDatabase(it, context, "batting") }
    }

    Surface(
        modifier = Modifier.fillMaxSize(), // This makes the Surface fill the entire screen
        color = Color(10, 18, 32)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isTablet) 32.dp else 16.dp), // Increase padding for tablets
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "The Gotham City Scoresheet App",
                fontSize = if (isTablet) 32.sp else 20.sp,// Larger font size for tablets
                color = Color(255, 252, 228)
            )

            Image(
                painter = painterResource(id = R.drawable.designer2), // Replace with your app icon resource
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(if (isTablet) 400.dp else 300.dp) // Larger image size for tablets
                    .padding(8.dp)
            )

            // Import Players button
            Button(
                onClick = { playersLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(255, 252, 228)
                )
            ) {
                Text(
                    "Import Players",
                    fontSize = if (isTablet) 26.sp else 22.sp,
                    color = Color(10, 18, 32)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Import Matches button
            Button(
                onClick = { matchesLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(255, 252, 228)
                )
            ) {
                Text(
                    "Import Matches",
                    fontSize = if (isTablet) 26.sp else 22.sp,
                    color = Color(10, 18, 32)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Import Teams button
            Button(
                onClick = { teamsLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(255, 252, 228)
                )
            ) {
                Text(
                    "Import Teams",
                    fontSize = if (isTablet) 26.sp else 22.sp,
                    color = Color(10, 18, 32)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Import Bowling Stats button
            Button(
                onClick = { bowlingLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(255, 252, 228)
                )
            ) {
                Text(
                    "Import Bowling Stats",
                    fontSize = if (isTablet) 26.sp else 22.sp,
                    color = Color(10, 18, 32)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Import Batting Stats button
            Button(
                onClick = { battingLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(255, 252, 228)
                )
            ) {
                Text(
                    "Import Batting Stats",
                    fontSize = if (isTablet) 26.sp else 22.sp,
                    color = Color(10, 18, 32)
                )
            }
        }
    }
}

