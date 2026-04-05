package com.example.cricketscoringapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.IOException
import java.io.OutputStream
import kotlin.math.abs
import kotlin.math.roundToInt
import android.util.Log

fun swapBatsmenDB(context: Context,matchId: String,batsman1: BatsmanStats, batsman2: BatsmanStats, updateFirebase: Boolean) {
    swapBatsmen(batsman1,batsman2)

    val dbHelper = CricketDatabaseHelper(context)
    if (batsman1.active.value) {
        dbHelper.updateBattingStats(matchId,batsman1.name.value,"non-striker","striker")
        dbHelper.updateBattingStats(matchId,batsman2.name.value,"striker","non-striker")
    } else {
        dbHelper.updateBattingStats(matchId,batsman1.name.value,"striker","non-striker")
        dbHelper.updateBattingStats(matchId,batsman2.name.value,"non-striker","striker")
    }

    if (updateFirebase) {
        val firebaseManager = FirebaseScoreManager.getInstance(context)
        val matchId = dbHelper.getMatchId()
        firebaseManager.swapBatsmen(matchId)
    }
}

fun swapBatsmen(batsman1: BatsmanStats, batsman2: BatsmanStats) {
    val tempActive = batsman1.active.value
    batsman1.active.value = batsman2.active.value
    batsman2.active.value = tempActive
}

fun swapBatsmen(context: Context,matchId: String,batsman1: BatsmanStats, batsman2: BatsmanStats, actionForBatsman: String) {
    if (((batsman1.name.value == actionForBatsman) && (!batsman1.active.value)) || ((batsman2.name.value == actionForBatsman) && (!batsman2.active.value))) {
        swapBatsmenDB(context,matchId,batsman1,batsman2,false)
    }
}

fun getActiveBatsman(firstBatsmanStats: BatsmanStats, secondBatsmanStats: BatsmanStats): String {
    return if (firstBatsmanStats.active.value) {
        firstBatsmanStats.name.value
    } else {
        secondBatsmanStats.name.value
    }
}

fun getInactiveBatsman(firstBatsmanStats: BatsmanStats, secondBatsmanStats: BatsmanStats): String {
    return if (firstBatsmanStats.active.value) {
        secondBatsmanStats.name.value
    } else {
        firstBatsmanStats.name.value
    }
}

