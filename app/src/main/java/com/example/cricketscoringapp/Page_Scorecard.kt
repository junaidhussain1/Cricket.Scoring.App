package com.example.cricketscoringapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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


@Composable
fun ScoreCardPage(navController: NavHostController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val dbHelper = CricketDatabaseHelper(context)
    val matchId = dbHelper.getMatchId()
    val currentBowler = remember { mutableStateOf(dbHelper.getCurrentBowler(matchId)) }
    val bowlingTeamId = dbHelper.getTeamForPlayer(matchId,currentBowler.value)
    val bowlingTeam = bowlingTeamId.let { dbHelper.getTeamPlayers(matchId, it,1) }
    val firstTeamId = dbHelper.getTeamForPlayer(matchId,dbHelper.getFirstBattingTeamStriker(matchId))
    val secondTeamId = if (firstTeamId == 1) 2 else 1

    val showWidesDialog = remember { mutableStateOf(false) }
    val selectedWidesOption = remember { mutableStateOf("") }
    val showNoBallDialog = remember { mutableStateOf(false) }
    val selectedNoBallOption = remember { mutableStateOf("") }
    val showByesDialog = remember { mutableStateOf(false) }
    val selectedByesOption = remember { mutableStateOf("") }
    val showLegByesDialog = remember { mutableStateOf(false) }
    val selectedLegByesOption = remember { mutableStateOf("") }
    val showWicketsDialog = remember { mutableStateOf(false) }
    val selectedWicketsOption = remember { mutableStateOf("") }
    val showNextBatsmanDialog = remember { mutableStateOf(false) }

    val showBowlerChangeDialog = remember { mutableStateOf(false) }
    val showKeeperChangeDialog = remember { mutableStateOf(false) }
    val showFielderDialog = remember { mutableStateOf(false) }
    val selectedFielder = remember { mutableStateOf("") }
    val manualBowlerChange = remember { mutableStateOf(false) }
    val runsToWin = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Retrieve the captain names from the view model
        var team1Captain = Player(dbHelper.getCaptainForTeam(matchId, 1))
        var team2Captain = Player(dbHelper.getCaptainForTeam(matchId, 2))
        val firstBattingTeamCaptain = Player(dbHelper.getBattingTeamCaptain(matchId,1))
        if (team1Captain != firstBattingTeamCaptain) {
            team2Captain = team1Captain
            team1Captain = firstBattingTeamCaptain
        }

        val team1Stats = dbHelper.getTeamStats(matchId,1,team1Captain.name)
        val firstBattingTeamStats = remember(team1Stats) {
            TeamStats(
                name = mutableStateOf(team1Stats.name.value),
                overs = mutableDoubleStateOf(team1Stats.overs.value),
                inningScore = mutableIntStateOf(team1Stats.inningScore.value),
                inningWickets = mutableIntStateOf(team1Stats.inningWickets.value),
                active = mutableStateOf(team1Stats.active.value)
            )
        }

        val team2Stats = dbHelper.getTeamStats(matchId,2,team2Captain.name)
        val secondBattingTeamStats = remember(team2Stats) {
            TeamStats(
                name = mutableStateOf(team2Stats.name.value),
                overs = mutableDoubleStateOf(team2Stats.overs.value),
                inningScore = mutableIntStateOf(team2Stats.inningScore.value),
                inningWickets = mutableIntStateOf(team2Stats.inningWickets.value),
                active = mutableStateOf(team2Stats.active.value)
            )
        }
        
        calcRunsToWin(team1Stats, team2Stats, runsToWin)

        val firstBatsman = dbHelper.getBatsmanByStatus(matchId,"striker")
        val firstBatsmanStats = remember {
            BatsmanStats(
                name = mutableStateOf(value = firstBatsman.name.value),
                runs = mutableIntStateOf(value = firstBatsman.runs.value),
                balls = mutableIntStateOf(value = firstBatsman.balls.value),
                fours = mutableIntStateOf(value = firstBatsman.fours.value),
                sixes = mutableIntStateOf(value = firstBatsman.sixes.value),
                wicketDescription = mutableStateOf(value = firstBatsman.wicketDescription.value),
                active = mutableStateOf(value = firstBatsman.active.value)
            )
        }

        val secondBatsman = dbHelper.getBatsmanByStatus(matchId,"non-striker")
        val secondBatsmanStats = remember {
            BatsmanStats(
                name = mutableStateOf(value = secondBatsman.name.value),
                runs = mutableIntStateOf(value = secondBatsman.runs.value),
                balls = mutableIntStateOf(value = secondBatsman.balls.value),
                fours = mutableIntStateOf(value = secondBatsman.fours.value),
                sixes = mutableIntStateOf(value = secondBatsman.sixes.value),
                wicketDescription = mutableStateOf(value = secondBatsman.name.value),
                active = mutableStateOf(value = secondBatsman.active.value)
            )
        }

        val currentOverBowler = dbHelper.getCurrentBowlerStats(matchId)
        val currentOverBowlerStats = remember(currentOverBowler) {
            BowlerStats(
                name = mutableStateOf(currentOverBowler.name.value),
                over = mutableDoubleStateOf(currentOverBowler.over.value),
                maiden = mutableIntStateOf(currentOverBowler.maiden.value),
                runs = mutableIntStateOf(currentOverBowler.runs.value),
                wickets = mutableIntStateOf(currentOverBowler.wickets.value),
                noballs = mutableIntStateOf(currentOverBowler.noballs.value),
                wides = mutableIntStateOf(currentOverBowler.wides.value),
                byes = mutableIntStateOf(currentOverBowler.byes.value),
                legbyes = mutableIntStateOf(currentOverBowler.legbyes.value),
                fours = mutableIntStateOf(currentOverBowler.fours.value),
                sixes = mutableIntStateOf(currentOverBowler.sixes.value),
                keepername = mutableStateOf(currentOverBowler.keepername.value),
                overrecord = mutableStateOf(currentOverBowler.overrecord.value)
            )
        }

        val consolidatedBowler = dbHelper.getConsolidatedBowlerStats(matchId,currentOverBowler.name.value)
        val consolidatedBowlerStats = remember(consolidatedBowler) {
            BowlerStats(
                name = mutableStateOf(consolidatedBowler.name.value),
                over = mutableDoubleStateOf(consolidatedBowler.over.value),
                maiden = mutableIntStateOf(consolidatedBowler.maiden.value),
                runs = mutableIntStateOf(consolidatedBowler.runs.value),
                wickets = mutableIntStateOf(consolidatedBowler.wickets.value),
                noballs = mutableIntStateOf(consolidatedBowler.noballs.value),
                wides = mutableIntStateOf(consolidatedBowler.wides.value),
                byes = mutableIntStateOf(consolidatedBowler.byes.value),
                legbyes = mutableIntStateOf(consolidatedBowler.legbyes.value),
                fours = mutableIntStateOf(consolidatedBowler.fours.value),
                sixes = mutableIntStateOf(consolidatedBowler.sixes.value),
                keepername = mutableStateOf(consolidatedBowler.keepername.value),
                overrecord = mutableStateOf(consolidatedBowler.overrecord.value)
            )
        }

        val balls = remember { mutableStateListOf<Ball>() }

        //Rebuild current over from current bowler stats in database
        if (currentOverBowlerStats.overrecord.value.contains(",")) {
            val ballValues = currentOverBowlerStats.overrecord.value.split("|")
            balls.clear()
            ballValues.forEach { value ->
                val pipeValues = value.split(",")
                val ball = Ball(pipeValues[0], pipeValues[1]) // Assuming Ball takes an Int
                balls.add(ball)
            }
        } else if (currentOverBowlerStats.overrecord.value != "") {
            val pipeValues = currentOverBowlerStats.overrecord.value.split(",")
            val ball = Ball(pipeValues[0], pipeValues[1]) // Assuming Ball takes an Int
            balls.add(ball)
        }

        //Handle End of Over, End of Innings, End of Match
        val team1wickets = dbHelper.getTeamWickets(matchId,firstTeamId)
        val team2wickets = dbHelper.getTeamWickets(matchId,secondTeamId)
        val team2batters = dbHelper.getTeamBatters(matchId,secondTeamId)
        val team1OversBowled = dbHelper.getTeamOversBowled(matchId, secondTeamId)
        val team2OversBowled = dbHelper.getTeamOversBowled(matchId, firstTeamId)

        if (dbHelper.getIsMatchStarted(matchId)) {
            if ((team1wickets == 12) and (team2batters == 0)) {
                dbHelper.updateBowlingStats(matchId,"bowled")
                handleLastBatsmen(context,matchId,firstBatsmanStats,secondBatsmanStats)
                navController.navigate("secondinningssetup")
            } else if ((team1wickets == 12) and (team2wickets == 12)) {
                handleEndOfMatch(context,matchId,firstBatsmanStats, secondBatsmanStats, runsToWin)
                navController.navigate("homepage")
            } else {
                if (endOfOverReached(balls)) {
                    if ((team1OversBowled == 12.0) && (team2OversBowled == 0.0)) {
                        dbHelper.updateBowlingStats(matchId,"bowled")
                        handleLastBatsmen(context,matchId,firstBatsmanStats,secondBatsmanStats)
                        navController.navigate("secondinningssetup")
                    } else if ((team1OversBowled == 12.0) && (team2OversBowled == 12.0)) {
                        handleEndOfMatch(context,matchId,firstBatsmanStats, secondBatsmanStats, runsToWin)
                        navController.navigate("homepage")
                    } else {
                        showBowlerChangeDialog.value = true
                    }
                }
            }
        }


        // Innings Score Box
        Box(
            modifier = Modifier
                .padding(8.dp)
                .border(
                    BorderStroke(2.dp, Color.White),
                )
                .fillMaxWidth()
        ) {
            Column {
                TeamScoreBox(
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
                    color1 = if (firstBattingTeamStats.active.value) Color(19, 207, 69) else Color.Black
                ) {
                    val teamID = 1
                    navController.navigate("inningstats/${teamID}")
                }
                TeamScoreBox(
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
                    color1 = if (firstBattingTeamStats.active.value) Color.Black else Color(19, 207, 69)
                ) {
                    val teamID = 2
                    navController.navigate("inningstats/${teamID}")
                }
            }
        }

        Text(
            text = runsToWin.value,
            fontSize = 16.sp
        )

        // Batsman Box
        Box(
            modifier = Modifier
                .padding(8.dp)
                .border(
                    BorderStroke(2.dp, Color.White),
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
                    fontColor1 = Color.Gray,
                    makePlayerTouchable = false
                ) {}

                val fontColor2: Color
                val fontColor3: Color
                val fontBold2: FontWeight
                val fontBold3: FontWeight

                if (firstBatsmanStats.active.value) {
                    fontColor2 = Color(19, 207, 69)
                    fontBold2 = FontWeight.Bold
                    fontColor3 = Color.Black
                    fontBold3 = FontWeight.Normal
                } else {
                    fontColor2 = Color.Black
                    fontBold2 = FontWeight.Normal
                    fontColor3 = Color(19, 207, 69)
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
                    swapBatsmenDB(context,matchId,firstBatsmanStats, secondBatsmanStats)
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
                    swapBatsmenDB(context,matchId,firstBatsmanStats, secondBatsmanStats)
                }
            }
        }

        // Bowler Box
        Box(
            modifier = Modifier
                .padding(8.dp)
                .border(
                    BorderStroke(2.dp, Color.White),
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
                    fontColor1 = Color.Gray,
                    makePlayerTouchable = false
                ) {}
                BatsmanBowlerKeeperBox(
                    col1 = consolidatedBowlerStats.name.value,
                    col2 = String.format(Locale.UK, "%.1f", consolidatedBowlerStats.over.value),
                    col3 = String.format(Locale.UK, "%d", consolidatedBowlerStats.maiden.value),
                    col4 = String.format(Locale.UK, "%d", consolidatedBowlerStats.runs.value),
                    col5 = String.format(Locale.UK, "%d", consolidatedBowlerStats.wickets.value),
                    fontBold1 = FontWeight.Bold,
                    fontColor1 = Color(19, 207, 69),
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
                                showBowlerChangeDialog.value = false }) {
                                Text("Cancel")
                            }
                        },
                        title = { Text("Change Bowler") },
                        text = {
                            Column {
                                bowlingTeam.forEach { player ->
                                    if (currentOverBowlerStats.name.value != player.name) {
                                        val consStats = dbHelper.getConsolidatedBowlerStats(matchId,player.name)
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
                                                    dbHelper.updateBowlingStats(matchId,"bowled")
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
                                                balls.clear()
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                player.name,
                                                fontSize = if (isTablet) 26.sp else 20.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                consStats.over.value.toString(),
                                                fontSize = if (isTablet) 26.sp else 20.sp,
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
                    BorderStroke(2.dp, Color.White),
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
                    fontColor1 = Color.Gray,
                    makePlayerTouchable = false
                ) {}
                BatsmanBowlerKeeperBox(
                    col1 = currentOverBowlerStats.keepername.value,
                    col2 = "",
                    col3 = "",
                    col4 = "",
                    col5 = "",
                    fontBold1 = FontWeight.Bold,
                    fontColor1 = Color(19, 207, 69),
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
                                        }, modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                player.name,
                                                fontSize = if (isTablet) 26.sp else 20.sp,
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
                    BorderStroke(2.dp, Color.White),
                )
                .fillMaxWidth()
        ) {
            Column {
                OverBox(
                    heading1 = "This Over:",
                    balls = balls,
                    headingFontBold1 = FontWeight.Normal,
                    ballsFontBold1 = FontWeight.Bold,
                    backcolor1 = Color.White
                )
            }
        }

        // 1st Row with 4 circle buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CircleButton("0", if (isTablet) 50 else 40) {
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
            }
            CircleButton("1", if (isTablet) 50 else 40) {
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
            }
            CircleButton("2", if (isTablet) 50 else 40) {
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
            }
            CircleButton("3", if (isTablet) 50 else 40) {
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
            }
        }

        // 2nd Row with 4 circle buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CircleButton("4", if (isTablet) 50 else 40) {
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
            }
            CircleButton("6", if (isTablet) 50 else 40) {
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
            }
            CircleButton("WIDE", if (isTablet) 26 else 16) {
                showWidesDialog.value = true
            }
            if (showWidesDialog.value) {
                AlertDialog(
                    onDismissRequest = { showWidesDialog.value = false },
                    title = { Text("Select WIDE Option") },
                    text = {
                        Column {
                            // List of options to choose from
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedWidesOption.value = "W"
                                showWidesDialog.value = false
                                updateStats(context,balls,selectedWidesOption.value,currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                            }) {
                                Text("WIDE", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedWidesOption.value = "W+1"
                                showWidesDialog.value = false
                                updateStats(context,balls,selectedWidesOption.value,currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                            }) {
                                Text("WIDE + 1", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedWidesOption.value = "W+2"
                                showWidesDialog.value = false
                                updateStats(context,balls,selectedWidesOption.value,currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                            }) {
                                Text("WIDE + 2", fontSize = if (isTablet) 30.sp else 20.sp)
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

            CircleButton("NO BALL", if (isTablet) 26 else 16) {
                showNoBallDialog.value = true
            }
            if (showNoBallDialog.value) {
                AlertDialog(
                    onDismissRequest = { showNoBallDialog.value = false },
                    title = { Text("Select NO BALL Option") },
                    text = {
                        // Define the grid using Column and Row
                        Column {
                            // 3 Rows with 4 Buttons each (12 options)
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
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                                                modifier = Modifier
                                                    .padding(0.dp)
                                                    .weight(1f) // Ensures equal space distribution for buttons
                                            ) {
                                                Text(optionText,
                                                    fontSize = if (isTablet) 26.sp else 16.sp)
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
            CircleButton("BYE", if (isTablet) 26 else 16) {
                showByesDialog.value = true
            }
            if (showByesDialog.value) {
                AlertDialog(
                    onDismissRequest = { showByesDialog.value = false },
                    title = { Text("Select BYES Option") },
                    text = {
                        Column {
                            // List of options to choose from
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedByesOption.value = "B1"
                                showByesDialog.value = false
                                updateStats(context,balls,selectedByesOption.value,currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                            }) {
                                Text("1 BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedByesOption.value = "B2"
                                showByesDialog.value = false
                                updateStats(context,balls,selectedByesOption.value,currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                            }) {
                                Text("2 BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedByesOption.value = "B3"
                                showByesDialog.value = false
                                updateStats(context,balls,selectedByesOption.value,currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                            }) {
                                Text("3 BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showByesDialog.value = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            CircleButton("LEG BYE", if (isTablet) 26 else 16) {
                showLegByesDialog.value = true
            }
            if (showLegByesDialog.value) {
                AlertDialog(
                    onDismissRequest = { showLegByesDialog.value = false },
                    title = { Text("Select LEG BYES Option") },
                    text = {
                        Column {
                            // List of options to choose from
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedLegByesOption.value = "LB1"
                                showLegByesDialog.value = false
                                updateStats(context,balls,selectedLegByesOption.value,currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                            }) {
                                Text("1 LEG-BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedLegByesOption.value = "LB2"
                                showLegByesDialog.value = false
                                updateStats(context,balls,selectedLegByesOption.value,currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                            }) {
                                Text("2 LEG-BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedLegByesOption.value = "LB3"
                                showLegByesDialog.value = false
                                updateStats(context,balls,selectedLegByesOption.value,currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                            }) {
                                Text("3 LEG-BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showLegByesDialog.value = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            CircleButton("WICKET", if (isTablet) 26 else 16) {
                showWicketsDialog.value = true
                checkAndPlayDuckSound(firstBatsmanStats,secondBatsmanStats,context)
            }
            if (showWicketsDialog.value) {
                selectedFielder.value = ""
                AlertDialog(
                    onDismissRequest = { showWicketsDialog.value = false },
                    title = { Text("Select WICKETS Option") },
                    text = {
                        Column {
                            // List of options to choose from
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedWicketsOption.value = "WKB"
                                showWicketsDialog.value = false
                                showNextBatsmanDialog.value = true
                            }) {
                                Text("Bowled", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedWicketsOption.value = "WKCB"
                                showWicketsDialog.value = false
                                selectedFielder.value = dbHelper.getCurrentKeeper(matchId)
                                showNextBatsmanDialog.value = true
                            }) {
                                Text("Caught Behind", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedWicketsOption.value = "WKC"
                                showWicketsDialog.value = false
                                showFielderDialog.value = true
                            }) {
                                Text("Caught", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedWicketsOption.value = "WKRO"
                                showWicketsDialog.value = false
                                showFielderDialog.value = true
                            }) {
                                Text("Run Out", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedWicketsOption.value = "WKRONB"
                                showWicketsDialog.value = false
                                showFielderDialog.value = true
                            }) {
                                Text("Run Out NB", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedWicketsOption.value = "WKST"
                                showWicketsDialog.value = false
                                selectedFielder.value = dbHelper.getCurrentKeeper(matchId)
                                showNextBatsmanDialog.value = true
                            }) {
                                Text("Stumped", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedWicketsOption.value = "WKSTW"
                                showWicketsDialog.value = false
                                selectedFielder.value = dbHelper.getCurrentKeeper(matchId)
                                showNextBatsmanDialog.value = true
                            }) {
                                Text("Stumped Wide", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedWicketsOption.value = "WKHW"
                                showWicketsDialog.value = false
                                showNextBatsmanDialog.value = true
                            }) {
                                Text("Hit Wicket", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedWicketsOption.value = "WKLB"
                                showWicketsDialog.value = false
                                showNextBatsmanDialog.value = true
                            }) {
                                Text("LBW", fontSize = if (isTablet) 30.sp else 20.sp)
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
                                Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                    selectedFielder.value = player.name
                                    showFielderDialog.value = false
                                    showNextBatsmanDialog.value = true
                                }) {
                                    Text(player.name, fontSize = if (isTablet) 30.sp else 20.sp)
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
                val strikerBatsman = getActiveBatsman(firstBatsmanStats,secondBatsmanStats)
                val nonStrikerBatsman = getInactiveBatsman(firstBatsmanStats,secondBatsmanStats)
                var excludeBatsman = ""

                val teamId = if (firstBatsmanStats.name.value != "") {
                    dbHelper.getTeamForPlayer(matchId, firstBatsmanStats.name.value)
                } else {
                    dbHelper.getTeamForPlayer(matchId, secondBatsmanStats.name.value)
                }
                val players = dbHelper.getTeamPlayers(matchId, teamId,1)

                //Remove Players who have batted/got out twice already
                val battedFullyAlreadyPlayers =
                    teamId.let { dbHelper.getFullyBattedAlreadyPlayers(matchId, it) }

                val previousBattingCount = dbHelper.getBattingCount(matchId,strikerBatsman)
                if (previousBattingCount > 0) {
                    excludeBatsman = strikerBatsman
                }

                val availablePlayers = players.filter { player ->
                    !battedFullyAlreadyPlayers.contains(player) && player != Player(nonStrikerBatsman) && player != Player(excludeBatsman)
                }

                if (availablePlayers.isEmpty()) {
                    showNextBatsmanDialog.value = false
                    val batsmanOut = getActiveBatsman(firstBatsmanStats, secondBatsmanStats)
                    selectedWicketsOption.value += ",$batsmanOut"
                    updateStats(context,balls,selectedWicketsOption.value,currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                    currentBowler.value = dbHelper.getCurrentBowler(matchId)
                    val wicketDescription = getWicketDescription(selectedWicketsOption.value,currentBowler.value,selectedFielder.value)
                    markBatsmanAsOutInDB(context,matchId,firstBatsmanStats,secondBatsmanStats,wicketDescription,"",false)
                    if (dbHelper.getTeamWickets(matchId,dbHelper.getTeamForPlayer(matchId,batsmanOut)) < 12) {
                        dbHelper.updateBattingStats(matchId,nonStrikerBatsman,"non-striker","striker")
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
                                        val batsmanOut = getActiveBatsman(
                                            firstBatsmanStats,
                                            secondBatsmanStats
                                        )

                                        val newBatsman = player.name
                                        selectedWicketsOption.value += ",$batsmanOut,$newBatsman"
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

                                        currentBowler.value = dbHelper.getCurrentBowler(matchId)
                                        val wicketDescription = getWicketDescription(
                                            selectedWicketsOption.value,
                                            currentBowler.value,
                                            selectedFielder.value
                                        )

                                        markBatsmanAsOutInDB(
                                            context,
                                            matchId,
                                            firstBatsmanStats,
                                            secondBatsmanStats,
                                            wicketDescription,
                                            newBatsman,
                                            true
                                        )
                                        dbHelper.addBattingStats(
                                            matchId,
                                            teamId,
                                            newBatsman,
                                            "striker"
                                        )
                                    }) {
                                        Text(
                                            player.name,
                                            fontSize = if (isTablet) 30.sp else 20.sp
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
            CircleButton("UNDO", if (isTablet) 26 else 16) {
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
            }
        }
    }
}

