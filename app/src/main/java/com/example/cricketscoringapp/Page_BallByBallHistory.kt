package com.example.cricketscoringapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cricketscoringapp.ui.theme.CricketAppTheme

@Composable
fun BallByBallHistoryPage(matchId: String, teamIdA: Int, teamIdB: Int) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val dbHelper = CricketDatabaseHelper(context)

    val ballHistoryA = remember { mutableStateListOf<BallEvent>() }
    val historyListStateA = rememberLazyListState()
    val ballHistoryB = remember { mutableStateListOf<BallEvent>() }
    val historyListStateB = rememberLazyListState()
    var teamACaptain by remember { mutableStateOf("") }
    var teamBCaptain by remember { mutableStateOf("") }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(matchId, teamIdA, teamIdB) {
        if (teamIdA != 0) {
            // Load ball history from database
            teamACaptain = dbHelper.getBattingTeamCaptain(matchId, teamIdA)
            val teamId = dbHelper.getTeamForPlayer(matchId, teamACaptain)
            val history = dbHelper.getBallByBallHistory(matchId, if (teamId == 1) 2 else 1)
            ballHistoryA.clear()
            ballHistoryA.addAll(history)
        }

        if (teamIdB != 0) {
            // Load ball history from database
            teamBCaptain = dbHelper.getBattingTeamCaptain(matchId, teamIdB)
            val teamId = dbHelper.getTeamForPlayer(matchId, teamBCaptain)
            val history = dbHelper.getBallByBallHistory(matchId, if (teamId == 1) 2 else 1)
            ballHistoryB.clear()
            ballHistoryB.addAll(history)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CricketAppTheme.colors.primaryDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Full Ball by Ball History",
            fontSize = CricketAppTheme.dimens.headerSize,
            color = CricketAppTheme.colors.textOnDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Custom Tab Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (teamIdA != 0) {
                Button(
                    onClick = { selectedTabIndex = 0 },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTabIndex == 0) CricketAppTheme.colors.accentGreen else Color(0xFF424242)
                    )
                ) {
                    Text(
                        text = "📊 Team $teamACaptain",
                        fontSize = CricketAppTheme.dimens.microSize,
                        textAlign = TextAlign.Center,
                        color = if (selectedTabIndex == 0) Color.Black else Color.White
                    )
                }
            }

            if (teamIdB != 0) {
                Button(
                    onClick = { selectedTabIndex = 1 },
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTabIndex == 1) CricketAppTheme.colors.accentGreen else Color(0xFF424242)
                    )
                ) {
                    Text(
                        text = "📊 Team $teamBCaptain",
                        fontSize = CricketAppTheme.dimens.microSize,
                        textAlign = TextAlign.Center,
                        color = if (selectedTabIndex == 1) Color.Black else Color.White
                    )
                }
            }
        }

        // Display content based on selected tab
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    if (teamIdA != 0) {
                        ModernBallByBallHistory(
                            ballHistory = ballHistoryA,
                            listState = historyListStateA
                        )
                    }
                }
                1 -> {
                    if (teamIdB != 0) {
                        ModernBallByBallHistory(
                            ballHistory = ballHistoryB,
                            listState = historyListStateB
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernBallByBallHistory(
    ballHistory: List<BallEvent>,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    Column(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(ballHistory) { ball ->
                if (ball.isOverSummary) {
                    OverSummaryItem(ball)
                } else {
                    ModernBallHistoryItem(ball)
                }
            }
        }
    }
}

@Composable
fun ModernBallHistoryItem(ball: BallEvent) {
    val resultColor = when {
        ball.result == "•" || ball.result == "0" -> Color(0xFF757575)
        ball.result.startsWith("WK") -> Color(0xFFFF1744)
        ball.result.startsWith("W") || ball.result.startsWith("NB") -> Color(0xFFFFA726)
        else -> Color(0xFF42A5F5)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${ball.over}     ${ball.bowler} to ${ball.batsman}",
            color = Color(0xFFE0E0E0),
            fontSize = CricketAppTheme.dimens.bodySize,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = ball.resultText,
            color = resultColor,
            fontSize = CricketAppTheme.dimens.bodySize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun OverSummaryItem(ball: BallEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFF1E1E1E))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val accentColor = CricketAppTheme.colors.accentGreen
        Text(
            text = "END OF OVER ${ball.over}",
            color = accentColor,
            fontSize = CricketAppTheme.dimens.bodySize,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${ball.overRuns} runs",
                color = accentColor,
                fontSize = CricketAppTheme.dimens.bodySize,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${ball.overExtras} extras",
                color = accentColor,
                fontSize = CricketAppTheme.dimens.bodySize,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = ball.totalScore,
                color = accentColor,
                fontSize = CricketAppTheme.dimens.bodySize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}