// Function to update the stats
fun updateStats(context: Context,
                balls: MutableList<Ball>,
                newValue: String,
                bowlerStats: BowlerStats,
                firstBatsmanStats: BatsmanStats,
                secondBatsmanStats: BatsmanStats,
                firstTeamStats: TeamStats,
                secondTeamStats: TeamStats) {
    val dbHelper = CricketDatabaseHelper(context)
    val matchId = dbHelper.getMatchId()
    val sideWallRule = dbHelper.getSideWallRule(matchId)
    val excludedValuesFromBallsBalled = setOf("W","W+1","W+2","NB","NB+1","NB+2","NB+3","NB+4","NB+6","NBL1","NBL2","NBL3","NBB1","NBB2","NBB3","WKRONB","WKROW","WKSTW")
    val excludedValuesFromBallsFaced1 = setOf("W","W+1","W+2","WKROW","WKSTW")
    val excludedValuesFromBallsFaced2 = setOf("W","W+1","W+2")
    val activeBatsman = getActiveBatsman(firstBatsmanStats,secondBatsmanStats)

    if (newValue != "UNDO") {
        val emptyIndex = balls.indexOfFirst { it.action.isEmpty() }
        if (emptyIndex == -1) {
            balls.add(Ball(newValue,activeBatsman)) // Add new value if no empty spot is found
        } else {
            balls[emptyIndex] = Ball(newValue,activeBatsman) // Update the first empty spot
        }

        val includedBallsCount = balls.count {
            val action = it.action.split(",").firstOrNull() ?: it.action
            action !in excludedValuesFromBallsBalled
        }

        if (includedBallsCount == 7) {
            if (emptyIndex == -1) {
                balls.removeAt(balls.lastIndex) // Remove the last item if added
            } else {
                balls[emptyIndex] = Ball("",activeBatsman) // Reset the value at the empty index
            }
            return
        }

        updateBowler(matchId,false,"over",bowlerStats,activeBatsman,0.0,newValue,context)

        // Increment the bowlerOver by 0.1 only if it's a valid ball value
        var containsExcludedValue = newValue.split(",").any { it in excludedValuesFromBallsBalled }
        if (!containsExcludedValue) {
            updateBowler(matchId,false,"over",bowlerStats,activeBatsman,0.1,"",context)
            updateTeam("overs", firstTeamStats, secondTeamStats, 0.1)
        }

        containsExcludedValue = newValue.split(",").any { it in excludedValuesFromBallsFaced1 }
        if (!containsExcludedValue) {
            updateBatsman(matchId,"balls", firstBatsmanStats, secondBatsmanStats, 1,context)
        }

        doUpdateStats(context,matchId,false,newValue,1, bowlerStats, firstBatsmanStats, secondBatsmanStats, firstTeamStats, secondTeamStats)

        val shouldSwap = when(sideWallRule) {
            0, 2 -> newValue == "1" || newValue == "3"
            1 -> newValue == "1" || newValue == "2" || newValue == "NB+1" || newValue == "NB+2" || newValue == "NB+3" || newValue == "B1" || newValue == "B2" || newValue == "B3" || newValue == "LB1" || newValue == "LB2" || newValue == "LB3"
            else -> false
        }

        if (shouldSwap) {
            swapBatsmenDB(context, matchId, firstBatsmanStats, secondBatsmanStats,false)
        }

        if (balls.size == 6 && balls.take(6).all { it.action == "0" || it.action.contains("WK") }) {
            updateBowler(matchId,false,"maiden",bowlerStats,activeBatsman,1.00,"",context)
        }
    } else {
        //Handle the UNDO option
        val lastNonEmptyIndex = balls.indexOfLast { it.action.isNotEmpty() }
        if (lastNonEmptyIndex == -1) return

        val lastBall = balls[lastNonEmptyIndex].action

        if (lastBall.contains("WK")) {
            Toast.makeText(context, "Wicket UNDO is not supported!", Toast.LENGTH_SHORT).show()
            return
        }
        // Decrement the bowlerOver by 0.1 only if the last ball was a valid ball value
        var containsExcludedValue = lastBall.split(",").any { it in excludedValuesFromBallsBalled }
        if (!containsExcludedValue) {
            when(sideWallRule) {
                0 -> {
                    if ((lastBall == "1") || (lastBall == "3")) {
                        swapBatsmen(
                            context,
                            matchId,
                            firstBatsmanStats,
                            secondBatsmanStats,
                            balls[lastNonEmptyIndex].batsman
                        )
                    }
                }
                1 -> {
                    if ((lastBall == "1") || (lastBall == "2")) {
                        swapBatsmen(
                            context,
                            matchId,
                            firstBatsmanStats,
                            secondBatsmanStats,
                            balls[lastNonEmptyIndex].batsman
                        )
                    }
                }
                2 -> {
                    if ((lastBall == "1") || (lastBall == "3")) {
                        swapBatsmen(
                            context,
                            matchId,
                            firstBatsmanStats,
                            secondBatsmanStats,
                            balls[lastNonEmptyIndex].batsman
                        )
                    }
                }
            }
            updateBowler(matchId,true,"over",bowlerStats,activeBatsman,-0.1,"",context)
            updateTeam("overs", firstTeamStats, secondTeamStats, -0.1)
        }

        doUpdateStats(context,matchId,true,lastBall,-1, bowlerStats, firstBatsmanStats, secondBatsmanStats, firstTeamStats, secondTeamStats)

        containsExcludedValue = lastBall.split(",").any { it in excludedValuesFromBallsFaced2 }
        if (!containsExcludedValue) {
            if (lastBall.contains("WK")) {
                //Restore out batsman
                val batsmanOut = balls[lastNonEmptyIndex].batsman

                val batsmanOutFromDB = dbHelper.getBatsmanStats(matchId,batsmanOut)
                dbHelper.deleteBatsman(matchId,activeBatsman)
                dbHelper.updateBattingStats(matchId,batsmanOutFromDB.name.value,"out","striker")

                containsExcludedValue = lastBall.split(",").any { it in excludedValuesFromBallsFaced1 }
                if (!containsExcludedValue) {
                    batsmanOutFromDB.balls.value -= 1
                }

                dbHelper.updateBattingStats(matchId,"striker",batsmanOutFromDB,"","","","")

                if (firstBatsmanStats.name.value == activeBatsman) {
                    firstBatsmanStats.name.value = batsmanOut
                    firstBatsmanStats.runs.value = batsmanOutFromDB.runs.value
                    firstBatsmanStats.balls.value = batsmanOutFromDB.balls.value
                    firstBatsmanStats.fours.value = batsmanOutFromDB.fours.value
                    firstBatsmanStats.sixes.value = batsmanOutFromDB.sixes.value
                } else if (secondBatsmanStats.name.value == activeBatsman) {
                    secondBatsmanStats.name.value = batsmanOut
                    secondBatsmanStats.runs.value = batsmanOutFromDB.runs.value
                    secondBatsmanStats.balls.value = batsmanOutFromDB.balls.value
                    secondBatsmanStats.fours.value = batsmanOutFromDB.fours.value
                    secondBatsmanStats.sixes.value = batsmanOutFromDB.sixes.value
                }
            } else {
                updateBatsman(matchId,"balls", firstBatsmanStats, secondBatsmanStats, -1,context)
            }
        }

        // Check if the first 5 balls were "0" and last index is 5
        if (lastNonEmptyIndex == 5 && balls.take(5).all { it.action == "0" }) {
            updateBowler(matchId,false,"maiden",bowlerStats,activeBatsman,-1.00,"",context)
        }

        bowlerStats.overrecord.value = removePipeDelimitedValue(lastNonEmptyIndex,bowlerStats.overrecord.value)
        dbHelper.updateBowlingStats(matchId,bowlerStats)

        balls[lastNonEmptyIndex] = Ball("","")
    }

    val battingTeam  = if (firstTeamStats.active.value) firstTeamStats.name.value else secondTeamStats.name.value
    val bowlingTeam  = if (firstTeamStats.active.value) secondTeamStats.name.value else firstTeamStats.name.value

    val battingStats = if (firstTeamStats.active.value) firstTeamStats else secondTeamStats
    val fieldingTeam = if (firstTeamStats.active.value) secondTeamStats else firstTeamStats

    //val strikingBatsmanStat = if (firstBatsmanStats.active.value) firstBatsmanStats else secondBatsmanStats
    //val nonStrikingBatsmanStat = if (firstBatsmanStats.active.value) secondBatsmanStats else firstBatsmanStats
    val strikingBatsmanStat = dbHelper.getBatsmanByStatus(matchId, "striker")
    val nonStrikingBatsmanStat = dbHelper.getBatsmanByStatus(matchId, "non-striker")

    val noOfOversAside = dbHelper.getNoOfOversAside(matchId).toDouble()
    val (runsToWinLocal, winningCaptain) = calcRunsToWin(firstTeamStats, secondTeamStats, noOfOversAside)

    val consolidatedBowler = dbHelper.getConsolidatedBowlerStats(matchId, bowlerStats.name.value)

    val scoreUpdate = LiveScoreUpdate(
        matchId          = matchId,
        battingTeam      = "Team $battingTeam",
        bowlingTeam      = "Team $bowlingTeam",
        innings1Runs     = battingStats.inningScore.value,
        innings1Wickets  = battingStats.inningWickets.value,
        innings1Overs    = "%.1f".format(battingStats.overs.value),
        innings2Runs     = fieldingTeam.inningScore.value,
        innings2Wickets  = fieldingTeam.inningWickets.value,
        innings2Overs    = "%.1f".format(fieldingTeam.overs.value),
        currentOver = balls.filter { it.action.isNotEmpty() }
            .joinToString(",") { if (it.action.startsWith("WK")) "OUT" else it.action },
        runsToWin        = runsToWinLocal,
        // Striker
        strikerName      = strikingBatsmanStat.name.value,
        strikerRuns      = strikingBatsmanStat.runs.value,
        strikerBalls     = strikingBatsmanStat.balls.value,
        striker4s        = strikingBatsmanStat.fours.value,
        striker6s        = strikingBatsmanStat.sixes.value,
        // Non-Striker
        nonStrikerName   = nonStrikingBatsmanStat.name.value,
        nonStrikerRuns   = nonStrikingBatsmanStat.runs.value,
        nonStrikerBalls  = nonStrikingBatsmanStat.balls.value,
        nonStriker4s     = nonStrikingBatsmanStat.fours.value,
        nonStriker6s     = nonStrikingBatsmanStat.sixes.value,
        // Bowler
        bowlerName       = consolidatedBowler.name.value,
        bowlerOvers      = "%.1f".format(consolidatedBowler.over.value),
        bowlerRuns       = consolidatedBowler.runs.value,
        bowlerWickets    = consolidatedBowler.wickets.value,
        // Partnership TODO
        //partnershipRuns  = firstBatsmanStats.runs.value + secondBatsmanStats.runs.value,
        //partnershipBalls = firstBatsmanStats.balls.value + secondBatsmanStats.balls.value,
        partnershipRuns  = 0,
        partnershipBalls = 0,
        status           = "live"
    )

    try {
        val firebaseManager = FirebaseScoreManager.getInstance(context)
        Log.d("Firebase", "Pushing score update - Match: $matchId, Runs: ${scoreUpdate.innings1Runs}, Wickets: ${scoreUpdate.innings1Wickets}")
        firebaseManager.pushLiveScore(matchId, scoreUpdate,context)

    } catch (e: Exception) {
        Log.e("FirebaseError", "Firebase failed: ${e.message}", e)
        e.printStackTrace()
        Toast.makeText(context, e.message, Toast.LENGTH_LONG)
            .show()
    }
}

