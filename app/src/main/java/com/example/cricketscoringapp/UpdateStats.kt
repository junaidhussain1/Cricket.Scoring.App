package com.example.cricketscoringapp

import java.util.Locale
import kotlin.math.abs

// Function to update the stats
fun updateStats(balls: MutableList<String>,
                newValue: String,
                bowlerStats: BowlerStats,
                activeBatsmanStats: BatsmanStats,
                secondBatsmanStats: BatsmanStats,
                battingTeamStats: TeamStats) {
    if (newValue == "UNDO") {
        for (i in balls.size - 1 downTo 0) {
            if (balls[i].isNotEmpty()) {
                // Decrement the bowlerOver by 0.1 only if last ball was a valid ball value
                if (balls[i] != "W" && balls[i] != "NB") {
                    bowlerStats.over.value -= 0.1f
                    activeBatsmanStats.balls.value -= 1
                    battingTeamStats.overs.value -= 0.1f
                }
                if (i == 5) {
                    if (balls[0] == "0" && balls[1] == "0" && balls[2] == "0" && balls[3] == "0" && balls[4] == "0"){
                        bowlerStats.maiden.value -= 1
                    }
                }
                when(balls[i]) {
                    "1" -> {
                        swapBatsmen(activeBatsmanStats,secondBatsmanStats)

                        bowlerStats.runs.value -= 1
                        activeBatsmanStats.runs.value -= 1
                        battingTeamStats.inningScore.value -= 1
                    }

                    "2" -> {
                        swapBatsmen(activeBatsmanStats,secondBatsmanStats)

                        bowlerStats.runs.value -= 2
                        activeBatsmanStats.runs.value -= 2
                        battingTeamStats.inningScore.value -= 2
                    }

                    "3" -> {
                        bowlerStats.runs.value -= 3
                        activeBatsmanStats.runs.value -= 3
                        battingTeamStats.inningScore.value -= 3
                    }

                    "4" -> {
                        bowlerStats.runs.value -= 4
                        activeBatsmanStats.runs.value -= 4
                        activeBatsmanStats.fours.value -= 1
                        battingTeamStats.inningScore.value -= 4
                    }

                    "6" -> {
                        bowlerStats.runs.value -= 6
                        activeBatsmanStats.runs.value -= 6
                        activeBatsmanStats.sixes.value -= 1
                        battingTeamStats.inningScore.value -= 6
                    }

                    "W" -> {
                        bowlerStats.runs.value -= 1
                        battingTeamStats.inningScore.value -= 1
                    }

                    "NB" -> {
                        bowlerStats.runs.value -= 1
                        battingTeamStats.inningScore.value -= 1
                    }

                    "B" -> {
                        bowlerStats.runs.value -= 1
                        battingTeamStats.inningScore.value -= 1
                    }

                    "LB" -> {
                        bowlerStats.runs.value -= 1
                        battingTeamStats.inningScore.value -= 1
                    }

                    "WI" -> {
                        bowlerStats.wickets.value -= 1
                        battingTeamStats.inningWickets.value -= 1
                        battingTeamStats.inningScore.value += 3
                    }
                }
                balls[i] = ""
                break
            }
        }
    } else {
        var overCompleted = false
        for (i in balls.indices) {
            if (balls[i].isEmpty()) {
                balls[i] = newValue
                // Increment the bowlerOver by 0.1 only if it's a valid ball value
                if (newValue != "W" && newValue != "NB") {
                    bowlerStats.over.value += 0.1f
                    activeBatsmanStats.balls.value += 1
                    battingTeamStats.overs.value += 0.1f
                }
                val tolerance = 0.0001
                val roundedValue = String.format(Locale.UK,"%.2f", bowlerStats.over.value).toDouble()
                if (abs(roundedValue % 1.0 - 0.7) < tolerance) {
                    bowlerStats.over.value -= 0.1f
                    activeBatsmanStats.balls.value -= 1
                    battingTeamStats.overs.value -= 0.1f
                    balls[i] = ""
                    overCompleted = true
                    break
                }
                when(newValue) {
                    "1" -> {
                        bowlerStats.runs.value += 1
                        activeBatsmanStats.runs.value += 1
                        battingTeamStats.inningScore.value += 1

                        swapBatsmen(activeBatsmanStats,secondBatsmanStats)
                    }
                    "2" -> {
                        bowlerStats.runs.value += 2
                        activeBatsmanStats.runs.value += 2
                        battingTeamStats.inningScore.value += 2

                        swapBatsmen(activeBatsmanStats,secondBatsmanStats)
                    }
                    "3" -> {
                        bowlerStats.runs.value += 3
                        activeBatsmanStats.runs.value += 3
                        battingTeamStats.inningScore.value += 3
                    }
                    "4" -> {
                        bowlerStats.runs.value += 4
                        activeBatsmanStats.runs.value += 4
                        activeBatsmanStats.fours.value += 1
                        battingTeamStats.inningScore.value += 4
                    }
                    "6" -> {
                        bowlerStats.runs.value += 6
                        activeBatsmanStats.runs.value += 6
                        activeBatsmanStats.sixes.value += 1
                        battingTeamStats.inningScore.value += 6
                    }
                    "W" -> {
                        bowlerStats.runs.value += 1
                        battingTeamStats.inningScore.value += 1
                    }
                    "NB" -> {
                        bowlerStats.runs.value += 1
                        battingTeamStats.inningScore.value += 1
                    }
                    "B" -> {
                        bowlerStats.runs.value += 1
                        battingTeamStats.inningScore.value += 1
                    }
                    "LB" -> {
                        bowlerStats.runs.value += 1
                        battingTeamStats.inningScore.value += 1
                    }
                    "WI" -> {
                        bowlerStats.wickets.value += 1
                        battingTeamStats.inningWickets.value += 1
                        battingTeamStats.inningScore.value -= 3
                    }
                }
                break
            }
        }
        if (!overCompleted && balls[0] == "0" && balls[1] == "0" && balls[2] == "0" && balls[3] == "0" && balls[4] == "0" && balls[5] == "0"){
            bowlerStats.maiden.value += 1
        }
    }
}