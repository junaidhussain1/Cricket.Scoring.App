package com.example.cricketscoringapp

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TeamScoreBox(name1: String,
                 overs1: String,
                 score1: String,
                 color1: Color
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Surface(color = color1, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)  // Optional padding
        ) {
            Text(
                text = "Team $name1!",
                fontSize = if (isTablet) 30.sp else 18.sp,  // Increase the font size here
                color =  Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(2f)// This pushes the next Text to the right
                    .align(Alignment.CenterVertically)
                    .wrapContentWidth(Alignment.Start)

            )
            Text(
                text = overs1,
                fontSize = if (isTablet) 30.sp else 24.sp,  // Increase the font size here
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)  // Occupies space proportionally
                    .align(Alignment.CenterVertically) // Aligns text vertically center
                    .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
            )
            Text(
                text = score1,
                fontSize = if (isTablet) 30.sp else 24.sp,  // Increase the font size here
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)  // Aligns text vertically center
                    .wrapContentWidth(Alignment.End) // Aligns text horizontally right
            )
        }
    }
}