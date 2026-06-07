package com.example.cricketscoringapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cricketscoringapp.ui.theme.CricketAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondInningsSetupPage(navController: NavHostController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val dbHelper = CricketDatabaseHelper(context)
    val matchId = dbHelper.getMatchId()
    val firstBattingTeamId = dbHelper.getTeamForPlayer(matchId,dbHelper.getFirstBattingTeamStriker(matchId))
    val battingTeamId = if (firstBattingTeamId == 1) { 2 } else { 1 }
    val bowlingTeamId = if (firstBattingTeamId == 1) { 1 } else { 2 }

    var facingBatsman by remember { mutableStateOf<Player?>(null) }
    var secondBatsman by remember { mutableStateOf<Player?>(null) }
    var openingBowler by remember { mutableStateOf<Player?>(null) }
    var openingKeeper by remember { mutableStateOf<Player?>(null) }

    val battingTeamList = remember { mutableStateListOf<Player>() }
    battingTeamList.clear()
    battingTeamList.addAll(dbHelper.getTeamPlayers(matchId, battingTeamId, 1))

    val bowlingTeamList = remember { mutableStateListOf<Player>() }
    bowlingTeamList.clear()
    bowlingTeamList.addAll(dbHelper.getTeamPlayers(matchId, bowlingTeamId, 1))

    var expanded4 by remember { mutableStateOf(false) }
    var expanded5 by remember { mutableStateOf(false) }
    var expanded6 by remember { mutableStateOf(false) }
    var expanded7 by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CricketAppTheme.colors.primaryDark
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            item {
                TeamStatsSection(matchId = matchId, pTeamId = 1, context = context)
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                // Row for Facing Batsman and Second Batsman
                Row(
                    modifier = Modifier

                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Facing Batsman
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded4,
                            onExpandedChange = { expanded4 = !expanded4 }
                        ) {
                            OutlinedTextField(
                                enabled = true,
                                readOnly = true,
                                value = facingBatsman?.name ?: "Select Batsman",
                                onValueChange = { },
                                label = {
                                    Text("Facing Batsman", fontSize = CricketAppTheme.dimens.smallSize)
                                },
                                textStyle = TextStyle(fontSize = CricketAppTheme.dimens.titleSize),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded4,
                                onDismissRequest = { expanded4 = false }
                            ) {
                                battingTeamList.forEach { player ->
                                    if (player.name != secondBatsman?.name) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(player.name, fontSize = CricketAppTheme.dimens.bodySize)
                                            },
                                            onClick = {
                                                facingBatsman = player
                                                val striker = dbHelper.getStriker(matchId)
                                                if (striker != "") {
                                                    dbHelper.updateStriker(matchId, player.name)
                                                } else {
                                                    dbHelper.addBattingStats(matchId, battingTeamId, player.name, "striker")
                                                }
                                                expanded4 = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Second Batsman
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded5,
                            onExpandedChange = { expanded5 = !expanded5 }
                        ) {
                            OutlinedTextField(
                                enabled = true,
                                readOnly = true,
                                value = secondBatsman?.name ?: "Select Batsman",
                                onValueChange = { },
                                label = {
                                    Text("Second Batsman", fontSize = CricketAppTheme.dimens.smallSize)
                                },
                                textStyle = TextStyle(fontSize = CricketAppTheme.dimens.titleSize),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded5,
                                onDismissRequest = { expanded5 = false }
                            ) {
                                battingTeamList.forEach { player ->
                                    if (player.name != facingBatsman?.name) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(player.name, fontSize = CricketAppTheme.dimens.bodySize)
                                            },
                                            onClick = {
                                                secondBatsman = player
                                                val nonStriker = dbHelper.getNonStriker(matchId)
                                                if (nonStriker != "") {
                                                    dbHelper.updateNonStriker(matchId, player.name)
                                                } else {
                                                    dbHelper.addBattingStats(matchId, battingTeamId, player.name, "non-striker")
                                                }
                                                expanded5 = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Row for Bowler and Keeper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Bowler
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded6,
                            onExpandedChange = { expanded6 = !expanded6 }
                        ) {
                            OutlinedTextField(
                                enabled = true,
                                readOnly = true,
                                value = openingBowler?.name ?: "Select Bowler",
                                onValueChange = { },
                                label = {
                                    Text("Bowler", fontSize = CricketAppTheme.dimens.smallSize)
                                },
                                textStyle = TextStyle(fontSize = CricketAppTheme.dimens.titleSize),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded6,
                                onDismissRequest = { expanded6 = false }
                            ) {
                                bowlingTeamList.forEach { player ->
                                    if (player.name != openingKeeper?.name) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(player.name, fontSize = CricketAppTheme.dimens.bodySize)
                                            },
                                            onClick = {
                                                openingBowler = player
                                                val bowler = dbHelper.getBowler(matchId)
                                                if (bowler != "") {
                                                    dbHelper.updateBowler(matchId, player.name)
                                                } else {
                                                    dbHelper.addBowlingStats(matchId, bowlingTeamId, player.name, "", "bowling")
                                                }
                                                expanded6 = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Keeper
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded7,
                            onExpandedChange = { expanded7 = !expanded7 }
                        ) {
                            OutlinedTextField(
                                enabled = true,
                                readOnly = true,
                                value = openingKeeper?.name ?: "Select Keeper",
                                onValueChange = { },
                                label = {
                                    Text("Wicket keeper", fontSize = CricketAppTheme.dimens.smallSize)
                                },
                                textStyle = TextStyle(fontSize = CricketAppTheme.dimens.titleSize),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded7,
                                onDismissRequest = { expanded7 = false }
                            ) {
                                bowlingTeamList.forEach { player ->
                                    if (player.name != openingBowler?.name) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(player.name, fontSize = CricketAppTheme.dimens.bodySize)
                                            },
                                            onClick = {
                                                openingKeeper = player
                                                val keeper = dbHelper.getKeeper(matchId)
                                                if (keeper != "") {
                                                    dbHelper.updateKeeper(matchId, player.name)
                                                } else {
                                                    dbHelper.updateBowlingStatsKeeper(matchId, bowlingTeamId, player.name)
                                                }
                                                expanded7 = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Show Continue button only if all fields are filled
                if (facingBatsman != null && secondBatsman != null && openingBowler != null && openingKeeper != null) {
                    Button(
                        onClick = {
                            dbHelper.insertPartnership(matchId, 2,
                                facingBatsman?.name ?: "",
                                secondBatsman?.name ?: ""
                            )
                            dbHelper.saveSnapshot(matchId)
                            navController.navigate("scorecard")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CricketAppTheme.colors.primaryCream)
                    ) {
                        Text(
                            text = "Continue Match",
                            fontSize = CricketAppTheme.dimens.buttonTextSize,
                            color = CricketAppTheme.colors.textOnLight
                        )
                    }
                }
            }
        }
    }
}
