package com.example.cricketscoringapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExistingMatchesPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Existing Matches",
            fontSize = 22.sp)

//        Spacer(modifier = Modifier.height(20.dp))
//        val context = LocalContext.current
//        val dbHelper = CricketDatabaseHelper(context)
//
//        val matches = dbHelper.getMatches(matchId, 1, 0)
//        for (match in matches) {
//            Text(text = player.name)
//        }
    }
}