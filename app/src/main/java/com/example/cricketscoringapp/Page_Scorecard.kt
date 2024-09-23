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
import java.util.Locale


@Composable
fun ScoreCardPage() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val dbHelper = CricketDatabaseHelper(context)
    val matchId = dbHelper.getMatchId()
    val currentBowler = remember { mutableStateOf(dbHelper.getCurrentBowler(matchId)) }
    val bowlingTeamId = dbHelper.getTeamForPlayer(matchId,currentBowler.value)
    val bowlingTeam = bowlingTeamId?.let { dbHelper.getTeamPlayers(matchId, it,1) }

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
                active = mutableStateOf(value = true)
            )
        }

        val team2Stats = dbHelper.getTeamStats(matchId,2,team2Captain.name)
        val secondBattingTeamStats = remember(team2Stats) {
            TeamStats(
                name = mutableStateOf(team2Stats.name.value),
                overs = mutableDoubleStateOf(team2Stats.overs.value),
                inningScore = mutableIntStateOf(team2Stats.inningScore.value),
                inningWickets = mutableIntStateOf(team2Stats.inningWickets.value),
                active = mutableStateOf(value = false)
            )
        }

        val firstBatsman = dbHelper.getBatsmanByStatus(matchId,"striker")
        val firstBatsmanStats = remember {
            BatsmanStats(
                name = mutableStateOf(value = firstBatsman.name.value),
                runs = mutableIntStateOf(value = firstBatsman.runs.value),
                balls = mutableIntStateOf(value = firstBatsman.balls.value),
                fours = mutableIntStateOf(value = firstBatsman.fours.value),
                sixes = mutableIntStateOf(value = firstBatsman.sixes.value),
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
                active = mutableStateOf(value = secondBatsman.active.value)
            )
        }

        val bowler = dbHelper.getCurrentBowlerStats(matchId)
        val bowlerStats = remember(bowler) {
            BowlerStats(
                name = mutableStateOf(bowler.name.value),
                over = mutableDoubleStateOf(bowler.over.value),
                maiden = mutableIntStateOf(bowler.maiden.value),
                runs = mutableIntStateOf(bowler.runs.value),
                wickets = mutableIntStateOf(bowler.wickets.value),
                noballs = mutableIntStateOf(bowler.noballs.value),
                wides = mutableIntStateOf(bowler.wides.value),
                byes = mutableIntStateOf(bowler.byes.value),
                legbyes = mutableIntStateOf(bowler.legbyes.value),
                fours = mutableIntStateOf(bowler.fours.value),
                sixes = mutableIntStateOf(bowler.sixes.value),
                keepername = mutableStateOf(bowler.keepername.value),
                overrecord = mutableStateOf(bowler.overrecord.value)
            )
        }

        val balls = remember { mutableStateListOf<Ball>() }

        if (bowlerStats.overrecord.value.contains(",")) {
            val ballValues = bowlerStats.overrecord.value.split("|")
            balls.clear()
            ballValues.forEach { value ->
                val pipeValues = value.split(",")
                val ball = Ball(pipeValues[0], pipeValues[1]) // Assuming Ball takes an Int
                balls.add(ball)
            }
        } else if (bowlerStats.overrecord.value != "") {
            val pipeValues = bowlerStats.overrecord.value.split(",")
            val ball = Ball(pipeValues[0], pipeValues[1]) // Assuming Ball takes an Int
            balls.add(ball)
        }

        //Handle end of over and end of innings
        if (endOfOverReached(balls)) {
            dbHelper.updateBowlingStats(matchId,"bowled")
            val ballsRemaining = calcRunsToWin(firstBattingTeamStats,secondBattingTeamStats, runsToWin)
            if (ballsRemaining > 0) {
                balls.clear()
                showBowlerChangeDialog.value = true
            } else {
                //end of innings
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
                    color1 = Color(19, 207, 69)
                )
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
                    color1 = Color.Black
                )
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
                    col1 = bowlerStats.name.value,
                    col2 = String.format(Locale.UK, "%.1f", bowlerStats.over.value),
                    col3 = String.format(Locale.UK, "%d", bowlerStats.maiden.value),
                    col4 = String.format(Locale.UK, "%d", bowlerStats.runs.value),
                    col5 = String.format(Locale.UK, "%d", bowlerStats.wickets.value),
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
                                bowlingTeam?.forEach { player ->
                                    if (bowlerStats.name.value != player.name) {
                                        val oversBowled = dbHelper.getBowlersOversBowled(
                                            matchId,
                                            bowlingTeamId,
                                            player.name
                                        )
                                        Button(
                                            onClick = {
                                                val existingBowler =
                                                    dbHelper.getLastBowler(matchId, bowlingTeamId)
                                                val existingKeeper =
                                                    dbHelper.getLastKeeper(matchId, bowlingTeamId)
                                                val newKeeper: String =
                                                    if (player.name == existingKeeper) {
                                                        existingBowler
                                                    } else {
                                                        existingKeeper
                                                    }
                                                if (manualBowlerChange.value) {
                                                    dbHelper.deleteCurrentBowler(matchId)
                                                    manualBowlerChange.value = false
                                                }
                                                dbHelper.addBowlingStats(
                                                    matchId,
                                                    bowlingTeamId,
                                                    player.name,
                                                    newKeeper,
                                                    "bowling"
                                                )
                                                showBowlerChangeDialog.value = false
                                                setCurrentBowlerAndKeeper(
                                                    bowlerStats,
                                                    player.name,
                                                    newKeeper
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
                                                oversBowled,
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
                    col1 = bowlerStats.keepername.value,
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
                                bowlingTeam?.forEach { player ->
                                    if ((bowlerStats.name.value != player.name) && (bowlerStats.keepername.value != player.name)) {
                                        Button(onClick = {
                                            dbHelper.updateBowlingStatsKeeper(
                                                matchId,
                                                bowlingTeamId,
                                                player.name
                                            )
                                            showKeeperChangeDialog.value = false
                                            setCurrentKeeper(bowlerStats, player.name)
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
                    bowlerStats,
                    firstBatsmanStats,
                    secondBatsmanStats,
                    firstBattingTeamStats,
                    secondBattingTeamStats,
                    runsToWin
                )
            }
            CircleButton("1", if (isTablet) 50 else 40) {
                updateStats(
                    context,
                    balls,
                    "1",
                    bowlerStats,
                    firstBatsmanStats,
                    secondBatsmanStats,
                    firstBattingTeamStats,
                    secondBattingTeamStats,
                    runsToWin
                )
            }
            CircleButton("2", if (isTablet) 50 else 40) {
                updateStats(
                    context,
                    balls,
                    "2",
                    bowlerStats,
                    firstBatsmanStats,
                    secondBatsmanStats,
                    firstBattingTeamStats,
                    secondBattingTeamStats,
                    runsToWin
                )
            }
            CircleButton("3", if (isTablet) 50 else 40) {
                updateStats(
                    context,
                    balls,
                    "3",
                    bowlerStats,
                    firstBatsmanStats,
                    secondBatsmanStats,
                    firstBattingTeamStats,
                    secondBattingTeamStats,
                    runsToWin
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
                    bowlerStats,
                    firstBatsmanStats,
                    secondBatsmanStats,
                    firstBattingTeamStats,
                    secondBattingTeamStats,
                    runsToWin
                )
            }
            CircleButton("6", if (isTablet) 50 else 40) {
                updateStats(
                    context,
                    balls,
                    "6",
                    bowlerStats,
                    firstBatsmanStats,
                    secondBatsmanStats,
                    firstBattingTeamStats,
                    secondBattingTeamStats,
                    runsToWin
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
                                updateStats(context,balls,selectedWidesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("WIDE", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedWidesOption.value = "W+1"
                                showWidesDialog.value = false
                                updateStats(context,balls,selectedWidesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("WIDE + 1", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedWidesOption.value = "W+2"
                                showWidesDialog.value = false
                                updateStats(context,balls,selectedWidesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
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
                                                        bowlerStats,
                                                        firstBatsmanStats,
                                                        secondBatsmanStats,
                                                        firstBattingTeamStats,
                                                        secondBattingTeamStats,
                                                        runsToWin
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
                                updateStats(context,balls,selectedByesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("1 BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedByesOption.value = "B2"
                                showByesDialog.value = false
                                updateStats(context,balls,selectedByesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("2 BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedByesOption.value = "B3"
                                showByesDialog.value = false
                                updateStats(context,balls,selectedByesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
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
                                updateStats(context,balls,selectedLegByesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("1 LEG-BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedLegByesOption.value = "LB2"
                                showLegByesDialog.value = false
                                updateStats(context,balls,selectedLegByesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("2 LEG-BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                selectedLegByesOption.value = "LB3"
                                showLegByesDialog.value = false
                                updateStats(context,balls,selectedLegByesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
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
                            when {
                                bowlingTeam!= null -> {
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
                val nonStrikerBatsman = getInactiveBatsman(firstBatsmanStats,secondBatsmanStats)

                val teamId = dbHelper.getTeamForPlayer(matchId, firstBatsmanStats.name.value)
                val players = teamId?.let { dbHelper.getTeamPlayers(matchId, it,1) }

                //Remove Players who have batted/got out twice already
                val battedFullyAlreadyPlayers =
                    teamId?.let { dbHelper.getFullyBattedAlreadyPlayers(matchId, it) }

                val availablePlayers = players?.filter { player ->
                    battedFullyAlreadyPlayers?.contains(player) == false && player != Player(nonStrikerBatsman)
                }
                //For current user, if current time getting out is second turn then don't allow any more

                AlertDialog(
                    onDismissRequest = {
                        showNextBatsmanDialog.value = false
                        showWicketsDialog.value = true
                                       },
                    title = { Text("Select NEXT Batsman") },
                    text = {
                        Column {
                            when {
                                availablePlayers != null -> {
                                    availablePlayers.forEach { player ->
                                        // List of options to choose from
                                        Button( modifier = Modifier.fillMaxWidth(), onClick = {
                                            showNextBatsmanDialog.value = false
                                            val batsmanOut:String = if (firstBatsmanStats.active.value) {
                                                firstBatsmanStats.name.value
                                            } else {
                                                secondBatsmanStats.name.value
                                            }

                                            val newBatsman = player.name
                                            selectedWicketsOption.value += ",$batsmanOut,$newBatsman"
                                            updateStats(context,balls,selectedWicketsOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)

                                            val wicketDescription = getWicketDescription(selectedWicketsOption.value,currentBowler.value,selectedFielder.value)

                                            if (firstBatsmanStats.active.value) {
                                                //save out batsman to database
                                                dbHelper.updateBattingStats(matchId,"out",firstBatsmanStats,wicketDescription)
                                                
                                                firstBatsmanStats.name.value = newBatsman
                                                firstBatsmanStats.active.value = true
                                                firstBatsmanStats.runs.value = 0
                                                firstBatsmanStats.balls.value = 0
                                                firstBatsmanStats.fours.value = 0
                                                firstBatsmanStats.sixes.value = 0
                                            } else {
                                                //save out batsman to database
                                                dbHelper.updateBattingStats(matchId,"out",secondBatsmanStats,wicketDescription)

                                                secondBatsmanStats.name.value = newBatsman
                                                secondBatsmanStats.active.value = true
                                                secondBatsmanStats.runs.value = 0
                                                secondBatsmanStats.balls.value = 0
                                                secondBatsmanStats.fours.value = 0
                                                secondBatsmanStats.sixes.value = 0
                                            }
                                            dbHelper.addBattingStats(matchId,teamId,newBatsman,"striker")
                                        }) {
                                            Text(player.name, fontSize = if (isTablet) 30.sp else 20.sp)
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
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
            CircleButton("UNDO", if (isTablet) 26 else 16) {
                updateStats(
                    context,
                    balls,
                    "UNDO",
                    bowlerStats,
                    firstBatsmanStats,
                    secondBatsmanStats,
                    firstBattingTeamStats,
                    secondBattingTeamStats,
                    runsToWin
                )
            }
        }
    }
}

