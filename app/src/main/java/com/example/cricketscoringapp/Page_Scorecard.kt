package com.example.cricketscoringapp

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.util.Locale


@Composable
fun ScoreCardPage(navController: NavHostController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val dbHelper = CricketDatabaseHelper(context)
    val matchId = dbHelper.getMatchId()
    val noOfOversAside = dbHelper.getNoOfOversAside(matchId).toDouble()
    val noOfPlayersAside = dbHelper.getNoOfPlayersAside(matchId)
    val currentBowler = remember { mutableStateOf(dbHelper.getCurrentBowler(matchId)) }
    val bowlingTeamId = dbHelper.getTeamForPlayer(matchId,currentBowler.value)
    val bowlingTeam = bowlingTeamId.let { dbHelper.getTeamPlayers(matchId, it,1) }
    val firstTeamId = dbHelper.getTeamForPlayer(matchId,dbHelper.getFirstBattingTeamStriker(matchId))
    val secondTeamId = if (firstTeamId == 1) 2 else 1

    val showWidesDialog = remember { mutableStateOf(false) }
    val selectedWidesOption = remember { mutableStateOf("") }
    val showNoBallDialog = remember { mutableStateOf(false) }
    val selectedNoBallOption = remember { mutableStateOf("") }
    val showExtrasDialog = remember { mutableStateOf(false) }
    val selectedExtrasOption = remember { mutableStateOf("") }
    val showWicketsDialog = remember { mutableStateOf(false) }
    val selectedWicketsOption = remember { mutableStateOf("") }
    val showNextBatsmanDialog = remember { mutableStateOf(false) }
    val showBatsmanRunOutSelectionDialog = remember { mutableStateOf(false) }
    val batsmanOverride = remember { mutableStateOf(false) }
    val showUndoConfirmationDialog = remember { mutableStateOf(false) }
    val showInningsConfirmationDialog = remember { mutableStateOf(false) }
    val showMoreDialog = remember { mutableStateOf(false) }

    val showBowlerChangeDialog = remember { mutableStateOf(false) }
    val showKeeperChangeDialog = remember { mutableStateOf(false) }
    val showFielderDialog = remember { mutableStateOf(false) }
    val selectedFielder = remember { mutableStateOf("") }
    val manualBowlerChange = remember { mutableStateOf(false) }

    // Ball by ball history
    val ballHistory = remember { mutableStateListOf<BallEvent>() }
    val historyListState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Retrieve the captain names from the view model
        var team1Captain = Player(dbHelper.getCaptainForTeam(matchId, 1))
        var team2Captain = Player(dbHelper.getCaptainForTeam(matchId, 2))
        val firstBattingTeamCaptain = Player(dbHelper.getBattingTeamCaptain(matchId,1))
        var team1Id = 1
        var team2Id = 2
        if (team1Captain != firstBattingTeamCaptain) {
            team2Captain = team1Captain
            team1Captain = firstBattingTeamCaptain
            team1Id = 2
            team2Id = 1
        }

        val team1Stats = dbHelper.getTeamStats(matchId,team1Id,team1Captain.name)
        val firstBattingTeamStats = remember(team1Stats) {
            TeamStats(
                name = mutableStateOf(team1Stats.name.value),
                overs = mutableDoubleStateOf(team1Stats.overs.value),
                inningScore = mutableIntStateOf(team1Stats.inningScore.value),
                inningWickets = mutableIntStateOf(team1Stats.inningWickets.value),
                active = mutableStateOf(team1Stats.active.value)
            )
        }

        val team2Stats = dbHelper.getTeamStats(matchId,team2Id,team2Captain.name)
        val secondBattingTeamStats = remember(team2Stats) {
            TeamStats(
                name = mutableStateOf(team2Stats.name.value),
                overs = mutableDoubleStateOf(team2Stats.overs.value),
                inningScore = mutableIntStateOf(team2Stats.inningScore.value),
                inningWickets = mutableIntStateOf(team2Stats.inningWickets.value),
                active = mutableStateOf(team2Stats.active.value)
            )
        }

        val (runsToWinLocal,winningCaptain) = calcRunsToWin(firstBattingTeamStats, secondBattingTeamStats, noOfOversAside)
        val runsToWin = remember(runsToWinLocal) {
            runsToWinLocal
        }

        val firstBatsman = dbHelper.getBatsmanByStatus(matchId,"striker")
        val firstBatsmanStats = remember {
            BatsmanStats(
                name = mutableStateOf(value = firstBatsman.name.value),
                runs = mutableIntStateOf(value = firstBatsman.runs.value),
                balls = mutableIntStateOf(value = firstBatsman.balls.value),
                fours = mutableIntStateOf(value = firstBatsman.fours.value),
                sixes = mutableIntStateOf(value = firstBatsman.sixes.value),
                dotballs = mutableIntStateOf(value = firstBatsman.dotballs.value),
                wicketDescription = mutableStateOf(value = firstBatsman.wicketDescription.value),
                active = mutableStateOf(value = firstBatsman.active.value)
            )
        }

        val secondBatsman = dbHelper.getBatsmanByStatus(matchId,"non-striker")
        val secondBatsmanStats = remember {
            BatsmanStats(
                name = mutableStateOf(value = secondBatsman.name.value),
                runs = mutableIntStateOf(value = secondBatsman.runs.value),
                balls = mutableIntStateOf(value = secondBatsman.balls.value),
                fours = mutableIntStateOf(value = secondBatsman.fours.value),
                sixes = mutableIntStateOf(value = secondBatsman.sixes.value),
                dotballs = mutableIntStateOf(value = secondBatsman.dotballs.value),
                wicketDescription = mutableStateOf(value = secondBatsman.name.value),
                active = mutableStateOf(value = secondBatsman.active.value)
            )
        }

        val currentOverBowler = dbHelper.getCurrentBowlerStats(matchId)
        val currentOverBowlerStats = remember(currentOverBowler) {
            BowlerStats(
                name = mutableStateOf(currentOverBowler.name.value),
                over = mutableDoubleStateOf(currentOverBowler.over.value),
                maiden = mutableIntStateOf(currentOverBowler.maiden.value),
                runs = mutableIntStateOf(currentOverBowler.runs.value),
                wickets = mutableIntStateOf(currentOverBowler.wickets.value),
                noballs = mutableIntStateOf(currentOverBowler.noballs.value),
                wides = mutableIntStateOf(currentOverBowler.wides.value),
                byes = mutableIntStateOf(currentOverBowler.byes.value),
                legbyes = mutableIntStateOf(currentOverBowler.legbyes.value),
                fours = mutableIntStateOf(currentOverBowler.fours.value),
                sixes = mutableIntStateOf(currentOverBowler.sixes.value),
                dotballs = mutableIntStateOf(currentOverBowler.dotballs.value),
                keepername = mutableStateOf(currentOverBowler.keepername.value),
                overrecord = mutableStateOf(currentOverBowler.overrecord.value)
            )
        }

        val consolidatedBowler = dbHelper.getConsolidatedBowlerStats(matchId,currentOverBowler.name.value)
        val consolidatedBowlerStats = remember(consolidatedBowler) {
            BowlerStats(
                name = mutableStateOf(consolidatedBowler.name.value),
                over = mutableDoubleStateOf(consolidatedBowler.over.value),
                maiden = mutableIntStateOf(consolidatedBowler.maiden.value),
                runs = mutableIntStateOf(consolidatedBowler.runs.value),
                wickets = mutableIntStateOf(consolidatedBowler.wickets.value),
                noballs = mutableIntStateOf(consolidatedBowler.noballs.value),
                wides = mutableIntStateOf(consolidatedBowler.wides.value),
                byes = mutableIntStateOf(consolidatedBowler.byes.value),
                legbyes = mutableIntStateOf(consolidatedBowler.legbyes.value),
                fours = mutableIntStateOf(consolidatedBowler.fours.value),
                sixes = mutableIntStateOf(consolidatedBowler.sixes.value),
                dotballs = mutableIntStateOf(consolidatedBowler.dotballs.value),
                keepername = mutableStateOf(consolidatedBowler.keepername.value),
                overrecord = mutableStateOf(consolidatedBowler.overrecord.value)
            )
        }

        val balls = remember { mutableStateListOf<Ball>() }

        //Rebuild current over from current bowler stats in database
        if (currentOverBowlerStats.overrecord.value.contains(",")) {
            val ballValues = currentOverBowlerStats.overrecord.value.split("|")
            balls.clear()
            ballValues.forEach { value ->
                val pipeValues = value.split(",")
                val ball = Ball(pipeValues[0], pipeValues[1])
                balls.add(ball)
            }
        } else if (currentOverBowlerStats.overrecord.value != "") {
            val pipeValues = currentOverBowlerStats.overrecord.value.split(",")
            val ball = Ball(pipeValues[0], pipeValues[1])
            balls.add(ball)
        }

        // Load ball history from database
        LaunchedEffect(matchId) {
            val history = dbHelper.getBallByBallHistory(matchId)
            ballHistory.clear()
            ballHistory.addAll(history)
        }

        // Auto-scroll to top when new ball is added
        LaunchedEffect(ballHistory.size) {
            if (ballHistory.isNotEmpty()) {
                historyListState.animateScrollToItem(0)
            }
        }

        //Automatically handle: End of Over, End of Innings, End of Match
        val team1wickets = dbHelper.getTeamWickets(matchId,firstTeamId)
        val team2wickets = dbHelper.getTeamWickets(matchId,secondTeamId)
        val team2batters = dbHelper.getTeamBatters(matchId,secondTeamId)
        val team1OversBowled = dbHelper.getTeamOversBowled(matchId, secondTeamId)
        val team2OversBowled = dbHelper.getTeamOversBowled(matchId, firstTeamId)

        if (dbHelper.getIsMatchStarted(matchId)) {
            if ((team1wickets == noOfPlayersAside * 2) and (team2batters == 0)) {
                dbHelper.updateBowlingStats(matchId,"bowled")
                handleLastBatsmen(context,matchId,firstBatsmanStats,secondBatsmanStats)
                navController.navigate("secondinningssetup")
                {
                    popUpTo("scorecard") { inclusive = true }
                }
            } else if (team2wickets == noOfPlayersAside * 2) {
                handleEndOfMatch(context,matchId,firstBatsmanStats, secondBatsmanStats, runsToWin, winningCaptain)
                navController.navigate("homepage")
            } else {
                if (endOfOverReached(balls)) {
                    if ((team1OversBowled == noOfOversAside) && (team2OversBowled == 0.0)) {
                        dbHelper.updateBowlingStats(matchId,"bowled")
                        handleLastBatsmen(context,matchId,firstBatsmanStats,secondBatsmanStats)
                        navController.navigate("secondinningssetup")
                        {
                            popUpTo("scorecard") { inclusive = true }
                        }
                    } else if (team2OversBowled == noOfOversAside) {
                        handleEndOfMatch(context,matchId,firstBatsmanStats, secondBatsmanStats, runsToWin, winningCaptain)
                        navController.navigate("homepage")
                    } else {
                        showBowlerChangeDialog.value = true
                    }
                }
            }
        }

        // MODERN COMPACT HEADER
        ModernCompactHeader(
            team1Name = firstBattingTeamStats.name.value,
            team1Score = String.format(Locale.UK, "%d/%d",
                firstBattingTeamStats.inningScore.value,
                firstBattingTeamStats.inningWickets.value),
            team1Overs = String.format(Locale.UK, "%.1f", firstBattingTeamStats.overs.value),
            team2Name = secondBattingTeamStats.name.value,
            team2Score = String.format(Locale.UK, "%d/%d",
                secondBattingTeamStats.inningScore.value,
                secondBattingTeamStats.inningWickets.value),
            team1Active = firstBattingTeamStats.active.value,
            oversRemaining = String.format(Locale.UK, "%.1f",
                noOfOversAside - (if (firstBattingTeamStats.active.value)
                    firstBattingTeamStats.overs.value
                else
                    secondBattingTeamStats.overs.value)),
            runsToWin = runsToWin,
            onTeamClick = { teamIdA ->
                val teamIdB = 0
                navController.navigate("inningstats/${matchId}/${teamIdA}/${teamIdB}")
            }
        )

        // SCROLLABLE CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // BATTING CARD
            ModernBattingCard(
                striker = firstBatsmanStats,
                nonStriker = secondBatsmanStats,
                onBatsmanClick = {
                    swapBatsmenDB(context, matchId, firstBatsmanStats, secondBatsmanStats)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // BOWLING CARD
            ModernBowlingCard(
                bowler = consolidatedBowlerStats,
                keeper = currentOverBowlerStats.keepername.value,
                onBowlerClick = {
                    if (balls.isEmpty() || balls.all { it.action.isBlank() }) {
                        showBowlerChangeDialog.value = true
                        manualBowlerChange.value = true
                    }
                },
                onKeeperClick = {
                    showKeeperChangeDialog.value = true
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // This Over Label
            Text(
                text = "This Over: (${String.format(Locale.UK, "%.1f", currentOverBowlerStats.over.value)})",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            // SCORING BUTTONS + BALL HISTORY (SIDE BY SIDE)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                // Scoring Buttons (2/3 width)
                Card(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .padding(end = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    ModernScoringButtons(
                        balls = balls,
                        currentOverBowlerStats = currentOverBowlerStats,
                        firstBatsmanStats = firstBatsmanStats,
                        secondBatsmanStats = secondBatsmanStats,
                        firstBattingTeamStats = firstBattingTeamStats,
                        secondBattingTeamStats = secondBattingTeamStats,
                        context = context,
                        isTablet = isTablet,
                        showWidesDialog = showWidesDialog,
                        showNoBallDialog = showNoBallDialog,
                        showExtrasDialog = showExtrasDialog,
                        showWicketsDialog = showWicketsDialog,
                        showMoreDialog = showMoreDialog,
                        showUndoConfirmationDialog = showUndoConfirmationDialog,
                        onUpdateBallHistory = { newBall ->
                            ballHistory.add(0, newBall)
                        }
                    )
                }

                // Ball by Ball History (1/3 width)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    ModernBallByBallHistory(
                        ballHistory = ballHistory,
                        listState = historyListState
                    )
                }
            }
        }

        // ALL DIALOGS (keeping your existing dialog logic)
        if (showWidesDialog.value) {
            WidesDialog(
                onDismiss = { showWidesDialog.value = false },
                onSelect = { option ->
                    selectedWidesOption.value = option
                    showWidesDialog.value = false
                    updateStats(context,balls,selectedWidesOption.value,currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)

                    // Add to ball history
                    val activeBatsman = if (firstBatsmanStats.active.value) firstBatsmanStats.name.value else secondBatsmanStats.name.value
                    ballHistory.add(0, BallEvent(
                        over = String.format(Locale.UK, "%.1f", currentOverBowlerStats.over.value),
                        bowler = currentOverBowlerStats.name.value,
                        batsman = activeBatsman,
                        result = option
                    ))
                },
                isTablet = isTablet
            )
        }

        if (showNoBallDialog.value) {
            NoBallDialog(
                onDismiss = { showNoBallDialog.value = false },
                onSelect = { option ->
                    selectedNoBallOption.value = option
                    showNoBallDialog.value = false
                    updateStats(context,balls,selectedNoBallOption.value,currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)

                    // Add to ball history
                    val activeBatsman = if (firstBatsmanStats.active.value) firstBatsmanStats.name.value else secondBatsmanStats.name.value
                    ballHistory.add(0, BallEvent(
                        over = String.format(Locale.UK, "%.1f", currentOverBowlerStats.over.value),
                        bowler = currentOverBowlerStats.name.value,
                        batsman = activeBatsman,
                        result = option
                    ))
                },
                isTablet = isTablet
            )
        }

        if (showExtrasDialog.value) {
            ExtrasDialog(
                onDismiss = { showExtrasDialog.value = false },
                onSelect = { option ->
                    selectedExtrasOption.value = option
                    showExtrasDialog.value = false
                    updateStats(context,balls,selectedExtrasOption.value,currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)

                    // Add to ball history
                    val activeBatsman = if (firstBatsmanStats.active.value) firstBatsmanStats.name.value else secondBatsmanStats.name.value
                    ballHistory.add(0, BallEvent(
                        over = String.format(Locale.UK, "%.1f", currentOverBowlerStats.over.value),
                        bowler = currentOverBowlerStats.name.value,
                        batsman = activeBatsman,
                        result = option
                    ))
                },
                isTablet = isTablet
            )
        }

        if (showMoreDialog.value) {
            MoreDialog(
                onDismiss = { showMoreDialog.value = false },
                onLBW = {
                    updateStats(context,balls,"LBW",currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                    showMoreDialog.value = false
                },
                onChangeBatsman = {
                    val activeBatsman = getActiveBatsman(firstBatsmanStats,secondBatsmanStats)
                    val activeBatsmanStats = dbHelper.getBatsmanStats(matchId,activeBatsman)
                    if (activeBatsmanStats.balls.value == 0) {
                        batsmanOverride.value = true
                        showNextBatsmanDialog.value = true
                        showMoreDialog.value = false
                    } else {
                        Toast.makeText(context, "You can only change Batsman who has faced 0 balls!", Toast.LENGTH_SHORT).show()
                    }
                },
                onRetireBatsman = {
                    Toast.makeText(context, "Not working yet!", Toast.LENGTH_SHORT).show()
                },
                onEndInnings = {
                    showMoreDialog.value = false
                    showInningsConfirmationDialog.value = true
                },
                isTablet = isTablet
            )
        }

        if (showInningsConfirmationDialog.value) {
            val endOf1stInning = ((team1OversBowled == 0.0) || (team2OversBowled == 0.0))
            ConfirmationDialog(
                message = if (endOf1stInning) "End the 1st Inning?" else "End the Match?",
                onConfirm = {
                    showInningsConfirmationDialog.value = false
                    showMoreDialog.value = false
                    if (endOf1stInning) {
                        dbHelper.updateBowlingStats(matchId,"bowled")
                        handleLastBatsmen(context,matchId,firstBatsmanStats,secondBatsmanStats)
                        navController.navigate("secondinningssetup")
                        {
                            popUpTo("scorecard") { inclusive = true }
                        }
                    } else {
                        handleEndOfMatch(context,matchId,firstBatsmanStats, secondBatsmanStats, runsToWin, winningCaptain)
                        navController.navigate("homepage")
                    }
                },
                onDismiss = {
                    showInningsConfirmationDialog.value = false
                    showMoreDialog.value = true
                }
            )
        }

        if (showWicketsDialog.value) {
            WicketsDialog(
                onDismiss = { showWicketsDialog.value = false },
                onSelect = { option ->
                    selectedWicketsOption.value = option
                    when (option) {
                        "WKB", "WKHW", "WKLB" -> {
                            showNextBatsmanDialog.value = true
                            showWicketsDialog.value = false
                        }
                        "WKCB", "WKST", "WKSTW" -> {
                            selectedFielder.value = dbHelper.getCurrentKeeper(matchId)
                            showNextBatsmanDialog.value = true
                            showWicketsDialog.value = false
                        }
                        "WKC" -> {
                            showFielderDialog.value = true
                            showWicketsDialog.value = false
                        }
                        "WKRO", "WKRONB", "WKROW" -> {
                            showBatsmanRunOutSelectionDialog.value = true
                            showWicketsDialog.value = false
                        }
                    }
                    checkAndPlayDuckSound(firstBatsmanStats,secondBatsmanStats,context)
                },
                isTablet = isTablet
            )
        }

        if (showBatsmanRunOutSelectionDialog.value) {
            BatsmanRunOutDialog(
                firstBatsmanStats = firstBatsmanStats,
                secondBatsmanStats = secondBatsmanStats,
                onDismiss = {
                    showFielderDialog.value = false
                    showWicketsDialog.value = true
                    showBatsmanRunOutSelectionDialog.value = false
                },
                onSelect = { isFirstBatsman ->
                    if (isFirstBatsman && !firstBatsmanStats.active.value) {
                        swapBatsmenDB(context,matchId,firstBatsmanStats,secondBatsmanStats)
                    } else if (!isFirstBatsman && !secondBatsmanStats.active.value) {
                        swapBatsmenDB(context,matchId,firstBatsmanStats,secondBatsmanStats)
                    }
                    showBatsmanRunOutSelectionDialog.value = false
                    showFielderDialog.value = true
                },
                isTablet = isTablet
            )
        }

        if (showFielderDialog.value) {
            FielderDialog(
                bowlingTeam = bowlingTeam,
                onDismiss = {
                    selectedFielder.value = ""
                    showFielderDialog.value = false
                    showWicketsDialog.value = true
                },
                onSelect = { fielder ->
                    selectedFielder.value = fielder.name
                    showFielderDialog.value = false
                    showNextBatsmanDialog.value = true
                },
                isTablet = isTablet
            )
        }

        if (showNextBatsmanDialog.value) {
            NextBatsmanDialog(
                matchId = matchId,
                dbHelper = dbHelper,
                firstBatsmanStats = firstBatsmanStats,
                secondBatsmanStats = secondBatsmanStats,
                noOfPlayersAside = noOfPlayersAside,
                balls = balls,
                selectedWicketsOption = selectedWicketsOption,
                currentOverBowlerStats = currentOverBowlerStats,
                firstBattingTeamStats = firstBattingTeamStats,
                secondBattingTeamStats = secondBattingTeamStats,
                currentBowler = currentBowler,
                selectedFielder = selectedFielder,
                batsmanOverride = batsmanOverride,
                context = context,
                onDismiss = {
                    showNextBatsmanDialog.value = false
                    showWicketsDialog.value = true
                },
                onComplete = {
                    showNextBatsmanDialog.value = false
                },
                isTablet = isTablet,
                onUpdateBallHistory = { newBall ->
                    ballHistory.add(0, newBall)
                }
            )
        }

        if (showUndoConfirmationDialog.value) {
            val lastNonEmptyIndex = balls.indexOfLast { it.action.isNotEmpty() }
            if (lastNonEmptyIndex != -1) {
                val lastBall = balls[lastNonEmptyIndex].action
                if (lastBall.contains("WK")) {
                    showUndoConfirmationDialog.value = false
                    Toast.makeText(context, "Wicket UNDO is not supported!", Toast.LENGTH_SHORT).show()
                } else {
                    ConfirmationDialog(
                        message = "Are you sure you want to UNDO?",
                        onConfirm = {
                            showUndoConfirmationDialog.value = false
                            updateStats(context,balls,"UNDO",currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)

                            // Remove from ball history
                            if (ballHistory.isNotEmpty()) {
                                ballHistory.removeAt(0)
                            }
                        },
                        onDismiss = {
                            showUndoConfirmationDialog.value = false
                        }
                    )
                }
            } else {
                showUndoConfirmationDialog.value = false
            }
        }

        if (showBowlerChangeDialog.value) {
            BowlerChangeDialog(
                bowlingTeam = bowlingTeam,
                currentBowlerName = currentOverBowlerStats.name.value,
                matchId = matchId,
                dbHelper = dbHelper,
                bowlingTeamId = bowlingTeamId,
                manualBowlerChange = manualBowlerChange,
                onDismiss = { showBowlerChangeDialog.value = false },
                onSelect = { player ->
                    val existingKeeper = dbHelper.getLastKeeper(matchId, bowlingTeamId)
                    if (player.name == existingKeeper) {
                        showKeeperChangeDialog.value = true
                    }
                    if (manualBowlerChange.value) {
                        dbHelper.deleteCurrentBowler(matchId)
                        manualBowlerChange.value = false
                    } else {
                        dbHelper.updateBowlingStats(matchId,"bowled")
                    }
                    dbHelper.addBowlingStats(matchId,bowlingTeamId,player.name,existingKeeper,"bowling")
                    showBowlerChangeDialog.value = false
                    setCurrentBowlerAndKeeper(currentOverBowlerStats,player.name,existingKeeper)
                    swapBatsmenDB(context,matchId,firstBatsmanStats, secondBatsmanStats)
                    balls.clear()
                },
                isTablet = isTablet
            )
        }

        if (showKeeperChangeDialog.value) {
            KeeperChangeDialog(
                bowlingTeam = bowlingTeam,
                currentBowlerName = currentOverBowlerStats.name.value,
                currentKeeperName = currentOverBowlerStats.keepername.value,
                onDismiss = { showKeeperChangeDialog.value = false },
                onSelect = { player ->
                    dbHelper.updateBowlingStatsKeeper(matchId,bowlingTeamId,player.name)
                    showKeeperChangeDialog.value = false
                    setCurrentKeeper(currentOverBowlerStats, player.name)
                },
                isTablet = isTablet
            )
        }
    }
}

// MODERN UI COMPONENTS

@Composable
fun ModernCompactHeader(
    team1Name: String,
    team1Score: String,
    team1Overs: String,
    team2Name: String,
    team2Score: String,
    team1Active: Boolean,
    oversRemaining: String,
    runsToWin: String,
    onTeamClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (team1Active) Color(0xFF00E676) else Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = team1Name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "$team1Score ($team1Overs)",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Swap view",
                tint = Color.White,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(20.dp)
            )

            Text(
                text = team2Score,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = team2Name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "$oversRemaining overs remaining",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2E2E2E))
                .padding(vertical = 6.dp),
            textAlign = TextAlign.Center
        )

        if (runsToWin.isNotBlank() && !runsToWin.contains("won")) {
            Text(
                text = runsToWin,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF424242))
                    .padding(vertical = 6.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ModernBattingCard(
    striker: BatsmanStats,
    nonStriker: BatsmanStats,
    onBatsmanClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "🏏 BATTING",
                color = Color(0xFF00E676),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Striker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text(
                        text = if (striker.active.value) "★ " else "   ",
                        color = Color(0xFFFFC107),
                        fontSize = 16.sp
                    )
                    Text(
                        text = striker.name.value,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = if (striker.active.value) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Text(
                    text = "${striker.runs.value}(${striker.balls.value})",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = if (striker.active.value) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Non-striker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text(
                        text = if (nonStriker.active.value) "★ " else "   ",
                        color = Color(0xFFFFC107),
                        fontSize = 16.sp
                    )
                    Text(
                        text = nonStriker.name.value,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = if (nonStriker.active.value) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Text(
                    text = "${nonStriker.runs.value}(${nonStriker.balls.value})",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = if (nonStriker.active.value) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun ModernBowlingCard(
    bowler: BowlerStats,
    keeper: String,
    onBowlerClick: () -> Unit,
    onKeeperClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "⚾ BOWLING",
                color = Color(0xFF00E676),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = bowler.name.value,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format(Locale.UK, "%.1f-", bowler.over.value) +
                            "${bowler.maiden.value}-${bowler.runs.value}-${bowler.wickets.value}",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Keeper: $keeper",
                color = Color(0xFFB0B0B0),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ModernScoringButtons(
    balls: MutableList<Ball>,
    currentOverBowlerStats: BowlerStats,
    firstBatsmanStats: BatsmanStats,
    secondBatsmanStats: BatsmanStats,
    firstBattingTeamStats: TeamStats,
    secondBattingTeamStats: TeamStats,
    context: android.content.Context,
    isTablet: Boolean,
    showWidesDialog: androidx.compose.runtime.MutableState<Boolean>,
    showNoBallDialog: androidx.compose.runtime.MutableState<Boolean>,
    showExtrasDialog: androidx.compose.runtime.MutableState<Boolean>,
    showWicketsDialog: androidx.compose.runtime.MutableState<Boolean>,
    showMoreDialog: androidx.compose.runtime.MutableState<Boolean>,
    showUndoConfirmationDialog: androidx.compose.runtime.MutableState<Boolean>,
    onUpdateBallHistory: (BallEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Row 1: 0, 1, 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModernScoringButton("0", Color(0xFF42A5F5), Modifier.weight(1f)) {
                updateStats(context,balls,"0",currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                onUpdateBallHistory(createBallEvent(currentOverBowlerStats, firstBatsmanStats, secondBatsmanStats, "0"))
            }
            ModernScoringButton("1", Color(0xFF42A5F5), Modifier.weight(1f)) {
                updateStats(context,balls,"1",currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                onUpdateBallHistory(createBallEvent(currentOverBowlerStats, firstBatsmanStats, secondBatsmanStats, "1"))
            }
            ModernScoringButton("2", Color(0xFF42A5F5), Modifier.weight(1f)) {
                updateStats(context,balls,"2",currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                onUpdateBallHistory(createBallEvent(currentOverBowlerStats, firstBatsmanStats, secondBatsmanStats, "2"))
            }
        }

        // Row 2: 3, 4, 6
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModernScoringButton("3", Color(0xFF42A5F5), Modifier.weight(1f)) {
                updateStats(context,balls,"3",currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                onUpdateBallHistory(createBallEvent(currentOverBowlerStats, firstBatsmanStats, secondBatsmanStats, "3"))
            }
            ModernScoringButton("4", Color(0xFF66BB6A), Modifier.weight(1f)) {
                updateStats(context,balls,"4",currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                onUpdateBallHistory(createBallEvent(currentOverBowlerStats, firstBatsmanStats, secondBatsmanStats, "4"))
            }
            ModernScoringButton("6", Color(0xFF66BB6A), Modifier.weight(1f)) {
                updateStats(context,balls,"6",currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                onUpdateBallHistory(createBallEvent(currentOverBowlerStats, firstBatsmanStats, secondBatsmanStats, "6"))
            }
        }

        // Row 3: WICKET
        ModernScoringButton("WICKET", Color(0xFFFF1744), Modifier.fillMaxWidth()) {
            showWicketsDialog.value = true
        }

        // Row 4: WIDE, NO BALL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModernScoringButton("WIDE", Color(0xFFFFA726), Modifier.weight(1f), true) {
                showWidesDialog.value = true
            }
            ModernScoringButton("NO BALL", Color(0xFFFFA726), Modifier.weight(1f), true) {
                showNoBallDialog.value = true
            }
        }

        // Row 5: DOT, EXTRAS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModernScoringButton("•", Color(0xFF78909C), Modifier.weight(1f)) {
                updateStats(context,balls,"0",currentOverBowlerStats,firstBatsmanStats,secondBatsmanStats,firstBattingTeamStats,secondBattingTeamStats)
                onUpdateBallHistory(createBallEvent(currentOverBowlerStats, firstBatsmanStats, secondBatsmanStats, "•"))
            }
            ModernScoringButton("EXTRAS", Color(0xFF78909C), Modifier.weight(1f), true) {
                showExtrasDialog.value = true
            }
        }

        // Row 6: MORE
        ModernScoringButton("MORE", Color(0xFF78909C), Modifier.fillMaxWidth(), true) {
            showMoreDialog.value = true
        }

        // Row 7: UNDO
        ModernScoringButton("↶ UNDO", Color(0xFF616161), Modifier.fillMaxWidth(), true) {
            showUndoConfirmationDialog.value = true
        }
    }
}

@Composable
fun ModernScoringButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    isOutline: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxHeight()
            .padding(2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isOutline) Color.Transparent else color
        ),
        border = if (isOutline) BorderStroke(2.dp, color) else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = if (isOutline) color else Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ModernBallByBallHistory(
    ballHistory: List<BallEvent>,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "📊 BALL BY BALL",
            color = Color(0xFF00E676),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(ballHistory) { ball ->
                ModernBallHistoryItem(ball)
            }
        }
    }
}

@Composable
fun ModernBallHistoryItem(ball: BallEvent) {
    val resultColor = when {
        ball.result == "•" || ball.result == "0" -> Color(0xFF757575)
        ball.result.contains("WK") -> Color(0xFFFF1744)
        ball.result in listOf("4", "6") -> Color(0xFF66BB6A)
        ball.result.contains("W") || ball.result.contains("NB") -> Color(0xFFFFA726)
        else -> Color(0xFF42A5F5)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${ball.over} ${ball.bowler}→${ball.batsman}",
            color = Color(0xFFE0E0E0),
            fontSize = 10.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = ball.result,
            color = resultColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

// Helper function to create ball event
fun createBallEvent(
    bowler: BowlerStats,
    striker: BatsmanStats,
    nonStriker: BatsmanStats,
    result: String
): BallEvent {
    val activeBatsman = if (striker.active.value) striker.name.value else nonStriker.name.value
    return BallEvent(
        over = String.format(Locale.UK, "%.1f", bowler.over.value),
        bowler = bowler.name.value,
        batsman = activeBatsman,
        result = result
    )
}

// DIALOG COMPONENTS (Simplified versions - you'll need to add your full logic)

@Composable
fun WidesDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    isTablet: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select WIDE Option") },
        text = {
            Column {
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("W") }) {
                    Text("WIDE", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("W+1") }) {
                    Text("WIDE + 1", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("W+2") }) {
                    Text("WIDE + 2", fontSize = if (isTablet) 30.sp else 20.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// NO BALL DIALOG
@Composable
fun NoBallDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    isTablet: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select NO BALL Option") },
        text = {
            Column {
                // 5 Rows with 3 Buttons each
                for (row in 0..4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (col in 1..3) {
                            val optionIndex = row * 3 + col
                            val optionText = when (optionIndex) {
                                1 -> "NB"
                                2 -> ""
                                3 -> ""
                                4 -> "NB+1"
                                5 -> "NB+2"
                                6 -> "NB+3"
                                7 -> "NB+4"
                                8 -> ""
                                9 -> "NB+6"
                                10 -> "NBL1"
                                11 -> "NBL2"
                                12 -> "NBL3"
                                13 -> "NBB1"
                                14 -> "NBB2"
                                15 -> "NBB3"
                                else -> ""
                            }

                            if (optionText != "") {
                                val backgroundColor = when {
                                    optionText.startsWith("NBL") -> Color.Green
                                    optionText.startsWith("NBB") -> Color.Yellow
                                    else -> Color.Unspecified
                                }
                                Button(
                                    onClick = { onSelect(optionText) },
                                    colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .weight(1f)
                                ) {
                                    Text(optionText, fontSize = if (isTablet) 26.sp else 16.sp)
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// EXTRAS DIALOG
@Composable
fun ExtrasDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    isTablet: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select BYES Option") },
        text = {
            Column {
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("B1") }) {
                    Text("1 BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("B2") }) {
                    Text("2 BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("B3") }) {
                    Text("3 BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("LB1") }) {
                    Text("1 LEG-BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("LB2") }) {
                    Text("2 LEG-BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("LB3") }) {
                    Text("3 LEG-BYE", fontSize = if (isTablet) 30.sp else 20.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// MORE DIALOG
@Composable
fun MoreDialog(
    onDismiss: () -> Unit,
    onLBW: () -> Unit,
    onChangeBatsman: () -> Unit,
    onRetireBatsman: () -> Unit,
    onEndInnings: () -> Unit,
    isTablet: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("More Options") },
        text = {
            Column {
                Button(modifier = Modifier.fillMaxWidth(), onClick = onLBW) {
                    Text("LBW (Warning -2)", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = onChangeBatsman) {
                    Text("Change Batsman", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = onRetireBatsman) {
                    Text("Retire Batsman", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = onEndInnings) {
                    Text("End Innings", fontSize = if (isTablet) 30.sp else 20.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// WICKETS DIALOG
@Composable
fun WicketsDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    isTablet: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select WICKETS Option") },
        text = {
            Column {
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("WKB") }) {
                    Text("Bowled", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("WKCB") }) {
                    Text("Caught Behind", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("WKC") }) {
                    Text("Caught", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("WKRO") }) {
                    Text("Run Out", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("WKRONB") }) {
                    Text("Run Out NB", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("WKROW") }) {
                    Text("Run Out Wide", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("WKST") }) {
                    Text("Stumped", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("WKSTW") }) {
                    Text("Stumped Wide", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("WKHW") }) {
                    Text("Hit Wicket", fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect("WKLB") }) {
                    Text("LBW", fontSize = if (isTablet) 30.sp else 20.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// BATSMAN RUN OUT SELECTION DIALOG
@Composable
fun BatsmanRunOutDialog(
    firstBatsmanStats: BatsmanStats,
    secondBatsmanStats: BatsmanStats,
    onDismiss: () -> Unit,
    onSelect: (Boolean) -> Unit, // true for first batsman, false for second
    isTablet: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select runout batsman?") },
        text = {
            Column {
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect(true) }) {
                    Text(firstBatsmanStats.name.value, fontSize = if (isTablet) 30.sp else 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect(false) }) {
                    Text(secondBatsmanStats.name.value, fontSize = if (isTablet) 30.sp else 20.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// FIELDER SELECTION DIALOG
@Composable
fun FielderDialog(
    bowlingTeam: List<Player>,
    onDismiss: () -> Unit,
    onSelect: (Player) -> Unit,
    isTablet: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Fielder") },
        text = {
            Column {
                bowlingTeam.forEach { player ->
                    Button(modifier = Modifier.fillMaxWidth(), onClick = { onSelect(player) }) {
                        Text(player.name, fontSize = if (isTablet) 30.sp else 20.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// NEXT BATSMAN DIALOG
@Composable
fun NextBatsmanDialog(
    matchId: String,
    dbHelper: CricketDatabaseHelper,
    firstBatsmanStats: BatsmanStats,
    secondBatsmanStats: BatsmanStats,
    noOfPlayersAside: Int,
    balls: MutableList<Ball>,
    selectedWicketsOption: androidx.compose.runtime.MutableState<String>,
    currentOverBowlerStats: BowlerStats,
    firstBattingTeamStats: TeamStats,
    secondBattingTeamStats: TeamStats,
    currentBowler: androidx.compose.runtime.MutableState<String>,
    selectedFielder: androidx.compose.runtime.MutableState<String>,
    batsmanOverride: androidx.compose.runtime.MutableState<Boolean>,
    context: android.content.Context,
    onDismiss: () -> Unit,
    onComplete: () -> Unit,
    isTablet: Boolean,
    onUpdateBallHistory: (BallEvent) -> Unit
) {
    val strikerBatsman = getActiveBatsman(firstBatsmanStats, secondBatsmanStats)
    val nonStrikerBatsman = getInactiveBatsman(firstBatsmanStats, secondBatsmanStats)
    var excludeBatsman = ""

    val teamId = if (firstBatsmanStats.name.value != "") {
        dbHelper.getTeamForPlayer(matchId, firstBatsmanStats.name.value)
    } else {
        dbHelper.getTeamForPlayer(matchId, secondBatsmanStats.name.value)
    }
    val players = dbHelper.getTeamPlayers(matchId, teamId, 1)

    val battedFullyAlreadyPlayers = dbHelper.getFullyBattedAlreadyPlayers(matchId, teamId)
    val previousBattingCount = dbHelper.getBattingCount(matchId, strikerBatsman)
    if (previousBattingCount > 0) {
        excludeBatsman = strikerBatsman
    }

    val availablePlayers = players.filter { player ->
        !battedFullyAlreadyPlayers.contains(player) &&
                player != Player(nonStrikerBatsman) &&
                player != Player(excludeBatsman)
    }

    if (availablePlayers.isEmpty()) {
        onComplete()
        val batsmanOut = getActiveBatsman(firstBatsmanStats, secondBatsmanStats)
        selectedWicketsOption.value += ",$batsmanOut"
        updateStats(
            context,
            balls,
            selectedWicketsOption.value,
            currentOverBowlerStats,
            firstBatsmanStats,
            secondBatsmanStats,
            firstBattingTeamStats,
            secondBattingTeamStats
        )
        currentBowler.value = dbHelper.getCurrentBowler(matchId)
        val wicketDescription = getWicketDescription(
            selectedWicketsOption.value,
            currentBowler.value,
            selectedFielder.value
        )
        val wicketType = getWicketType(selectedWicketsOption.value)
        markBatsmanAsOutInDB(
            context,
            matchId,
            firstBatsmanStats,
            secondBatsmanStats,
            wicketDescription,
            wicketType,
            currentBowler.value,
            selectedFielder.value,
            "",
            false
        )
        if (dbHelper.getTeamWickets(matchId, dbHelper.getTeamForPlayer(matchId, batsmanOut)) < (noOfPlayersAside * 2)) {
            dbHelper.updateBattingStats(matchId, nonStrikerBatsman, "non-striker", "striker")
        }

        // Add wicket to ball history
        val activeBatsman = if (firstBatsmanStats.active.value) firstBatsmanStats.name.value else secondBatsmanStats.name.value
        onUpdateBallHistory(
            BallEvent(
                over = String.format(Locale.UK, "%.1f", currentOverBowlerStats.over.value),
                bowler = currentOverBowlerStats.name.value,
                batsman = activeBatsman,
                result = "W"
            )
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select NEXT Batsman") },
            text = {
                Column {
                    availablePlayers.forEach { player ->
                        Button(modifier = Modifier.fillMaxWidth(), onClick = {
                            onComplete()
                            val newBatsman = player.name

                            if (!batsmanOverride.value) {
                                val batsmanOut = getActiveBatsman(firstBatsmanStats, secondBatsmanStats)
                                selectedWicketsOption.value += ",$batsmanOut,$newBatsman"
                                updateStats(
                                    context,
                                    balls,
                                    selectedWicketsOption.value,
                                    currentOverBowlerStats,
                                    firstBatsmanStats,
                                    secondBatsmanStats,
                                    firstBattingTeamStats,
                                    secondBattingTeamStats
                                )

                                currentBowler.value = dbHelper.getCurrentBowler(matchId)
                                val wicketDescription = getWicketDescription(
                                    selectedWicketsOption.value,
                                    currentBowler.value,
                                    selectedFielder.value
                                )
                                val wicketType = getWicketType(selectedWicketsOption.value)

                                markBatsmanAsOutInDB(
                                    context,
                                    matchId,
                                    firstBatsmanStats,
                                    secondBatsmanStats,
                                    wicketDescription,
                                    wicketType,
                                    currentBowler.value,
                                    selectedFielder.value,
                                    newBatsman,
                                    true
                                )

                                dbHelper.addBattingStats(matchId, teamId, newBatsman, "striker")

                                // Add wicket to ball history
                                onUpdateBallHistory(
                                    BallEvent(
                                        over = String.format(Locale.UK, "%.1f", currentOverBowlerStats.over.value),
                                        bowler = currentOverBowlerStats.name.value,
                                        batsman = batsmanOut,
                                        result = "W"
                                    )
                                )
                            } else {
                                dbHelper.updateStriker(matchId, newBatsman)
                                firstBatsmanStats.name.value = newBatsman
                                batsmanOverride.value = false
                            }
                        }) {
                            Text(player.name, fontSize = if (isTablet) 30.sp else 20.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

// BOWLER CHANGE DIALOG
@Composable
fun BowlerChangeDialog(
    bowlingTeam: List<Player>,
    currentBowlerName: String,
    matchId: String,
    dbHelper: CricketDatabaseHelper,
    bowlingTeamId: Int,
    manualBowlerChange: androidx.compose.runtime.MutableState<Boolean>,
    onDismiss: () -> Unit,
    onSelect: (Player) -> Unit,
    isTablet: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Change Bowler") },
        text = {
            Column {
                bowlingTeam.forEach { player ->
                    if (currentBowlerName != player.name) {
                        val consStats = dbHelper.getConsolidatedBowlerStats(matchId, player.name)
                        Button(
                            onClick = { onSelect(player) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                player.name,
                                fontSize = if (isTablet) 26.sp else 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                consStats.over.value.toString(),
                                fontSize = if (isTablet) 26.sp else 20.sp,
                                textAlign = TextAlign.Right
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    )
}

// KEEPER CHANGE DIALOG
@Composable
fun KeeperChangeDialog(
    bowlingTeam: List<Player>,
    currentBowlerName: String,
    currentKeeperName: String,
    onDismiss: () -> Unit,
    onSelect: (Player) -> Unit,
    isTablet: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Change Keeper") },
        text = {
            Column {
                bowlingTeam.forEach { player ->
                    if ((currentBowlerName != player.name) && (currentKeeperName != player.name)) {
                        Button(
                            onClick = { onSelect(player) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                player.name,
                                fontSize = if (isTablet) 26.sp else 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    )
}