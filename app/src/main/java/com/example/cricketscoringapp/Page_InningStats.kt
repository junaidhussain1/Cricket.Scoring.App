package com.example.cricketscoringapp

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.LaunchedEffect

@Composable
fun InningStatsPage(matchId: String, teamIdA: Int, teamIdB: Int) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val dbHelper = CricketDatabaseHelper(context)

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var teamACaptain by remember { mutableStateOf("") }
    var teamBCaptain by remember { mutableStateOf("") }

    LaunchedEffect(matchId, teamIdA, teamIdB) {
        if (teamIdA != 0) {
            teamACaptain = dbHelper.getBattingTeamCaptain(matchId, teamIdA)
        }
        if (teamIdB != 0) {
            teamBCaptain = dbHelper.getBattingTeamCaptain(matchId, teamIdB)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(10, 18, 32)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Innings Statistics",
                fontSize = 22.sp,
                color = Color(255, 252, 228),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Tab Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (teamIdA != 0) {
                    Button(
                        onClick = { selectedTabIndex = 0 },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTabIndex == 0) Color(0xFF00E676) else Color(0xFF424242)
                        )
                    ) {
                        Text(
                            text = if (teamACaptain.isNotEmpty()) "Team $teamACaptain" else "Team A",
                            fontSize = if (isTablet) 16.sp else 12.sp,
                            textAlign = TextAlign.Center,
                            color = if (selectedTabIndex == 0) Color.Black else Color.White
                        )
                    }
                }

                if (teamIdB != 0) {
                    Button(
                        onClick = { selectedTabIndex = 1 },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTabIndex == 1) Color(0xFF00E676) else Color(0xFF424242)
                        )
                    ) {
                        Text(
                            text = if (teamBCaptain.isNotEmpty()) "Team $teamBCaptain" else "Team B",
                            fontSize = if (isTablet) 16.sp else 12.sp,
                            textAlign = TextAlign.Center,
                            color = if (selectedTabIndex == 1) Color.Black else Color.White
                        )
                    }
                }

                // Match Runworm Tab
                Button(
                    onClick = { selectedTabIndex = 2 },
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTabIndex == 2) Color(0xFF00E676) else Color(0xFF424242)
                    )
                ) {
                    Text(
                        text = "Match Runworm",
                        fontSize = if (isTablet) 16.sp else 12.sp,
                        textAlign = TextAlign.Center,
                        color = if (selectedTabIndex == 2) Color.Black else Color.White
                    )
                }
            }

            // Display content based on selected tab
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        if (teamIdA != 0) {
                            item {
                                TeamStatsSection(matchId = matchId, pTeamId = teamIdA, context = context)
                            }
                        }
                    }
                    1 -> {
                        if (teamIdB != 0) {
                            item {
                                TeamStatsSection(matchId = matchId, pTeamId = teamIdB, context = context)
                            }
                        }
                    }
                    2 -> {
                        item {
                            MatchRunwormSection(matchId = matchId, context = context)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchRunwormSection(matchId: String, context: Context) {
    val dbHelper = CricketDatabaseHelper(context)

    Text(
        text = "Match Runworm",
        style = MaterialTheme.typography.headlineLarge,
        color = Color(255, 252, 228),
        modifier = Modifier.padding(bottom = 8.dp)
    )

    val teamCaptain1 = dbHelper.getFirstBattingTeamCaptain(matchId)
    val teamCaptain2 = dbHelper.getSecondBattingTeamCaptain(matchId)
    val teamIdBB = dbHelper.getTeamForPlayer(matchId, teamCaptain1)
    val teamIdAA = dbHelper.getTeamForPlayer(matchId, teamCaptain2)
    val bowlersList1 = remember { mutableStateListOf<BowlerStats>() }
    val bowlersList2 = remember { mutableStateListOf<BowlerStats>() }

    var ballNumber = 0
    var ballValueInt = 0
    var wicketCount1 = 0

    val entries1: MutableList<Entry> = mutableListOf()
    val entries2: MutableList<Entry> = mutableListOf()
    val wicketEntries1 = mutableListOf<Entry>()
    val wicketEntries2 = mutableListOf<Entry>()

    bowlersList1.clear()
    bowlersList1.addAll(dbHelper.getBowlingStats(matchId, teamIdAA))

    bowlersList1.forEach { player ->
        val overBallsValues = player.overrecord.value.split("|")

        overBallsValues.forEach { ballValue ->
            ballNumber += 1
            ballValueInt += getBallValueInt(ballValue)

            // Runworm entry
            entries1.add(
                Entry(ballNumber.toFloat(), ballValueInt.toFloat())
            )

            if (ballValue.contains("WK")) {
                wicketCount1++
                wicketEntries1.add(
                    Entry(
                        ballNumber.toFloat(),
                        ballValueInt.toFloat(),
                        wicketCount1
                    )
                )
            }
        }
    }

    ballNumber = 0
    ballValueInt = 0
    var wicketCount2 = 0
    bowlersList2.clear()
    bowlersList2.addAll(dbHelper.getBowlingStats(matchId, teamIdBB))
    bowlersList2.forEach { player ->
        val overBallsValues = player.overrecord.value.split("|")

        overBallsValues.forEach { ballValue ->
            ballNumber += 1
            ballValueInt += getBallValueInt(ballValue)

            entries2.add(
                Entry(ballNumber.toFloat(), ballValueInt.toFloat())
            )

            if (ballValue.contains("WK")) {
                wicketCount2++
                wicketEntries2.add(
                    Entry(
                        ballNumber.toFloat(),
                        ballValueInt.toFloat(),
                        wicketCount2
                    )
                )
            }
        }
    }

    Box {
        AndroidView(
            factory = { context ->
                LineChart(context).apply {
                    val dataSet1 = LineDataSet(entries1, "Team $teamCaptain1").apply {
                        color = android.graphics.Color.CYAN
                        lineWidth = 4f
                        setDrawValues(false)
                        setDrawCircles(false)
                    }
                    val dataSet2 = LineDataSet(entries2, "Team $teamCaptain2").apply {
                        color = android.graphics.Color.MAGENTA
                        lineWidth = 4f
                        setDrawValues(false)
                        setDrawCircles(false)
                    }

                    setExtraOffsets(36f, 32f, 16f, 64f)

                    legend.textSize = 20f
                    legend.formSize = 16f
                    legend.xEntrySpace = 16f
                    legend.yEntrySpace = 5f
                    legend.textColor = android.graphics.Color.WHITE
                    legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    legend.orientation = Legend.LegendOrientation.HORIZONTAL

                    // Wicket dots
                    val wicketSet1 = LineDataSet(wicketEntries1, "").apply {
                        setDrawCircles(true)
                        setDrawCircleHole(false)
                        circleRadius = 8f
                        setCircleColor(android.graphics.Color.RED)
                        color = android.graphics.Color.TRANSPARENT
                        lineWidth = 0f
                        setDrawValues(true)
                        valueFormatter = WicketValueFormatter()
                        valueTextColor = android.graphics.Color.WHITE
                        valueTextSize = 24f
                        label = ""
                    }

                    val wicketSet2 = LineDataSet(wicketEntries2, "").apply {
                        setDrawCircles(true)
                        setDrawCircleHole(false)
                        circleRadius = 8f
                        setCircleColor(android.graphics.Color.RED)
                        color = android.graphics.Color.TRANSPARENT
                        lineWidth = 0f
                        setDrawValues(true)
                        valueFormatter = WicketValueFormatter()
                        valueTextColor = android.graphics.Color.WHITE
                        valueTextSize = 24f
                        label = ""
                    }

                    val lineData = LineData(dataSet1, dataSet2, wicketSet1, wicketSet2)
                    data = lineData

                    description.isEnabled = false
                    setTouchEnabled(true)
                    setPinchZoom(true)
                    animateX(1000)

                    legend.textSize = 16f
                    legend.textColor = android.graphics.Color.WHITE

                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        axisMinimum = 0f
                        granularity = 1f
                        setDrawGridLines(false)
                        setDrawLabels(true)
                        textColor = android.graphics.Color.WHITE
                        axisLineWidth = 4f
                        textSize = 14f
                    }

                    axisRight.isEnabled = false

                    axisLeft.apply {
                        isEnabled = true
                        setDrawLabels(true)
                        setDrawGridLines(false)
                        textColor = android.graphics.Color.WHITE
                        textSize = 14f
                        axisMinimum = -10f
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(8.dp)
        )

        // Y-axis label
        Text(
            text = "RUNS",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .rotate(-90f)
                .padding(start = 12.dp)
        )

        // X-axis label
        Text(
            text = "BALLS",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp)
        )
    }
}
class WicketValueFormatter : ValueFormatter() {
    override fun getPointLabel(entry: Entry): String {
        return entry.data?.toString() ?: ""
    }
}

fun getBallValueInt(ballValue: String): Int {
    val firstValue = if (ballValue.contains(",")) ballValue.split(",")[0] else ballValue
    when {
        firstValue in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9") ->
            return firstValue.toInt()

        firstValue == "W" -> return 1
        firstValue == "W+1" -> return 2
        firstValue == "W+2" -> return 3
        firstValue == "W+3" -> return 4
        firstValue == "W+4" -> return 5
        firstValue == "W+5" -> return 6
        firstValue == "W+6" -> return 7

        firstValue == "NB" -> return 1
        firstValue == "NB+1" -> return 2
        firstValue == "NB+2" -> return 3
        firstValue == "NB+3" -> return 4
        firstValue == "NB+4" -> return 5
        firstValue == "NB+5" -> return 6
        firstValue == "NB+6" -> return 7

        firstValue.startsWith("WK") -> return -3
    }

    return 0
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