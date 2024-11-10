package com.example.cricketscoringapp

import android.content.Context
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.runtime.MutableState

fun swapBatsmenDB(context: Context,matchId: String,batsman1: BatsmanStats, batsman2: BatsmanStats) {
    swapBatsmen(batsman1,batsman2)

    val dbHelper = CricketDatabaseHelper(context)
    if (batsman1.active.value) {
        dbHelper.updateBattingStats(matchId,batsman1.name.value,"non-striker","striker")
        dbHelper.updateBattingStats(matchId,batsman2.name.value,"striker","non-striker")
    } else {
        dbHelper.updateBattingStats(matchId,batsman1.name.value,"striker","non-striker")
        dbHelper.updateBattingStats(matchId,batsman2.name.value,"non-striker","striker")
    }
}

fun swapBatsmen(batsman1: BatsmanStats, batsman2: BatsmanStats) {
    val tempActive = batsman1.active.value
    batsman1.active.value = batsman2.active.value
    batsman2.active.value = tempActive
}

fun swapBatsmen(context: Context,matchId: String,batsman1: BatsmanStats, batsman2: BatsmanStats, actionForBatsman: String) {
    if (((batsman1.name.value == actionForBatsman) && (!batsman1.active.value)) || ((batsman2.name.value == actionForBatsman) && (!batsman2.active.value))) {
        //swapBatsmen(batsman1,batsman2)
        swapBatsmenDB(context,matchId,batsman1,batsman2)
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

        if ((newValue == "1") || (newValue == "2")) swapBatsmenDB(context,matchId,firstBatsmanStats, secondBatsmanStats)

        if (balls.size == 6 && balls.take(6).all { it.action == "0" || it.action.contains("WK") }) {
            updateBowler(matchId,false,"maiden",bowlerStats,activeBatsman,1.00,"",context)
        }
    } else {
        //Handle the UNDO option
        val lastNonEmptyIndex = balls.indexOfLast { it.action.isNotEmpty() }
        if (lastNonEmptyIndex == -1) return

        val lastBall = balls[lastNonEmptyIndex].action

        // Decrement the bowlerOver by 0.1 only if the last ball was a valid ball value
        var containsExcludedValue = lastBall.split(",").any { it in excludedValuesFromBallsBalled }
        if (!containsExcludedValue) {
            if ((lastBall == "1") || (lastBall == "2")) {
                swapBatsmen(context,matchId,firstBatsmanStats,secondBatsmanStats,balls[lastNonEmptyIndex].batsman)
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
}

fun calcRunsToWin(firstTeamStats: TeamStats, secondTeamStats: TeamStats, runsToWinTxt: MutableState<String>) : Int {
    val ballsRemaining: Int
    val oversRemaining: Double
    var runsToWin = 0
    var winningTeam = ""
    if (firstTeamStats.active.value) {
        if (secondTeamStats.inningScore.value != 0) {
            runsToWin = secondTeamStats.inningScore.value - firstTeamStats.inningScore.value + 1
            if (runsToWin <= 0) {
                winningTeam = firstTeamStats.name.value
            }
        }
        ballsRemaining = calculateBalls(12.00) - calculateBalls(firstTeamStats.overs.value)
        oversRemaining = calculateOversRemaining(ballsRemaining)
    } else {
        if (firstTeamStats.inningScore.value != 0) {
            runsToWin = firstTeamStats.inningScore.value - secondTeamStats.inningScore.value + 1
            if (runsToWin <= 0) {
                winningTeam = secondTeamStats.name.value
            }
        }
        ballsRemaining = calculateBalls(12.00) - calculateBalls(secondTeamStats.overs.value)
        oversRemaining = calculateOversRemaining(ballsRemaining)
    }

    if (winningTeam.isNotEmpty()) {
        runsToWinTxt.value = "Team $winningTeam is the winner!"
    } else {
        if (firstTeamStats.overs.value.toInt() == 0  ||  secondTeamStats.overs.value.toInt() == 0) {
            runsToWinTxt.value = "$oversRemaining overs remaining!"
        } else {
            runsToWinTxt.value = "$runsToWin runs to win from $ballsRemaining balls!"
        }
    }

    return ballsRemaining
}

fun calcNoOfWickets(context: Context,matchId: String,firstTeamStats: TeamStats) : Int {
    val dbHelper = CricketDatabaseHelper(context)
    return if (firstTeamStats.active.value) {
        dbHelper.getTeamWickets(matchId,1)
    } else {
        dbHelper.getTeamWickets(matchId,2)
    }
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
    val extraBalls = ((overs - fullOvers) * 10).toInt()

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
            }
            if (newValue.contains("WKROW")) {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
                updateBowler(matchId,undo,"wides",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
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
                updateBatsman(matchId,"runs", firstBatsmanStats, secondBatsmanStats, 4 * multiplier,context)
                updateBatsman(matchId,"fours", firstBatsmanStats, secondBatsmanStats, 1 * multiplier,context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 5.0 * multiplier)
            }

            "NB+6" -> {
                updateBowler(matchId,undo,"runs",bowlerStats,activeBatsman,7.00 * multiplier,"",context)
                updateBowler(matchId,undo,"noballs",bowlerStats,activeBatsman,1.00 * multiplier,"",context)
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

fun handleEndOfMatch(context: Context, matchId: String, firstBatsmanStats: BatsmanStats, secondBatsmanStats: BatsmanStats, runsToWin: MutableState<String>) {
    val dbHelper = CricketDatabaseHelper(context)
    dbHelper.updateBowlingStats(matchId,"bowled")
    handleLastBatsmen(context,matchId,firstBatsmanStats,secondBatsmanStats)
    dbHelper.updateMatchIsFinished(matchId, "")
    Toast.makeText(context, "End of Match (${runsToWin.value})!", Toast.LENGTH_LONG)
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
                matchStatsForPlayer.mBowler,
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
            uploadRow.mBowler,                      // Column AD
            uploadRow.oversBowled,                  // Column AE
            uploadRow.runsConceded,                 // Column AF
            uploadRow.wickets,                      // Column AG
            uploadRow.maiden,                       // Column AH
            uploadRow.sixes,                        // Column AI
            uploadRow.fours,                        // Column AJ
            uploadRow.dotBalls,                     // Column AK
            uploadRow.wides,                        // Column AL
            uploadRow.noBalls,                      // Column AM
            uploadRow.winLossTie                    // Column AN
        )
    }

    val matchDataSize = matchStats.size

    return Pair(transformedData,matchDataSize)
}
