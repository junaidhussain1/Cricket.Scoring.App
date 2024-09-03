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

// Function to update the stats
fun updateStats(balls: MutableList<Ball>,
                newValue: String,
                bowlerStats: BowlerStats,
                firstBatsmanStats: BatsmanStats,
                secondBatsmanStats: BatsmanStats,
                firstTeamStats: TeamStats,
                secondTeamStats: TeamStats,
                runsToWinTxt: MutableState<String>) {
    val excludedValues = setOf("W","W+1","W+2","W+3","W+4","W+5","W+6","NB","NB+1","NB+2","NB+3","NB+4","NB+5","NB+6")

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

        val includedBallsCount = balls.count { it.action !in excludedValues }
        if (includedBallsCount == 7) {
            if (emptyIndex == -1) {
                balls.removeLast() // Remove the last item if added
            } else {
                balls[emptyIndex] = Ball("",activeBatsman) // Reset the value at the empty index
            }
            return
        }

        // Increment the bowlerOver by 0.1 only if it's a valid ball value
        if (newValue !in excludedValues) {
            bowlerStats.over.value += 0.1f
            updateBatsman("balls", firstBatsmanStats, secondBatsmanStats, 1)
            updateTeam("overs", firstTeamStats, secondTeamStats, 0.1)
        }

        when (newValue) {
            "1" -> {
                bowlerStats.runs.value += 1
                updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 1)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0)
                swapBatsmen(firstBatsmanStats, secondBatsmanStats)
            }

            "2" -> {
                bowlerStats.runs.value += 2
                updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 2)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0)
                swapBatsmen(firstBatsmanStats, secondBatsmanStats)
            }

            "3" -> {
                bowlerStats.runs.value += 3
                updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 3)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0)
            }

            "4" -> {
                bowlerStats.runs.value += 4
                updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 4)
                updateBatsman("fours", firstBatsmanStats, secondBatsmanStats, 1)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 4.0)
            }

            "6" -> {
                bowlerStats.runs.value += 6
                updateBatsman("runs", firstBatsmanStats, secondBatsmanStats, 6)
                updateBatsman("sixes", firstBatsmanStats, secondBatsmanStats, 1)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 6.0)
            }

            "W" -> {
                bowlerStats.runs.value += 1
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0)
            }

            "W+1" -> {
                bowlerStats.runs.value += 2
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0)
            }

            "W+2" -> {
                bowlerStats.runs.value += 3
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0)
            }

            "NB" -> {
                bowlerStats.runs.value += 1
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0)
            }

            "NB+1" -> {
                bowlerStats.runs.value += 2
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 2.0)
            }

            "NB+2" -> {
                bowlerStats.runs.value += 3
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 3.0)
            }

            "NB+3" -> {
                bowlerStats.runs.value += 4
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 4.0)
            }

            "NB+4" -> {
                bowlerStats.runs.value += 5
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 5.0)
            }

            "NB+5" -> {
                bowlerStats.runs.value += 6
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 6.0)
            }

            "NB+6" -> {
                bowlerStats.runs.value += 7
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 7.0)
            }

            "B" -> {
                bowlerStats.runs.value += 1
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0)
            }

            "LB" -> {
                bowlerStats.runs.value += 1
                updateTeam("inningScore", firstTeamStats, secondTeamStats, 1.0)
            }

            "WI" -> {
                bowlerStats.wickets.value += 1
                updateTeam("inningWickets", firstTeamStats, secondTeamStats, 1.0)
                updateTeam("inningScore", firstTeamStats, secondTeamStats, -3.0)
            }
        }

        if (balls.take(6).all { it.action == "0" }) {
            bowlerStats.maiden.value += 1
        }

    } else {
        //Handle the UNDO option
        val lastNonEmptyIndex = balls.indexOfLast { it.action.isNotEmpty() }
        if (lastNonEmptyIndex == -1) return

        val lastBall = balls[lastNonEmptyIndex].action

        // Decrement the bowlerOver by 0.1 only if the last ball was a valid ball value
        if (lastBall !in excludedValues) {
            if ((lastBall == "1") || (lastBall == "2")) {
                swapBatsmen(firstBatsmanStats,secondBatsmanStats,balls[lastNonEmptyIndex].batsman)
            }
            bowlerStats.over.value -= 0.1f
            updateBatsman("balls", firstBatsmanStats, secondBatsmanStats, -1)
            updateTeam("overs", firstTeamStats, secondTeamStats, -0.1)
        }

        // Check if the first 5 balls were "0" and last index is 5
        if (lastNonEmptyIndex == 5 && balls.take(5).all { it.action == "0" }) {
            bowlerStats.maiden.value -= 1
        }

        when(lastBall) {
            "1" -> {
                bowlerStats.runs.value -= 1
                updateBatsman("runs",firstBatsmanStats,secondBatsmanStats,-1)
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-1.0)
            }

            "2" -> {
                bowlerStats.runs.value -= 2
                updateBatsman("runs",firstBatsmanStats,secondBatsmanStats,-2)
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-2.0)
            }

            "3" -> {
                bowlerStats.runs.value -= 3
                updateBatsman("runs",firstBatsmanStats,secondBatsmanStats,-3)
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-3.0)
            }

            "4" -> {
                bowlerStats.runs.value -= 4
                updateBatsman("runs",firstBatsmanStats,secondBatsmanStats,-4)
                updateBatsman("fours",firstBatsmanStats,secondBatsmanStats,-1)
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-4.0)
            }

            "6" -> {
                bowlerStats.runs.value -= 6
                updateBatsman("runs",firstBatsmanStats,secondBatsmanStats,-6)
                updateBatsman("sixes",firstBatsmanStats,secondBatsmanStats,-1)
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-6.0)
            }

            "W" -> {
                bowlerStats.runs.value -= 1
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-1.0)
            }
            "W+1" -> {
                bowlerStats.runs.value -= 2
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-2.0)
            }
            "W+2" -> {
                bowlerStats.runs.value -= 3
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-3.0)
            }
            "NB" -> {
                bowlerStats.runs.value -= 1
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-1.0)
            }
            "NB+1" -> {
                bowlerStats.runs.value -= 2
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-2.0)
            }
            "NB+2" -> {
                bowlerStats.runs.value -= 3
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-3.0)
            }
            "NB+3" -> {
                bowlerStats.runs.value -= 4
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-4.0)
            }
            "NB+4" -> {
                bowlerStats.runs.value -= 5
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-5.0)
            }
            "NB+6" -> {
                bowlerStats.runs.value -= 7
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-7.0)
            }

            "B" -> {
                bowlerStats.runs.value -= 1
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-1.0)
            }

            "LB" -> {
                bowlerStats.runs.value -= 1
                updateTeam("inningScore",firstTeamStats,secondTeamStats,-1.0)
            }

            "WI" -> {
                bowlerStats.wickets.value -= 1
                updateTeam("inningWickets",firstTeamStats,secondTeamStats,-1.0)
                updateTeam("inningScore",firstTeamStats,secondTeamStats,3.0)
            }
        }

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

