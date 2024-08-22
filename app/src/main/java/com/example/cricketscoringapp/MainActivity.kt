package com.example.cricketscoringapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cricketscoringapp.ui.theme.CricketScoringAppTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CricketScoringAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val balls = remember { mutableStateListOf("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "") }

                        // Define states for team stats...
                        val activeBattingTeamStats = remember {
                            mutableStateOf(TeamStats(
                                name = mutableStateOf(""),
                                overs = mutableDoubleStateOf(0.0),
                                inningScore = mutableIntStateOf(0),
                                inningWickets = mutableIntStateOf(0)
                            ))
                        }

                        // Load data from Google Sheets
//                        LaunchedEffect(Unit) {
//                            val data = loadDataFromGoogleSheets()
//                            activeBattingTeamStats.value = data
//                        }

                        //val SCOPES = listOf(SheetsScopes.SPREADSHEETS_READONLY)

//                        override fun onCreate(savedInstanceState: Bundle?) {
//                            super.onCreate(savedInstanceState)
//                            enableEdgeToEdge()
//
//                            // Authenticate and fetch data from Google Sheets
//                            authenticateAndFetchData()
//                        }

                        // Fetch data from Google Sheets API
//                        LaunchedEffect(Unit) {
//                            lifecycleScope.launch {
//                                try {
//                                    val response = RetrofitInstance.googleSheetsService.getSheetData(
//                                        spreadsheetId = "1hoqVNgiQz6e2lkhZV_3ZizhFTlpoMa8QBLFXyhWR7c0",
//                                        range = "Farhan!A1:A6", // Specify the range in the sheet
//                                        apiKey = "110628859663-8l4mvdj0oombbv62hqscnm7deec7kdsv.apps.googleusercontent.com"
//                                    )
//                                    // Assuming data comes in order [name, overs, score, wickets]
//                                    response.values.firstOrNull()?.let { row ->
//                                        activeBattingTeamStats.value = TeamStats(
//                                            name = mutableStateOf(row[0]),
//                                            overs = mutableDoubleStateOf(row[1].toDouble()),
//                                            inningScore = mutableIntStateOf(row[2].toInt()),
//                                            inningWickets = mutableIntStateOf(row[3].toInt())
//                                        )
//                                    }
//                                } catch (e: HttpException) {
//                                    Log.e("API Error", "HTTP error: ${e.code()} - ${e.message()}")
//                                } catch (e: Exception) {
//                                    Log.e("API Error", "General error: ${e.message}")
//                                }
//                            }
//                        }

                        val secondBattingTeamStats = remember { TeamStats(
                            name = mutableStateOf("Junaid"),
                            overs = mutableDoubleStateOf(12.0),
                            inningScore = mutableIntStateOf(value = 56),
                            inningWickets = mutableIntStateOf(value = 9)
                        ) }

                        val activeBatsmanStats = remember { BatsmanStats(
                            name = mutableStateOf("Tanveer"),
                            runs = mutableIntStateOf(value = 2),
                            balls = mutableIntStateOf(value = 5),
                            fours = mutableIntStateOf(value = 0),
                            sixes = mutableIntStateOf(value = 2),
                            active = mutableStateOf(value = true)
                        ) }

                        val secondBatsmanStats = remember { BatsmanStats(
                            name = mutableStateOf("Fahid"),
                            runs = mutableIntStateOf(value = 14),
                            balls = mutableIntStateOf(value = 12),
                            fours = mutableIntStateOf(value = 1),
                            sixes = mutableIntStateOf(value = 1),
                            active = mutableStateOf(value = false)
                        ) }

                        val bowlerStats = remember { BowlerStats(
                            name = mutableStateOf("Adnan"),
                            over = mutableDoubleStateOf(2.0),
                            maiden = mutableIntStateOf(0),
                            runs = mutableIntStateOf(0),
                            wickets = mutableIntStateOf(0)
                        ) }

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
                                    //name1 = activeBattingTeamStats.name.value,
                                    name1 = activeBattingTeamStats.value.name.value,
                                    overs1 = String.format(Locale.UK, "%.1f", activeBattingTeamStats.value.overs.value),
                                    score1 = String.format(Locale.UK, "%d", activeBattingTeamStats.value.inningScore.value) + "/" +
                                            String.format(Locale.UK, "%d", activeBattingTeamStats.value.inningWickets.value),
                                    color1 = Color(19, 207, 69))
                                TeamScoreBox(
                                    name1 = secondBattingTeamStats.name.value,
                                    overs1 = String.format(Locale.UK, "%.1f", secondBattingTeamStats.overs.value),
                                    score1 = String.format(Locale.UK, "%d", secondBattingTeamStats.inningScore.value) + "/" +
                                            String.format(Locale.UK, "%d", secondBattingTeamStats.inningWickets.value),
                                    color1 = Color.LightGray)
                            }
                        }

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
                                BatsmanBowlerBox(col1 = "Batsman", col2 = "R", col3 = "B", col4 = "4s", col5 = "6s", fontBold1 = FontWeight.Bold, fontColor1 = Color.Gray)

                                val fontColor2:Color
                                val fontColor3:Color
                                val fontBold2:FontWeight
                                val fontBold3:FontWeight

                                if (activeBatsmanStats.active.value) {
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
                                    col1 = activeBatsmanStats.name.value,
                                    col2 = String.format(Locale.UK, "%d", activeBatsmanStats.runs.value),
                                    col3 = String.format(Locale.UK, "%d", activeBatsmanStats.balls.value),
                                    col4 = String.format(Locale.UK, "%d", activeBatsmanStats.fours.value),
                                    col5 = String.format(Locale.UK, "%d", activeBatsmanStats.sixes.value),
                                    fontBold1 = fontBold2,
                                    fontColor1 = fontColor2)

                                BatsmanBowlerBox(
                                    col1 = "Fahid",
                                    col2 = String.format(Locale.UK, "%d", secondBatsmanStats.runs.value),
                                    col3 = String.format(Locale.UK, "%d", secondBatsmanStats.balls.value),
                                    col4 = String.format(Locale.UK, "%d", secondBatsmanStats.fours.value),
                                    col5 = String.format(Locale.UK, "%d", secondBatsmanStats.sixes.value),
                                    fontBold1 = fontBold3,
                                    fontColor1 = fontColor3)
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
                                BatsmanBowlerBox(col1 = "Bowler", col2 = "O", col3 = "M", col4 = "R", col5 = "W", fontBold1 = FontWeight.Bold, fontColor1 = Color.Gray)
                                BatsmanBowlerBox(
                                    col1 = bowlerStats.name.value,
                                    col2 = String.format(Locale.UK,"%.1f", bowlerStats.over.value),
                                    col3 = String.format(Locale.UK, "%d", bowlerStats.maiden.value),
                                    col4 = String.format(Locale.UK, "%d", bowlerStats.runs.value),
                                    col5 = String.format(Locale.UK, "%d", bowlerStats.wickets.value),
                                    fontBold1 = FontWeight.Bold,
                                    fontColor1 = Color(19, 207, 69)
                                )
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
                                    fontColor1 = Color.Black)
                            }
                        }

                        // 1st Row with 6 circle buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CircleButton("0", fontSize = 22) { updateStats(balls,"0", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                            CircleButton("1", fontSize = 22) { updateStats(balls,"1", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                            CircleButton("2", fontSize = 22) { updateStats(balls,"2", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                            CircleButton("3", fontSize = 22) { updateStats(balls,"3", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                            CircleButton("4", fontSize = 22) { updateStats(balls,"4", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                            CircleButton("6", fontSize = 22) { updateStats(balls,"6", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                        }

                        // 2nd Row with 6 circle buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CircleButton("WIDE", fontSize = 12) { updateStats(balls,"W", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                            CircleButton("NO BALL", fontSize = 12) { updateStats(balls,"NB", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                            CircleButton("BYE", fontSize = 12) { updateStats(balls,"B", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                            CircleButton("LEG BYE", fontSize = 12) { updateStats(balls,"LB", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                            CircleButton("WICKET", fontSize = 12) { updateStats(balls,"WI", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                            CircleButton("UNDO", fontSize = 12) { updateStats(balls,"UNDO", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                        }
                    }
                }
            }
        }
    }
}

