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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Team2PlayerSelectionPage() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val dbHelper = CricketDatabaseHelper(context)

    val matchId = dbHelper.getMatchId()

    val team1CaptainName = Player(dbHelper.getCaptainForTeam(matchId,1))
    val team2CaptainName = Player(dbHelper.getCaptainForTeam(matchId,2))

    val playersList = remember { mutableStateListOf<Player>() }
    playersList.clear()
    playersList.addAll(dbHelper.getAllPlayers())

    // Get players already selected for Team 1 from the DB
    val initialTeam2Players = dbHelper.getTeamPlayers(matchId, 2,0)

    // State to keep track of selected players
    val selectedPlayers = remember { mutableStateListOf<Player>().apply { addAll(initialTeam2Players) } }

    val team1PlayersDB = dbHelper.getTeamPlayers(matchId, 1,0)
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

        //Text(text = "Mid Bowlers",modifier = Modifier.align(Alignment.End))

        val filteredPlayers = playersList.filter { it != team1Captain && it !in team1PlayersDB}

        // Player Selection for Team 2
        LazyColumn(
            modifier = Modifier.fillMaxHeight(0.8f)
        ) {
            items(filteredPlayers.size) { index ->
                val player = filteredPlayers[index]
                val isSelected = selectedPlayers.contains(player) || (player == team2Captain)

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
                                    dbHelper.addTeamPlayer(matchId,2,player.name,0,0)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "You can only select 5 players",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else if (player.name != team2CaptainName.name) {
                                selectedPlayers.remove(player)
                                dbHelper.removeTeamPlayer(matchId,2,player.name)
                            }
                        }
                    )

                    Text(player.name, fontSize = if (isTablet) 32.sp else 20.sp, modifier = Modifier.padding(start = 8.dp))

                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}