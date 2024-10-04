package com.example.cricketscoringapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

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
    battingTeamList.addAll(
        dbHelper.getTeamPlayers(
            matchId,
            battingTeamId,
            1
        )
    )
    val bowlingTeamList = remember { mutableStateListOf<Player>() }
    bowlingTeamList.clear()
    bowlingTeamList.addAll(
        dbHelper.getTeamPlayers(
            matchId,
            bowlingTeamId,
            1
        )
    )

    var expanded4 by remember { mutableStateOf(false) }
    var expanded5 by remember { mutableStateOf(false) }
    var expanded6 by remember { mutableStateOf(false) }
    var expanded7 by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(10, 18, 32)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row to hold both Facing Batsman and Second Batsman
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // Facing Batsman (Striker)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded4,
                        onExpandedChange = {
                                expanded4 = !expanded4
                        }
                    ) {
                        OutlinedTextField(
                            enabled = true,
                            readOnly = true,
                            value = facingBatsman?.name ?: "Select Batsman",
                            onValueChange = { },
                            label = {
                                Text(
                                    "Facing Batsman",
                                    fontSize = if (isTablet) 22.sp else 14.sp
                                )
                            },
                            textStyle = TextStyle(fontSize = if (isTablet) 32.sp else 14.sp),
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
                                    androidx.compose.material3.DropdownMenuItem(
                                        enabled = true,
                                        text = {
                                            Text(
                                                text = player.name,
                                                fontSize = if (isTablet) 30.sp else 14.sp
                                            )
                                        },
                                        onClick = {
                                            facingBatsman = player
                                            facingBatsman?.name?.let {
                                                val striker =
                                                    dbHelper.getStriker(matchId)
                                                if (striker != "") {
                                                    dbHelper.updateStriker(
                                                        matchId,
                                                        it
                                                    )
                                                } else {
                                                    dbHelper.addBattingStats(
                                                        matchId,
                                                        battingTeamId,
                                                        it,
                                                        "striker"
                                                    )
                                                }
                                            }
                                            expanded4 = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Second Batsman (Non Striker)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded5,
                        onExpandedChange = {
                                expanded5 = !expanded5
                        }
                    ) {
                        OutlinedTextField(
                            enabled = true,
                            readOnly = true,
                            value = secondBatsman?.name ?: "Select Batsman",
                            onValueChange = { },
                            label = {
                                Text(
                                    "Second Batsman",
                                    fontSize = if (isTablet) 22.sp else 14.sp
                                )
                            },
                            textStyle = TextStyle(fontSize = if (isTablet) 32.sp else 14.sp),
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
                                    androidx.compose.material3.DropdownMenuItem(
                                        enabled = true,
                                        text = {
                                            Text(
                                                text = player.name,
                                                fontSize = if (isTablet) 30.sp else 14.sp
                                            )
                                        },
                                        onClick = {
                                            secondBatsman = player
                                                secondBatsman?.name?.let {
                                                    val nonStriker =
                                                        dbHelper.getNonStriker(matchId)
                                                    if (nonStriker != "") {
                                                        dbHelper.updateNonStriker(
                                                            matchId,
                                                            it
                                                        )
                                                    } else {
                                                        dbHelper.addBattingStats(
                                                            matchId,
                                                            battingTeamId,
                                                            it,
                                                            "non-striker"
                                                        )
                                                    }
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

            // Row to hold both Bowler and Wicket Keeper
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Select Bowler
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded6,
                        onExpandedChange = {
                                expanded6 = !expanded6
                        }
                    ) {
                        OutlinedTextField(
                            enabled = true,
                            readOnly = true,
                            value = openingBowler?.name ?: "Select Bowler",
                            onValueChange = { },
                            label = {
                                Text(
                                    "Bowler",
                                    fontSize = if (isTablet) 22.sp else 14.sp
                                )
                            },
                            textStyle = TextStyle(fontSize = if (isTablet) 32.sp else 14.sp),
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
                                    androidx.compose.material3.DropdownMenuItem(
                                        enabled = true,
                                        text = {
                                            Text(
                                                text = player.name,
                                                fontSize = if (isTablet) 30.sp else 14.sp
                                            )
                                        },
                                        onClick = {
                                            openingBowler = player
                                            val bowler =
                                                dbHelper.getBowler(matchId)
                                            if (bowler != "") {
                                                dbHelper.updateBowler(
                                                    matchId,
                                                    player.name
                                                )
                                            } else {
                                                dbHelper.addBowlingStats(
                                                    matchId,
                                                    bowlingTeamId,
                                                    player.name,
                                                    "",
                                                    "bowling"
                                                )
                                            }
                                            expanded6 = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Select Wicket-keeper
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded7,
                        onExpandedChange = {
                                expanded7 = !expanded7
                        }
                    ) {
                        OutlinedTextField(
                            enabled = true,
                            readOnly = true,
                            value = openingKeeper?.name ?: "Select Keeper",
                            onValueChange = { },
                            label = {
                                Text(
                                    "Wicket keeper",
                                    fontSize = if (isTablet) 22.sp else 14.sp
                                )
                            },
                            textStyle = TextStyle(fontSize = if (isTablet) 32.sp else 14.sp),
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
                                    androidx.compose.material3.DropdownMenuItem(
                                        enabled = true,
                                        text = {
                                            Text(
                                                text = player.name,
                                                fontSize = if (isTablet) 30.sp else 14.sp
                                            )
                                        },
                                        onClick = {
                                            openingKeeper = player
                                            val keeper =
                                                dbHelper.getKeeper(matchId)
                                            if (keeper != "") {
                                                dbHelper.updateKeeper(
                                                    matchId,
                                                    player.name
                                                )
                                            } else {
                                                dbHelper.updateBowlingStatsKeeper(
                                                    matchId,
                                                    bowlingTeamId,
                                                    player.name
                                                )
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

            //Only show if both teams contain 6 players and batting team, keeper and bowler have been set
            if ((facingBatsman != null)
                && (secondBatsman != null)
                && (openingBowler != null)
                && (openingKeeper != null)
            ) {
                Button(
                    onClick = {
                        navController.navigate("scorecard")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(255, 252, 228)
                    )
                ) {
                    Text(text = "Continue Match",
                        fontSize = if (isTablet) 22.sp else 16.sp)
                }
            }
        }
    }
}