fun calcRunsToWin(firstTeamStats: TeamStats, secondTeamStats: TeamStats, noOfOversAside: Double) : Pair<String,String> {
    val ballsRemaining: Int
    val oversRemaining: Double
    var runsToWin = 0
    val runsToWinTxt: String
    var winningTeam = ""

    if (firstTeamStats.active.value) {
        // First team is batting (second innings)
        if (secondTeamStats.inningScore.value != 0) {
            runsToWin = secondTeamStats.inningScore.value - firstTeamStats.inningScore.value + 1

            // Only declare winner if they've already won (negative runsToWin means they passed the target)
            if (runsToWin <= 0) {
                winningTeam = firstTeamStats.name.value
            }
        }
        ballsRemaining = calculateBalls(noOfOversAside) - calculateBalls(firstTeamStats.overs.value)
        oversRemaining = calculateOversRemaining(ballsRemaining)
    } else {
        // Second team is batting (second innings)
        if (firstTeamStats.inningScore.value != 0) {
            runsToWin = firstTeamStats.inningScore.value - secondTeamStats.inningScore.value + 1

            // Only declare winner if they've already won (negative runsToWin means they passed the target)
            if (runsToWin <= 0) {
                winningTeam = secondTeamStats.name.value
            }
        }
        ballsRemaining = calculateBalls(noOfOversAside) - calculateBalls(secondTeamStats.overs.value)
        oversRemaining = calculateOversRemaining(ballsRemaining)
    }

    // REMOVED the problematic section that was setting winningTeam based on current scores
    // Only set winningTeam if all overs are complete and no winner has been determined yet
    if (winningTeam.isEmpty() && ballsRemaining == 0) {
        winningTeam = when {
            firstTeamStats.inningScore.value > secondTeamStats.inningScore.value ->
                firstTeamStats.name.value
            secondTeamStats.inningScore.value > firstTeamStats.inningScore.value ->
                secondTeamStats.name.value
            else -> "Draw" // Match tied
        }
    }

    val runsToWinAbs = abs(runsToWin)
    runsToWinTxt = if (winningTeam.isNotEmpty() && winningTeam != "Draw") {
        "Team $winningTeam is winning by $runsToWinAbs runs!"
    } else if (winningTeam == "Draw") {
        "Match is a draw!"
    } else {
        // Check if second innings has started (second team has bowled at least one ball)
        if (!firstTeamStats.active.value) {
            // Second innings - second team is batting
            "$runsToWin runs to win from $ballsRemaining balls!"
        } else {
            // First innings - first team is batting
            "$oversRemaining overs remaining!"
        }
    }

    return Pair(runsToWinTxt, winningTeam)
}

