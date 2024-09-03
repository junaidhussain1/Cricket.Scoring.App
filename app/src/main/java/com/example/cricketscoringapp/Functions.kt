package com.example.cricketscoringapp

import androidx.compose.runtime.MutableState

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

fun doUpdateStats(newValue: String, multiplier: Int, bowlerStats: BowlerStats, firstBatsmanStats: BatsmanStats, secondBatsmanStats: BatsmanStats, firstTeamStats: TeamStats, secondTeamStats: TeamStats) {
    when (newValue) {
        "1" -> {
            bowlerStats.runs.value += (1 * multiplier)
            updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 1 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0 * multiplier)
        }

        "2" -> {
            bowlerStats.runs.value += (2 * multiplier)
            updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 2 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
        }

        "3" -> {
            bowlerStats.runs.value += (3 * multiplier)
            updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 3 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
        }

        "4" -> {
            bowlerStats.runs.value += (4 * multiplier)
            updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 4 * multiplier)
            updateBatsman("fours", firstBatsmanStats, secondBatsmanStats, 1 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 4.0 * multiplier)
        }

        "6" -> {
            bowlerStats.runs.value += (6 * multiplier)
            updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 6 * multiplier)
            updateBatsman("sixes", firstBatsmanStats, secondBatsmanStats, 1 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 6.0 * multiplier)
        }

        "W" -> {
            bowlerStats.runs.value += (1 * multiplier)
            bowlerStats.wides.value += (1 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0 * multiplier)
        }

        "W+1" -> {
            bowlerStats.runs.value += (1 * multiplier)
            bowlerStats.wides.value += (1 * multiplier)
            bowlerStats.byes.value += (1 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
        }

        "W+2" -> {
            bowlerStats.runs.value += (1 * multiplier)
            bowlerStats.wides.value += (1 * multiplier)
            bowlerStats.byes.value += (2 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
        }

        "NB" -> {
            bowlerStats.runs.value += (1 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0 * multiplier)
        }

        "NB+1" -> {
            bowlerStats.runs.value += (2 * multiplier)
            bowlerStats.noballs.value += (1 * multiplier)
            updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 1 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
        }

        "NB+2" -> {
            bowlerStats.runs.value += (3 * multiplier)
            bowlerStats.noballs.value += (1 * multiplier)
            updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 2 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
        }

        "NB+3" -> {
            bowlerStats.runs.value += (4 * multiplier)
            bowlerStats.noballs.value += (1 * multiplier)
            updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 3 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 4.0 * multiplier)
        }

        "NB+4" -> {
            bowlerStats.runs.value += (5 * multiplier)
            bowlerStats.noballs.value += (1 * multiplier)
            updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 4 * multiplier)
            updateBatsman("fours", firstBatsmanStats, secondBatsmanStats, 1 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 5.0 * multiplier)
        }

        "NB+6" -> {
            bowlerStats.runs.value += (7 * multiplier)
            bowlerStats.noballs.value += (1 * multiplier)
            updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 6 * multiplier)
            updateBatsman("sixes", firstBatsmanStats, secondBatsmanStats, 1 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 7.0 * multiplier)
        }

        "NBL1" -> {
            bowlerStats.runs.value += (1 * multiplier)
            bowlerStats.noballs.value += (1 * multiplier)
            bowlerStats.legbyes.value += (1 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
        }

        "NBL2" -> {
            bowlerStats.runs.value += (1 * multiplier)
            bowlerStats.noballs.value += (1 * multiplier)
            bowlerStats.legbyes.value += (2 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
        }

        "NBL3" -> {
            bowlerStats.runs.value += (1 * multiplier)
            bowlerStats.noballs.value += (1 * multiplier)
            bowlerStats.legbyes.value += (3 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 4.0 * multiplier)
        }

        "NBB1" -> {
            bowlerStats.runs.value += (1 * multiplier)
            bowlerStats.byes.value += (1 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
        }

        "NBB2" -> {
            bowlerStats.runs.value += (1 * multiplier)
            bowlerStats.byes.value += (2 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
        }

        "NBB3" -> {
            bowlerStats.runs.value += (1 * multiplier)
            bowlerStats.byes.value += (3 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 4.0 * multiplier)
        }

        "B1" -> {
            bowlerStats.byes.value += (1 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0 * multiplier)
        }

        "B2" -> {
            bowlerStats.byes.value += (2 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
        }

        "B3" -> {
            bowlerStats.byes.value += (3 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
        }

        "LB1" -> {
            bowlerStats.legbyes.value += (1 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0 * multiplier)
        }

        "LB2" -> {
            bowlerStats.legbyes.value += (2 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0 * multiplier)
        }

        "LB3" -> {
            bowlerStats.legbyes.value += (3 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0 * multiplier)
        }

        "WKB" -> {
            bowlerStats.wickets.value += (1 * multiplier)
            updateTeam("inningWickets", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, -3.0 * multiplier)
        }

        "WKCB" -> {
            bowlerStats.wickets.value += (1 * multiplier)
            updateTeam("inningWickets", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, -3.0 * multiplier)
        }

        "WKC" -> {
            bowlerStats.wickets.value += (1 * multiplier)
            updateTeam("inningWickets", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, -3.0 * multiplier)
        }

        "WKRO" -> {
            bowlerStats.wickets.value += (1 * multiplier)
            updateTeam("inningWickets", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, -3.0 * multiplier)
        }

        "WKRONB" -> {
            bowlerStats.wickets.value += (1 * multiplier)
            updateTeam("inningWickets", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, -3.0 * multiplier)
        }

        "WKST" -> {
            bowlerStats.wickets.value += (1 * multiplier)
            updateTeam("inningWickets", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, -3.0 * multiplier)
        }

        "WKSTW" -> {
            bowlerStats.wickets.value += (1 * multiplier)
            updateTeam("inningWickets", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, -3.0 * multiplier)
        }

        "WKHW" -> {
            bowlerStats.wickets.value += (1 * multiplier)
            updateTeam("inningWickets", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, -3.0 * multiplier)
        }

        "WKLB" -> {
            bowlerStats.wickets.value += (1 * multiplier)
            updateTeam("inningWickets", firstTeamStats, secondTeamStats, 1.0 * multiplier)
            updateTeam("inningScore", firstTeamStats, secondTeamStats, -3.0 * multiplier)
        }
    }
}

// Function to update the stats
fun updateStats(balls: MutableList<Ball>,
                newValue: String,
                bowlerStats: BowlerStats,
                firstBatsmanStats: BatsmanStats,
                secondBatsmanStats: BatsmanStats,
                firstTeamStats: TeamStats,
                secondTeamStats: TeamStats,
                runsToWinTxt: MutableState<String>) {
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

        // Increment the bowlerOver by 0.1 only if it's a valid ball value
        if (newValue !in excludedValuesFromBallsBalled) {
            bowlerStats.over.value += 0.1f
            updateTeam("overs", firstTeamStats, secondTeamStats, 0.1)
        }
        if (newValue !in excludedValuesFromBallsFaced) {
            updateBatsman("balls", firstBatsmanStats, secondBatsmanStats, 1)
        }

        doUpdateStats(newValue,1, bowlerStats, firstBatsmanStats, secondBatsmanStats, firstTeamStats, secondTeamStats)

        if ((newValue == "1") || (newValue == "2")) swapBatsmen(firstBatsmanStats, secondBatsmanStats)

        if (balls.size == 6 && balls.take(6).all { it.action == "0" }) {
            bowlerStats.maiden.value += 1
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
            bowlerStats.over.value -= 0.1f
            updateTeam("overs", firstTeamStats, secondTeamStats, -0.1)
        }

        if (lastBall !in excludedValuesFromBallsFaced) {
            updateBatsman("balls", firstBatsmanStats, secondBatsmanStats, -1)
        }

        // Check if the first 5 balls were "0" and last index is 5
        if (lastNonEmptyIndex == 5 && balls.take(5).all { it.action == "0" }) {
            bowlerStats.maiden.value -= 1
        }

        doUpdateStats(lastBall,-1, bowlerStats, firstBatsmanStats, secondBatsmanStats, firstTeamStats, secondTeamStats)

        balls[lastNonEmptyIndex] = Ball("",activeBatsman)
    }

    val ballsRemaining: Int
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
    } else {
        if (firstTeamStats.inningScore.value != 0) {
            runsToWin = firstTeamStats.inningScore.value - secondTeamStats.inningScore.value + 1
            if (runsToWin <= 0) {
                winningTeam = secondTeamStats.name.value
            }
        }
        ballsRemaining = calculateBalls(12.00) - calculateBalls(secondTeamStats.overs.value)
    }

    if (winningTeam.isNotEmpty()) {
        runsToWinTxt.value = "Team $winningTeam is the winner!"
    } else {
        if (firstTeamStats.overs.value.toInt() == 0  ||  secondTeamStats.overs.value.toInt() == 0) {
            runsToWinTxt.value = "$ballsRemaining balls remaining!"
        } else {
            runsToWinTxt.value = "$runsToWin runs to win from $ballsRemaining balls!"
        }
    }
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