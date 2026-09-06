package com.example.cricketscoringapp

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.cricketscoringapp.ui.theme.CricketAppTheme


@Composable
fun ScoreCardPage(navController: NavHostController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Performance: remember dbHelper and basic match values
    val dbHelper = remember { CricketDatabaseHelper(context) }
    val matchId = remember { dbHelper.getMatchId() }
    val noOfOversAside = remember(matchId) { dbHelper.getNoOfOversAside(matchId).toDouble() }
    val noOfPlayersAside = remember(matchId) { dbHelper.getNoOfPlayersAside(matchId) }
    
    var refreshKey by remember { mutableIntStateOf(0) }

    // Performance: remember current actors
    val currentBowlerName = remember(matchId, refreshKey) { dbHelper.getCurrentBowler(matchId) }
    val currentBowler = remember { mutableStateOf(currentBowlerName) }
    
    val bowlingTeamId = remember(matchId, currentBowler.value) { dbHelper.getTeamForPlayer(matchId, currentBowler.value) }
    val bowlingTeam = remember(matchId, bowlingTeamId) { dbHelper.getTeamPlayers(matchId, bowlingTeamId, 1) }
    
    val firstTeamId = remember(matchId, refreshKey) { dbHelper.getTeamForPlayer(matchId, dbHelper.getFirstBattingTeamStriker(matchId)) }
    val secondTeamId = if (firstTeamId == 1) 2 else 1

    val showWidesDialog = remember { mutableStateOf(false) }
    val selectedWidesOption = remember { mutableStateOf("") }
    val showNoBallDialog = remember { mutableStateOf(false) }
    val selectedNoBallOption = remember { mutableStateOf("") }
    val showExtrasDialog = remember { mutableStateOf(false) }
    val selectedExtrasOption = remember { mutableStateOf("") }
    val showWicketsDialog = remember { mutableStateOf(false) }
    val selectedWicketsOption = remember { mutableStateOf("") }
    val showNextBatsmanDialog = remember { mutableStateOf(false) }
    val showBatsmanRunOutSelectionDialog = remember { mutableStateOf(false) }
    val batsmanOverride = remember { mutableStateOf(false) }
    val showUndoConfirmationDialog = remember { mutableStateOf(false) }
    val showInningsConfirmationDialog = remember { mutableStateOf(false) }
    val showMoreDialog = remember { mutableStateOf(false) }

    val showBowlerChangeDialog = remember { mutableStateOf(false) }
    val showKeeperChangeDialog = remember { mutableStateOf(false) }
    val showFielderDialog = remember { mutableStateOf(false) }
    val showActiveBatsmanSelectionDialog = remember { mutableStateOf(false) }
    val selectedFielder = remember { mutableStateOf("") }
    val manualBowlerChange = remember { mutableStateOf(false) }

    val pendingStrikerName = remember { mutableStateOf("") }
    val pendingNonStrikerName = remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CricketAppTheme.colors.primaryDark
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Performance: remember captain names
            val team1CaptainData = remember(matchId) { Player(dbHelper.getCaptainForTeam(matchId, 1)) }
            val team2CaptainData = remember(matchId) { Player(dbHelper.getCaptainForTeam(matchId, 2)) }
            val firstBattingTeamCaptainData = remember(matchId) { Player(dbHelper.getBattingTeamCaptain(matchId, 1)) }
            
            var team1Captain = team1CaptainData
            var team2Captain = team2CaptainData
            val firstBattingTeamCaptain = firstBattingTeamCaptainData
            
            var team1Id = 1
            var team2Id = 2
            if (team1Captain != firstBattingTeamCaptain) {
                team2Captain = team1Captain
                team1Captain = firstBattingTeamCaptain
                team1Id = 2
                team2Id = 1
            }

            // Performance: Wrap heavy DB calls inside remember
            val firstBattingTeamStats = remember(matchId, team1Id, team1Captain.name, refreshKey) {
                val stats = dbHelper.getTeamStats(matchId, team1Id, team1Captain.name)
                TeamStats(
                    name = mutableStateOf(stats.name.value),
                    overs = mutableDoubleStateOf(stats.overs.value),
                    inningScore = mutableIntStateOf(stats.inningScore.value),
                    inningWickets = mutableIntStateOf(stats.inningWickets.value),
                    active = mutableStateOf(stats.active.value)
                )
            }

            val secondBattingTeamStats = remember(matchId, team2Id, team2Captain.name, refreshKey) {
                val stats = dbHelper.getTeamStats(matchId, team2Id, team2Captain.name)
                TeamStats(
                    name = mutableStateOf(stats.name.value),
                    overs = mutableDoubleStateOf(stats.overs.value),
                    inningScore = mutableIntStateOf(stats.inningScore.value),
                    inningWickets = mutableIntStateOf(stats.inningWickets.value),
                    active = mutableStateOf(stats.active.value)
                )
            }

            val (runsToWinLocal, winningCaptain) = remember(firstBattingTeamStats, secondBattingTeamStats, refreshKey) {
                calcRunsToWin(firstBattingTeamStats, secondBattingTeamStats, noOfOversAside)
            }
            val runsToWin = runsToWinLocal

            val firstBatsmanStats = remember(matchId, refreshKey) {
                val stats = dbHelper.getBatsmanByStatus(matchId, "striker")
                BatsmanStats(
                    name = mutableStateOf(value = stats.name.value),
                    runs = mutableIntStateOf(value = stats.runs.value),
                    balls = mutableIntStateOf(value = stats.balls.value),
                    fours = mutableIntStateOf(value = stats.fours.value),
                    sixes = mutableIntStateOf(value = stats.sixes.value),
                    dotballs = mutableIntStateOf(value = stats.dotballs.value),
                    wicketDescription = mutableStateOf(value = stats.wicketDescription.value),
                    active = mutableStateOf(value = stats.active.value)
                )
            }

            val secondBatsmanStats = remember(matchId, refreshKey) {
                val stats = dbHelper.getBatsmanByStatus(matchId, "non-striker")
                BatsmanStats(
                    name = mutableStateOf(value = stats.name.value),
                    runs = mutableIntStateOf(value = stats.runs.value),
                    balls = mutableIntStateOf(value = stats.balls.value),
                    fours = mutableIntStateOf(value = stats.fours.value),
                    sixes = mutableIntStateOf(value = stats.sixes.value),
                    dotballs = mutableIntStateOf(value = stats.dotballs.value),
                    wicketDescription = mutableStateOf(value = stats.wicketDescription.value),
                    active = mutableStateOf(value = stats.active.value)
                )
            }

            val currentOverBowlerStats = remember(matchId, refreshKey) {
                val stats = dbHelper.getCurrentBowlerStats(matchId)
                BowlerStats(
                    name = mutableStateOf(stats.name.value),
                    over = mutableDoubleStateOf(stats.over.value),
                    maiden = mutableIntStateOf(stats.maiden.value),
                    runs = mutableIntStateOf(stats.runs.value),
                    wickets = mutableIntStateOf(stats.wickets.value),
                    noballs = mutableIntStateOf(stats.noballs.value),
                    wides = mutableIntStateOf(stats.wides.value),
                    byes = mutableIntStateOf(stats.byes.value),
                    legbyes = mutableIntStateOf(stats.legbyes.value),
                    fours = mutableIntStateOf(stats.fours.value),
                    sixes = mutableIntStateOf(stats.sixes.value),
                    dotballs = mutableIntStateOf(stats.dotballs.value),
                    keepername = mutableStateOf(stats.keepername.value),
                    overrecord = mutableStateOf(stats.overrecord.value)
                )
            }

            val consolidatedBowlerStats = remember(matchId, currentOverBowlerStats.name.value, refreshKey) {
                val stats = dbHelper.getConsolidatedBowlerStats(matchId, currentOverBowlerStats.name.value)
                BowlerStats(
                    name = mutableStateOf(stats.name.value),
                    over = mutableDoubleStateOf(stats.over.value),
                    maiden = mutableIntStateOf(stats.maiden.value),
                    runs = mutableIntStateOf(stats.runs.value),
                    wickets = mutableIntStateOf(stats.wickets.value),
                    noballs = mutableIntStateOf(stats.noballs.value),
                    wides = mutableIntStateOf(stats.wides.value),
                    byes = mutableIntStateOf(stats.byes.value),
                    legbyes = mutableIntStateOf(stats.legbyes.value),
                    fours = mutableIntStateOf(stats.fours.value),
                    sixes = mutableIntStateOf(stats.sixes.value),
                    dotballs = mutableIntStateOf(stats.dotballs.value),
                    keepername = mutableStateOf(stats.keepername.value),
                    overrecord = mutableStateOf(stats.overrecord.value)
                )
            }

            val balls = remember(refreshKey) { mutableStateListOf<Ball>() }
            remember(currentOverBowlerStats.overrecord.value, refreshKey) {
                balls.clear()
                balls.addAll(buildBallsFromOverRecord(currentOverBowlerStats))
                true
            }

            // Automatically handle: End of Over, End of Innings, End of Match
            val team1wickets = remember(matchId, firstTeamId, refreshKey) { dbHelper.getTeamWickets(matchId, firstTeamId) }
            val team2wickets = remember(matchId, secondTeamId, refreshKey) { dbHelper.getTeamWickets(matchId, secondTeamId) }
            val team2batters = remember(matchId, secondTeamId, refreshKey) { dbHelper.getTeamBatters(matchId, secondTeamId) }
            val team1OversBowled = remember(matchId, secondTeamId, refreshKey) { dbHelper.getTeamOversBowled(matchId, secondTeamId) }
            val team2OversBowled = remember(matchId, firstTeamId, refreshKey) { dbHelper.getTeamOversBowled(matchId, firstTeamId) }

            if (dbHelper.getIsMatchStarted(matchId)) {
                if ((team1wickets == noOfPlayersAside * 2) and (team2batters == 0)) {
                    dbHelper.updateBowlingStats(matchId, "bowled")
                    handleLastBatsmen(context, matchId, firstBatsmanStats, secondBatsmanStats)
                    navController.navigate("secondinningssetup")
                    {
                        popUpTo("scorecard") { inclusive = true }
                    }
                } else if (team2wickets == noOfPlayersAside * 2) {
                    handleEndOfMatch(
                        context,
                        matchId,
                        firstBatsmanStats,
                        secondBatsmanStats,
                        runsToWin,
                        winningCaptain
                    )
                    navController.navigate("homepage")
                } else {
                    if (endOfOverReached(balls)) {
                        if ((team1OversBowled == noOfOversAside) && (team2OversBowled == 0.0)) {
                            dbHelper.updateBowlingStats(matchId, "bowled")
                            handleLastBatsmen(context, matchId, firstBatsmanStats, secondBatsmanStats)
                            navController.navigate("secondinningssetup")
                            {
                                popUpTo("scorecard") { inclusive = true }
                            }
                        } else if (team2OversBowled == noOfOversAside) {
                            handleEndOfMatch(
                                context,
                                matchId,
                                firstBatsmanStats,
                                secondBatsmanStats,
                                runsToWin,
                                winningCaptain
                            )
                            navController.navigate("homepage")
                        } else {
                            showBowlerChangeDialog.value = true
                        }
                    }
                }
            }

            //Full Score Card button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween, // 🔹 Text on left, Button on right
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = runsToWin,
                    fontSize = CricketAppTheme.dimens.microSize,
                    color = CricketAppTheme.colors.textOnDark
                )

                ActionSquareButton(
                    text = "Full Scorecard",
                    isTablet = isTablet
                ) {
                    val match = dbHelper.getMatch(matchId)
                    val teamIdA = dbHelper.getTeamForPlayer(
                        matchId,
                        match!!.firstBattingTeamCaptain
                    )
                    val teamIdB = dbHelper.getTeamForPlayer(
                        matchId,
                        match.secondBattingTeamCaptain
                    )
                    navController.navigate("inningstats/${matchId}/${teamIdA}/${teamIdB}")
                }
            }

            // Innings Score Box
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .border(
                        BorderStroke(2.dp, CricketAppTheme.colors.primaryCream),
                    )
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {

                    TeamScoreBox(
                        modifier = Modifier.weight(1f),
                        name1 = firstBattingTeamStats.name.value,
                        overs1 = String.format(
                            Locale.UK,
                            "%.1f",
                            firstBattingTeamStats.overs.value
                        ),
                        score1 = String.format(
                            Locale.UK,
                            "%d",
                            firstBattingTeamStats.inningScore.value
                        ) + "/" +
                                String.format(
                                    Locale.UK,
                                    "%d",
                                    firstBattingTeamStats.inningWickets.value
                                ),
                        color1 = if (firstBattingTeamStats.active.value) CricketAppTheme.colors.accentGreen else Color.Black
                    ) {
                    }
                    TeamScoreBox(
                        modifier = Modifier.weight(1f),
                        name1 = secondBattingTeamStats.name.value,
                        overs1 = String.format(
                            Locale.UK,
                            "%.1f",
                            secondBattingTeamStats.overs.value
                        ),
                        score1 = String.format(
                            Locale.UK,
                            "%d",
                            secondBattingTeamStats.inningScore.value
                        ) + "/" +
                                String.format(
                                    Locale.UK,
                                    "%d",
                                    secondBattingTeamStats.inningWickets.value
                                ),
                        color1 = if (firstBattingTeamStats.active.value) Color.Black else CricketAppTheme.colors.accentGreen
                    ) {
                    }
                }
            }

            // Batsman Box
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .border(
                        BorderStroke(2.dp, CricketAppTheme.colors.primaryCream),
                    )
                    .fillMaxWidth()
            ) {
                Column {
                    BatsmanBowlerKeeperBox(
                        col1 = "Batsman",
                        col2 = "R",
                        col3 = "B",
                        col4 = "4s",
                        col5 = "6s",
                        fontBold1 = FontWeight.Bold,
                        fontColor1 = CricketAppTheme.colors.secondaryGray,
                        makePlayerTouchable = false
                    ) {}

                    val fontColor2: Color
                    val fontColor3: Color
                    val fontBold2: FontWeight
                    val fontBold3: FontWeight

                    if (firstBatsmanStats.active.value) {
                        fontColor2 = CricketAppTheme.colors.accentGreen
                        fontBold2 = FontWeight.Bold
                        fontColor3 = Color.Black
                        fontBold3 = FontWeight.Normal
                    } else {
                        fontColor2 = Color.Black
                        fontBold2 = FontWeight.Normal
                        fontColor3 = CricketAppTheme.colors.accentGreen
                        fontBold3 = FontWeight.Bold
                    }
                    BatsmanBowlerKeeperBox(
                        col1 = firstBatsmanStats.name.value,
                        col2 = String.format(Locale.UK, "%d", firstBatsmanStats.runs.value),
                        col3 = String.format(Locale.UK, "%d", firstBatsmanStats.balls.value),
                        col4 = String.format(Locale.UK, "%d", firstBatsmanStats.fours.value),
                        col5 = String.format(Locale.UK, "%d", firstBatsmanStats.sixes.value),
                        fontBold1 = fontBold2,
                        fontColor1 = fontColor2,
                        makePlayerTouchable = true
                    ) {
                        swapBatsmenDB(context, matchId, firstBatsmanStats, secondBatsmanStats, true)
                        dbHelper.saveSnapshot(matchId)
                    }

                    BatsmanBowlerKeeperBox(
                        col1 = secondBatsmanStats.name.value,
                        col2 = String.format(Locale.UK, "%d", secondBatsmanStats.runs.value),
                        col3 = String.format(Locale.UK, "%d", secondBatsmanStats.balls.value),
                        col4 = String.format(Locale.UK, "%d", secondBatsmanStats.fours.value),
                        col5 = String.format(Locale.UK, "%d", secondBatsmanStats.sixes.value),
                        fontBold1 = fontBold3,
                        fontColor1 = fontColor3,
                        makePlayerTouchable = true
                    ) {
                        swapBatsmenDB(context, matchId, firstBatsmanStats, secondBatsmanStats, true)
                        dbHelper.saveSnapshot(matchId)
                    }
                }
            }

            // Bowler Box
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .border(
                        BorderStroke(2.dp, CricketAppTheme.colors.primaryCream),
                    )
                    .fillMaxWidth()
            ) {
                Column {
                    BatsmanBowlerKeeperBox(
                        col1 = "Bowler",
                        col2 = "O",
                        col3 = "M",
                        col4 = "R",
                        col5 = "W",
                        fontBold1 = FontWeight.Bold,
                        fontColor1 = CricketAppTheme.colors.secondaryGray,
                        makePlayerTouchable = false
                    ) {}
                    BatsmanBowlerKeeperBox(
                        col1 = consolidatedBowlerStats.name.value,
                        col2 = String.format(Locale.UK, "%.1f", consolidatedBowlerStats.over.value),
                        col3 = String.format(Locale.UK, "%d", consolidatedBowlerStats.maiden.value),
                        col4 = String.format(Locale.UK, "%d", consolidatedBowlerStats.runs.value),
                        col5 = String.format(Locale.UK, "%d", consolidatedBowlerStats.wickets.value),
                        fontBold1 = FontWeight.Bold,
                        fontColor1 = CricketAppTheme.colors.accentGreen,
                        makePlayerTouchable = true
                    ) {
                        if (balls.isEmpty() || balls.all { it.action.isBlank() }) {
                            showBowlerChangeDialog.value = true
                            manualBowlerChange.value = true
                        }
                    }

                    if (showBowlerChangeDialog.value) {
                        AlertDialog(
                            onDismissRequest = { showBowlerChangeDialog.value = false },
                            confirmButton = {
                                Button(onClick = {
                                    showBowlerChangeDialog.value = false
                                }) {
                                    Text("Cancel")
                                }
                            },
                            title = { Text("Change Bowler") },
                            text = {
                                Column {
                                    bowlingTeam.forEach { player ->
                                        if (currentOverBowlerStats.name.value != player.name) {
                                            val consStats = dbHelper.getConsolidatedBowlerStats(
                                                matchId,
                                                player.name
                                            )
                                            Button(
                                                onClick = {
                                                    val existingKeeper =
                                                        dbHelper.getLastKeeper(matchId, bowlingTeamId)

                                                    if (player.name == existingKeeper) {
                                                        showKeeperChangeDialog.value = true
                                                    }

                                                    if (manualBowlerChange.value) {
                                                        dbHelper.deleteCurrentBowler(matchId)
                                                        manualBowlerChange.value = false
                                                    } else {
                                                        dbHelper.updateBowlingStats(matchId, "bowled")
                                                    }
                                                    dbHelper.addBowlingStats(
                                                        matchId,
                                                        bowlingTeamId,
                                                        player.name,
                                                        existingKeeper,
                                                        "bowling"
                                                    )
                                                    showBowlerChangeDialog.value = false
                                                    setCurrentBowlerAndKeeper(
                                                    currentOverBowlerStats,
                                                    player.name,
                                                    existingKeeper
                                                )
                                                swapBatsmenDB(
                                                    context,
                                                    matchId,
                                                    firstBatsmanStats,
                                                    secondBatsmanStats,
                                                    true
                                                )

                                                refreshKey++
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    player.name,
                                                    fontSize = CricketAppTheme.dimens.bodySize,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    consStats.over.value.toString(),
                                                    fontSize = CricketAppTheme.dimens.bodySize,
                                                    textAlign = TextAlign.Right
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Keeper Box
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .border(
                        BorderStroke(2.dp, CricketAppTheme.colors.primaryCream),
                    )
                    .fillMaxWidth()
            ) {
                Column {
                    BatsmanBowlerKeeperBox(
                        col1 = "Keeper",
                        col2 = "",
                        col3 = "",
                        col4 = "",
                        col5 = "",
                        fontBold1 = FontWeight.Bold,
                        fontColor1 = CricketAppTheme.colors.secondaryGray,
                        makePlayerTouchable = false
                    ) {}
                    BatsmanBowlerKeeperBox(
                        col1 = currentOverBowlerStats.keepername.value,
                        col2 = "",
                        col3 = "",
                        col4 = "",
                        col5 = "",
                        fontBold1 = FontWeight.Bold,
                        fontColor1 = CricketAppTheme.colors.accentGreen,
                        makePlayerTouchable = true
                    ) {
                        showKeeperChangeDialog.value = true
                    }
                    if (showKeeperChangeDialog.value) {
                        AlertDialog(
                            onDismissRequest = { showKeeperChangeDialog.value = false },
                            confirmButton = {
                                Button(onClick = {
                                    showKeeperChangeDialog.value = false
                                }) {
                                    Text("Cancel")
                                }
                            },
                            title = { Text("Change Keeper") },
                            text = {
                                Column {
                                    bowlingTeam.forEach { player ->
                                        if ((currentOverBowlerStats.name.value != player.name) && (currentOverBowlerStats.keepername.value != player.name)) {
                                            Button(onClick = {
                                                dbHelper.updateBowlingStatsKeeper(
                                                    matchId,
                                                    bowlingTeamId,
                                                    player.name
                                                )
                                                showKeeperChangeDialog.value = false
                                                setCurrentKeeper(currentOverBowlerStats, player.name)
                                                dbHelper.saveSnapshot(matchId)
                                            }, modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    player.name,
                                                    fontSize = CricketAppTheme.dimens.bodySize,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // This Over Box
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .border(
                        BorderStroke(2.dp, CricketAppTheme.colors.primaryCream),
                    )
                    .fillMaxWidth()
            ) {
                Column {
                    OverBox(
                        heading1 = "This Over:",
                        balls = balls,
                        headingFontBold1 = FontWeight.Bold,
                        ballsFontBold1 = FontWeight.Bold,
                        backcolor1 = CricketAppTheme.colors.primaryCream
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1st Row with 4 circle buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircleButton("0", CricketAppTheme.dimens.circleButtonSize) {
                    updateStats(
                        context,
                        balls,
                        "0",
                        currentOverBowlerStats,
                        firstBatsmanStats,
                        secondBatsmanStats,
                        firstBattingTeamStats,
                        secondBattingTeamStats
                    )
                    refreshKey++
                }
                CircleButton("1", CricketAppTheme.dimens.circleButtonSize) {
                    updateStats(
                        context,
                        balls,
                        "1",
                        currentOverBowlerStats,
                        firstBatsmanStats,
                        secondBatsmanStats,
                        firstBattingTeamStats,
                        secondBattingTeamStats
                    )
                    refreshKey++
                }
                CircleButton("2", CricketAppTheme.dimens.circleButtonSize) {
                    updateStats(
                        context,
                        balls,
                        "2",
                        currentOverBowlerStats,
                        firstBatsmanStats,
                        secondBatsmanStats,
                        firstBattingTeamStats,
                        secondBattingTeamStats
                    )
                    refreshKey++
                }
                CircleButton("3", CricketAppTheme.dimens.circleButtonSize) {
                    updateStats(
                        context,
                        balls,
                        "3",
                        currentOverBowlerStats,
                        firstBatsmanStats,
                        secondBatsmanStats,
                        firstBattingTeamStats,
                        secondBattingTeamStats
                    )
                    refreshKey++
                }
            }

            // 2nd Row with 4 circle buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircleButton("4", CricketAppTheme.dimens.circleButtonSize) {
                    updateStats(
                        context,
                        balls,
                        "4",
                        currentOverBowlerStats,
                        firstBatsmanStats,
                        secondBatsmanStats,
                        firstBattingTeamStats,
                        secondBattingTeamStats
                    )
                    refreshKey++
                }
                CircleButton("6", CricketAppTheme.dimens.circleButtonSize) {
                    updateStats(
                        context,
                        balls,
                        "6",
                        currentOverBowlerStats,
                        firstBatsmanStats,
                        secondBatsmanStats,
                        firstBattingTeamStats,
                        secondBattingTeamStats
                    )
                    refreshKey++
                }
                CircleButton("WIDE", CricketAppTheme.dimens.circleButtonSizeSmall) {
                    showWidesDialog.value = true
                }
                if (showWidesDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showWidesDialog.value = false },
                        title = { Text("Select WIDE Option") },
                        text = {
                            Column {
                                // List of options to choose from
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedWidesOption.value = "W"
                                    showWidesDialog.value = false
                                    updateStats(
                                        context,
                                        balls,
                                        selectedWidesOption.value,
                                        currentOverBowlerStats,
                                        firstBatsmanStats,
                                        secondBatsmanStats,
                                        firstBattingTeamStats,
                                        secondBattingTeamStats
                                    )
                                    refreshKey++
                                }) {
                                    Text("WIDE", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedWidesOption.value = "W+1"
                                    showWidesDialog.value = false
                                    updateStats(
                                        context,
                                        balls,
                                        selectedWidesOption.value,
                                        currentOverBowlerStats,
                                        firstBatsmanStats,
                                        secondBatsmanStats,
                                        firstBattingTeamStats,
                                        secondBattingTeamStats
                                    )
                                    refreshKey++
                                }) {
                                    Text("WIDE + 1", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedWidesOption.value = "W+2"
                                    showWidesDialog.value = false
                                    updateStats(
                                        context,
                                        balls,
                                        selectedWidesOption.value,
                                        currentOverBowlerStats,
                                        firstBatsmanStats,
                                        secondBatsmanStats,
                                        firstBattingTeamStats,
                                        secondBattingTeamStats
                                    )
                                    refreshKey++
                                }) {
                                    Text("WIDE + 2", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                            }
                        },

                        confirmButton = {
                            Button(onClick = { showWidesDialog.value = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                CircleButton("NO BALL", CricketAppTheme.dimens.circleButtonSizeSmall) {
                    showNoBallDialog.value = true
                }
                if (showNoBallDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showNoBallDialog.value = false },
                        title = { Text("Select NO BALL Option") },
                        text = {
                            // Define the grid using Column and Row
                            Column {
                                // 3 Rows with 4 Buttons each (twelve options)
                                for (row in 0..4) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        // Create buttons in each row
                                        for (col in 1..3) {
                                            val optionIndex = row * 3 + col
                                            val optionText = when (optionIndex) {
                                                1 -> "NB"
                                                2 -> ""
                                                3 -> ""
                                                4 -> "NB+1"
                                                5 -> "NB+2"
                                                6 -> "NB+3"
                                                7 -> "NB+4"
                                                8 -> ""
                                                9 -> "NB+6"
                                                10 -> "NBL1"
                                                11 -> "NBL2"
                                                12 -> "NBL3"
                                                13 -> "NBB1"
                                                14 -> "NBB2"
                                                15 -> "NBB3"
                                                else -> ""
                                            }

                                            if (optionText != "") {
                                                val backgroundColor = when {
                                                    optionText.startsWith("NBL") -> Color.Green // Green for NBL buttons
                                                    optionText.startsWith("NBB") -> Color.Yellow   // Red for NBB buttons
                                                    else -> Color.Unspecified  // Default color for other buttons
                                                }
                                                Button(
                                                    onClick = {
                                                        selectedNoBallOption.value = optionText
                                                        showNoBallDialog.value = false
                                                        updateStats(
                                                            context,
                                                            balls,
                                                            selectedNoBallOption.value,
                                                            currentOverBowlerStats,
                                                            firstBatsmanStats,
                                                            secondBatsmanStats,
                                                            firstBattingTeamStats,
                                                            secondBattingTeamStats
                                                        )
                                                        refreshKey++
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                                                    modifier = Modifier
                                                        .padding(0.dp)
                                                        .weight(1f) // Ensures equal space distribution for buttons
                                                ) {
                                                    Text(
                                                        optionText,
                                                        fontSize = CricketAppTheme.dimens.buttonTextSize
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp)) // Add space of 16.dp between NBL and NBB
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showNoBallDialog.value = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }

            // 3rd Row with 4 circle buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircleButton("EXTRAS", CricketAppTheme.dimens.circleButtonSizeSmall) {
                    showExtrasDialog.value = true
                }
                if (showExtrasDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showExtrasDialog.value = false },
                        title = { Text("Select BYES Option") },
                        text = {
                            Column {
                                // List of options to choose from
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedExtrasOption.value = "B1"
                                    showExtrasDialog.value = false
                                    updateStats(
                                        context,
                                        balls,
                                        selectedExtrasOption.value,
                                        currentOverBowlerStats,
                                        firstBatsmanStats,
                                        secondBatsmanStats,
                                        firstBattingTeamStats,
                                        secondBattingTeamStats
                                    )
                                    refreshKey++
                                }) {
                                    Text("1 BYE", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedExtrasOption.value = "B2"
                                    showExtrasDialog.value = false
                                    updateStats(
                                        context,
                                        balls,
                                        selectedExtrasOption.value,
                                        currentOverBowlerStats,
                                        firstBatsmanStats,
                                        secondBatsmanStats,
                                        firstBattingTeamStats,
                                        secondBattingTeamStats
                                    )
                                    refreshKey++
                                }) {
                                    Text("2 BYE", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedExtrasOption.value = "B3"
                                    showExtrasDialog.value = false
                                    updateStats(
                                        context,
                                        balls,
                                        selectedExtrasOption.value,
                                        currentOverBowlerStats,
                                        firstBatsmanStats,
                                        secondBatsmanStats,
                                        firstBattingTeamStats,
                                        secondBattingTeamStats
                                    )
                                    refreshKey++
                                }) {
                                    Text("3 BYE", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(32.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedExtrasOption.value = "LB1"
                                    showExtrasDialog.value = false
                                    updateStats(
                                        context,
                                        balls,
                                        selectedExtrasOption.value,
                                        currentOverBowlerStats,
                                        firstBatsmanStats,
                                        secondBatsmanStats,
                                        firstBattingTeamStats,
                                        secondBattingTeamStats                                )
                                    refreshKey++
                                }) {
                                    Text("1 LEG-BYE", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedExtrasOption.value = "LB2"
                                    showExtrasDialog.value = false
                                    updateStats(
                                        context,
                                        balls,
                                        selectedExtrasOption.value,
                                        currentOverBowlerStats,
                                        firstBatsmanStats,
                                        secondBatsmanStats,
                                        firstBattingTeamStats,
                                        secondBattingTeamStats
                                    )
                                    refreshKey++
                                }) {
                                    Text("2 LEG-BYE", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedExtrasOption.value = "LB3"
                                    showExtrasDialog.value = false
                                    updateStats(
                                        context,
                                        balls,
                                        selectedExtrasOption.value,
                                        currentOverBowlerStats,
                                        firstBatsmanStats,
                                        secondBatsmanStats,
                                        firstBattingTeamStats,
                                        secondBattingTeamStats
                                    )
                                    refreshKey++
                                }) {
                                    Text("3 LEG-BYE", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showExtrasDialog.value = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
                CircleButton("MORE", CricketAppTheme.dimens.circleButtonSizeSmall) {
                    showMoreDialog.value = true
                }
                if (showMoreDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showMoreDialog.value = false },
                        title = { Text("More Options") },
                        text = {
                            Column {
                                // List of options to choose from
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    updateStats(
                                        context,
                                        balls,
                                        "LBW",
                                        currentOverBowlerStats,
                                        firstBatsmanStats,
                                        secondBatsmanStats,
                                        firstBattingTeamStats,
                                        secondBattingTeamStats
                                    )
                                    refreshKey++
                                    showMoreDialog.value = false
                                }) {
                                    Text("LBW (Warning -2)", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    Toast.makeText(context, "Not working yet!", Toast.LENGTH_SHORT)
                                        .show()
                                }) {
                                    Text("Retire Batsman", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    showMoreDialog.value = false
                                    showInningsConfirmationDialog.value = true
                                }) {
                                    Text("End Innings", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showMoreDialog.value = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showInningsConfirmationDialog.value) {
                    val endOf1stInning = ((team1OversBowled == 0.0) || (team2OversBowled == 0.0))
                    ConfirmationDialog(
                        message = if (endOf1stInning) "End the 1st Inning?" else "End the Match?",
                        onConfirm = {
                            showInningsConfirmationDialog.value = false
                            showMoreDialog.value = false
                            if (endOf1stInning) {
                                //If 1st innings then ask to confirm end of innings and start of second
                                dbHelper.updateBowlingStats(matchId, "bowled")
                                handleLastBatsmen(
                                    context,
                                    matchId,
                                    firstBatsmanStats,
                                    secondBatsmanStats
                                )
                                navController.navigate(
                                    "secondinningssetup" +
                                            ""
                                )
                                {
                                    popUpTo("scorecard") { inclusive = true }
                                }
                            } else {
                                //If 2nd innings then ask to confirm end of match
                                handleEndOfMatch(
                                    context,
                                    matchId,
                                    firstBatsmanStats,
                                    secondBatsmanStats,
                                    runsToWin,
                                    winningCaptain
                                )
                                navController.navigate("homepage")
                            }
                        },
                        onDismiss = {
                            showInningsConfirmationDialog.value = false
                            showMoreDialog.value = true
                        }
                    )
                }

                CircleButton("WICKET", CricketAppTheme.dimens.circleButtonSizeSmall) {
                    showWicketsDialog.value = true
                    checkAndPlayDuckSound(firstBatsmanStats, secondBatsmanStats, context)
                }
                if (showWicketsDialog.value) {
                    selectedFielder.value = ""
                    AlertDialog(
                        onDismissRequest = { showWicketsDialog.value = false },
                        title = { Text("Select WICKETS Option") },
                        text = {
                            Column {
                                // List of options to choose from
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedWicketsOption.value = "WKB"
                                    showNextBatsmanDialog.value = true
                                    showWicketsDialog.value = false
                                }) {
                                    Text("Bowled", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedWicketsOption.value = "WKCB"
                                    selectedFielder.value = dbHelper.getCurrentKeeper(matchId)
                                    showNextBatsmanDialog.value = true
                                    showWicketsDialog.value = false
                                }) {
                                    Text("Caught Behind", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedWicketsOption.value = "WKC"
                                    showFielderDialog.value = true
                                    showWicketsDialog.value = false
                                }) {
                                    Text("Caught", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedWicketsOption.value = "WKRO"
                                    showBatsmanRunOutSelectionDialog.value = true
                                    showWicketsDialog.value = false
                                }) {
                                    Text("Run Out", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedWicketsOption.value = "WKRONB"
                                    showBatsmanRunOutSelectionDialog.value = true
                                    showWicketsDialog.value = false
                                }) {
                                    Text("Run Out NB", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedWicketsOption.value = "WKROW"
                                    showBatsmanRunOutSelectionDialog.value = true
                                    showWicketsDialog.value = false
                                }) {
                                    Text("Run Out Wide", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedWicketsOption.value = "WKST"
                                    selectedFielder.value = dbHelper.getCurrentKeeper(matchId)
                                    showNextBatsmanDialog.value = true
                                    showWicketsDialog.value = false
                                }) {
                                    Text("Stumped", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedWicketsOption.value = "WKSTW"
                                    selectedFielder.value = dbHelper.getCurrentKeeper(matchId)
                                    showNextBatsmanDialog.value = true
                                    showWicketsDialog.value = false
                                }) {
                                    Text("Stumped Wide", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedWicketsOption.value = "WKHW"
                                    showNextBatsmanDialog.value = true
                                    showWicketsDialog.value = false
                                }) {
                                    Text("Hit Wicket", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedWicketsOption.value = "WKLB"
                                    showNextBatsmanDialog.value = true
                                    showWicketsDialog.value = false
                                }) {
                                    Text("LBW", fontSize = CricketAppTheme.dimens.bodySize)
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showWicketsDialog.value = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showBatsmanRunOutSelectionDialog.value) {
                    AlertDialog(
                        onDismissRequest = {
                            showFielderDialog.value = false
                            showWicketsDialog.value = true
                        },
                        title = { Text("Select runout batsman?") },
                        text = {
                            Column {
                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    if (!firstBatsmanStats.active.value) {
                                        swapBatsmenDB(
                                            context,
                                            matchId,
                                            firstBatsmanStats,
                                            secondBatsmanStats,
                                            true
                                        )
                                    }
                                    showBatsmanRunOutSelectionDialog.value = false
                                    showFielderDialog.value = true
                                }) {
                                    Text(
                                        firstBatsmanStats.name.value,
                                        fontSize = CricketAppTheme.dimens.bodySize
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                    if (!secondBatsmanStats.active.value) {
                                        swapBatsmenDB(
                                            context,
                                            matchId,
                                            firstBatsmanStats,
                                            secondBatsmanStats,
                                            true
                                        )
                                    }
                                    showBatsmanRunOutSelectionDialog.value = false
                                    showFielderDialog.value = true
                                }) {
                                    Text(
                                        secondBatsmanStats.name.value,
                                        fontSize = CricketAppTheme.dimens.bodySize
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                selectedFielder.value = ""
                                showFielderDialog.value = false
                                showBatsmanRunOutSelectionDialog.value = false
                            }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showFielderDialog.value) {
                    AlertDialog(
                        onDismissRequest = {
                            showFielderDialog.value = false
                            showWicketsDialog.value = true
                        },
                        title = { Text("Select Fielder") },
                        text = {
                            Column {
                                bowlingTeam.forEach { player ->
                                    // List of options to choose from
                                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                        selectedFielder.value = player.name
                                        showFielderDialog.value = false
                                        showNextBatsmanDialog.value = true
                                    }) {
                                        Text(player.name, fontSize = CricketAppTheme.dimens.bodySize)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                selectedFielder.value = ""
                                showFielderDialog.value = false
                            }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showNextBatsmanDialog.value) {

                    //Get List of Team Players
                    val strikerBatsman = getActiveBatsman(firstBatsmanStats, secondBatsmanStats)
                    val nonStrikerBatsman = getInactiveBatsman(firstBatsmanStats, secondBatsmanStats)
                    var excludeBatsman = ""

                    val teamId = if (firstBatsmanStats.name.value != "") {
                        dbHelper.getTeamForPlayer(matchId, firstBatsmanStats.name.value)
                    } else {
                        dbHelper.getTeamForPlayer(matchId, secondBatsmanStats.name.value)
                    }
                    val players = dbHelper.getTeamPlayers(matchId, teamId, 1)

                    //Remove Players who have batted/got out twice already
                    val battedFullyAlreadyPlayers =
                        teamId.let { dbHelper.getFullyBattedAlreadyPlayers(matchId, it) }

                    val previousBattingCount = dbHelper.getBattingCount(matchId, strikerBatsman)
                    if (previousBattingCount > 0) {
                        excludeBatsman = strikerBatsman
                    }

                    val availablePlayers = players.filter { player ->
                        !battedFullyAlreadyPlayers.contains(player) && player != Player(
                            nonStrikerBatsman
                        ) && player != Player(excludeBatsman)
                    }

                    if (availablePlayers.isEmpty()) {
                        showNextBatsmanDialog.value = false
                        val batsmanOut = getActiveBatsman(firstBatsmanStats, secondBatsmanStats)
                        selectedWicketsOption.value += ",$batsmanOut"
                        updateStats(
                            context,
                            balls,
                            selectedWicketsOption.value,
                            currentOverBowlerStats,
                            firstBatsmanStats,
                            secondBatsmanStats,
                            firstBattingTeamStats,
                            secondBattingTeamStats
                        )
                        refreshKey++
                        currentBowler.value = dbHelper.getCurrentBowler(matchId)
                        val wicketDescription = getWicketDescription(
                            selectedWicketsOption.value,
                            currentBowler.value,
                            selectedFielder.value
                        )
                        val wicketType = getWicketType(selectedWicketsOption.value)
                        markBatsmanAsOutInDB(
                            context,
                            matchId,
                            firstBatsmanStats,
                            secondBatsmanStats,
                            wicketDescription,
                            wicketType,
                            currentBowler.value,
                            selectedFielder.value,
                            "",
                            false
                        )
                        if (dbHelper.getTeamWickets(
                                matchId,
                                dbHelper.getTeamForPlayer(matchId, batsmanOut)
                            ) < (noOfPlayersAside * 2)
                        ) {
                            dbHelper.updateBattingStats(
                                matchId,
                                nonStrikerBatsman,
                                "non-striker",
                                "striker"
                            )
                        }
                    } else {

                        //For current user, if current time getting out is second turn then don't allow any more
                        AlertDialog(
                            onDismissRequest = {
                                showNextBatsmanDialog.value = false
                                showWicketsDialog.value = true
                            },
                            title = { Text("Select NEXT Batsman") },
                            text = {
                                Column {
                                    availablePlayers.forEach { player ->
                                        // List of options to choose from
                                        Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                            showNextBatsmanDialog.value = false
                                            val newBatsman = player.name

                                            if (!batsmanOverride.value) {
                                                val batsmanOut = getActiveBatsman(
                                                    firstBatsmanStats,
                                                    secondBatsmanStats
                                                )

                                                // Capture BEFORE any state changes
                                                val strikerBeforeWicket = firstBatsmanStats.name.value
                                                val nonStrikerBeforeWicket = secondBatsmanStats.name.value

                                                selectedWicketsOption.value += ",$batsmanOut,$newBatsman,${selectedFielder.value.ifEmpty { "NA" }}"
                                                updateStats(
                                                    context,
                                                    balls,
                                                    selectedWicketsOption.value,
                                                    currentOverBowlerStats,
                                                    firstBatsmanStats,
                                                    secondBatsmanStats,
                                                    firstBattingTeamStats,
                                                    secondBattingTeamStats
                                                )
                                                refreshKey++

                                                currentBowler.value = dbHelper.getCurrentBowler(matchId)
                                                val wicketDescription = getWicketDescription(
                                                    selectedWicketsOption.value,
                                                    currentBowler.value,
                                                    selectedFielder.value
                                                )
                                                val wicketType = getWicketType(
                                                    selectedWicketsOption.value
                                                )

                                                markBatsmanAsOutInDB(
                                                    context,
                                                    matchId,
                                                    firstBatsmanStats,
                                                    secondBatsmanStats,
                                                    wicketDescription,
                                                    wicketType,
                                                    currentBowler.value,
                                                    selectedFielder.value,
                                                    newBatsman,
                                                    true
                                                )

                                                dbHelper.addBattingStats(
                                                    matchId,
                                                    teamId,
                                                    newBatsman,
                                                    "striker"
                                                )

                                                dbHelper.insertPartnership(matchId, teamId, dbHelper.getStriker(matchId), dbHelper.getNonStriker(matchId))
                                                dbHelper.saveSnapshot(matchId)

                                                // Check if it's a runout wicket - if so, ask which batsman should be on strike
                                                val isRunOut = selectedWicketsOption.value.startsWith("WKRO")
                                                if (isRunOut) {
                                                    // The two batsmen now at the crease are:
                                                    // - newBatsman (just came in)
                                                    // - whoever was NOT run out from the original pair
                                                    val survivingBatsman = if (batsmanOut == strikerBeforeWicket) {
                                                        nonStrikerBeforeWicket
                                                    } else {
                                                        strikerBeforeWicket
                                                    }
                                                    pendingStrikerName.value = newBatsman
                                                    pendingNonStrikerName.value = survivingBatsman
                                                    showActiveBatsmanSelectionDialog.value = true
                                                }

                                            } else {
                                                dbHelper.updateStriker(matchId, newBatsman)
                                                firstBatsmanStats.name.value = newBatsman
                                                batsmanOverride.value = false
                                            }
                                        }) {
                                            Text(
                                                player.name,
                                                fontSize = CricketAppTheme.dimens.bodySize
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showNextBatsmanDialog.value = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }

                if (showActiveBatsmanSelectionDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showActiveBatsmanSelectionDialog.value = false },
                        title = { Text("Select Facing Batsman") },
                        text = {
                            Column {
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        val currentStriker = dbHelper.getStriker(matchId)
                                        if (currentStriker != pendingStrikerName.value) {
                                            swapBatsmenDB(context, matchId, firstBatsmanStats, secondBatsmanStats, true)
                                        }
                                        showActiveBatsmanSelectionDialog.value = false
                                    }
                                ) {
                                    Text(
                                        text = pendingStrikerName.value,
                                        fontSize = CricketAppTheme.dimens.bodySize
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        // Make pendingNonStrikerName the active striker
                                        val currentStriker = dbHelper.getStriker(matchId)
                                        if (currentStriker != pendingNonStrikerName.value) {
                                            swapBatsmenDB(context, matchId, firstBatsmanStats, secondBatsmanStats, true)
                                        }
                                        showActiveBatsmanSelectionDialog.value = false
                                    }
                                ) {
                                    Text(
                                        text = pendingNonStrikerName.value,
                                        fontSize = CricketAppTheme.dimens.bodySize
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showActiveBatsmanSelectionDialog.value = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                CircleButton("UNDO", CricketAppTheme.dimens.circleButtonSizeSmall) {
                    showUndoConfirmationDialog.value = true
                }

                if (showUndoConfirmationDialog.value) {
                    ConfirmationDialog(
                        message = "Are you sure you want to UNDO?",
                        onConfirm = {
                            showUndoConfirmationDialog.value = false

                            updateStats(
                                context,
                                balls,
                                "UNDO",
                                currentOverBowlerStats,
                                firstBatsmanStats,
                                secondBatsmanStats,
                                firstBattingTeamStats,
                                secondBattingTeamStats
                            )
                            refreshKey++
                        },
                        onDismiss = {
                            showUndoConfirmationDialog.value = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActionSquareButton(
    text: String,
    isTablet: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp), // 🔹 square
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CricketAppTheme.colors.primaryCream
        )
    ) {
        Text(
            text = text,
            fontSize = CricketAppTheme.dimens.microSize,
            textAlign = TextAlign.Center,
            color = CricketAppTheme.colors.textOnLight,
            modifier = Modifier.padding(4.dp)
        )
    }
}