fun updateBowler(
    matchId: String,
    undo: Boolean,
    statType: String,
    bowlerStats: BowlerStats,
    activeBatsman: String,
    updateValue: Double,
    ballAction: String,
    context: Context)
{
    val dbHelper = CricketDatabaseHelper(context)

    when(statType){
        "over" -> {
            bowlerStats.over.value += updateValue

            if (bowlerStats.over.value % 1 == 0.6) {
                bowlerStats.over.value = 1.0
            }
        }
        "maiden" -> {
            bowlerStats.maiden.value += updateValue.toInt()
        }
        "runs" -> {
            bowlerStats.runs.value += updateValue.toInt()
        }
        "wickets" -> {
            bowlerStats.wickets.value += updateValue.toInt()
        }
        "noballs" -> {
            bowlerStats.noballs.value += updateValue.toInt()
        }
        "wides" -> {
            bowlerStats.wides.value += updateValue.toInt()
        }
        "byes" -> {
            bowlerStats.byes.value += updateValue.toInt()
        }
        "legbyes" -> {
            bowlerStats.legbyes.value += updateValue.toInt()
        }
        "fours" -> {
            bowlerStats.fours.value += updateValue.toInt()
        }
        "sixes" -> {
            bowlerStats.sixes.value += updateValue.toInt()
        }
        "dotballs" -> {
            bowlerStats.dotballs.value += updateValue.toInt()
        }
    }
    if (!undo) {
        if (ballAction != "")  {
            val ballActionString = if (!ballAction.contains("WK")) {
                "$ballAction,$activeBatsman"
            } else {
                ballAction
            }
            if (bowlerStats.overrecord.value == "") {
                bowlerStats.overrecord.value = ballActionString
            } else {
                bowlerStats.overrecord.value += "|$ballActionString"
            }
        }
    }
    dbHelper.updateBowlingStats(matchId,bowlerStats)
}

fun updateBatsman(
    matchId: String,
    statType: String,
    firstBatsmanStats: BatsmanStats,
    secondBatsmanStats: BatsmanStats,
    updateValue: Int,
    context: Context)
{
    val dbHelper = CricketDatabaseHelper(context)

    when(statType){
        "runs" -> {
            if (firstBatsmanStats.active.value) {
                firstBatsmanStats.runs.value += updateValue
            } else {
                secondBatsmanStats.runs.value += updateValue
            }
        }
        "balls" -> {
            if (firstBatsmanStats.active.value) {
                firstBatsmanStats.balls.value += updateValue
            } else {
                secondBatsmanStats.balls.value += updateValue
            }
        }
        "fours" -> {
            if (firstBatsmanStats.active.value) {
                firstBatsmanStats.fours.value += updateValue
            } else {
                secondBatsmanStats.fours.value += updateValue
            }
        }
        "sixes" -> {
            if (firstBatsmanStats.active.value) {
                firstBatsmanStats.sixes.value += updateValue
            } else {
                secondBatsmanStats.sixes.value += updateValue
            }
        }
        "dotballs" -> {
            if (firstBatsmanStats.active.value) {
                firstBatsmanStats.dotballs.value += updateValue
            } else {
                secondBatsmanStats.dotballs.value += updateValue
            }
        }
    }
    if (firstBatsmanStats.active.value) {
        dbHelper.updateBattingStats(matchId, "striker", firstBatsmanStats, "","","","")
    } else {
        dbHelper.updateBattingStats(matchId, "striker", secondBatsmanStats, "","","","")
    }
}

fun updateTeam(
    statType: String,
    firstTeamStats: TeamStats,
    secondTeamStats: TeamStats,
    updateValue: Double)
{
    when(statType){
        "overs" -> {
            if (firstTeamStats.active.value) {
                firstTeamStats.overs.value += updateValue
            } else {
                secondTeamStats.overs.value += updateValue
            }
        }
        "inningScore" -> {
            if (firstTeamStats.active.value) {
                firstTeamStats.inningScore.value += updateValue.toInt()
            } else {
                secondTeamStats.inningScore.value += updateValue.toInt()
            }
        }
        "inningWickets" -> {
            if (firstTeamStats.active.value) {
                firstTeamStats.inningWickets.value += updateValue.toInt()
            } else {
                secondTeamStats.inningWickets.value += updateValue.toInt()
            }
        }
    }
}

fun calculateBalls(overs: Double): Int {
    // Get the whole number part of the overs
    val fullOvers = overs.toInt()
    // Get the fractional part of the overs, representing the extra balls
    val extraBalls = ((overs - fullOvers) * 10).roundToInt()

    // Total balls = (6 balls per over * number of full overs) + extra balls
    return (fullOvers * 6) + extraBalls
}

fun calculateOversRemaining(ballsRemaining: Int): Double {
    val overs = ballsRemaining / 6
    val balls = ballsRemaining % 6
    return overs + balls / 10.0
}

fun removePipeDelimitedValue(noOfBalls: Int, pipeDelimitedValue: String): String {
    // Split the string into a list of values
    val values = pipeDelimitedValue.split("|").toMutableList()

    // If the list has more values than noOfBalls, remove the excess
    if (values.size > noOfBalls) {
        values.subList(noOfBalls, values.size).clear() // Remove all values after the noOfBalls index
    }

    // Join the remaining values back into a pipeDelimitedValue string
    return values.joinToString("|")
}

