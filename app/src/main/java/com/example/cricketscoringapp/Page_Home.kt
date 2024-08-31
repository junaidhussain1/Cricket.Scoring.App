package com.example.cricketscoringapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun HomePage(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome to Cricket Scoring App",
            fontSize = 22.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.navigate("playermgt") },
            modifier = Modifier.fillMaxWidth()) {
            Text(text = "Player Management",
                fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.navigate("newmatch") },
            modifier = Modifier.fillMaxWidth()) {
            Text(text = "New Match",
                fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.navigate("existingmatches") },
            modifier = Modifier.fillMaxWidth()) {
            Text(text = "Existing Matches",
                fontSize = 18.sp)
        }
    }
}