package com.example.cricketscoringapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OverBox(
    heading1: String,
    balls: List<String>,
    headingFontBold1: FontWeight,
    ballsFontBold1: FontWeight,
    fontColor1: Color,
    backcolor1: Color
) {
    val lazyListState = rememberLazyListState() // Remember the list state

    // Find the index of the last non-blank ball
    val lastNonBlankIndex = balls.indexOfLast { it.isNotBlank() }

    LaunchedEffect(balls, lastNonBlankIndex) {
        if (lastNonBlankIndex != -1) {
            // Get the list of currently visible items
            val visibleItemsInfo = lazyListState.layoutInfo.visibleItemsInfo

            // Check if the last non-blank item is visible
            val isLastItemVisible = visibleItemsInfo.any { it.index == lastNonBlankIndex }

            // Scroll to the last non-blank item if it's not visible
            if (!isLastItemVisible) {
                lazyListState.animateScrollToItem(lastNonBlankIndex)
            }
        }
    }

    Surface(color = backcolor1, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(8.dp)
        ) {
            // Use a Row to keep the heading and balls on the same row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp) // Optional padding for spacing
            ) {
                // Heading
                Text(
                    text = heading1,
                    fontSize = 20.sp,
                    fontWeight = headingFontBold1,
                    color = fontColor1,
                    modifier = Modifier
                        .padding(end = 8.dp) // Optional padding between heading and balls
                )

                // Horizontal Scrollable LazyRow for Balls
                LazyRow(
                    state = lazyListState,
                    modifier = Modifier
                        .wrapContentWidth() // Adjust to the width of content, no unnecessary space
                        .heightIn(max = 56.dp) // Set a maximum height for the LazyRow
                ) {
                    items(balls.size) { index ->
                        val fontColor2 = if (balls[index].contains("WI", ignoreCase = true)) {
                            Color.Red
                        } else {
                            Color.Black
                        }

                        Text(
                            text = balls[index],
                            fontWeight = ballsFontBold1,
                            fontSize = 22.sp,
                            color = fontColor2,
                            modifier = Modifier
                                .padding(horizontal = 8.dp) // Padding between ball texts
                        )
                    }
                }
            }
        }
    }
}