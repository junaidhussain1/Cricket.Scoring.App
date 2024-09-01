package com.example.cricketscoringapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
fun StartNewMatchPage() {
    val context = LocalContext.current
    val dbHelper = CricketDatabaseHelper(context)
    val matchId = dbHelper.getMatchId()

    val showWidesDialog = remember { mutableStateOf(false) }
    val selectedWidesOption = remember { mutableStateOf("") }
    val showNoBallDialog = remember { mutableStateOf(false) }
    val selectedNoBallOption = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Retrieve the captain names from the view model
        val team1Captain = Player(dbHelper.getCaptainForTeam(matchId, 1))
        val team2Captain = Player(dbHelper.getCaptainForTeam(matchId, 2))

        val balls = remember {
            mutableStateListOf(
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
            )
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
                wickets = mutableIntStateOf(0)
            )
        }

        // Innings Score Box
        Box(
            modifier = Modifier
                .padding(8.dp)
                .border(
                    BorderStroke(2.dp, Color.Black),
                    shape = RoundedCornerShape(8.dp)
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
                    color1 = Color.LightGray
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
                    BorderStroke(2.dp, Color.Black),
                    shape = RoundedCornerShape(8.dp)
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
                    makeBatsmanTouchable = false
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
                    makeBatsmanTouchable = true
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
                    makeBatsmanTouchable = true
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
                    shape = RoundedCornerShape(8.dp)
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
                    makeBatsmanTouchable = false
                ) {}
                BatsmanBowlerBox(
                    col1 = bowlerStats.name.value,
                    col2 = String.format(Locale.UK, "%.1f", bowlerStats.over.value),
                    col3 = String.format(Locale.UK, "%d", bowlerStats.maiden.value),
                    col4 = String.format(Locale.UK, "%d", bowlerStats.runs.value),
                    col5 = String.format(Locale.UK, "%d", bowlerStats.wickets.value),
                    fontBold1 = FontWeight.Bold,
                    fontColor1 = Color(19, 207, 69),
                    makeBatsmanTouchable = false
                ) {}
            }
        }

        // This Over Box
        Box(
            modifier = Modifier
                .padding(8.dp)
                .border(
                    BorderStroke(2.dp, Color.Black),
                    shape = RoundedCornerShape(8.dp)
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
                            TextButton(onClick = {
                                selectedWidesOption.value = "W+3"
                                showWidesDialog.value = false
                                updateStats(balls,selectedWidesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("WIDE + 3", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedWidesOption.value = "W+4"
                                showWidesDialog.value = false
                                updateStats(balls,selectedWidesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("WIDE + 4", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedWidesOption.value = "W+5"
                                showWidesDialog.value = false
                                updateStats(balls,selectedWidesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("WIDE + 5", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedWidesOption.value = "W+6"
                                showWidesDialog.value = false
                                updateStats(balls,selectedWidesOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("WIDE + 6", fontSize = 20.sp)
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
//                updateStats(
//                    navController,
//                    balls,
//                    "NB",
//                    bowlerStats,
//                    firstBatsmanStats,
//                    secondBatsmanStats,
//                    firstBattingTeamStats,
//                    secondBattingTeamStats,
//                    runsToWin
//                )
            }
            if (showNoBallDialog.value) {
                AlertDialog(
                    onDismissRequest = { showNoBallDialog.value = false },
                    title = { Text("Select NO BALL Option") },
                    text = {
                        Column {
                            // List of options to choose from
                            TextButton(onClick = {
                                selectedNoBallOption.value = "NB"
                                showNoBallDialog.value = false
                                updateStats(balls,selectedNoBallOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("NO BALL", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedNoBallOption.value = "NB+1"
                                showNoBallDialog.value = false
                                updateStats(balls,selectedNoBallOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("NO BALL + 1", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedNoBallOption.value = "NB+2"
                                showNoBallDialog.value = false
                                updateStats(balls,selectedNoBallOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("NO BALL + 2", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedNoBallOption.value = "NB+3"
                                showNoBallDialog.value = false
                                updateStats(balls,selectedNoBallOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("NO BALL + 3", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedNoBallOption.value = "NB+4"
                                showNoBallDialog.value = false
                                updateStats(balls,selectedNoBallOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("NO BALL + 4", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedNoBallOption.value = "NB+5"
                                showNoBallDialog.value = false
                                updateStats(balls,selectedNoBallOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("NO BALL + 5", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                selectedNoBallOption.value = "NB+6"
                                showNoBallDialog.value = false
                                updateStats(balls,selectedNoBallOption.value,bowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats,runsToWin)
                            }) {
                                Text("NO BALL + 6", fontSize = 20.sp)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showNoBallDialog.value = false }) {
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
                updateStats(
                    balls,
                    "B",
                    bowlerStats,
                    firstBatsmanStats,
                    secondBatsmanStats,
                    firstBattingTeamStats,
                    secondBattingTeamStats,
                    runsToWin
                )
            }
            CircleButton("LEG BYE", fontSize = 16) {
                updateStats(
                    balls,
                    "LB",
                    bowlerStats,
                    firstBatsmanStats,
                    secondBatsmanStats,
                    firstBattingTeamStats,
                    secondBattingTeamStats,
                    runsToWin
                )
            }
            CircleButton("WICKET", fontSize = 16) {
                updateStats(
                    balls,
                    "WI",
                    bowlerStats,
                    firstBatsmanStats,
                    secondBatsmanStats,
                    firstBattingTeamStats,
                    secondBattingTeamStats,
                    runsToWin
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