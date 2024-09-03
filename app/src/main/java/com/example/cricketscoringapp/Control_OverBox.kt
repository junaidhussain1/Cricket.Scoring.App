package com.example.cricketscoringapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun OverBox(
    heading1: String,
    balls: List<Ball>,
    headingFontBold1: FontWeight,
    ballsFontBold1: FontWeight,
    fontColor1: Color,
    backcolor1: Color
) {
    Surface(color = backcolor1, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = heading1,
                    fontSize = 20.sp,
                    fontWeight = headingFontBold1,
                    color = fontColor1,
                    modifier = Modifier
                        .padding(end = 8.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .wrapContentWidth()
                        .heightIn(max = 56.dp)
                ) {
                    items(balls.size) { index ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically, // Ensure items are centered vertically
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                        ) {
                            // Check if action is "WI"
                            if (balls[index].action.startsWith("WK")) {
                                // Draw a red circle if the action is "WI"
                                Canvas(
                                    modifier = Modifier
                                        .size(24.dp) // Size of the circle
                                ) {
                                    drawCircle(
                                        color = Color.Red,
                                        radius = size.minDimension / 2
                                    )
                                }
                            } else {
                                // Display the action value as a Text
                                Text(
                                    text = balls[index].action,
                                    fontWeight = ballsFontBold1,
                                    fontSize = 22.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}