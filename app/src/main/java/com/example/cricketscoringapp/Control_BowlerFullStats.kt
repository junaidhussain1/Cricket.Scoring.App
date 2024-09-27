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
fun BowlerFullStats(bowler: String,
                    totalOvers: String,
                    totalMaidens: String,
                    totalRuns: String,
                    totalWickets: String,
                    totalNoBalls: String,
                    totalWides: String,
                    totalFours: String,
                    totalSixes: String,
                    fontBold1: FontWeight,
                    fontColor1: Color) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val fontSize1 = if (isTablet) 16.sp else 10.sp

    Surface(color = Color.Black, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)  // Optional padding
        ) {
            Text(
                text = bowler,
                fontWeight = fontBold1,
                fontSize = fontSize1,
                color = fontColor1,
                modifier = Modifier.weight(2f) // This pushes the next Text to the right
            )
            Text(
                text = totalOvers,
                fontWeight = fontBold1,
                fontSize = fontSize1,
                color = fontColor1,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically) // Aligns text vertically center
                    .wrapContentWidth(Alignment.Start) // Centers text horizontally
            )
            Text(
                text = totalMaidens,
                fontWeight = fontBold1,
                fontSize = fontSize1,
                color = fontColor1,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)  // Aligns text vertically center
                    .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
            )
            Text(
                text = totalRuns,
                fontWeight = fontBold1,
                fontSize = fontSize1,
                color = fontColor1,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)  // Aligns text vertically center
                    .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
            )
            Text(
                text = totalWickets,
                fontWeight = fontBold1,
                fontSize = fontSize1,
                color = fontColor1,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)  // Aligns text vertically center
                    .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
            )
            Text(
                text = totalNoBalls,
                fontWeight = fontBold1,
                fontSize = fontSize1,
                color = fontColor1,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)  // Aligns text vertically center
                    .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
            )
            Text(
                text = totalWides,
                fontWeight = fontBold1,
                fontSize = fontSize1,
                color = fontColor1,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)  // Aligns text vertically center
                    .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
            )
            Text(
                text = totalFours,
                fontWeight = fontBold1,
                fontSize = fontSize1,
                color = fontColor1,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)  // Aligns text vertically center
                    .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
            )
            Text(
                text = totalSixes,
                fontWeight = fontBold1,
                fontSize = fontSize1,
                color = fontColor1,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)  // Aligns text vertically center
                    .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
            )
        }
    }
}