package com.example.cricketscoringapp

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMatchPage(navController: NavHostController, captainViewModel: CaptainViewModel) {
    val context = LocalContext.current
    val dbHelper = CricketDatabaseHelper(context)
    val matchId = dbHelper.getMatchId()
    val playersList = remember { mutableStateListOf<Player>() }

    playersList.clear()
    playersList.addAll(dbHelper.getAllPlayers())

    var team1Captain by remember { mutableStateOf<Player?>(null) }
    var team2Captain by remember { mutableStateOf<Player?>(null) }

    team1Captain = Player(dbHelper.getCaptain(matchId,1))
    team2Captain = Player(dbHelper.getCaptain(matchId,2))

    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Recycle Bin Button in the Top Right Corner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            IconButton(
                onClick = {
                    dbHelper.deleteMatch(matchId)
                    Toast.makeText(context, "Match Cleared", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Recycle Bin"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                                dbHelper.addTeamPlayer(matchId,1,player.name,1,0)
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
                                dbHelper.addTeamPlayer(matchId,2,player.name,1,0)
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
                Text(text = "Select Team ${team1Captain?.name} Players")
            }

            val team1Players = dbHelper.getTeamPlayers(matchId,1)
            for (player in team1Players) {
                Text(text = player.name)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (team2Captain != null) {
            Button(
                onClick = {
                    navController.navigate("team2PlayerSelection")
                }
            ) {
                Text(text = "Select Team ${team2Captain?.name} Players")
            }

            val team2Players = dbHelper.getTeamPlayers(matchId,2)
            for (player in team2Players) {
                Text(text = player.name)
            }
        }
    }
}

@Composable
fun ShowConfirmationDialog() {
    var showDialog by remember { mutableStateOf(false) }

    // Trigger to show the dialog
    Button(onClick = { showDialog = true }) {
        Text(text = "Show Confirmation")
    }

    // Confirmation Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false }, // Close the dialog when touched outside or back press
            title = { Text(text = "Confirm Action") },
            text = { Text(text = "Are you sure you want to delete this item?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Handle confirm action
                        showDialog = false
                        // Perform the deletion or any other action here
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Handle dismiss action
                        showDialog = false
                    }
                ) {
                    Text("No")
                }
            },
            properties = DialogProperties(dismissOnClickOutside = false)
        )
    }
}