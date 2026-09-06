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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cricketscoringapp.ui.theme.CricketAppTheme

@Composable
fun ExistingMatchesPage(navController: NavHostController) {
    val context = LocalContext.current
    
    // Performance: Initialize dbHelper only once
    val dbHelper = remember { CricketDatabaseHelper(context) }

    // Performance: Cache the matches list so it doesn't re-query the DB on every recomposition
    val matches = remember { dbHelper.getMatches().filter { it.isFinished } }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CricketAppTheme.colors.primaryDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Existing Matches",
                fontSize = CricketAppTheme.dimens.headerSize,
                color = CricketAppTheme.colors.textOnDark
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(matches) { match ->
                    val team1Captain = match.team1CaptainName
                    val team2Captain = match.team2CaptainName
                    val matchDate = match.matchDate
                    
                    if (team1Captain.isNotEmpty() && team2Captain.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(5f)
                                    .padding(start = 30.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CricketAppTheme.colors.primaryCream
                                    ),
                                    onClick = {
                                        val matchId = match.matchId
                                        val teamIdA = dbHelper.getTeamForPlayer(matchId, match.firstBattingTeamCaptain)
                                        val teamIdB = dbHelper.getTeamForPlayer(matchId, match.secondBattingTeamCaptain)
                                        navController.navigate("inningstats/${matchId}/${teamIdA}/${teamIdB}")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Text(
                                            text = when {
                                                match.isStarted -> "$matchDate - $team1Captain vs $team2Captain (In Progress)"
                                                match.isFinished -> "$matchDate - $team1Captain vs $team2Captain"
                                                else -> "Match not started"
                                            },
                                            style = androidx.compose.ui.text.TextStyle(
                                                textAlign = TextAlign.Start,
                                                fontSize = CricketAppTheme.dimens.matchListSize,
                                                color = CricketAppTheme.colors.textOnLight
                                            )
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 30.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                IconButton(
                                    enabled = match.isFinished && !match.isSynced,
                                    onClick = {
                                        val (dataToWrite, _) = getMatchDataToUpload(context, match.matchId)
                                        saveAndShareCsv(context, "$matchDate - $team1Captain vs $team2Captain.csv", dataToWrite)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share CSV",
                                        tint = Color.Green,
                                        modifier = Modifier.size(CricketAppTheme.dimens.iconSize)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
