package com.example.cricketscoringapp

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun Team2PlayerSelectionPage(
    navController: NavHostController,
    captainViewModel: CaptainViewModel
) {
    val captains = captainViewModel.captains
    val team1CaptainName = captains.getOrElse(0) { Player("Team 1 Captain") }
    val team2CaptainName = captains.getOrElse(1) { Player("Team 2 Captain") }

    val context = LocalContext.current
    val dbHelper = CricketDatabaseHelper(context)
    val playersList = remember { mutableStateListOf<Player>() }
    playersList.clear()
    playersList.addAll(dbHelper.getAllPlayers())

    val team1Players = captainViewModel.team1
    val team2Players = remember { mutableStateListOf<Player>() }
    val team1Captain = playersList.find { it.name == team1CaptainName.name }
    val team2Captain = playersList.find { it.name == team2CaptainName.name }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Select Players for ${team2Captain?.name}", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        val filteredPlayers = playersList.filter { it != team1Captain && it != team2Captain && it !in team1Players}

        // Player Selection for Team 2
        LazyColumn(
            modifier = Modifier.fillMaxHeight(0.8f)
        ) {
            items(filteredPlayers.size) { index ->
                val player = filteredPlayers[index]
                val isSelected = team2Players.contains(player)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            if (isSelected) {
                                team2Players.remove(player)
                            } else {
                                if (team2Players.size < 5) {
                                    team2Players.add(player)
                                } else {
                                    Toast.makeText(context, "You can only select 5 players", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    Text(text = player.name)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigate back to the main page or to Team 2 Player Selection
        Button(
            onClick = {
                if (team2Players.size == 5) {
                    navController.navigate("newmatch")
                } else {
                    Toast.makeText(context, "Please select 5 players for Team 2", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text(text = "Confirm Team 2 Players")
        }
    }
}