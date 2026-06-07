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
import com.example.cricketscoringapp.ui.theme.CricketAppTheme
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
        color = CricketAppTheme.colors.primaryDark
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
                fontSize = CricketAppTheme.dimens.titleSize,
                color = CricketAppTheme.colors.textOnDark
            )

            Image(
                painter = painterResource(id = R.drawable.designer2), // Replace with your app icon resource
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(CricketAppTheme.dimens.imageSizeLarge)
                    .padding(8.dp)
            )

            // Import Players button
            Button(
                onClick = { playersLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CricketAppTheme.colors.primaryCream
                )
            ) {
                Text(
                    "Import Players",
                    fontSize = CricketAppTheme.dimens.buttonTextSize,
                    color = CricketAppTheme.colors.textOnLight
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Import Matches button
            Button(
                onClick = { matchesLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CricketAppTheme.colors.primaryCream
                )
            ) {
                Text(
                    "Import Matches",
                    fontSize = CricketAppTheme.dimens.buttonTextSize,
                    color = CricketAppTheme.colors.textOnLight
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Import Teams button
            Button(
                onClick = { teamsLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CricketAppTheme.colors.primaryCream
                )
            ) {
                Text(
                    "Import Teams",
                    fontSize = CricketAppTheme.dimens.buttonTextSize,
                    color = CricketAppTheme.colors.textOnLight
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Import Bowling Stats button
            Button(
                onClick = { bowlingLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CricketAppTheme.colors.primaryCream
                )
            ) {
                Text(
                    "Import Bowling Stats",
                    fontSize = CricketAppTheme.dimens.buttonTextSize,
                    color = CricketAppTheme.colors.textOnLight
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Import Batting Stats button
            Button(
                onClick = { battingLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CricketAppTheme.colors.primaryCream
                )
            ) {
                Text(
                    "Import Batting Stats",
                    fontSize = CricketAppTheme.dimens.buttonTextSize,
                    color = CricketAppTheme.colors.textOnLight
                )
            }
        }
    }
}

