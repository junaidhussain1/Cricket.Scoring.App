package com.example.cricketscoringapp

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController


@Composable
fun SettingsPage(navController: NavHostController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val dbHelper = CricketDatabaseHelper(context)

    // Check screen size and adjust the layout accordingly
    val isTablet = configuration.screenWidthDp >= 600

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
                painter = painterResource(id = R.drawable.updatedimage), // Replace with your app icon resource
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(if (isTablet) 400.dp else 300.dp) // Larger image size for tablets
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = { navController.navigate("googlesheetsettings") },
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f), // Adjust button width for tablets
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(255, 252, 228) // Set the background color
                )
            ) {
                Text(
                    text = "Google Sheet Settings",
                    fontSize = if (isTablet) 26.sp else 22.sp,
                    color = Color(10, 18, 32)// Larger font size for tablets
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

