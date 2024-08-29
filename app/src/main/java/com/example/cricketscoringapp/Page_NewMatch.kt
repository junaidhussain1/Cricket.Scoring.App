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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMatchPage(navController: NavHostController) {
    val context = LocalContext.current
    val dbHelper = CricketDatabaseHelper(context)
    val matchId = dbHelper.getMatchId()
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

    team1Captain = Player(dbHelper.getCaptain(matchId, 1))
    team2Captain = Player(dbHelper.getCaptain(matchId, 2))

    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }
    var expanded3 by remember { mutableStateOf(false) }
    var expanded4 by remember { mutableStateOf(false) }
    var expanded5 by remember { mutableStateOf(false) }
    var expanded6 by remember { mutableStateOf(false) }
    var expanded7 by remember { mutableStateOf(false) }

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
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy") // Define the format
            val formattedDate = currentDate.format(formatter)
            Text(text = "Match: $formattedDate", style = MaterialTheme.typography.headlineSmall)

            IconButton(
                onClick = {
                    dbHelper.deleteMatch(matchId)
                    navController.navigate("homepage")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Recycle Bin"
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
                    onExpandedChange = { expanded1 = !expanded1 }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = team1Captain?.name ?: "Select Captain",
                        onValueChange = { },
                        label = { Text("Team 1 Captain") },
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
                                text = { Text(text = player.name) },
                                onClick = {
                                    team1Captain = player
                                    team1Captain?.let {
                                        dbHelper.addTeamPlayer(matchId, 1, player.name, 1, 0)
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
                    onExpandedChange = { expanded2 = !expanded2 }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = team2Captain?.name ?: "Select Captain",
                        onValueChange = { },
                        label = { Text("Team 2 Captain") },
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
                                text = { Text(text = player.name) },
                                onClick = {
                                    team2Captain = player
                                    team2Captain?.let {
                                        dbHelper.addTeamPlayer(matchId, 2, player.name, 1, 0)
                                    }
                                    expanded2 = false
                                }
                            )
                        }
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

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
                    if (dbHelper.getCaptain(matchId, 1) != "") {
                        Button(
                            onClick = {
                                navController.navigate("team1PlayerSelection")
                            },
                            //shape = ButtonDefaults.shape.,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RectangleShape,
                            content = {
                                Text(
                                    text = "Select Team ${team1Captain?.name} Players",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth() // Ensure text takes full width of the button
                                )
                            }
                        )

                        val team1Players = dbHelper.getTeamPlayers(matchId, 1, 0)
                        for (player in team1Players) {
                            Text(text = player.name)
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

                    if (dbHelper.getCaptain(matchId, 2) != "") {
                        Button(
                            onClick = {
                                navController.navigate("team2PlayerSelection")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RectangleShape,
                            content = {
                                Text(
                                    text = "Select Team ${team2Captain?.name} Players",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth() // Ensure text takes full width of the button
                                )
                            }
                        )

                        val team2Players = dbHelper.getTeamPlayers(matchId, 2, 0)
                        for (player in team2Players) {
                            Text(text = player.name)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if ((dbHelper.getTeamSize(matchId,1) == 6) && (dbHelper.getTeamSize(matchId,2) == 6))
        {
            // Select Batting Team
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
                        onExpandedChange = { expanded3 = !expanded3 }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = battingTeamCaptain?.name ?: "Select Captain",
                            onValueChange = { },
                            label = { Text("Batting Team") },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded3,
                            onDismissRequest = { expanded3 = false }
                        ) {
                            androidx.compose.material3.DropdownMenuItem(
                                    text = { team1Captain?.name?.let { Text(text = it) } },
                                    onClick = {
                                        team1Captain?.let { captain ->
                                            battingTeamCaptain = captain
                                        }
                                        expanded3 = false
                                        facingBatsman = null
                                        secondBatsman = null
                                    }
                                )

                            androidx.compose.material3.DropdownMenuItem(
                                text = { team2Captain?.name?.let { Text(text = it) } },
                                onClick = {
                                    team2Captain?.let { captain ->
                                        battingTeamCaptain = captain
                                    }
                                    expanded3 = false
                                }
                            )
                        }
                    }
                }
            }

            // Row to hold both Facing Batsman and Second Batsman
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val battingTeamId =
                    battingTeamCaptain?.name?.let {
                        dbHelper.getTeamForPlayer(matchId,
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
                    battingTeamList.addAll(dbHelper.getTeamPlayers(matchId,battingTeamId,1))

                    bowlingTeamList.clear()
                    bowlingTeamList.addAll(dbHelper.getTeamPlayers(matchId,bowlingTeamId,1))
                }

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
                            readOnly = true,
                            value = facingBatsman?.name ?: "Select Batsman",
                            onValueChange = { },
                            label = { Text("Facing Batsman") },
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
                                        text = { Text(text = player.name) },
                                        onClick = {
                                            facingBatsman = player
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
                        onExpandedChange = { expanded5= !expanded5 }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = secondBatsman?.name ?: "Select Batsman",
                            onValueChange = { },
                            label = { Text("Second Batsman") },
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
                                        text = { Text(text = player.name) },
                                        onClick = {
                                            secondBatsman = player
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
                        onExpandedChange = { expanded6 = !expanded6 }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = openingBowler?.name ?: "Select Bowler",
                            onValueChange = { },
                            label = { Text("Bowler") },
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
                                        text = { Text(text = player.name) },
                                        onClick = {
                                            openingBowler = player
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
                        onExpandedChange = { expanded7 = !expanded7 }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = openingKeeper?.name ?: "Select Keeper",
                            onValueChange = { },
                            label = { Text("Wicket-keeper") },
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
                                        text = { Text(text = player.name) },
                                        onClick = {
                                            openingKeeper = player
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
            if ((dbHelper.getTeamSize(matchId,1) == 6)
                && (dbHelper.getTeamSize(matchId,2) == 6)
                && (facingBatsman != null)
                && (secondBatsman != null)
                && (openingBowler != null)
                && (openingKeeper != null))
            {
                Button(onClick = { navController.navigate("startnewmatch") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                        .padding(end = 8.dp),
                    shape = RectangleShape) {
                    Text(text = "Start New Match")
                }
            }
        }
    }
}

@Composable
fun ShowConfirmationDialog() {
    var showDialog by remember { mutableStateOf(false) }

    // Trigger to show the dialog
    Button(onClick = { showDialog = true }) {
        Text(text = "Show Confirmation")
    }

    // Confirmation Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false }, // Close the dialog when touched outside or back press
            title = { Text(text = "Confirm Action") },
            text = { Text(text = "Are you sure you want to delete this item?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Handle confirm action
                        showDialog = false
                        // Perform the deletion or any other action here
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Handle dismiss action
                        showDialog = false
                    }
                ) {
                    Text("No")
                }
            },
            properties = DialogProperties(dismissOnClickOutside = false)
        )
    }
}