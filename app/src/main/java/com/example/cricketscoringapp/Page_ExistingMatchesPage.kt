package com.example.cricketscoringapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExistingMatchesPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Existing Matches",
            fontSize = 22.sp)

        Spacer(modifier = Modifier.height(20.dp))
        val context = LocalContext.current
        val dbHelper = CricketDatabaseHelper(context)

        val matches = dbHelper.getMatches()
        for (match in matches) {
            val team1Captain = Player(dbHelper.getCaptainForTeam(match.matchId, 1)).name
            val team2Captain = Player(dbHelper.getCaptainForTeam(match.matchId, 2)).name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp), // Adds some padding between rows
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier
                        .weight(1f)
                        .padding(start = 30.dp),
                        horizontalAlignment = Alignment.Start
                )
                {

                    Text(
                        text = "$team1Captain vs $team2Captain",
                        //modifier = Modifier.fillMaxWidth(), // Ensures the text fills the width of the parent container
                        style = androidx.compose.ui.text.TextStyle(
                            textAlign = TextAlign.Start,
                            fontSize = 18.sp
                        )
                    )
                }

                Column(modifier = Modifier
                    .weight(1f)
                    .padding(end = 30.dp),
                    horizontalAlignment = Alignment.End
                )
                {

                // Synchronization Icon
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync Status",
                    tint = if (match.isSynced) Color.Green else Color.Gray
                )
                    }
            }
        }
    }
}