//fun loadDataFromGoogleSheets(): TeamStats {
//    val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
//    val httpTransport = AndroidHttp.newCompatibleTransport()
//    val credentials = GoogleCredentials.fromStream(resources.openRawResource(R.raw.client_secret))
//        .createScoped(listOf("https://www.googleapis.com/auth/spreadsheets.readonly"))
//
//    val requestInitializer = HttpCredentialsAdapter(credentials)
//
//    val service = Sheets.Builder(httpTransport, jsonFactory, requestInitializer)
//        .setApplicationName("CricketScoringApp")
//        .build()
//
//    val spreadsheetId = "1hoqVNgiQz6e2lkhZV_3ZizhFTlpoMa8QBLFXyhWR7c0"
//    val range = "Junaid!A1:A6"
//    val response: ValueRange = service.spreadsheets().values()
//        .get(spreadsheetId, range)
//        .execute()
//
    //val values = response.getValues()
    // Assuming the data is in the first row
//    val row = values[0]
//
//    return TeamStats(
//        name = mutableStateOf(row[0].toString()),
//        overs = mutableDoubleStateOf(row[1].toString().toDouble()),
//        inningScore = mutableIntStateOf(row[2].toString().toInt()),
//        inningWickets = mutableIntStateOf(row[3].toString().toInt())
//    )
//}