//fun updateStats(balls: MutableList<String>,
//                newValue: String,
//                bowlerStats: BowlerStats,
//                firstBatsmanStats: BatsmanStats,
//                secondBatsmanStats: BatsmanStats,
//                firstTeamStats: TeamStats,
//                secondTeamStats: TeamStats,
//                runsToWinTxt: MutableState<String>) {
//    val excludedValues = setOf("W","W+1","W+2","W+3","W+4","W+5","W+6","NB","NB+1","NB+2","NB+3","NB+4","NB+5","NB+6")
//    if (newValue != "UNDO") {
//        var overCompleted = false
//        for (i in balls.indices) {
//            if (balls[i].isEmpty()) {
//                balls[i] = newValue
//                // Increment the bowlerOver by 0.1 only if it's a valid ball value
//                if (newValue !in excludedValues) {
//                    bowlerStats.over.value += 0.1f
//                    updateBatsman("balls",firstBatsmanStats,secondBatsmanStats,1)
//                    updateTeam("overs",firstTeamStats,secondTeamStats,0.1)
//                }
//                val tolerance = 0.0001
//                val roundedValue = String.format(Locale.UK,"%.2f", bowlerStats.over.value).toDouble()
//                if (abs(roundedValue % 1.0 - 0.7) < tolerance) {
//                    bowlerStats.over.value -= 0.1f
//                    updateBatsman("balls",firstBatsmanStats,secondBatsmanStats,-1)
//                    updateTeam("overs",firstTeamStats,secondTeamStats,-0.1)
//                    balls[i] = ""
//                    overCompleted = true
//                    break
//                }
//                when(newValue) {
//                    "1" -> {
//                        bowlerStats.runs.value += 1
//                        updateBatsman("runs",firstBatsmanStats,secondBatsmanStats,1)
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,1.0)
//                        swapBatsmen(firstBatsmanStats,secondBatsmanStats)
//                    }
//                    "2" -> {
//                        bowlerStats.runs.value += 2
//                        updateBatsman("runs",firstBatsmanStats,secondBatsmanStats,2)
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,2.0)
//                        swapBatsmen(firstBatsmanStats,secondBatsmanStats)
//                    }
//                    "3" -> {
//                        bowlerStats.runs.value += 3
//                        updateBatsman("runs",firstBatsmanStats,secondBatsmanStats,3)
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,3.0)
//                    }
//                    "4" -> {
//                        bowlerStats.runs.value += 4
//                        updateBatsman("runs",firstBatsmanStats,secondBatsmanStats,4)
//                        updateBatsman("fours",firstBatsmanStats,secondBatsmanStats,1)
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,4.0)
//                    }
//                    "6" -> {
//                        bowlerStats.runs.value += 6
//                        updateBatsman("runs",firstBatsmanStats,secondBatsmanStats,6)
//                        updateBatsman("sixes",firstBatsmanStats,secondBatsmanStats,1)
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,6.0)
//                    }
//                    "W" -> {
//                        bowlerStats.runs.value += 1
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,1.0)
//                    }
//                    "W+1" -> {
//                        bowlerStats.runs.value += 2
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,2.0)
//                    }
//                    "W+2" -> {
//                        bowlerStats.runs.value += 3
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,3.0)
//                    }
//                    "NB" -> {
//                        bowlerStats.runs.value += 1
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,1.0)
//                    }
//                    "NB+1" -> {
//                        bowlerStats.runs.value += 2
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,2.0)
//                    }
//                    "NB+2" -> {
//                        bowlerStats.runs.value += 3
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,3.0)
//                    }
//                    "NB+3" -> {
//                        bowlerStats.runs.value += 4
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,4.0)
//                    }
//                    "NB+4" -> {
//                        bowlerStats.runs.value += 5
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,5.0)
//                    }
//                    "NB+5" -> {
//                        bowlerStats.runs.value += 6
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,6.0)
//                    }
//                    "NB+6" -> {
//                        bowlerStats.runs.value += 7
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,7.0)
//                    }
//                    "B" -> {
//                        bowlerStats.runs.value += 1
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,1.0)
//                    }
//                    "LB" -> {
//                        bowlerStats.runs.value += 1
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,1.0)
//                    }
//                    "WI" -> {
//                        bowlerStats.wickets.value += 1
//                        updateTeam("inningWickets",firstTeamStats,secondTeamStats,1.0)
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-3.0)
//                    }
//                }
//                break
//            }
//        }
//        if (!overCompleted && balls[0] == "0" && balls[1] == "0" && balls[2] == "0" && balls[3] == "0" && balls[4] == "0" && balls[5] == "0"){
//            bowlerStats.maiden.value += 1
//        }
//    } else {
//        for (i in balls.size - 1 downTo 0) {
//            if (balls[i].isNotEmpty()) {
//                // Decrement the bowlerOver by 0.1 only if last ball was a valid ball value
//                if (balls[i] !in excludedValues) {
//                    bowlerStats.over.value -= 0.1f
//                    updateBatsman("balls",firstBatsmanStats,secondBatsmanStats,-1)
//                    updateTeam("overs",firstTeamStats,secondTeamStats,-0.1)
//                }
//                if (i == 5) {
//                    if (balls[0] == "0" && balls[1] == "0" && balls[2] == "0" && balls[3] == "0" && balls[4] == "0"){
//                        bowlerStats.maiden.value -= 1
//                    }
//                }
//                when(balls[i]) {
//                    "1" -> {
//                        swapBatsmen(firstBatsmanStats,secondBatsmanStats)
//                        bowlerStats.runs.value -= 1
//                        updateBatsman("runs",firstBatsmanStats,secondBatsmanStats,-1)
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-1.0)
//                    }
//
//                    "2" -> {
//                        swapBatsmen(firstBatsmanStats,secondBatsmanStats)
//                        bowlerStats.runs.value -= 2
//                        updateBatsman("runs",firstBatsmanStats,secondBatsmanStats,-2)
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-2.0)
//                    }
//
//                    "3" -> {
//                        bowlerStats.runs.value -= 3
//                        updateBatsman("runs",firstBatsmanStats,secondBatsmanStats,-3)
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-3.0)
//                    }
//
//                    "4" -> {
//                        bowlerStats.runs.value -= 4
//                        updateBatsman("runs",firstBatsmanStats,secondBatsmanStats,-4)
//                        updateBatsman("fours",firstBatsmanStats,secondBatsmanStats,-1)
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-4.0)
//                    }
//
//                    "6" -> {
//                        bowlerStats.runs.value -= 6
//                        updateBatsman("runs",firstBatsmanStats,secondBatsmanStats,-6)
//                        updateBatsman("sixes",firstBatsmanStats,secondBatsmanStats,-1)
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-6.0)
//                    }
//
//                    "W" -> {
//                        bowlerStats.runs.value -= 1
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-1.0)
//                    }
//                    "W+1" -> {
//                        bowlerStats.runs.value -= 2
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-2.0)
//                    }
//                    "W+2" -> {
//                        bowlerStats.runs.value -= 3
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-3.0)
//                    }
//                    "NB" -> {
//                        bowlerStats.runs.value -= 1
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-1.0)
//                    }
//                    "NB+1" -> {
//                        bowlerStats.runs.value -= 2
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-2.0)
//                    }
//                    "NB+2" -> {
//                        bowlerStats.runs.value -= 3
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-3.0)
//                    }
//                    "NB+3" -> {
//                        bowlerStats.runs.value -= 4
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-4.0)
//                    }
//                    "NB+4" -> {
//                        bowlerStats.runs.value -= 5
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-5.0)
//                    }
//                    "NB+6" -> {
//                        bowlerStats.runs.value -= 7
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-7.0)
//                    }
//
//                    "B" -> {
//                        bowlerStats.runs.value -= 1
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-1.0)
//                    }
//
//                    "LB" -> {
//                        bowlerStats.runs.value -= 1
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,-1.0)
//                    }
//
//                    "WI" -> {
//                        bowlerStats.wickets.value -= 1
//                        updateTeam("inningWickets",firstTeamStats,secondTeamStats,-1.0)
//                        updateTeam("inningScore",firstTeamStats,secondTeamStats,3.0)
//                    }
//                }
//                balls[i] = ""
//                break
//            }
//        }
//    }
//
//    val ballsRemaining: Int
//    var runsToWin = 0
//    var winningTeam = ""
//    if (firstTeamStats.active.value) {
//        if (secondTeamStats.inningScore.value != 0) {
//            runsToWin = secondTeamStats.inningScore.value - firstTeamStats.inningScore.value + 1
//            if (runsToWin <= 0) {
//                winningTeam = firstTeamStats.name.value
//            }
//        }
//        ballsRemaining = calculateBalls(12.00) - calculateBalls(firstTeamStats.overs.value)
//    } else {
//        if (firstTeamStats.inningScore.value != 0) {
//            runsToWin = firstTeamStats.inningScore.value - secondTeamStats.inningScore.value + 1
//            if (runsToWin <= 0) {
//                winningTeam = secondTeamStats.name.value
//            }
//        }
//        ballsRemaining = calculateBalls(12.00) - calculateBalls(secondTeamStats.overs.value)
//    }
//
//    if (winningTeam.isNotEmpty()) {
//        runsToWinTxt.value = "Team $winningTeam is the winner!"
//    } else {
//        if (firstTeamStats.overs.value.toInt() == 0  ||  secondTeamStats.overs.value.toInt() == 0) {
//            runsToWinTxt.value = "$ballsRemaining balls remaining!"
//        } else {
//            runsToWinTxt.value = "$runsToWin runs to win from $ballsRemaining balls!"
//        }
//    }
//}

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


