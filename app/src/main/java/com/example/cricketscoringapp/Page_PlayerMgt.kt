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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle

@Composable
fun PlayerMgtPage() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(10, 18, 32)
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
                label = { Text("Enter Player Name", fontSize = if (isTablet) 22.sp else 14.sp) },
                textStyle = TextStyle(fontSize = if (isTablet) 32.sp else 14.sp),
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
                            Toast.makeText(context, "Player already exists!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(context, "Player name cannot be empty", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(255, 252, 228) // Button background color
                ),
            ) {
                Text(
                    "Add Player",
                    fontSize = if (isTablet) 32.sp else 20.sp,// Larger font size for tablets)
                    color = Color(10, 18, 32)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display the list of players in a scrollable LazyColumn
            if (playersList.isNotEmpty()) {
                Text(
                    "Player List: " + playersList.size.toString(),
                    style = MaterialTheme.typography.headlineSmall
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
                Text("No players found")
            }
        }
    }
}