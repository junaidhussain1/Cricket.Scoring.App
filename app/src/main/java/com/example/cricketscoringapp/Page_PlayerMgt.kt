package com.example.cricketscoringapp

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cricketscoringapp.ui.theme.CricketAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PlayerMgtPage() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CricketAppTheme.colors.primaryDark
    ) {

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
                label = { Text("Enter Player Name", fontSize = CricketAppTheme.dimens.smallSize) },
                textStyle = TextStyle(fontSize = CricketAppTheme.dimens.titleSize),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done // Set the IME action to "Done"
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Trigger the add player logic when the "Done" button is pressed
                        if (playerName.isNotEmpty()) {
                            if (!dbHelper.isPlayerAlreadyExists(playerName)) {
                                dbHelper.addPlayer(playerName)
                                Toast.makeText(context, "Player Added", Toast.LENGTH_SHORT).show()
                                playerName = ""
                                // Show all players
                                playersList.clear()
                                playersList.addAll(dbHelper.getAllPlayers())
                            } else {
                                Toast.makeText(context, "Player already exists!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Player name cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Button to add player to the database
            Button(
                onClick = {
                    if (playerName.isNotEmpty()) {
                        if (!dbHelper.isPlayerAlreadyExists(playerName)) {
                            dbHelper.addPlayer(playerName)
                            Toast.makeText(context, "Player Added", Toast.LENGTH_SHORT).show()
                            playerName = ""
                            //Show all players
                            playersList.clear()
                            playersList.addAll(dbHelper.getAllPlayers())
                        } else {
                            Toast.makeText(context, "Player already exists!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(context, "Player name cannot be empty", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CricketAppTheme.colors.primaryCream
                ),
            ) {
                Text(
                    "Add Player",
                    fontSize = CricketAppTheme.dimens.titleSize,
                    color = CricketAppTheme.colors.textOnLight
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            IconButton(
                onClick = {
                    val googleSheetsService = GoogleSheetsService()

                    CoroutineScope(Dispatchers.Main).launch {
                        // Read data asynchronously from Google Sheets
                        val data = googleSheetsService.readData(context,"Names!A:A")

                        data.drop(1).forEach { row ->
                            if (row.isNotEmpty()) {
                                // Assuming the player's name is in the first column of each row
                                val player = row[0] as? String ?: ""

                                if (player.isNotEmpty()) {
                                    if (!dbHelper.isPlayerAlreadyExists(player)) {
                                        dbHelper.addPlayer(player)
                                        Toast.makeText(context, "Player Added", Toast.LENGTH_SHORT).show()

                                        // Clear and update player list
                                        playersList.clear()
                                        playersList.addAll(dbHelper.getAllPlayers())
                                    } else {
                                        Toast.makeText(context, "Player already exists!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Player name cannot be empty", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync Status",
                    tint = Color.Green,
                    modifier = Modifier.size(CricketAppTheme.dimens.iconSize)
                )
            }

            // Display the list of players in a scrollable LazyColumn
            if (playersList.isNotEmpty()) {
                Text(
                    "Player List: " + playersList.size.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = CricketAppTheme.dimens.bodySize,
                    color = CricketAppTheme.colors.textOnDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(0.5f)
                        .weight(1f)
                )
                {
                    items(playersList) { player ->
                        PlayerRow(player = player.name, onDelete = {
                            dbHelper.deletePlayer(player.name)
                            playersList.remove(player)
                        })
                    }
                }
            } else {
                Text("No players found", color = CricketAppTheme.colors.textOnDark)
            }
        }
    }
}