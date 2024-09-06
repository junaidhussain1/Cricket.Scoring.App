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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale


@Composable
fun ScoreCardPage() {
    val context = LocalContext.current
    val dbHelper = CricketDatabaseHelper(context)
    val matchId = dbHelper.getMatchId()

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

        val balls = remember {
            mutableStateListOf<Ball>()
        }
        val runsToWin = remember { mutableStateOf("") }

        val firstBattingTeamStats = remember {
            TeamStats(
                name = mutableStateOf(team1Captain.name),
                overs = mutableDoubleStateOf(0.0),
                inningScore = mutableIntStateOf(0),
                inningWickets = mutableIntStateOf(0),
                active = mutableStateOf(value = true)
            )
        }

        val secondBattingTeamStats = remember {
            TeamStats(
                name = mutableStateOf(team2Captain.name),
                overs = mutableDoubleStateOf(0.0),
                inningScore = mutableIntStateOf(value = 0),
                inningWickets = mutableIntStateOf(value = 0),
                active = mutableStateOf(value = false)
            )
        }

        val firstBatsmanStats = remember {
            BatsmanStats(
                name = mutableStateOf(dbHelper.getBatsmanByStatus(matchId,"striker")),
                runs = mutableIntStateOf(value = 0),
                balls = mutableIntStateOf(value = 0),
                fours = mutableIntStateOf(value = 0),
                sixes = mutableIntStateOf(value = 0),
                active = mutableStateOf(value = true)
            )
        }

        val secondBatsmanStats = remember {
            BatsmanStats(
                name = mutableStateOf(dbHelper.getBatsmanByStatus(matchId,"non-striker")),
                runs = mutableIntStateOf(value = 0),
                balls = mutableIntStateOf(value = 0),
                fours = mutableIntStateOf(value = 0),
                sixes = mutableIntStateOf(value = 0),
                active = mutableStateOf(value = false)
            )
        }

        val bowlerStats = remember {
            BowlerStats(
                name = mutableStateOf(dbHelper.getCurrentBowler(matchId)),
                over = mutableDoubleStateOf(.0),
                maiden = mutableIntStateOf(0),
                runs = mutableIntStateOf(0),
                wickets = mutableIntStateOf(0),
                noballs = mutableIntStateOf(0),
                wides = mutableIntStateOf(0),
                byes = mutableIntStateOf(0),
                legbyes = mutableIntStateOf(0)
            )
        }

        // Innings Score Box
        Box(
            modifier = Modifier
                .padding(8.dp)
                .border(
                    BorderStroke(2.dp, Color.White),
                    //shape = RoundedCornerShape(8.dp)
                ) // Set border thickness and color
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
                    //shape = RoundedCornerShape(8.dp)
                ) // Set border thickness and color
                .fillMaxWidth()
        ) {
            Column {
                BatsmanBowlerBox(
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
                BatsmanBowlerBox(
                    col1 = firstBatsmanStats.name.value,
                    col2 = String.format(Locale.UK, "%d", firstBatsmanStats.runs.value),
                    col3 = String.format(Locale.UK, "%d", firstBatsmanStats.balls.value),
                    col4 = String.format(Locale.UK, "%d", firstBatsmanStats.fours.value),
                    col5 = String.format(Locale.UK, "%d", firstBatsmanStats.sixes.value),
                    fontBold1 = fontBold2,
                    fontColor1 = fontColor2,
                    makePlayerTouchable = true
                ) {
                    swapBatsmen(firstBatsmanStats, secondBatsmanStats)
                }

                BatsmanBowlerBox(
                    col1 = secondBatsmanStats.name.value,
                    col2 = String.format(Locale.UK, "%d", secondBatsmanStats.runs.value),
                    col3 = String.format(Locale.UK, "%d", secondBatsmanStats.balls.value),
                    col4 = String.format(Locale.UK, "%d", secondBatsmanStats.fours.value),
                    col5 = String.format(Locale.UK, "%d", secondBatsmanStats.sixes.value),
                    fontBold1 = fontBold3,
                    fontColor1 = fontColor3,
                    makePlayerTouchable = true
                ) {
                    swapBatsmen(firstBatsmanStats, secondBatsmanStats)
                }
            }
        }

        // Bowler Box
        Box(
            modifier = Modifier
                .padding(8.dp)
                .border(
                    BorderStroke(2.dp, Color.Black),
                    //shape = RoundedCornerShape(8.dp)
                ) // Set border thickness and color
                .fillMaxWidth()
        ) {
            Column {
                BatsmanBowlerBox(
                    col1 = "Bowler",
                    col2 = "O",
                    col3 = "M",
                    col4 = "R",
                    col5 = "W",
                    fontBold1 = FontWeight.Bold,
                    fontColor1 = Color.Gray,
                    makePlayerTouchable = false
                ) {}
                BatsmanBowlerBox(
                    col1 = bowlerStats.name.value,
                    col2 = String.format(Locale.UK, "%.1f", bowlerStats.over.value),
                    col3 = String.format(Locale.UK, "%d", bowlerStats.maiden.value),
                    col4 = String.format(Locale.UK, "%d", bowlerStats.runs.value),
                    col5 = String.format(Locale.UK, "%d", bowlerStats.wickets.value),
                    fontBold1 = FontWeight.Bold,
                    fontColor1 = Color(19, 207, 69),
                    makePlayerTouchable = true
                ) {
                    //validate to make sure it is start of over.
                    //if it is start of over, then show lookup to change bowler.
                }
            }
        }

        // This Over Box
        Box(
            modifier = Modifier
                .padding(8.dp)
                .border(
                    BorderStroke(2.dp, Color.Black),
                    //shape = RoundedCornerShape(8.dp)
                ) // Set border thickness and color
                .fillMaxWidth()
        ) {
            Column {
                OverBox(
                    heading1 = "This Over:",
                    balls = balls,
                    headingFontBold1 = FontWeight.Normal,
                    ballsFontBold1 = FontWeight.Bold,
                    backcolor1 = Color.White,
                    fontColor1 = Color.Black
                )
            }
        }

        // 1st Row with 4 circle buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CircleButton("0", fontSize = 40) {
                updateStats(
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
            CircleButton("1", fontSize = 40) {
                updateStats(
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
            CircleButton("2", fontSize = 40) {
                updateStats(
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
            CircleButton("3", fontSize = 40) {
                updateStats(
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CircleButton("4", fontSize = 40) {
                updateStats(
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
            CircleButton("6", fontSize = 40) {
                updateStats(
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
            CircleButton("WIDE", fontSize = 16) {
                showWidesDialog.value = true
            }
            if (showWidesDialog.value) {
                AlertDialog(
                    onDismissRequest = { showWidesDialog.value = false },
                    title = { Text("Select WIDE Option") },
                    text = {
                        Column {
                            // List of options to choose from
                            TextButton(onClick = {
                                selectedWidesOption.value = "W"
                                showWidesDialog.value = false
                                updateStats(balls,selectedWidesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("WIDE", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedWidesOption.value = "W+1"
                                showWidesDialog.value = false
                                updateStats(balls,selectedWidesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("WIDE + 1", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedWidesOption.value = "W+2"
                                showWidesDialog.value = false
                                updateStats(balls,selectedWidesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("WIDE + 2", fontSize = 20.sp)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showWidesDialog.value = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            CircleButton("NO BALL", fontSize = 16) {
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
                                                    fontSize = 16.sp)
                                            }
                                        }
                                    }
                                }

                                if (row == 2) {  // Assuming the NBL buttons are in row 2
                                    Spacer(modifier = Modifier.height(16.dp)) // Add space of 16.dp between NBL and NBB
                                }
                                if (row == 3) {  // Assuming the NBL buttons are in row 2
                                    Spacer(modifier = Modifier.height(16.dp)) // Add space of 16.dp between NBL and NBB
                                }
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CircleButton("BYE", fontSize = 16) {
                showByesDialog.value = true
            }
            if (showByesDialog.value) {
                AlertDialog(
                    onDismissRequest = { showByesDialog.value = false },
                    title = { Text("Select BYES Option") },
                    text = {
                        Column {
                            // List of options to choose from
                            TextButton(onClick = {
                                selectedByesOption.value = "B1"
                                showByesDialog.value = false
                                updateStats(balls,selectedByesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("1 BYE", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedNoBallOption.value = "B2"
                                showByesDialog.value = false
                                updateStats(balls,selectedByesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("2 BYE", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedNoBallOption.value = "B3"
                                showByesDialog.value = false
                                updateStats(balls,selectedByesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("3 BYE", fontSize = 20.sp)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showByesDialog.value = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            CircleButton("LEG BYE", fontSize = 16) {
                showLegByesDialog.value = true
            }
            if (showLegByesDialog.value) {
                AlertDialog(
                    onDismissRequest = { showLegByesDialog.value = false },
                    title = { Text("Select LEG BYES Option") },
                    text = {
                        Column {
                            // List of options to choose from
                            TextButton(onClick = {
                                selectedLegByesOption.value = "LB1"
                                showLegByesDialog.value = false
                                updateStats(balls,selectedLegByesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("1 LEG-BYE", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedLegByesOption.value = "LB2"
                                showLegByesDialog.value = false
                                updateStats(balls,selectedLegByesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("2 LEG-BYE", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedLegByesOption.value = "LB3"
                                showLegByesDialog.value = false
                                updateStats(balls,selectedLegByesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("3 LEG-BYE", fontSize = 20.sp)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showLegByesDialog.value = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            CircleButton("WICKET", fontSize = 16) {
                showWicketsDialog.value = true
            }
            if (showWicketsDialog.value) {
                AlertDialog(
                    onDismissRequest = { showWicketsDialog.value = false },
                    title = { Text("Select WICKETS Option") },
                    text = {
                        Column {
                            // List of options to choose from
                            TextButton(onClick = {
                                selectedWicketsOption.value = "WKB"
                                showWicketsDialog.value = false
                                showNextBatsmanDialog.value = true
                            }) {
                                Text("Bowled", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedWicketsOption.value = "WKCB"
                                showWicketsDialog.value = false
                                showNextBatsmanDialog.value = true
                            }) {
                                Text("Caught Behind", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedWicketsOption.value = "WKC"
                                showWicketsDialog.value = false
                                showNextBatsmanDialog.value = true
                            }) {
                                Text("Caught", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedWicketsOption.value = "WKRO"
                                showWicketsDialog.value = false
                                showNextBatsmanDialog.value = true
                            }) {
                                Text("Run Out", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedWicketsOption.value = "WKRONB"
                                showWicketsDialog.value = false
                                showNextBatsmanDialog.value = true
                            }) {
                                Text("Run Out NB", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedWicketsOption.value = "WKST"
                                showWicketsDialog.value = false
                                showNextBatsmanDialog.value = true
                            }) {
                                Text("Stumped", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedWicketsOption.value = "WKSTW"
                                showWicketsDialog.value = false
                                showNextBatsmanDialog.value = true
                            }) {
                                Text("Stumped Wide", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedWicketsOption.value = "WKHW"
                                showWicketsDialog.value = false
                                showNextBatsmanDialog.value = true
                            }) {
                                Text("Hit Wicket", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedWicketsOption.value = "WKLB"
                                showWicketsDialog.value = false
                                showNextBatsmanDialog.value = true
                            }) {
                                Text("LBW", fontSize = 20.sp)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showWicketsDialog.value = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showNextBatsmanDialog.value) {

                //Get List of Team Players
                val nonStrikerBatsman = if (firstBatsmanStats.active.value) {
                    secondBatsmanStats.name.value
                } else {
                    firstBatsmanStats.name.value
                }
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
                                        TextButton(onClick = {
                                            showNextBatsmanDialog.value = false
                                            updateStats(balls,selectedWicketsOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                                            if (firstBatsmanStats.active.value) {
                                                firstBatsmanStats.name.value = player.name
                                                firstBatsmanStats.active.value = true
                                                firstBatsmanStats.runs.value = 0
                                                firstBatsmanStats.balls.value = 0
                                                firstBatsmanStats.fours.value = 0
                                                firstBatsmanStats.sixes.value = 0
                                            } else {
                                                secondBatsmanStats.name.value = player.name
                                                secondBatsmanStats.active.value = true
                                                secondBatsmanStats.runs.value = 0
                                                secondBatsmanStats.balls.value = 0
                                                secondBatsmanStats.fours.value = 0
                                                secondBatsmanStats.sixes.value = 0
                                            }
                                        }) {
                                            Text(player.name, fontSize = 20.sp)
                                        }
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
            CircleButton("UNDO", fontSize = 16) {
                updateStats(
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