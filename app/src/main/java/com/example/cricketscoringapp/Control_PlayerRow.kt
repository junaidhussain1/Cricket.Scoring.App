package com.example.cricketscoringapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerRow(player: String, onDelete: () -> Unit) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = player,
            modifier = Modifier.weight(1f),
            fontSize = if (isTablet) 32.sp else 22.sp)
        Button(
            onClick = onDelete,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(255, 252, 228)
            )
        ) {
                Text(
                    text = "Delete",
                    fontSize = if (isTablet) 32.sp else 22.sp,
                    color = Color(10, 18, 32)
            )
        }
    }
}