fun swapBatsmen(batsman1: BatsmanStats, batsman2: BatsmanStats) {
    val tempActive = batsman1.active
    batsman1.active = batsman2.active
    batsman2.active = tempActive
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CricketScoringAppTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val balls = remember { mutableStateListOf("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "") }

            // Define states for team stats...
            val activeBattingTeamStats = remember {
                mutableStateOf(TeamStats(
                    name = mutableStateOf(""),
                    overs = mutableDoubleStateOf(0.0),
                    inningScore = mutableIntStateOf(0),
                    inningWickets = mutableIntStateOf(0)
                ))
            }

            // Fetch data from Google Sheets API
//            LaunchedEffect(Unit) {
//                lifecycleScope.launch {
//                    try {
//                        val response = RetrofitInstance.googleSheetsService.getSheetData(
//                            spreadsheetId = "1hoqVNgiQz6e2lkhZV_3ZizhFTlpoMa8QBLFXyhWR7c0/",
//                            range = "Farhan!A1:A6", // Specify the range in the sheet
//                            apiKey = "110628859663-8l4mvdj0oombbv62hqscnm7deec7kdsv.apps.googleusercontent.com"
//                        )
//                        // Assuming data comes in order [name, overs, score, wickets]
//                        response.values.firstOrNull()?.let { row ->
//                            activeBattingTeamStats.value = TeamStats(
//                                name = mutableStateOf(row[0]),
//                                overs = mutableDoubleStateOf(row[1].toDouble()),
//                                inningScore = mutableIntStateOf(row[2].toInt()),
//                                inningWickets = mutableIntStateOf(row[3].toInt())
//                            )
//                        }
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//            }

            val secondBattingTeamStats = remember { TeamStats(
                name = mutableStateOf("Junaid"),
                overs = mutableDoubleStateOf(12.0),
                inningScore = mutableIntStateOf(value = 56),
                inningWickets = mutableIntStateOf(value = 9)
            ) }

            val activeBatsmanStats = remember { BatsmanStats(
                name = mutableStateOf("Tanveer"),
                runs = mutableIntStateOf(value = 2),
                balls = mutableIntStateOf(value = 5),
                fours = mutableIntStateOf(value = 0),
                sixes = mutableIntStateOf(value = 2),
                active = mutableStateOf(value = true)
            ) }

            val secondBatsmanStats = remember { BatsmanStats(
                name = mutableStateOf("Fahid"),
                runs = mutableIntStateOf(value = 14),
                balls = mutableIntStateOf(value = 12),
                fours = mutableIntStateOf(value = 1),
                sixes = mutableIntStateOf(value = 1),
                active = mutableStateOf(value = false)
            ) }

            val bowlerStats = remember { BowlerStats(
                name = mutableStateOf("Adnan"),
                over = mutableDoubleStateOf(2.0),
                maiden = mutableIntStateOf(0),
                runs = mutableIntStateOf(0),
                wickets = mutableIntStateOf(0)
            ) }

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
                        //name1 = activeBattingTeamStats.name.value,
                        name1 = activeBattingTeamStats.value.name.value,
                        overs1 = String.format(Locale.UK, "%.1f", activeBattingTeamStats.value.overs.value),
                        score1 = String.format(Locale.UK, "%d", activeBattingTeamStats.value.inningScore.value) + "/" +
                                String.format(Locale.UK, "%d", activeBattingTeamStats.value.inningWickets.value),
                        color1 = Color(19, 207, 69))
                    TeamScoreBox(
                        name1 = secondBattingTeamStats.name.value,
                        overs1 = String.format(Locale.UK, "%.1f", secondBattingTeamStats.overs.value),
                        score1 = String.format(Locale.UK, "%d", secondBattingTeamStats.inningScore.value) + "/" +
                                String.format(Locale.UK, "%d", secondBattingTeamStats.inningWickets.value),
                        color1 = Color.LightGray)
                }
            }

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
                    BatsmanBowlerBox(col1 = "Batsman", col2 = "R", col3 = "B", col4 = "4s", col5 = "6s", fontBold1 = FontWeight.Bold, fontColor1 = Color.Gray)

                    val fontColor2:Color
                    val fontColor3:Color
                    val fontBold2:FontWeight
                    val fontBold3:FontWeight

                    if (activeBatsmanStats.active.value) {
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
                        col1 = activeBatsmanStats.name.value,
                        col2 = String.format(Locale.UK, "%d", activeBatsmanStats.runs.value),
                        col3 = String.format(Locale.UK, "%d", activeBatsmanStats.balls.value),
                        col4 = String.format(Locale.UK, "%d", activeBatsmanStats.fours.value),
                        col5 = String.format(Locale.UK, "%d", activeBatsmanStats.sixes.value),
                        fontBold1 = fontBold2,
                        fontColor1 = fontColor2)

                    BatsmanBowlerBox(
                        col1 = "Fahid",
                        col2 = String.format(Locale.UK, "%d", secondBatsmanStats.runs.value),
                        col3 = String.format(Locale.UK, "%d", secondBatsmanStats.balls.value),
                        col4 = String.format(Locale.UK, "%d", secondBatsmanStats.fours.value),
                        col5 = String.format(Locale.UK, "%d", secondBatsmanStats.sixes.value),
                        fontBold1 = fontBold3,
                        fontColor1 = fontColor3)
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
                    BatsmanBowlerBox(col1 = "Bowler", col2 = "O", col3 = "M", col4 = "R", col5 = "W", fontBold1 = FontWeight.Bold, fontColor1 = Color.Gray)
                    BatsmanBowlerBox(
                        col1 = bowlerStats.name.value,
                        col2 = String.format(Locale.UK,"%.1f", bowlerStats.over.value),
                        col3 = String.format(Locale.UK, "%d", bowlerStats.maiden.value),
                        col4 = String.format(Locale.UK, "%d", bowlerStats.runs.value),
                        col5 = String.format(Locale.UK, "%d", bowlerStats.wickets.value),
                        fontBold1 = FontWeight.Bold,
                        fontColor1 = Color(19, 207, 69)
                    )
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
                        fontColor1 = Color.Black)
                }
            }

            // 1st Row with 6 circle buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircleButton("0", fontSize = 22) { updateStats(balls,"0", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                CircleButton("1", fontSize = 22) { updateStats(balls,"1", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                CircleButton("2", fontSize = 22) { updateStats(balls,"2", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                CircleButton("3", fontSize = 22) { updateStats(balls,"3", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                CircleButton("4", fontSize = 22) { updateStats(balls,"4", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                CircleButton("6", fontSize = 22) { updateStats(balls,"6", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
            }

            // 2nd Row with 6 circle buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircleButton("WIDE", fontSize = 12) { updateStats(balls,"W", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                CircleButton("NO BALL", fontSize = 12) { updateStats(balls,"NB", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                CircleButton("BYE", fontSize = 12) { updateStats(balls,"B", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                CircleButton("LEG BYE", fontSize = 12) { updateStats(balls,"LB", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                CircleButton("WICKET", fontSize = 12) { updateStats(balls,"WI", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
                CircleButton("UNDO", fontSize = 12) { updateStats(balls,"UNDO", bowlerStats, activeBatsmanStats, secondBatsmanStats,activeBattingTeamStats.value) }
            }
        }
    }
}


