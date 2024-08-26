package com.example.cricketscoringapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMatchPage(navController: NavHostController, captainViewModel: CaptainViewModel) {
    val context = LocalContext.current
    val dbHelper = CricketDatabaseHelper(context)
    val playersList = remember { mutableStateListOf<Player>() }

    playersList.clear()
    playersList.addAll(dbHelper.getAllPlayers())

    var team1Captain by remember { mutableStateOf<Player?>(null) }
    var team2Captain by remember { mutableStateOf<Player?>(null) }

    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "New Match", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Team 1 Captain Dropdown
        Text(text = "Select Team 1 Captain")
        ExposedDropdownMenuBox(
            expanded = expanded1,
            onExpandedChange = { expanded1 = !expanded1 }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = team1Captain?.name ?: "Select Captain",
                onValueChange = { },
                label = { Text("Team 1 Captain") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded1,
                onDismissRequest = { expanded1 = false }
            ) {
                playersList.forEach { player ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(text = player.name) },
                        onClick = {
                            team1Captain = player
                            team1Captain?.let { captain ->
                                captainViewModel.addCaptain(captain)
                            }
                            expanded1 = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Team 2 Captain Dropdown
        Text(text = "Select Team 2 Captain")
        ExposedDropdownMenuBox(
            expanded = expanded2,
            onExpandedChange = { expanded2 = !expanded2 }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = team2Captain?.name ?: "Select Captain",
                onValueChange = { },
                label = { Text("Team 2 Captain") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded2,
                onDismissRequest = { expanded2 = false }
            ) {
                val remainingPlayers = playersList.filter { it != team1Captain }
                remainingPlayers.forEach { player ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(text = player.name) },
                        onClick = {
                            team2Captain = player
                            team2Captain?.let { captain ->
                                captainViewModel.addCaptain(captain)
                            }
                            expanded2 = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons to navigate to team player selection pages
        if (team1Captain != null) {
            Button(
                onClick = {
                    navController.navigate("team1PlayerSelection")
                }
            ) {
                Text(text = "Select Team 1 Players")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (team2Captain != null) {
            Button(
                onClick = {
                    navController.navigate("team2PlayerSelection")
                }
            ) {
                Text(text = "Select Team 2 Players")
            }
        }
    }
}