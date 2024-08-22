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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BatsmanBowlerBox(col1: String, col2: String, col3: String, col4: String, col5: String, fontBold1: FontWeight, fontColor1: Color) {
    Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)  // Optional padding
        ) {
            Text(
                text = col1,
                fontWeight = fontBold1,
                color = fontColor1,
                modifier = Modifier.weight(3f) // This pushes the next Text to the right
            )
            Text(
                text = col2,
                fontWeight = fontBold1,
                color = fontColor1,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically) // Aligns text vertically center
                    .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
            )
            Text(
                text = col3,
                fontWeight = fontBold1,
                color = fontColor1,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)  // Aligns text vertically center
                    .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
            )
            Text(
                text = col4,
                fontWeight = fontBold1,
                color = fontColor1,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)  // Aligns text vertically center
                    .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
            )
            Text(
                text = col5,
                fontWeight = fontBold1,
                color = fontColor1,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)  // Aligns text vertically center
                    .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
            )
        }
    }
}