fun endOfOverReached(balls: MutableList<Ball>): Boolean {
    val excludedValuesFromBallsBalled = setOf("W","W+1","W+2","NB","NB+1","NB+2","NB+3","NB+4","NB+6","NBL1","NBL2","NBL3","NBB1","NBB2","NBB3","WKRONB","WKROW","WKSTW")
    val noOfValidBallsBowled = balls.filter {
        val action = it.action.split(",").firstOrNull() ?: it.action
        action !in excludedValuesFromBallsBalled
    }.size

    return noOfValidBallsBowled >= 6
}

fun checkAndPlayDuckSound(firstBatsmanStats: BatsmanStats,secondBatsmanStats: BatsmanStats,context: Context) {
    if (((firstBatsmanStats.active.value) && (firstBatsmanStats.runs.value == 0)) ||
        ((secondBatsmanStats.active.value) && (secondBatsmanStats.runs.value == 0))
    ) {
        playDuckSound(context)
    }
}

fun playDuckSound(context: Context) {
    var mediaPlayer = MediaPlayer.create(context, R.raw.duckonrepeat)

    if (mediaPlayer.isPlaying) {
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer.create(context, R.raw.duckonrepeat)
    }
    mediaPlayer.start()
}

fun setCurrentBowlerAndKeeper(bowlerStats: BowlerStats, bowlerName: String, keeperName: String) {
    bowlerStats.name.value = bowlerName
    bowlerStats.keepername.value = keeperName
    bowlerStats.over.value = 0.0
    bowlerStats.maiden.value = 0
    bowlerStats.runs.value = 0
    bowlerStats.wickets.value = 0
    bowlerStats.noballs.value = 0
    bowlerStats.wides.value = 0
    bowlerStats.fours.value = 0
    bowlerStats.sixes.value = 0
    bowlerStats.dotballs.value = 0
    bowlerStats.byes.value = 0
    bowlerStats.legbyes.value = 0
}

fun setCurrentKeeper(bowlerStats: BowlerStats, keeperName: String) {
    bowlerStats.keepername.value = keeperName
}

fun getWicketDescription(wicketType: String, bowler: String, fielder: String) : String {
    val processedWicketType = wicketType.substringBefore(",")
    when (processedWicketType) {
        "WKB" -> return "b $bowler"
        in listOf("WKC", "WKCB") -> {
            return if (bowler == fielder)
                "c&b $bowler"
            else
                "c $fielder b $bowler"
        }
        in listOf("WKRO", "WKRONB","WKROW") -> return "run out $fielder"
        in listOf("WKST", "WKSTW") -> return "st $fielder b $bowler"
        "WKHW" -> return "hit wicket"
        "WKLB" -> return "lbw b $bowler"
    }
    return ""
}

fun getWicketType(wicketType: String) : String {
    val processedWicketType = wicketType.substringBefore(",")
    when (processedWicketType) {
        "WKB" -> return "bowled"
        "WKC" -> return "caught"
        "WKCB" -> return "caught behind"
        in listOf("WKRO", "WKRONB","WKROW") -> return "run out"
        in listOf("WKST", "WKSTW") -> return "stumped"
        "WKHW" -> return "hit wicket"
        "WKLB" -> return "lbw"
    }
    return ""
}

