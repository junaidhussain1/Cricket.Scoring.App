package com.example.cricketscoringapp

import android.content.Context
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

fun swapBatsmen(batsman1: BatsmanStats, batsman2: BatsmanStats, actionForBatsman: String) {
    if (((batsman1.name.value == actionForBatsman) && (!batsman1.active.value)) || ((batsman2.name.value == actionForBatsman) && (!batsman2.active.value))) {
        swapBatsmen(batsman1,batsman2)
    }
}

fun doUpdateStats(context: Context,matchId: String,newValue: String, multiplier: Int, bowlerStats: BowlerStats, firstBatsmanStats: BatsmanStats, secondBatsmanStats: BatsmanStats, firstTeamStats: TeamStats, secondTeamStats: TeamStats) {
    if (newValue.contains("WK")) {
        updateBowler(matchId,false,"wickets",bowlerStats,1.00 * multiplier,"",context)
        updateTeam("inningWickets", firstTeamStats, secondTeamStats, 1.0 * multiplier)
        updateTeam("inningScore", firstTeamStats, secondTeamStats, -3.0 * multiplier)
    } else {
        when (newValue) {
            "1" -> {
                updateBowler(matchId,false,"runs",bowlerStats,1.00 * multiplier,"",context)
                updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 1 * multiplier)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            }

            "2" -> {
                updateBowler(matchId,false,"runs",bowlerStats,2.00 * multiplier,"",context)
                updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 2 * multiplier)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
            }

            "3" -> {
                updateBowler(matchId,false,"runs",bowlerStats,3.00 * multiplier,"",context)
                updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 3 * multiplier)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
            }

            "4" -> {
                updateBowler(matchId,false,"runs",bowlerStats,4.00 * multiplier,"",context)
                updateBowler(matchId,false,"fours",bowlerStats,1.00 * multiplier,"",context)
                updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 4 * multiplier)
                updateBatsman("fours", firstBatsmanStats, secondBatsmanStats, 1 * multiplier)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 4.0 * multiplier)
            }

            "6" -> {
                updateBowler(matchId,false,"runs",bowlerStats,6.00 * multiplier,"",context)
                updateBowler(matchId,false,"sixes",bowlerStats,1.00 * multiplier,"",context)
                updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 6 * multiplier)
                updateBatsman("sixes", firstBatsmanStats, secondBatsmanStats, 1 * multiplier)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 6.0 * multiplier)
            }

            "W" -> {
                updateBowler(matchId,false,"runs",bowlerStats,1.00 * multiplier,"",context)
                updateBowler(matchId,false,"wides",bowlerStats,1.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            }

            "W+1" -> {
                updateBowler(matchId,false,"runs",bowlerStats,1.00 * multiplier,"",context)
                updateBowler(matchId,false,"wides",bowlerStats,1.00 * multiplier,"",context)
                updateBowler(matchId,false,"byes",bowlerStats,1.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
            }

            "W+2" -> {
                updateBowler(matchId,false,"runs",bowlerStats,1.00 * multiplier,"",context)
                updateBowler(matchId,false,"wides",bowlerStats,1.00 * multiplier,"",context)
                updateBowler(matchId,false,"byes",bowlerStats,2.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
            }

            "NB" -> {
                updateBowler(matchId,false,"runs",bowlerStats,1.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            }

            "NB+1" -> {
                updateBowler(matchId,false,"runs",bowlerStats,2.00 * multiplier,"",context)
                updateBowler(matchId,false,"noballs",bowlerStats,1.00 * multiplier,"",context)
                updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 1 * multiplier)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
            }

            "NB+2" -> {
                updateBowler(matchId,false,"runs",bowlerStats,3.00 * multiplier,"",context)
                updateBowler(matchId,false,"noballs",bowlerStats,1.00 * multiplier,"",context)
                updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 2 * multiplier)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
            }

            "NB+3" -> {
                updateBowler(matchId,false,"runs",bowlerStats,4.00 * multiplier,"",context)
                updateBowler(matchId,false,"noballs",bowlerStats,1.00 * multiplier,"",context)
                updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 3 * multiplier)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 4.0 * multiplier)
            }

            "NB+4" -> {
                updateBowler(matchId,false,"runs",bowlerStats,5.00 * multiplier,"",context)
                updateBowler(matchId,false,"noballs",bowlerStats,1.00 * multiplier,"",context)
                updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 4 * multiplier)
                updateBatsman("fours", firstBatsmanStats, secondBatsmanStats, 1 * multiplier)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 5.0 * multiplier)
            }

            "NB+6" -> {
                updateBowler(matchId,false,"runs",bowlerStats,7.00 * multiplier,"",context)
                updateBowler(matchId,false,"noballs",bowlerStats,1.00 * multiplier,"",context)
                updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 6 * multiplier)
                updateBatsman("sixes", firstBatsmanStats, secondBatsmanStats, 1 * multiplier)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 7.0 * multiplier)
            }

            "NBL1" -> {
                updateBowler(matchId,false,"runs",bowlerStats,1.00 * multiplier,"",context)
                updateBowler(matchId,false,"noballs",bowlerStats,1.00 * multiplier,"",context)
                updateBowler(matchId,false,"legbyes",bowlerStats,1.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
            }

            "NBL2" -> {
                updateBowler(matchId,false,"runs",bowlerStats,1.00 * multiplier,"",context)
                updateBowler(matchId,false,"noballs",bowlerStats,1.00 * multiplier,"",context)
                updateBowler(matchId,false,"legbyes",bowlerStats,2.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
            }

            "NBL3" -> {
                updateBowler(matchId,false,"runs",bowlerStats,1.00 * multiplier,"",context)
                updateBowler(matchId,false,"noballs",bowlerStats,1.00 * multiplier,"",context)
                updateBowler(matchId,false,"legbyes",bowlerStats,3.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 4.0 * multiplier)
            }

            "NBB1" -> {
                updateBowler(matchId,false,"runs",bowlerStats,1.00 * multiplier,"",context)
                updateBowler(matchId,false,"byes",bowlerStats,1.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
            }

            "NBB2" -> {
                updateBowler(matchId,false,"runs",bowlerStats,1.00 * multiplier,"",context)
                updateBowler(matchId,false,"byes",bowlerStats,2.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
            }

            "NBB3" -> {
                updateBowler(matchId,false,"runs",bowlerStats,1.00 * multiplier,"",context)
                updateBowler(matchId,false,"byes",bowlerStats,3.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 4.0 * multiplier)
            }

            "B1" -> {
                updateBowler(matchId,false,"byes",bowlerStats,1.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            }

            "B2" -> {
                updateBowler(matchId,false,"byes",bowlerStats,2.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
            }

            "B3" -> {
                updateBowler(matchId,false,"byes",bowlerStats,3.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
            }

            "LB1" -> {
                updateBowler(matchId,false,"legbyes",bowlerStats,1.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            }

            "LB2" -> {
                updateBowler(matchId,false,"legbyes",bowlerStats,2.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
            }

            "LB3" -> {
                updateBowler(matchId,false,"legbyes",bowlerStats,3.00 * multiplier,"",context)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
            }
        }
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
                secondTeamStats: TeamStats,
                runsToWinTxt: MutableState<String>) {
    val dbHelper = CricketDatabaseHelper(context)
    val matchId = dbHelper.getMatchId()
    val excludedValuesFromBallsBalled = setOf("W","W+1","W+2","NB","NB+1","NB+2","NB+3","NB+4","NB+6","NBL1","NBL2","NBL3","NBB1","NBB2","NBB3")
    val excludedValuesFromBallsFaced = setOf("W","W+1","W+2")

    val activeBatsman = if (firstBatsmanStats.active.value) {
        firstBatsmanStats.name.value
    } else {
        secondBatsmanStats.name.value
    }

    if (newValue != "UNDO") {
        val emptyIndex = balls.indexOfFirst { it.action.isEmpty() }
        if (emptyIndex == -1) {
            balls.add(Ball(newValue,activeBatsman)) // Add new value if no empty spot is found
        } else {
            balls[emptyIndex] = Ball(newValue,activeBatsman) // Update the first empty spot
        }

        val includedBallsCount = balls.count { it.action !in excludedValuesFromBallsBalled }
        if (includedBallsCount == 7) {
            if (emptyIndex == -1) {
                balls.removeLast() // Remove the last item if added
            } else {
                balls[emptyIndex] = Ball("",activeBatsman) // Reset the value at the empty index
            }
            return
        }

        updateBowler(matchId,false,"over",bowlerStats,0.0,newValue,context)

        // Increment the bowlerOver by 0.1 only if it's a valid ball value
        if (newValue !in excludedValuesFromBallsBalled) {
            updateBowler(matchId,false,"over",bowlerStats,0.1,"",context)
            updateTeam("overs", firstTeamStats, secondTeamStats, 0.1)
        }
        if (newValue !in excludedValuesFromBallsFaced) {
            updateBatsman("balls", firstBatsmanStats, secondBatsmanStats, 1)
        }

        doUpdateStats(context,matchId,newValue,1, bowlerStats, firstBatsmanStats, secondBatsmanStats, firstTeamStats, secondTeamStats)

        if ((newValue == "1") || (newValue == "2")) swapBatsmenDB(context,matchId,firstBatsmanStats, secondBatsmanStats)

        if (balls.size == 6 && balls.take(6).all { it.action == "0" }) {
            updateBowler(matchId,false,"maiden",bowlerStats,1.00,"",context)
        }

        //end of over handling
        if (balls.filter { it.action !in excludedValuesFromBallsFaced }.size >= 6) {
            //Toast.makeText(context, "Condition met", Toast.LENGTH_SHORT).show()
        }

    } else {
        //Handle the UNDO option
        val lastNonEmptyIndex = balls.indexOfLast { it.action.isNotEmpty() }
        if (lastNonEmptyIndex == -1) return

        val lastBall = balls[lastNonEmptyIndex].action

        // Decrement the bowlerOver by 0.1 only if the last ball was a valid ball value
        if (lastBall !in excludedValuesFromBallsBalled) {
            if ((lastBall == "1") || (lastBall == "2")) {
                swapBatsmen(firstBatsmanStats,secondBatsmanStats,balls[lastNonEmptyIndex].batsman)
            }
            updateBowler(matchId,true,"over",bowlerStats,-0.1,"",context)
            updateTeam("overs", firstTeamStats, secondTeamStats, -0.1)
        }

        if (lastBall !in excludedValuesFromBallsFaced) {
            if (lastBall.contains("WK")) {
                //Restore out batsman
                val parts = lastBall.split("|")
                val batsmanOut = parts[1]
                val newBatsman = parts[2]

                val batsmanOutFromDB = dbHelper.getBatsmanStats(matchId,batsmanOut)
                dbHelper.deleteBatsman(matchId,newBatsman)
                dbHelper.updateBattingStats(matchId,batsmanOutFromDB.name.value,"out","striker")
                batsmanOutFromDB.balls.value -= 1
                dbHelper.updateBattingStats(matchId,"striker",batsmanOutFromDB,"")

                if (firstBatsmanStats.name.value == newBatsman) {
                    firstBatsmanStats.name.value = batsmanOut
                    firstBatsmanStats.runs.value = batsmanOutFromDB.runs.value
                    firstBatsmanStats.balls.value = batsmanOutFromDB.balls.value
                    firstBatsmanStats.fours.value = batsmanOutFromDB.fours.value
                    firstBatsmanStats.sixes.value = batsmanOutFromDB.sixes.value
                } else if (secondBatsmanStats.name.value == newBatsman) {
                    secondBatsmanStats.name.value = batsmanOut
                    secondBatsmanStats.runs.value = batsmanOutFromDB.runs.value
                    secondBatsmanStats.balls.value = batsmanOutFromDB.balls.value
                    secondBatsmanStats.fours.value = batsmanOutFromDB.fours.value
                    secondBatsmanStats.sixes.value = batsmanOutFromDB.sixes.value
                }
            } else {
                updateBatsman("balls", firstBatsmanStats, secondBatsmanStats, -1)
            }
        }

        // Check if the first 5 balls were "0" and last index is 5
        if (lastNonEmptyIndex == 5 && balls.take(5).all { it.action == "0" }) {
            updateBowler(matchId,false,"maiden",bowlerStats,-1.00,"",context)
        }

        doUpdateStats(context,matchId,lastBall,-1, bowlerStats, firstBatsmanStats, secondBatsmanStats, firstTeamStats, secondTeamStats)

        balls[lastNonEmptyIndex] = Ball("",activeBatsman)
    }

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
}

fun updateBowler(
    matchId: String,
    undo: Boolean,
    statType: String,
    bowlerStats: BowlerStats,
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
        "legbyes" -> {
            bowlerStats.legbyes.value += updateValue.toInt()
        }
        "fours" -> {
            bowlerStats.fours.value += updateValue.toInt()
        }
        "sixes" -> {
            bowlerStats.sixes.value += updateValue.toInt()
        }
    }
    if (!undo) {
        if (ballAction != "") {
            if (bowlerStats.overrecord.value == "") {
                bowlerStats.overrecord.value = ballAction
            } else {
                bowlerStats.overrecord.value += ",$ballAction"
            }
        }
    } else {
        bowlerStats.overrecord.value = removeLastValueFromCSV(bowlerStats.overrecord.value)
    }
    dbHelper.updateBowlingStats(matchId,bowlerStats)
}

fun updateBatsman(
    statType: String,
    firstBatsmanStats: BatsmanStats,
    secondBatsmanStats: BatsmanStats,
    updateValue: Int)
{
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

fun removeLastValueFromCSV(csv: String): String {
    // Split the string into a list of values
    val values = csv.split(",").toMutableList()

    // Remove the last value if the list is not empty
    if (values.isNotEmpty()) {
        values.removeAt(values.size - 1)
    }

    // Join the remaining values back into a CSV string
    return values.joinToString(",")
}

fun endOfOverReached(balls: MutableList<Ball>): Boolean {
    val excludedValuesFromBallsFaced = setOf("W","W+1","W+2")
    return balls.filter { it.action !in excludedValuesFromBallsFaced }.size >= 6
}