package com.example.cricketscoringapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

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

            val teamCaptain = dbHelper.getBattingTeamCaptain(matchId,teamID)
            val battersList = remember { mutableStateListOf<BatsmanStats>() }
            val bowlersList = remember { mutableStateListOf<BowlerStats>() }
            var totalByes = 0
            var totalLegByes = 0

            battersList.clear()
            battersList.addAll(dbHelper.getTeamBattingStats(matchId, teamID))

            bowlersList.clear()
            bowlersList.addAll(dbHelper.getTeamBowlingStats(matchId, teamID))

            Text(
                "Team $teamCaptain",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(255, 252, 228)
            )

            if (battersList.isNotEmpty()) {
                Text(
                    "Batting Stats",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(255, 252, 228),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(0.5f)
                        .weight(1f)
                ) {
                    // First row for headers
                    item {
                        BatsmanFullStats(
                            batsman = "Batsman",
                            wicketDescription = "Status",
                            runs = "R",
                            balls = "B",
                            fours = "4s",
                            sixes = "6s",
                            strikeRate = "S/R",
                            fontBold1 = FontWeight.Bold,
                            fontColor1 = Color.Gray
                        )
                    }

                    // List of batters
                    items(battersList) { player ->
                        val strikeRate = if (player.balls.value == 0) {
                            0.0
                        } else {
                            player.runs.value.toDouble() / player.balls.value.toDouble() * 100
                        }
0
                        BatsmanFullStats(
                            batsman = player.name.value,
                            wicketDescription = player.wicketDescription.value,
                            runs = String.format(
                                Locale.UK,
                                "%d",
                                player.runs.value
                            ),
                            balls = String.format(
                                Locale.UK,
                                "%d",
                                player.balls.value // Fixed runs -> balls
                            ),
                            fours = String.format(
                                Locale.UK,
                                "%d",
                                player.fours.value
                            ),
                            sixes = String.format(
                                Locale.UK,
                                "%d",
                                player.sixes.value
                            ),
                            strikeRate = String.format(
                                Locale.UK,
                                "%.2f",
                                strikeRate
                            ),
                            fontBold1 = FontWeight.Normal, // Changed to normal for list items
                            fontColor1 = Color.White
                        )
                    }
                }
            }

            if (bowlersList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Bowling Stats",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(255, 252, 228),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(0.5f)
                        .weight(1f)
                ) {
                    // First row for headers
                    item {
                        BowlerFullStats(
                            bowler = "Bowler",
                            totalOvers = "O",
                            totalMaidens = "M",
                            totalRuns = "R",
                            totalWickets = "WI",
                            totalNoBalls = "NB",
                            totalWides = "WD",
                            totalFours = "4s",
                            totalSixes = "6s",
                            fontBold1 = FontWeight.Bold,
                            fontColor1 = Color.Gray
                        )
                    }

                    // List of bowlers
                    items(bowlersList) { player ->
                        totalByes += player.byes.value
                        totalLegByes += player.legbyes.value
                        BowlerFullStats(
                            bowler = player.name.value,
                            totalOvers = String.format(
                                Locale.UK,
                                "%.2f",
                                player.over.value
                            ),
                            totalMaidens = String.format(
                                Locale.UK,
                                "%d",
                                player.maiden.value
                            ),
                            totalRuns = String.format(
                                Locale.UK,
                                "%d",
                                player.runs.value
                            ),
                            totalWickets = String.format(
                                Locale.UK,
                                "%d",
                                player.wickets.value
                            ),
                            totalNoBalls = String.format(
                                Locale.UK,
                                "%d",
                                player.noballs.value
                            ),
                            totalWides = String.format(
                                Locale.UK,
                                "%d",
                                player.wides.value
                            ),
                            totalFours = String.format(
                                Locale.UK,
                                "%d",
                                player.fours.value
                            ),
                            totalSixes = String.format(
                                Locale.UK,
                                "%d",
                                player.sixes.value
                            ),
                            fontBold1 = FontWeight.Normal, // Changed to normal for list items
                            fontColor1 = Color.White
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Extras Given",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(255, 252, 228)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(2.dp)  // Optional padding
                        ) {
                            Text(
                                text = "Byes",
                                fontWeight = FontWeight.Normal,
                                fontSize = if (isTablet) 16.sp else 10.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f) // This pushes the next Text to the right
                            )
                            Text(
                                text = totalByes.toString(),
                                fontWeight = FontWeight.Normal,
                                fontSize = if (isTablet) 16.sp else 10.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.CenterVertically) // Aligns text vertically center
                                    .wrapContentWidth(Alignment.Start) // Centers text horizontally
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(2.dp)  // Optional padding
                        ) {
                            Text(
                                text = "Legbyes",
                                fontWeight = FontWeight.Normal,
                                fontSize = if (isTablet) 16.sp else 10.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f) // This pushes the next Text to the right
                            )
                            Text(
                                text = totalLegByes.toString(),
                                fontWeight = FontWeight.Normal,
                                fontSize = if (isTablet) 16.sp else 10.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.CenterVertically) // Aligns text vertically center
                                    .wrapContentWidth(Alignment.Start) // Centers text horizontally
                            )
                        }
                    }
                }
            }
        }
    }
}