fun doUpdateStats(context: Context,matchId: String,undo:Boolean, newValue: String, multiplier: Int, bowlerStats: BowlerStats, firstBatsmanStats: BatsmanStats, secondBatsmanStats: BatsmanStats, firstTeamStats: TeamStats, secondTeamStats: TeamStats) {
    val activeBatsman = getActiveBatsman(firstBatsmanStats,secondBatsmanStats)
    if (newValue.contains("WK")) {
        if (newValue.contains("WKRO")) {
            if (newValue.contains("WKRONB")) {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBowler(matchId,undo,"noballs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBatsman(matchId,"dotballs", firstBatsmanStats, secondBatsmanStats, 1 * multiplier,context)
            } else if (newValue.contains("WKROW")) {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBowler(matchId,undo,"wides",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
            } else {
                updateBatsman(matchId,"dotballs", firstBatsmanStats, secondBatsmanStats, 1 * multiplier,context)
                updateBowler(matchId,undo,"dotballs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
            }
        } else {
            updateBowler(matchId,undo,"wickets",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
            updateBatsman(matchId,"dotballs", firstBatsmanStats, secondBatsmanStats, 1 * multiplier,context)
            updateBowler(matchId,undo,"dotballs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
        }
        updateTeam("inningWickets", firstTeamStats, secondTeamStats, 1.0 * multiplier)
        updateTeam("inningScore", firstTeamStats, secondTeamStats, -3.0 * multiplier)
    } else {
        when (newValue) {
            "0" -> {
                updateBowler(matchId,undo,"dotballs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBatsman(matchId,"dotballs", firstBatsmanStats, secondBatsmanStats, 1 * multiplier,context)
            }

            "1" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBatsman(matchId,"runs", firstBatsmanStats, secondBatsmanStats, 1 * multiplier,context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            }

            "2" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,2.00 * multiplier,"",context)
                updateBatsman(matchId,"runs", firstBatsmanStats, secondBatsmanStats, 2 * multiplier,context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
            }

            "3" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,3.00 * multiplier,"",context)
                updateBatsman(matchId,"runs", firstBatsmanStats, secondBatsmanStats, 3 * multiplier,context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
            }

            "4" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,4.00 * multiplier,"",context)
                updateBowler(matchId,undo,"fours",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBatsman(matchId,"runs", firstBatsmanStats, secondBatsmanStats, 4 * multiplier,context)
                updateBatsman(matchId,"fours", firstBatsmanStats, secondBatsmanStats, 1 * multiplier,context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 4.0 * multiplier)
            }

            "6" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,6.00 * multiplier,"",context)
                updateBowler(matchId,undo,"sixes",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBatsman(matchId,"runs", firstBatsmanStats, secondBatsmanStats, 6 * multiplier,context)
                updateBatsman(matchId,"sixes", firstBatsmanStats, secondBatsmanStats, 1 * multiplier,context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 6.0 * multiplier)
            }

            "W" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBowler(matchId,undo,"wides",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            }

            "W+1" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,2.00 * multiplier,"",context)
                updateBowler(matchId,undo,"wides",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBatsman(matchId,"runs", firstBatsmanStats, secondBatsmanStats, 1 * multiplier,context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
            }

            "W+2" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,3.00 * multiplier,"",context)
                updateBowler(matchId,undo,"wides",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBatsman(matchId,"runs", firstBatsmanStats, secondBatsmanStats, 2 * multiplier,context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
            }

            "NB" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBowler(matchId,undo,"noballs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBatsman(matchId,"dotballs", firstBatsmanStats, secondBatsmanStats, 1 * multiplier,context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            }

            "NB+1" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,2.00 * multiplier,"",context)
                updateBowler(matchId,undo,"noballs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBatsman(matchId,"runs", firstBatsmanStats, secondBatsmanStats, 1 * multiplier,context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
            }

            "NB+2" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,3.00 * multiplier,"",context)
                updateBowler(matchId,undo,"noballs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBatsman(matchId,"runs", firstBatsmanStats, secondBatsmanStats, 2 * multiplier,context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
            }

            "NB+3" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,4.00 * multiplier,"",context)
                updateBowler(matchId,undo,"noballs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBatsman(matchId,"runs", firstBatsmanStats, secondBatsmanStats, 3 * multiplier,context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 4.0 * multiplier)
            }

            "NB+4" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,5.00 * multiplier,"",context)
                updateBowler(matchId,undo,"noballs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBowler(matchId,undo,"fours",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBatsman(matchId,"runs", firstBatsmanStats, secondBatsmanStats, 4 * multiplier,context)
                updateBatsman(matchId,"fours", firstBatsmanStats, secondBatsmanStats, 1 * multiplier,context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 5.0 * multiplier)
            }

            "NB+6" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,7.00 * multiplier,"",context)
                updateBowler(matchId,undo,"noballs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBowler(matchId,undo,"sixes",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBatsman(matchId,"runs", firstBatsmanStats, secondBatsmanStats, 6 * multiplier,context)
                updateBatsman(matchId,"sixes", firstBatsmanStats, secondBatsmanStats, 1 * multiplier,context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 7.0 * multiplier)
            }

            "NBL1" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBowler(matchId,undo,"noballs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBowler(matchId,undo,"legbyes",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
            }

            "NBL2" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,.00 * multiplier,"",context)
                updateBowler(matchId,undo,"noballs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBowler(matchId,undo,"legbyes",bowlerStats,activeBatsman,2.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
            }

            "NBL3" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBowler(matchId,undo,"noballs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBowler(matchId,undo,"legbyes",bowlerStats,activeBatsman,3.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 4.0 * multiplier)
            }

            "NBB1" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBowler(matchId,undo,"byes",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
            }

            "NBB2" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBowler(matchId,undo,"byes",bowlerStats,activeBatsman,2.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
            }

            "NBB3" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBowler(matchId,undo,"byes",bowlerStats,activeBatsman,3.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 4.0 * multiplier)
            }

            "B1" -> {
                updateBowler(matchId,undo,"byes",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            }

            "B2" -> {
                updateBowler(matchId,undo,"byes",bowlerStats,activeBatsman,2.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
            }

            "B3" -> {
                updateBowler(matchId,undo,"byes",bowlerStats,activeBatsman,3.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
            }

            "LB1" -> {
                updateBowler(matchId,undo,"legbyes",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            }

            "LB2" -> {
                updateBowler(matchId,undo,"legbyes",bowlerStats,activeBatsman,2.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
            }

            "LB3" -> {
                updateBowler(matchId,undo,"legbyes",bowlerStats,activeBatsman,3.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
            }

            //Warning only. WKLB is for the wicket.
            "LBW" -> {
                updateBowler(matchId,undo,"dotballs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                //updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,-2.00 * multiplier,"",context)
                updateBatsman(matchId,"dotballs", firstBatsmanStats, secondBatsmanStats, 1 * multiplier,context)
                updateBatsman(matchId,"runs", firstBatsmanStats, secondBatsmanStats, -2 * multiplier,context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, -2.0 * multiplier)
            }
        }
    }
}

fun handleLastBatsmen(context: Context, matchId: String, firstBatsman: BatsmanStats, secondBatsman: BatsmanStats) {
    val dbHelper = CricketDatabaseHelper(context)

    if (firstBatsman.name.value != "") {
        if (firstBatsman.active.value) {
            dbHelper.updateBattingStats(
                matchId,
                firstBatsman.name.value,
                "striker",
                "not out"
            )
        } else {
            dbHelper.updateBattingStats(
                matchId,
                firstBatsman.name.value,
                "non-striker",
                "not out"
            )
        }
    }
    if (secondBatsman.name.value != "") {
        if (secondBatsman.active.value) {
            dbHelper.updateBattingStats(
                matchId,
                secondBatsman.name.value,
                "striker",
                "not out"
            )
        } else {
            dbHelper.updateBattingStats(
                matchId,
                secondBatsman.name.value,
                "non-striker",
                "not out"
            )
        }
    }
}

fun markBatsmanAsOutInDB(context: Context,matchId: String,firstBatsmanStats: BatsmanStats,secondBatsmanStats: BatsmanStats, wicketDescription: String, wicketType: String,wicketBowler: String, wicketFielder: String, newBatsman: String, newActive: Boolean) {
    val dbHelper = CricketDatabaseHelper(context)

    if (firstBatsmanStats.active.value) {
        //save out batsman to database
        dbHelper.updateBattingStats(matchId,"out",firstBatsmanStats,wicketDescription,wicketType,wicketBowler, wicketFielder)

        firstBatsmanStats.name.value = newBatsman
        firstBatsmanStats.active.value = newActive
        firstBatsmanStats.runs.value = 0
        firstBatsmanStats.balls.value = 0
        firstBatsmanStats.fours.value = 0
        firstBatsmanStats.sixes.value = 0
        firstBatsmanStats.dotballs.value = 0
    } else {
        //save out batsman to database
        dbHelper.updateBattingStats(matchId,"out",secondBatsmanStats,wicketDescription,wicketType,wicketBowler, wicketFielder)

        secondBatsmanStats.name.value = newBatsman
        secondBatsmanStats.active.value = newActive
        secondBatsmanStats.runs.value = 0
        secondBatsmanStats.balls.value = 0
        secondBatsmanStats.fours.value = 0
        secondBatsmanStats.sixes.value = 0
        secondBatsmanStats.dotballs.value = 0
    }
}

fun handleEndOfMatch(context: Context, matchId: String, firstBatsmanStats: BatsmanStats, secondBatsmanStats: BatsmanStats, runsToWin:String, winningCaptain:String) {
    val dbHelper = CricketDatabaseHelper(context)
    dbHelper.updateBowlingStats(matchId,"bowled")
    handleLastBatsmen(context,matchId,firstBatsmanStats,secondBatsmanStats)
    dbHelper.updateMatchIsFinished(matchId, winningCaptain)

    try {
        val firebaseManager = FirebaseScoreManager.getInstance(context)
        firebaseManager.pushMatchComplete(matchId)

    } catch (e: Exception) {
        Log.e("FirebaseError", "Firebase failed: ${e.message}", e)
        e.printStackTrace()
        Toast.makeText(context, e.message, Toast.LENGTH_LONG)
            .show()
    }

    Toast.makeText(context, "End of Match (${runsToWin})!", Toast.LENGTH_LONG)
        .show()
}

fun getMatchDataToUpload(context: Context, matchId: String): Pair<List<List<Any>>, Int> {
    // Define a list of players using the Player data class
    val dbHelper = CricketDatabaseHelper(context)
    val matchStats = dbHelper.getMatchStats(matchId)

    val uploadData = matchStats.map { matchStatsForPlayer ->
            UploadRow(
                matchStatsForPlayer.player,
                matchStatsForPlayer.captain,
                matchStatsForPlayer.catches,
                matchStatsForPlayer.stumping,
                matchStatsForPlayer.runOuts,
                matchStatsForPlayer.firstInningsRunsScored,
                matchStatsForPlayer.firstInningsBallsFaced,
                matchStatsForPlayer.firstInningsFours,
                matchStatsForPlayer.firstInningsSixes,
                matchStatsForPlayer.firstInningDotBalls,
                matchStatsForPlayer.firstInningBattingStatus,
                matchStatsForPlayer.firstInningHowOut,
                matchStatsForPlayer.firstInningBowler,
                matchStatsForPlayer.firstInningCaughtBy,
                matchStatsForPlayer.firstInningRunOutBy,
                matchStatsForPlayer.secondInningsRunsScored,
                matchStatsForPlayer.secondInningsBallsFaced,
                matchStatsForPlayer.secondInningsFours,
                matchStatsForPlayer.secondInningsSixes,
                matchStatsForPlayer.secondInningDotBalls,
                matchStatsForPlayer.secondInningBattingStatus,
                matchStatsForPlayer.secondInningHowOut,
                matchStatsForPlayer.secondInningBowler,
                matchStatsForPlayer.secondInningCaughtBy,
                matchStatsForPlayer.secondInningRunOutBy,
                matchStatsForPlayer.oversBowled,
                matchStatsForPlayer.runsConceded,
                matchStatsForPlayer.wickets,
                matchStatsForPlayer.maiden,
                matchStatsForPlayer.sixes,
                matchStatsForPlayer.fours,
                matchStatsForPlayer.dotBalls,
                matchStatsForPlayer.wides,
                matchStatsForPlayer.noBalls,
                matchStatsForPlayer.winLossTie
                )
    }

    // Map the player data to the format required for Google Sheets
    val transformedData =  uploadData.map { uploadRow ->
        listOf<Any>(
            uploadRow.player,                       // Column A
            uploadRow.captain,                      // Column B
            "",//null,                              // Column C
            "",//null,                              // Column D
            "",//null,                              // Column E
            "",//null,                              // Column F
            uploadRow.catches,                      // Column G
            uploadRow.stumping,                     // Column H
            uploadRow.runOuts,                      // Column I
            uploadRow.firstInningsRunsScored,       // Column J
            uploadRow.firstInningsBallsFaced,       // Column K
            uploadRow.firstInningsFours,            // Column L
            uploadRow.firstInningsSixes,            // Column M
            uploadRow.firstInningDotBalls,          // Column N
            uploadRow.firstInningBattingStatus,     // Column O
            uploadRow.firstInningHowOut,            // Column P
            uploadRow.firstInningBowler,            // Column Q
            uploadRow.firstInningCaughtBy,          // Column R
            uploadRow.firstInningRunOutBy,          // Column S
            uploadRow.secondInningsRunsScored,      // Column T
            uploadRow.secondInningsBallsFaced,      // Column U
            uploadRow.secondInningsFours,           // Column V
            uploadRow.secondInningsSixes,           // Column W
            uploadRow.secondInningDotBalls,         // Column X
            uploadRow.secondInningBattingStatus,    // Column Y
            uploadRow.secondInningHowOut,           // Column Z
            uploadRow.secondInningBowler,           // Column AA
            uploadRow.secondInningCaughtBy,         // Column AB
            uploadRow.secondInningRunOutBy,         // Column AC
            uploadRow.oversBowled,                  // Column AD
            uploadRow.runsConceded,                 // Column AE
            uploadRow.wickets,                      // Column AF
            uploadRow.maiden,                       // Column AG
            uploadRow.sixes,                        // Column AH
            uploadRow.fours,                        // Column AI
            uploadRow.dotBalls,                     // Column AJ
            uploadRow.wides,                        // Column AK
            uploadRow.noBalls,                      // Column AL
            uploadRow.winLossTie                    // Column AM
        )
    }.toMutableList()

    // Add a blank separator row
    transformedData.add(List(39) { "" })
    transformedData.add(listOf<Any>(
        "Over",             // Column A
        "Bowler",           // Column B
        "Batsman",          // Column C
        "Result",           // Column D
        "Result Text",      // Column E
        "Is Over Summary",  // Column F
        "Over Runs",        // Column G
        "Over Extras",      // Column H
        "Total Score"       // Column I
    ))

    // Fetch and append ball-by-ball data for Team 1
    val ballByBallDataTeam1 = dbHelper.getBallByBallHistory(matchId, 2)
    ballByBallDataTeam1.forEach { ballEvent ->
        transformedData.add(listOf<Any>(
            ballEvent.over,             // Column A
            ballEvent.bowler,           // Column B
            ballEvent.batsman,          // Column C
            if (ballEvent.result.startsWith("WK")) "WICKET" else ballEvent.result,           // Column D
            ballEvent.resultText.replace("•", "0"),  // Replace dot ball symbol with "0"
            ballEvent.isOverSummary,    // Column F
            ballEvent.overRuns,         // Column G
            ballEvent.overExtras,       // Column H
            ballEvent.totalScore        // Column I
        ))
    }

    // Add a blank separator row between teams
    transformedData.add(List(39) { "" })
    transformedData.add(listOf<Any>(
        "Over",             // Column A
        "Bowler",           // Column B
        "Batsman",          // Column C
        "Result",           // Column D
        "Result Text",      // Column E
        "Is Over Summary",  // Column F
        "Over Runs",        // Column G
        "Over Extras",      // Column H
        "Total Score"       // Column I
    ))

    // Fetch and append ball-by-ball data for Team 2
    val ballByBallDataTeam2 = dbHelper.getBallByBallHistory(matchId, 1)
    ballByBallDataTeam2.forEach { ballEvent ->
        transformedData.add(listOf<Any>(
            ballEvent.over,             // Column A
            ballEvent.bowler,           // Column B
            ballEvent.batsman,          // Column C
            if (ballEvent.result.startsWith("WK")) "WICKET" else ballEvent.result,           // Column D
            ballEvent.resultText.replace("•", "0"),  // Replace dot ball symbol with "0"
            ballEvent.isOverSummary,    // Column F
            ballEvent.overRuns,         // Column G
            ballEvent.overExtras,       // Column H
            ballEvent.totalScore        // Column I
        ))
    }

    val matchDataSize = matchStats.size

    return Pair(transformedData,matchDataSize)
}

fun OutputStream.writeCsv(listOfData: List<List<Any>>) {
    val writer = bufferedWriter()

    // Example header — update to match your actual columns
    writer.write(
        listOf(
            "Player", "Captain", "", "", "", "", "Catches", "Stumping", "Run Outs",
            "1st Innings Runs", "1st Innings Balls", "1st Innings Fours", "1st Innings Sixes",
            "1st Innings Dot Balls", "1st Innings Batting Status", "1st Innings How Out",
            "1st Innings Bowler", "1st Innings Caught By", "1st Innings Run Out By",
            "2nd Innings Runs", "2nd Innings Balls", "2nd Innings Fours", "2nd Innings Sixes",
            "2nd Innings Dot Balls", "2nd Innings Batting Status", "2nd Innings How Out",
            "2nd Innings Bowler", "2nd Innings Caught By", "2nd Innings Run Out By",
            "Overs", "Runs Conceded", "Wickets", "Maidens", "Sixes Bowled",
            "Fours Bowled", "Dot Balls Bowled", "Wides", "No Balls", "Win Loss Tie"
        ).joinToString(",")
    )
    writer.newLine()

    // Write each row
    listOfData.forEach { row ->
        val csvRow = row.joinToString(",") { field ->
            // Escape double quotes in fields
            val str = field.toString().replace("\"", "\"\"")
            "\"$str\""
        }
        writer.write(csvRow)
        writer.newLine()
    }

    writer.flush()
    writer.close()
}

fun saveAndShareCsv(context: Context, fileName: String, listOfData: List<List<Any>>) {
    val resolver = context.contentResolver

    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }

    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

    if (uri != null) {
        resolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.writeCsv(listOfData)
        }

        // Create share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // so other apps can read it
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share CSV via"))
    } else {
        throw IOException("Failed to create file in Downloads")
    }
}

