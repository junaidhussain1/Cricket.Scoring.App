package com.example.cricketscoringapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cricketscoringapp.ui.theme.CricketScoringAppTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainScreenContent()
        }
    }
}

@Composable
fun MainScreenContent() {
    CricketScoringAppTheme {
        val navController = rememberNavController()
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppNavHost(navController = navController)

            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    val captainViewModel: CaptainViewModel = viewModel()

    NavHost(navController = navController, startDestination = "homepage") {
        composable("homepage") { HomePage(navController = navController)}
        composable("playermgt") { PlayerMgtPage() }
        composable("newmatch") { NewMatchPage(navController = navController, captainViewModel = captainViewModel) }
        composable("startnewmatch") { StartNewMatchPage(captainViewModel = captainViewModel) }
    }
}

@Composable
fun HomePage(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome to Cricket Scoring App")
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("playermgt") }) {
            Text(text = "Player Management")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("newmatch") }) {
            Text(text = "New Match")
        }
    }
}

@Composable
fun PlayerMgtPage() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val dbHelper = CricketDatabaseHelper(context)
        var playerName by remember { mutableStateOf("") }
        val playersList = remember { mutableStateListOf<Player>() }

        //Show all players
        playersList.clear()
        playersList.addAll(dbHelper.getAllPlayers())

        // TextField to input player name
        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("Enter Player Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Button to add player to the database
        Button(
            onClick = {
                if (playerName.isNotEmpty()) {
                    if (!dbHelper.playerAlreadyExists(playerName)) {
                        dbHelper.addPlayer(playerName)
                        Toast.makeText(context, "Player Added", Toast.LENGTH_SHORT).show()
                        playerName = ""
                        //Show all players
                        playersList.clear()
                        playersList.addAll(dbHelper.getAllPlayers())
                    } else {
                        Toast.makeText(context, "Player already exists!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Player name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text("Add Player")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the list of players in a scrollable LazyColumn
        if (playersList.isNotEmpty()) {
            Text("Player List:" + playersList.size.toString(), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier
                .fillMaxHeight(0.5f)
                .weight(1f))
            {
                items(playersList) { player ->
                    PlayerRow(player = player.name, onDelete = {
                        dbHelper.deletePlayer(player.id)
                        playersList.remove(player)
                    })
                }
            }
        } else {
            Text("No players found")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMatchPage(navController: NavHostController, captainViewModel: CaptainViewModel) {
    val context = LocalContext.current
    val dbHelper = CricketDatabaseHelper(context)
    val playersList = remember { mutableStateListOf<Player>() }
    val remainingPlayersList = remember { mutableStateListOf<Player>() }

    playersList.clear()
    playersList.addAll(dbHelper.getAllPlayers()) // Fetch players from database

    remainingPlayersList.clear()
    remainingPlayersList.addAll(dbHelper.getAllPlayers()) // Fetch players from database

    var team1Captain by remember { mutableStateOf<Player?>(null) }
    var team2Captain by remember { mutableStateOf<Player?>(null) }

    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //captainViewModel.clearCaptains()
        Text(text = "New Match", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Team 1 Captain Dropdown
        Text(text = "Select Team 1 Captain")
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
                            team1Captain?.let { captain ->
                                captainViewModel.addCaptain(captain.name)
                            }
                            expanded1 = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Team 2 Captain Dropdown
        Text(text = "Select Team 2 Captain")
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
                remainingPlayersList.remove(team1Captain)
                remainingPlayersList.forEach { player ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(text = player.name) },
                        onClick = {
                            team2Captain = player
                            team2Captain?.let { captain ->
                                captainViewModel.addCaptain(captain.name)
                            }
                            expanded2 = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Proceed to start the match
        Button(onClick = { navController.navigate("startnewmatch") }) {
            Text(text = "Start New Match")
        }
    }
}


@Composable
fun StartNewMatchPage(captainViewModel: CaptainViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Retrieve the captain names from the view model
        val captains = captainViewModel.captains
        val team1Captain = captains.getOrElse(0) { "Team 1 Captain" }
        val team2Captain = captains.getOrElse(1) { "Team 2 Captain" }

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
                name = mutableStateOf(team1Captain),
                overs = mutableDoubleStateOf(7.0),
                inningScore = mutableIntStateOf(36),
                inningWickets = mutableIntStateOf(2),
                active = mutableStateOf(value = true)
            )
        }

        val secondBattingTeamStats = remember {
            TeamStats(
                name = mutableStateOf(team2Captain),
                overs = mutableDoubleStateOf(12.0),
                inningScore = mutableIntStateOf(value = 56),
                inningWickets = mutableIntStateOf(value = 9),
                active = mutableStateOf(value = false)
            )
        }

        val firstBatsmanStats = remember {
            BatsmanStats(
                name = mutableStateOf("Tanveer"),
                runs = mutableIntStateOf(value = 2),
                balls = mutableIntStateOf(value = 5),
                fours = mutableIntStateOf(value = 0),
                sixes = mutableIntStateOf(value = 2),
                active = mutableStateOf(value = true)
            )
        }

        val secondBatsmanStats = remember {
            BatsmanStats(
                name = mutableStateOf("Fahid"),
                runs = mutableIntStateOf(value = 14),
                balls = mutableIntStateOf(value = 12),
                fours = mutableIntStateOf(value = 1),
                sixes = mutableIntStateOf(value = 1),
                active = mutableStateOf(value = false)
            )
        }

        val bowlerStats = remember {
            BowlerStats(
                name = mutableStateOf("Adnan"),
                over = mutableDoubleStateOf(2.0),
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
                updateStats(
                    balls,
                    "W",
                    bowlerStats,
                    firstBatsmanStats,
                    secondBatsmanStats,
                    firstBattingTeamStats,
                    secondBattingTeamStats,
                    runsToWin
                )
            }
            CircleButton("NO BALL", fontSize = 16) {
                updateStats(
                    balls,
                    "NB",
                    bowlerStats,
                    firstBatsmanStats,
                    secondBatsmanStats,
                    firstBattingTeamStats,
                    secondBattingTeamStats,
                    runsToWin
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

@Composable
fun PlayerRow(player: String, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = player, modifier = Modifier.weight(1f))
        Button(onClick = onDelete) {
            Text(text = "Delete")
        }
    }
}

fun swapBatsmen(batsman1: BatsmanStats, batsman2: BatsmanStats) {
    val tempActive = batsman1.active.value
    batsman1.active.value = batsman2.active.value
    batsman2.active.value = tempActive
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MainScreenContent()
}


