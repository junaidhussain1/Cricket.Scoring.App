package com.example.cricketscoringapp


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.cricketscoringapp.ui.theme.CricketAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMatchSetupPage(navController: NavHostController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Performance: Initialize dbHelper and basic match values only once
    val dbHelper = remember { CricketDatabaseHelper(context) }
    val matchId = remember { dbHelper.getMatchId() }
    val matchStarted by remember { mutableStateOf(dbHelper.getIsMatchStarted(matchId)) }

    val playersList = remember { mutableStateListOf<Player>().also { it.addAll(dbHelper.getAllPlayers()) } }
    
    var refreshKey1 by remember { mutableStateOf(0) }
    var refreshKey2 by remember { mutableStateOf(0) }
    
    // Performance: Cache initial state from DB
    var team1Captain by remember(refreshKey1) { mutableStateOf<Player?>(Player(dbHelper.getCaptainForTeam(matchId, 1))) }
    var team2Captain by remember(refreshKey2) { mutableStateOf<Player?>(Player(dbHelper.getCaptainForTeam(matchId, 2))) }
    var battingTeamCaptain by remember(refreshKey1, refreshKey2) { mutableStateOf<Player?>(Player(dbHelper.getBattingTeamCaptain(matchId, 1))) }
    var facingBatsman by remember { mutableStateOf<Player?>(Player(dbHelper.getFirstBattingTeamStriker(matchId))) }
    var secondBatsman by remember { mutableStateOf<Player?>(Player(dbHelper.getFirstBattingTeamNonStriker(matchId))) }
    var openingBowler by remember { mutableStateOf<Player?>(Player(dbHelper.getSecondBattingTeamBowler(matchId))) }
    var openingKeeper by remember { mutableStateOf<Player?>(Player(dbHelper.getSecondBattingTeamKeeper(matchId))) }

    var sideWallRule by remember { mutableStateOf(dbHelper.getSideWallRule(matchId)) }
    var noOfOversAside by remember { mutableStateOf(dbHelper.getNoOfOversAside(matchId)) }
    var noOfPlayersAside by remember { mutableStateOf(dbHelper.getNoOfPlayersAside(matchId)) }

    // Performance: Cache heavy DB calls
    val team1Players = remember(matchId, refreshKey1) { dbHelper.getTeamPlayers(matchId, 1, 1) }
    val team2Players = remember(matchId, refreshKey2) { dbHelper.getTeamPlayers(matchId, 2, 1) }
    val team1Size = remember(matchId, refreshKey1) { dbHelper.getTeamSize(matchId, 1) }
    val team2Size = remember(matchId, refreshKey2) { dbHelper.getTeamSize(matchId, 2) }

    var expanded00 by remember { mutableStateOf(false) }
    var expanded01 by remember { mutableStateOf(false) }
    var expanded02 by remember { mutableStateOf(false) }
    var expanded03 by remember { mutableStateOf(false) }
    var expanded04 by remember { mutableStateOf(false) }
    var expanded05 by remember { mutableStateOf(false) }
    var expanded06 by remember { mutableStateOf(false) }
    var expanded07 by remember { mutableStateOf(false) }
    var expanded08 by remember { mutableStateOf(false) }
    var expanded09 by remember { mutableStateOf(false) }

    // Performance: Side effect for date update
    LaunchedEffect(matchId) {
        val currentDate = LocalDate.now()
        val sqlFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        dbHelper.updateMatchDate(matchId, currentDate.format(sqlFormatter))
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CricketAppTheme.colors.primaryDark
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Row to hold Recycle Bin Button and "New Match" text in the same row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentDate = LocalDate.now()
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val formattedDate = currentDate.format(formatter)

                    Text(
                        text = "Match: $formattedDate",
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = CricketAppTheme.dimens.headerSize,
                        color = CricketAppTheme.colors.textOnDark
                    )

                    IconButton(
                        onClick = {
                            dbHelper.deleteMatch(matchId)
                            navController.navigate("homepage")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Recycle Bin",
                            tint = CricketAppTheme.colors.textOnDark
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Row to hold no of Overs Aside, no of Players Aside & Side Wall Rule
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // No of Overs Aside
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded00,
                            onExpandedChange = {
                                if (!matchStarted && team1Captain?.name.orEmpty().isEmpty() && team2Captain?.name.orEmpty().isEmpty()) {
                                    expanded00 = !expanded00
                                }
                            }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                enabled = true,
                                value = noOfOversAside.toString(),
                                onValueChange = { },
                                label = {
                                    Text(
                                        "Overs aside",
                                        fontSize = CricketAppTheme.dimens.smallSize
                                    )
                                },
                                textStyle = TextStyle(fontSize = CricketAppTheme.dimens.titleSize),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded00,
                                onDismissRequest = { expanded00 = false }
                            ) {
                                for (i in 1..50)
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = i.toString(),
                                                fontSize = CricketAppTheme.dimens.bodySize
                                            )
                                        },
                                        onClick = {
                                            noOfOversAside = i
                                            dbHelper.updateOversAside(matchId, noOfOversAside)
                                            expanded00 = false
                                        }
                                    )
                            }
                        }
                    }

                    // No of Players Aside
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp, end = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded01,
                            onExpandedChange = {
                                if (!matchStarted && team1Captain?.name.orEmpty().isEmpty() && team2Captain?.name.orEmpty().isEmpty()) {
                                    expanded01 = !expanded01
                                }
                            }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                enabled = true,
                                value = noOfPlayersAside.toString(),
                                onValueChange = { },
                                label = {
                                    Text(
                                        text = "Players aside",
                                        fontSize = CricketAppTheme.dimens.smallSize
                                    )
                                },
                                textStyle = TextStyle(fontSize = CricketAppTheme.dimens.titleSize),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded01,
                                onDismissRequest = { expanded01 = false }
                            ) {
                                for (i in 5..12)
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = i.toString(),
                                                fontSize = CricketAppTheme.dimens.bodySize
                                            )
                                        },
                                        onClick = {
                                            noOfPlayersAside = i
                                            dbHelper.updatePlayersAside(matchId, noOfPlayersAside)
                                            expanded01 = false
                                        }
                                    )
                            }
                        }
                    }

                    // Side Wall Rule
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded09,
                            onExpandedChange = {
                                if (!matchStarted && team1Captain?.name.orEmpty().isEmpty() && team2Captain?.name.orEmpty().isEmpty()) {
                                    expanded09 = !expanded09
                                }
                            }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                enabled = true,
                                value = "+$sideWallRule",
                                onValueChange = { },
                                label = {
                                    Text(
                                        text = "Side wall rule",
                                        fontSize = CricketAppTheme.dimens.smallSize
                                    )
                                },
                                textStyle = TextStyle(fontSize = CricketAppTheme.dimens.titleSize),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded09,
                                onDismissRequest = { expanded09 = false }
                            ) {
                                for (i in 0..2)
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = i.toString(),
                                                fontSize = CricketAppTheme.dimens.bodySize
                                            )
                                        },
                                        onClick = {
                                            sideWallRule = i
                                            dbHelper.updateSideWallRule(matchId, sideWallRule)
                                            expanded09 = false
                                        }
                                    )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // Row to hold both Team 1 Captain and Team 2 Captain dropdowns
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Team 1 Captain Dropdown
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded02,
                            onExpandedChange = {
                                if (!matchStarted) {
                                    expanded02 = !expanded02
                                }
                            }
                        ) {
                            OutlinedTextField(
                                enabled = !matchStarted,
                                readOnly = true,
                                value = team1Captain?.name ?: "Select Captain",
                                onValueChange = { },
                                label = {
                                    Text(
                                        "Team 1 Captain",
                                        fontSize = CricketAppTheme.dimens.smallSize
                                    )
                                },
                                textStyle = TextStyle(fontSize = CricketAppTheme.dimens.titleSize),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded02,
                                onDismissRequest = { expanded02 = false }
                            ) {
                                playersList.forEach { player ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        enabled = !matchStarted,
                                        text = {
                                            Text(
                                                text = player.name,
                                                fontSize = CricketAppTheme.dimens.bodySize
                                            )
                                        },
                                        onClick = {
                                            team1Captain = player
                                            team1Captain?.let {
                                                dbHelper.addTeamPlayer(
                                                    matchId,
                                                    1,
                                                    player.name,
                                                    1,
                                                    0
                                                )
                                            }
                                            refreshKey1++
                                            expanded02 = false
                                        }
                                    )
                                }
                            }
                        }
                    }


                    // Team 2 Captain Dropdown
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded03,
                            onExpandedChange = {
                                if (!matchStarted) {
                                    expanded03 = !expanded03
                                }
                            }
                        ) {
                            OutlinedTextField(
                                enabled = !matchStarted,
                                readOnly = true,
                                value = team2Captain?.name ?: "Select Captain",
                                onValueChange = { },
                                label = {
                                    Text(
                                        text = "Team 2 Captain",
                                        fontSize = CricketAppTheme.dimens.smallSize
                                    )
                                },
                                textStyle = TextStyle(fontSize = CricketAppTheme.dimens.titleSize),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded03,
                                onDismissRequest = { expanded03 = false }
                            ) {
                                val remainingPlayers = playersList.filter { it != team1Captain }
                                remainingPlayers.forEach { player ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        enabled = !matchStarted,
                                        text = {
                                            Text(
                                                text = player.name,
                                                fontSize = CricketAppTheme.dimens.bodySize
                                            )
                                        },
                                        onClick = {
                                            team2Captain = player
                                            team2Captain?.let {
                                                dbHelper.addTeamPlayer(
                                                    matchId,
                                                    2,
                                                    player.name,
                                                    1,
                                                    0
                                                )
                                            }
                                            refreshKey2++
                                            expanded03 = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val textColor = if (!matchStarted) CricketAppTheme.colors.textOnDark else CricketAppTheme.colors.secondaryGray

                if ((team1Captain?.name.orEmpty().isNotEmpty()) && (team2Captain?.name.orEmpty().isNotEmpty())) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Team 1 column: Button + Players List
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Buttons to navigate to team player selection pages
                            if (team1Captain?.name.orEmpty().isNotEmpty()) {
                                Button(
                                    enabled = !matchStarted,
                                    onClick = {
                                        navController.navigate("team1PlayerSelection")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CricketAppTheme.colors.primaryCream
                                    ),
                                    content = {
                                        Text(
                                            text = "Select Team Players",
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth(),
                                            fontSize = CricketAppTheme.dimens.buttonTextSize,
                                            color = CricketAppTheme.colors.textOnLight
                                        )
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                for (player in team1Players) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = player.name,
                                        color = textColor,
                                        fontSize = CricketAppTheme.dimens.bodySize
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Team 2 column: Button + Players List
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            if (team2Captain?.name.orEmpty().isNotEmpty()) {
                                Button(
                                    enabled = !matchStarted,
                                    onClick = {
                                        navController.navigate("team2PlayerSelection")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CricketAppTheme.colors.primaryCream
                                    ),
                                    content = {
                                        Text(
                                            text = "Select Team Players",
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth(),
                                            fontSize = CricketAppTheme.dimens.buttonTextSize,
                                            color = CricketAppTheme.colors.textOnLight
                                        )
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                for (player in team2Players) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = player.name,
                                        color = textColor,
                                        fontSize = CricketAppTheme.dimens.bodySize
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if ((team1Size == noOfPlayersAside) && (team2Size == noOfPlayersAside)) {
                    // Select Batting Team Captain
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ExposedDropdownMenuBox(
                                expanded = expanded04,
                                onExpandedChange = {
                                    if (!matchStarted) {
                                        expanded04 = !expanded04
                                    }
                                }
                            ) {
                                OutlinedTextField(
                                    enabled = !matchStarted,
                                    readOnly = true,
                                    value = battingTeamCaptain?.name ?: "Select Captain",
                                    onValueChange = { },
                                    label = {
                                        Text(
                                            "Batting Team",
                                            fontSize = CricketAppTheme.dimens.smallSize
                                        )
                                    },
                                    textStyle = TextStyle(fontSize = CricketAppTheme.dimens.titleSize),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded04,
                                    onDismissRequest = { expanded04 = false }
                                ) {
                                    team1Captain?.name?.let {
                                        androidx.compose.material3.DropdownMenuItem(
                                            enabled = !matchStarted,
                                            text = {
                                                Text(
                                                    text = it,
                                                    fontSize = CricketAppTheme.dimens.bodySize
                                                )
                                            },
                                            onClick = {
                                                battingTeamCaptain = team1Captain
                                                dbHelper.updateMatchCaptain(matchId, 1, it)
                                                dbHelper.updateMatchCaptain(matchId, 2, team2Captain!!.name)
                                                val teamA = dbHelper.getTeamForPlayer(matchId, it)
                                                if (teamA == 2) {
                                                    dbHelper.updateTeamID(matchId, 1, 3)
                                                    dbHelper.updateTeamID(matchId, 2, 1)
                                                    dbHelper.updateTeamID(matchId, 3, 2)
                                                }
                                                refreshKey1++
                                                refreshKey2++
                                                expanded04 = false
                                                facingBatsman = null
                                                secondBatsman = null
                                            }
                                        )
                                    }

                                    team2Captain?.name?.let {
                                        androidx.compose.material3.DropdownMenuItem(
                                            enabled = !matchStarted,
                                            text = {
                                                Text(
                                                    text = it,
                                                    fontSize = CricketAppTheme.dimens.bodySize
                                                )
                                            },
                                            onClick = {
                                                battingTeamCaptain = team2Captain
                                                dbHelper.updateMatchCaptain(matchId, 1, it)
                                                dbHelper.updateMatchCaptain(matchId, 2, team1Captain!!.name)
                                                val teamA = dbHelper.getTeamForPlayer(matchId, it)
                                                if (teamA == 2) {
                                                    dbHelper.updateTeamID(matchId, 1, 3)
                                                    dbHelper.updateTeamID(matchId, 2, 1)
                                                    dbHelper.updateTeamID(matchId, 3, 2)
                                                }
                                                refreshKey1++
                                                refreshKey2++
                                                expanded04 = false
                                                facingBatsman = null
                                                secondBatsman = null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val battingTeamId = battingTeamCaptain?.name?.let { dbHelper.getTeamForPlayer(matchId, it) }
                    val bowlingTeamId = if (battingTeamId == 1) 2 else 1

                    val battingTeamPlayers = remember(matchId, battingTeamId, refreshKey1, refreshKey2) {
                        if (battingTeamId != null) dbHelper.getTeamPlayers(matchId, battingTeamId, 1) else emptyList()
                    }
                    val bowlingTeamPlayers = remember(matchId, bowlingTeamId, refreshKey1, refreshKey2) {
                        if (battingTeamId != null) dbHelper.getTeamPlayers(matchId, bowlingTeamId, 1) else emptyList()
                    }

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
                                expanded = expanded05,
                                onExpandedChange = {
                                    if (!matchStarted) {
                                        expanded05 = !expanded05
                                    }
                                }
                            ) {
                                OutlinedTextField(
                                    enabled = !matchStarted,
                                    readOnly = true,
                                    value = facingBatsman?.name ?: "Select Batsman",
                                    onValueChange = { },
                                    label = {
                                        Text(
                                            "Facing Batsman",
                                            fontSize = CricketAppTheme.dimens.smallSize
                                        )
                                    },
                                    textStyle = TextStyle(fontSize = CricketAppTheme.dimens.titleSize),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded05,
                                    onDismissRequest = { expanded05 = false }
                                ) {
                                    battingTeamPlayers.forEach { player ->
                                        if (player.name != secondBatsman?.name) {
                                            androidx.compose.material3.DropdownMenuItem(
                                                enabled = !matchStarted,
                                                text = {
                                                    Text(
                                                        text = player.name,
                                                        fontSize = CricketAppTheme.dimens.bodySize
                                                    )
                                                },
                                                onClick = {
                                                    facingBatsman = player
                                                    if (battingTeamId != null) {
                                                        facingBatsman?.name?.let {
                                                            val striker = dbHelper.getStriker(matchId)
                                                            if (striker != "") {
                                                                dbHelper.updateStriker(matchId, it)
                                                            } else {
                                                                dbHelper.addBattingStats(matchId, battingTeamId, it, "striker")
                                                            }
                                                            dbHelper.updateMatchOpeningStriker(matchId, it)
                                                        }
                                                    }
                                                    expanded05 = false
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
                                expanded = expanded06,
                                onExpandedChange = {
                                    if (!matchStarted) {
                                        expanded06 = !expanded06
                                    }
                                }
                            ) {
                                OutlinedTextField(
                                    enabled = !matchStarted,
                                    readOnly = true,
                                    value = secondBatsman?.name ?: "Select Batsman",
                                    onValueChange = { },
                                    label = {
                                        Text(
                                            "Second Batsman",
                                            fontSize = CricketAppTheme.dimens.smallSize
                                        )
                                    },
                                    textStyle = TextStyle(fontSize = CricketAppTheme.dimens.titleSize),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded06,
                                    onDismissRequest = { expanded06 = false }
                                ) {
                                    battingTeamPlayers.forEach { player ->
                                        if (player.name != facingBatsman?.name) {
                                            androidx.compose.material3.DropdownMenuItem(
                                                enabled = !matchStarted,
                                                text = {
                                                    Text(
                                                        text = player.name,
                                                        fontSize = CricketAppTheme.dimens.bodySize
                                                    )
                                                },
                                                onClick = {
                                                    secondBatsman = player
                                                    if (battingTeamId != null) {
                                                        secondBatsman?.name?.let {
                                                            val nonStriker = dbHelper.getNonStriker(matchId)
                                                            if (nonStriker != "") {
                                                                dbHelper.updateNonStriker(matchId, it)
                                                            } else {
                                                                dbHelper.addBattingStats(matchId, battingTeamId, it, "non-striker")
                                                            }
                                                            dbHelper.updateMatchOpeningNonStriker(matchId, it)
                                                        }
                                                    }
                                                    expanded06 = false
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
                                expanded = expanded07,
                                onExpandedChange = {
                                    if (!matchStarted) {
                                        expanded07 = !expanded07
                                    }
                                }
                            ) {
                                OutlinedTextField(
                                    enabled = !matchStarted,
                                    readOnly = true,
                                    value = openingBowler?.name ?: "Select Bowler",
                                    onValueChange = { },
                                    label = {
                                        Text(
                                            "Bowler",
                                            fontSize = CricketAppTheme.dimens.smallSize
                                        )
                                    },
                                    textStyle = TextStyle(fontSize = CricketAppTheme.dimens.titleSize),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded07,
                                    onDismissRequest = { expanded07 = false }
                                ) {
                                    bowlingTeamPlayers.forEach { player ->
                                        if (player.name != openingKeeper?.name) {
                                            androidx.compose.material3.DropdownMenuItem(
                                                enabled = !matchStarted,
                                                text = {
                                                    Text(
                                                        text = player.name,
                                                        fontSize = CricketAppTheme.dimens.bodySize
                                                    )
                                                },
                                                onClick = {
                                                    openingBowler = player
                                                    val bowler = dbHelper.getBowler(matchId)
                                                    if (bowler != "") {
                                                        dbHelper.updateBowler(matchId, player.name)
                                                    } else {
                                                        dbHelper.addBowlingStats(matchId, bowlingTeamId, player.name, "", "bowling")
                                                    }
                                                    dbHelper.updateMatchOpeningBowler(matchId, player.name)
                                                    expanded07 = false
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
                                expanded = expanded08,
                                onExpandedChange = {
                                    if (!matchStarted) {
                                        expanded08 = !expanded08
                                    }
                                }
                            ) {
                                OutlinedTextField(
                                    enabled = !matchStarted,
                                    readOnly = true,
                                    value = openingKeeper?.name ?: "Select Keeper",
                                    onValueChange = { },
                                    label = {
                                        Text(
                                            "Wicket keeper",
                                            fontSize = CricketAppTheme.dimens.smallSize
                                        )
                                    },
                                    textStyle = TextStyle(fontSize = CricketAppTheme.dimens.titleSize),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded08,
                                    onDismissRequest = { expanded08 = false }
                                ) {
                                    bowlingTeamPlayers.forEach { player ->
                                        if (player.name != openingBowler?.name) {
                                            androidx.compose.material3.DropdownMenuItem(
                                                enabled = !matchStarted,
                                                text = {
                                                    Text(
                                                        text = player.name,
                                                        fontSize = CricketAppTheme.dimens.bodySize
                                                    )
                                                },
                                                onClick = {
                                                    openingKeeper = player
                                                    val keeper = dbHelper.getKeeper(matchId)
                                                    if (keeper != "") {
                                                        dbHelper.updateKeeper(matchId, player.name)
                                                    } else {
                                                        dbHelper.updateBowlingStatsKeeper(matchId, bowlingTeamId, player.name)
                                                    }
                                                    dbHelper.updateMatchOpeningKeeper(matchId, player.name)
                                                    expanded08 = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    //Only show if both teams contain all players and batting team, keeper and bowler have been set
                    if ((team1Size == noOfPlayersAside)
                        && (team2Size == noOfPlayersAside)
                        && (facingBatsman?.name.orEmpty().isNotEmpty())
                        && (secondBatsman?.name.orEmpty().isNotEmpty())
                        && (openingBowler?.name.orEmpty().isNotEmpty())
                        && (openingKeeper?.name.orEmpty().isNotEmpty())
                    ) {
                        Button(
                            onClick = {
                                if (!matchStarted)
                                {
                                    dbHelper.updateMatchIsStarted(matchId)
                                    dbHelper.insertPartnership(matchId, 1,
                                        facingBatsman?.name ?: "",
                                        secondBatsman?.name ?: ""
                                    )
                                    dbHelper.saveSnapshot(matchId)
                                }
                                navController.navigate("scorecard")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CricketAppTheme.colors.primaryCream
                            )

                        ) {
                            if (!matchStarted) {
                                Text(
                                    text = "Start New Match",
                                    fontSize = CricketAppTheme.dimens.buttonTextSize,
                                    color = CricketAppTheme.colors.textOnLight
                                )
                            } else {
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
    }
}