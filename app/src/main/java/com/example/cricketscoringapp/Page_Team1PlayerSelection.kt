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

@Composable
fun Team1PlayerSelectionPage(
    //captainViewModel: CaptainViewModel
) {
    val context = LocalContext.current
    val dbHelper = CricketDatabaseHelper(context)
    val matchId = dbHelper.getMatchId()

    val team1CaptainName = Player(dbHelper.getCaptainForTeam(matchId, 1))
    val team2CaptainName = Player(dbHelper.getCaptainForTeam(matchId, 2))

    val playersList = remember { mutableStateListOf<Player>() }
    playersList.clear()
    playersList.addAll(dbHelper.getAllPlayers())

    // Get players already selected for Team 1 from the DB
    val initialTeam1Players = dbHelper.getTeamPlayers(matchId, 1,0)

    // State to keep track of selected players
    val selectedPlayers = remember { mutableStateListOf<Player>().apply { addAll(initialTeam1Players) } }

    val team2PlayersDB = dbHelper.getTeamPlayers(matchId, 2,0)
    val team1Captain = playersList.find { it.name == team1CaptainName.name }
    val team2Captain = playersList.find { it.name == team2CaptainName.name }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Select Players for ${team1Captain?.name}", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        val filteredPlayers = playersList.filter { it != team1Captain && it != team2Captain && it !in team2PlayersDB }

        // Player Selection for Team 1
        LazyColumn(
            modifier = Modifier.fillMaxHeight(0.8f)
        ) {
            items(filteredPlayers.size) { index ->
                val player = filteredPlayers[index]
                val isSelected = selectedPlayers.contains(player)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (selectedPlayers.size < 5) {  // Assuming a limit of 5 players for the team
                                    selectedPlayers.add(player)
                                    //captainViewModel.addTeam1Player(player)
                                    dbHelper.addTeamPlayer(matchId, 1, player.name, 0)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "You can only select 5 players",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                selectedPlayers.remove(player)
                                //captainViewModel.removeTeam1Player(player)
                                dbHelper.removeTeamPlayer(matchId, 1, player.name)
                            }
                        }
                    )
                    Text(text = player.name)
                }
            }
        }
    }
}