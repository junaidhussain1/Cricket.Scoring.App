package com.example.cricketscoringapp

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ExistingMatchesPage(navController: NavHostController) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Existing Matches",
            fontSize = 22.sp)

        Spacer(modifier = Modifier.height(20.dp))
        val context = LocalContext.current
        val dbHelper = CricketDatabaseHelper(context)

        val matches = dbHelper.getMatches().filter { it.isFinished }

        if (matches.isNotEmpty()) {
            for (match in matches) {
                val team1Captain = Player(dbHelper.getCaptainForTeam(match.matchId, 1)).name
                val team2Captain = Player(dbHelper.getCaptainForTeam(match.matchId, 2)).name
                if (team1Captain.isNotEmpty() && team2Captain.isNotEmpty()) {
                    val matchDate = dbHelper.getDateForMatch(match.matchId)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp), // Adds some padding between rows
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(5f)
                                .padding(start = 30.dp),
                            horizontalAlignment = Alignment.Start
                        )
                        {
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(255, 252, 228) // Set the background color
                                ),
                                onClick = {
                                    // Define the action when the button is clicked
                                    val matchId = match.matchId
                                    val teamIdA = 1
                                    val teamIdB = 2
                                    navController.navigate("inningstats/${matchId}/${teamIdA}/${teamIdB}")
                                },
                                modifier = Modifier.fillMaxWidth() // Make the button take full width
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(), // Fill the entire width of the button
                                    horizontalArrangement = Arrangement.Start // Align content to the left
                                ) {
                                    Text(
                                        text = when {
                                            match.isStarted -> "$matchDate - $team1Captain vs $team2Captain (In Progress)"
                                            match.isFinished -> "$matchDate - $team1Captain vs $team2Captain"
                                            else -> "Match not started" // Fallback text when neither isStarted nor isFinished
                                        },
                                        style = androidx.compose.ui.text.TextStyle(
                                            textAlign = TextAlign.Start,
                                            fontSize = if (isTablet) 20.sp else 10.sp
                                        )
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 30.dp),
                            horizontalAlignment = Alignment.End
                        )
                        {
                            // Synchronization Icon
                            IconButton(
                                enabled = match.isFinished && !match.isSynced,
                                onClick = {
                                    val googleSheetsService = GoogleSheetsService()

                                    CoroutineScope(Dispatchers.Main).launch {
                                        try {
                                            // Step 1: Read existing data from column A to find the last row
                                            val existingData = googleSheetsService.readData(context,"Data Raw!A:A")
                                            val lastRowIndex = existingData.size // This gives the number of existing rows

                                            val (dataToWrite, matchDataSize) = getMatchDataToUpload(context, match.matchId)

                                            val startRow = lastRowIndex + 1
                                            val endRow = lastRowIndex + matchDataSize

                                            // Define the range starting from the next available row in column A
                                            val rangeToWrite = "Data Raw!A${lastRowIndex + 1}:AN${lastRowIndex + matchDataSize}"

                                            // Step 3: Write the new data to the calculated range
                                            val rtnMessage = googleSheetsService.writeData(context, rangeToWrite, startRow, endRow, dataToWrite)

                                            // Switch back to the Main thread to show Toast
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    rtnMessage,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync Status",
                                    tint = if (match.isSynced) Color.Green else Color.Gray,
                                    modifier = Modifier.size(if (isTablet) 48.dp else 24.dp) // Size based on isTablet
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}