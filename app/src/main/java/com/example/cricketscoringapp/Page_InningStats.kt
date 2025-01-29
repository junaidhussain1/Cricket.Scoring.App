package com.example.cricketscoringapp

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
fun InningStatsPage(matchId: String, teamIdA: Int, teamIdB: Int) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(10, 18, 32)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (teamIdA != 0) {
                item {
                    TeamStatsSection(matchId = matchId, pTeamId = teamIdA, context = context)
                    Spacer(modifier = Modifier.height(16.dp)) // Add spacing between sections
                }
            }

            if (teamIdB != 0) {
                item {
                    TeamStatsSection(matchId = matchId, pTeamId = teamIdB, context = context)
                }
            }
        }
    }
}

@Composable
fun TeamStatsSection (matchId: String, pTeamId: Int, context: Context) {
    val dbHelper = CricketDatabaseHelper(context)
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val teamCaptain = dbHelper.getBattingTeamCaptain(matchId, pTeamId)
    val teamId = dbHelper.getTeamForPlayer(matchId,teamCaptain)
    val battersList = remember { mutableStateListOf<BatsmanStats>() }
    val bowlersList = remember { mutableStateListOf<BowlerStats>() }
    var totalByes = 0
    var totalLegByes = 0
    var totalWides = 0
    var totalNoBalls = 0

    battersList.clear()
    battersList.addAll(dbHelper.getTeamBattingStats(matchId, teamId))

    bowlersList.clear()
    bowlersList.addAll(dbHelper.getTeamBowlingStats(matchId, (if (teamId == 1) 2 else 1)))

    val teamStats = dbHelper.getTeamStats(matchId,teamId,teamCaptain)
    val battingTeamStats = remember(teamStats) {
        TeamStats(
            name = mutableStateOf(teamStats.name.value),
            overs = mutableDoubleStateOf(teamStats.overs.value),
            inningScore = mutableIntStateOf(teamStats.inningScore.value),
            inningWickets = mutableIntStateOf(teamStats.inningWickets.value),
            active = mutableStateOf(teamStats.active.value)
        )
    }

    val teamScore = String.format(
                Locale.UK,
                "%d",
                battingTeamStats.inningScore.value
            ) + "/" +
            String.format(
                Locale.UK,
                "%d",
                battingTeamStats.inningWickets.value
            )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Team $teamCaptain ($teamScore)",
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

            BatsmanFullStats(
                batsman = "Batsman",
                wicketDescription = "Status",
                runs = "R",
                balls = "B",
                fours = "4s",
                sixes = "6s",
                dotballs = "DB",
                strikeRate = "S/R",
                fontBold1 = FontWeight.Bold,
                fontColor1 = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // List of batters
            battersList.forEach { player ->
                val strikeRate = if (player.balls.value == 0) {
                    0.0
                } else {
                    player.runs.value.toDouble() / player.balls.value.toDouble() * 100
                }

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
                    dotballs = String.format(
                        Locale.UK,
                        "%d",
                        player.dotballs.value
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

            BowlerFullStats(
                bowler = "Bowler",
                totalOvers = "O",
                totalMaidens = "M",
                totalRuns = "R",
                totalWickets = "Wk",
                totalNoBalls = "NB",
                totalWides = "WD",
                totalFours = "4s",
                totalSixes = "6s",
                totalDotBalls = "DB",
                totalEconomy = "Ec",
                fontBold1 = FontWeight.Bold,
                fontColor1 = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // List of bowlers
            bowlersList.forEach { player ->
                val economy = if (player.over.value == 0.0) {
                    0.0
                } else {
                    player.runs.value / player.over.value
                }

                totalByes += player.byes.value
                totalLegByes += player.legbyes.value
                totalWides += player.wides.value
                totalNoBalls += player.noballs.value
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
                    totalDotBalls = String.format(
                        Locale.UK,
                        "%d",
                        player.dotballs.value
                    ),
                    totalEconomy = String.format(
                        Locale.UK,
                        "%.2f",
                        economy
                    ),
                    fontBold1 = FontWeight.Normal, // Changed to normal for list items
                    fontColor1 = Color.White
                )
            }

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)  // Optional padding
            ) {
                Text(
                    text = "Wides",
                    fontWeight = FontWeight.Normal,
                    fontSize = if (isTablet) 16.sp else 10.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f) // This pushes the next Text to the right
                )
                Text(
                    text = totalWides.toString(),
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
                    text = "No Balls",
                    fontWeight = FontWeight.Normal,
                    fontSize = if (isTablet) 16.sp else 10.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f) // This pushes the next Text to the right
                )
                Text(
                    text = totalNoBalls.toString(),
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