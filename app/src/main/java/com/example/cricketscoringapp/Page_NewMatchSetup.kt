package com.example.cricketscoringapp


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMatchSetupPage(navController: NavHostController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val dbHelper = CricketDatabaseHelper(context)
    val matchId = dbHelper.getMatchId()
    val matchStarted = dbHelper.getIsMatchStarted(matchId)
    val playersList = remember { mutableStateListOf<Player>() }
    val battingTeamList = remember { mutableStateListOf<Player>() }
    val bowlingTeamList = remember { mutableStateListOf<Player>() }

    playersList.clear()
    playersList.addAll(dbHelper.getAllPlayers())

    var team1Captain by remember { mutableStateOf<Player?>(null) }
    var team2Captain by remember { mutableStateOf<Player?>(null) }
    var battingTeamCaptain by remember { mutableStateOf<Player?>(null) }
    var facingBatsman by remember { mutableStateOf<Player?>(null) }
    var secondBatsman by remember { mutableStateOf<Player?>(null) }
    var openingBowler by remember { mutableStateOf<Player?>(null) }
    var openingKeeper by remember { mutableStateOf<Player?>(null) }

    team1Captain = Player(dbHelper.getCaptainForTeam(matchId, 1))
    team2Captain = Player(dbHelper.getCaptainForTeam(matchId, 2))
    battingTeamCaptain = Player((dbHelper.getBattingTeamCaptain(matchId, 1)))
    facingBatsman = Player(dbHelper.getFirstBattingTeamStriker(matchId))
    secondBatsman = Player(dbHelper.getFirstBattingTeamNonStriker(matchId))
    openingBowler = Player(dbHelper.getSecondBattingTeamBowler(matchId))
    openingKeeper = Player(dbHelper.getSecondBattingTeamKeeper(matchId))

    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }
    var expanded3 by remember { mutableStateOf(false) }
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
            // Row to hold Recycle Bin Button and "New Match" text in the same row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentDate = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy") // Define the format
                val formattedDate = currentDate.format(formatter)
                Text(
                    text = "Match: $formattedDate",
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = if (isTablet) 40.sp else 22.sp,
                    color = Color(255, 252, 228)
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
                        tint = Color(255, 252, 228)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

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
                        expanded = expanded1,
                        onExpandedChange = {
                            if (!matchStarted) {
                                expanded1 = !expanded1
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
                                    fontSize = if (isTablet) 22.sp else 14.sp
                                )
                            },
                            textStyle = TextStyle(fontSize = if (isTablet) 32.sp else 14.sp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded1,
                            onDismissRequest = { expanded1 = false }
                        ) {
                            playersList.forEach { player ->
                                androidx.compose.material3.DropdownMenuItem(
                                    enabled = !matchStarted,
                                    text = {
                                        Text(
                                            text = player.name,
                                            fontSize = if (isTablet) 30.sp else 14.sp
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
                                        expanded1 = false
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
                        expanded = expanded2,
                        onExpandedChange = {
                            if (!matchStarted) {
                                expanded2 = !expanded2
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
                                    fontSize = if (isTablet) 22.sp else 14.sp
                                )
                            },
                            textStyle = TextStyle(fontSize = if (isTablet) 32.sp else 14.sp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded2,
                            onDismissRequest = { expanded2 = false }
                        ) {
                            val remainingPlayers = playersList.filter { it != team1Captain }
                            remainingPlayers.forEach { player ->
                                androidx.compose.material3.DropdownMenuItem(
                                    enabled = !matchStarted,
                                    text = {
                                        Text(
                                            text = player.name,
                                            fontSize = if (isTablet) 30.sp else 14.sp
                                        )
                                    },
                                    onClick = {
                                        team2Captain = player
                                        team2Captain?.let {
                                            dbHelper.addTeamPlayer(matchId, 2, player.name, 1,0)
                                        }
                                        expanded2 = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val textColor = if (!matchStarted) Color.White else Color.Gray

            if ((team1Captain!!.name != "") && (team2Captain!!.name != "")) {
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
                        if (dbHelper.getCaptainForTeam(matchId, 1) != "") {
                            Button(
                                enabled = !matchStarted,
                                onClick = {
                                    navController.navigate("team1PlayerSelection")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(255, 252, 228) // Set the background color
                                ),
                                content = {
                                    Text(
                                        text = "Select Team Players",
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                        fontSize = if (isTablet) 22.sp else 16.sp
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = team1Captain!!.name,
                                color = textColor,
                                fontSize = if (isTablet) 30.sp else 16.sp
                            )

                            val team1Players = dbHelper.getTeamPlayers(matchId, 1, 0)

                            for (player in team1Players) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = player.name,
                                    color = textColor,
                                    fontSize = if (isTablet) 30.sp else 16.sp
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

                        if (dbHelper.getCaptainForTeam(matchId, 2) != "") {
                            Button(
                                enabled = !matchStarted,
                                onClick = {
                                    navController.navigate("team2PlayerSelection")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(255, 252, 228) // Set the background color
                                ),
                                content = {
                                    Text(
                                        text = "Select Team Players",
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                        fontSize = if (isTablet) 22.sp else 16.sp
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = team2Captain!!.name,
                                color = textColor,
                                fontSize = if (isTablet) 30.sp else 16.sp
                            )

                            val team2Players = dbHelper.getTeamPlayers(matchId, 2, 0)

                            for (player in team2Players) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = player.name,
                                    color = textColor,
                                    fontSize = if (isTablet) 30.sp else 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if ((dbHelper.getTeamSize(matchId, 1) == 6) && (dbHelper.getTeamSize(
                    matchId,
                    2
                ) == 6)
            ) {
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
                            expanded = expanded3,
                            onExpandedChange = {
                                if (!matchStarted) {
                                    expanded3 = !expanded3
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
                                        fontSize = if (isTablet) 22.sp else 14.sp
                                    )
                                },
                                textStyle = TextStyle(fontSize = if (isTablet) 32.sp else 14.sp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded3,
                                onDismissRequest = { expanded3 = false }
                            ) {
                                androidx.compose.material3.DropdownMenuItem(
                                    enabled = !matchStarted,
                                    text = {
                                        team1Captain?.name?.let {
                                            Text(
                                                text = it,
                                                fontSize = if (isTablet) 30.sp else 14.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        team1Captain?.let { captain ->
                                            battingTeamCaptain = captain
                                            dbHelper.updateMatchCaptain(
                                                matchId,
                                                1,
                                                captain.name
                                            )
                                            dbHelper.updateMatchCaptain(
                                                matchId,
                                                2,
                                                team2Captain!!.name
                                            )
                                        }
                                        expanded3 = false
                                        facingBatsman = null
                                        secondBatsman = null
                                    }
                                )

                                androidx.compose.material3.DropdownMenuItem(
                                    enabled = !matchStarted,
                                    text = {
                                        team2Captain?.name?.let {
                                            Text(
                                                text = it,
                                                fontSize = if (isTablet) 30.sp else 14.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        team2Captain?.let { captain ->
                                            battingTeamCaptain = captain
                                            dbHelper.updateMatchCaptain(
                                                matchId,
                                                1,
                                                captain.name
                                            )
                                            dbHelper.updateMatchCaptain(
                                                matchId,
                                                2,
                                                team1Captain!!.name
                                            )
                                        }
                                        expanded3 = false
                                        facingBatsman = null
                                        secondBatsman = null
                                    }
                                )
                            }
                        }
                    }
                }

                val battingTeamId =
                    battingTeamCaptain?.name?.let {
                        dbHelper.getTeamForPlayer(
                            matchId,
                            it
                        )
                    }

                val bowlingTeamId = if (battingTeamId == 1) {
                    2
                } else {
                    1
                }

                if (battingTeamId != null) {
                    battingTeamList.clear()
                    battingTeamList.addAll(
                        dbHelper.getTeamPlayers(
                            matchId,
                            battingTeamId,
                            1
                        )
                    )

                    bowlingTeamList.clear()
                    bowlingTeamList.addAll(
                        dbHelper.getTeamPlayers(
                            matchId,
                            bowlingTeamId,
                            1
                        )
                    )
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
                            expanded = expanded4,
                            onExpandedChange = {
                                if (!matchStarted) {
                                    expanded4 = !expanded4
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
                                            enabled = !matchStarted,
                                            text = {
                                                Text(
                                                    text = player.name,
                                                    fontSize = if (isTablet) 30.sp else 14.sp
                                                )
                                            },
                                            onClick = {
                                                facingBatsman = player
                                                if (battingTeamId != null) {
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
                                                        dbHelper.updateMatchOpeningStriker(matchId,it)
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
                                if (!matchStarted) {
                                    expanded5 = !expanded5
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
                                            enabled = !matchStarted,
                                            text = {
                                                Text(
                                                    text = player.name,
                                                    fontSize = if (isTablet) 30.sp else 14.sp
                                                )
                                            },
                                            onClick = {
                                                secondBatsman = player
                                                if (battingTeamId != null) {
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
                                                        dbHelper.updateMatchOpeningNonStriker(matchId,it)
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
                                if (!matchStarted) {
                                    expanded6 = !expanded6
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
                                            enabled = !matchStarted,
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
                                                dbHelper.updateMatchOpeningBowler(matchId,player.name)
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
                                if (!matchStarted) {
                                    expanded7 = !expanded7
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
                                            enabled = !matchStarted,
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
                                                dbHelper.updateMatchOpeningKeeper(matchId,player.name)
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
                if ((dbHelper.getTeamSize(matchId, 1) == 6)
                    && (dbHelper.getTeamSize(matchId, 2) == 6)
                    && (facingBatsman?.name != "")
                    && (secondBatsman?.name != "")
                    && (openingBowler?.name != "")
                    && (openingKeeper?.name != "")
                ) {
                    Button(
                        onClick = {
                            if (!matchStarted) dbHelper.updateMatchIsStarted(matchId)
                            navController.navigate("scorecard")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(255, 252, 228)
                        )

                    ) {
                        if (!matchStarted) {
                            Text(
                                text = "Start New Match",
                                fontSize = if (isTablet) 22.sp else 16.sp
                            )
                        } else {
                            Text(text = "Continue Match",
                                fontSize = if (isTablet) 22.sp else 16.sp)
                        }
                    }
                }
            }
        }
    }
}
