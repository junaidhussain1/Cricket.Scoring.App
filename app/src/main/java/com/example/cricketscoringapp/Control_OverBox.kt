package com.example.cricketscoringapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.cricketscoringapp.ui.theme.CricketAppTheme

@Composable
fun OverBox(
    heading1: String,
    balls: List<Ball>,
    headingFontBold1: FontWeight,
    ballsFontBold1: FontWeight,
    backcolor1: Color
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Surface(color = backcolor1, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(
                start = 0.dp,
                top = 8.dp,
                end = 8.dp,
                bottom = 8.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .height(if (isTablet) 30.dp else 20.dp)
            ) {
                Text(
                    text = heading1,
                    fontSize = CricketAppTheme.dimens.bodySize,
                    fontWeight = headingFontBold1,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height((if (isTablet) 30.dp else 20.dp))
                )

                LazyRow(
                    modifier = Modifier
                        .wrapContentWidth()
                        .heightIn(max = 56.dp)
                ) {
                    items(balls.size) { index ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                        ) {
                            // Check if action is "WK"
                            if (balls[index].action.startsWith("WK")) {
                                Image(
                                    painter = painterResource(id = R.drawable.out_image2),
                                    modifier = Modifier
                                        .fillMaxSize(), // Fill the IconButton
                                    contentDescription = "Out Image",
                                    contentScale = ContentScale.Fit
                                )
                            } else if (balls[index].action == "0") {
                                Image(
                                    painter = painterResource(id = R.drawable.ball_4),
                                    modifier = Modifier
                                        .fillMaxSize(), // Fill the IconButton
                                    contentDescription = "Ball Image",
                                    contentScale = ContentScale.Fit
                                )
                            }
                            else {
                                // Display the action value as a Text
                                Text(
                                    text = balls[index].action,
                                    fontWeight = ballsFontBold1,
                                    fontSize = CricketAppTheme.dimens.titleSize,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}