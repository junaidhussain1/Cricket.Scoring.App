package com.example.cricketscoringapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun InningStatsPage(teamID: Int) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(10, 18, 32)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val dbHelper = CricketDatabaseHelper(context)
            val matchId = dbHelper.getMatchId()
            val battersList = remember { mutableStateListOf<BatsmanStats>() }

            //Show all players
            battersList.clear()
            battersList.addAll(dbHelper.getTeamBattingStats(matchId,teamID))

            // Display the list of players in a scrollable LazyColumn
            if (battersList.isNotEmpty()) {
                Text(
                    "Batting Stats",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(255, 252, 228)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(0.5f)
                        .weight(1f)
                )
                {
                    items(battersList) { player ->
                        //PlayerRow(player = player.name, onDelete = {
                        //    dbHelper.deletePlayer(player.name)
                        //    playersList.remove(player)
                        //})
                        Text(player.name.value)
                    }
                }
            } else {
                Text("No players found")
            }
        }
    }
}