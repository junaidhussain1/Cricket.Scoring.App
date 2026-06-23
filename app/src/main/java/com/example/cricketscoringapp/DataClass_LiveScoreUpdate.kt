package com.example.cricketscoringapp

import androidx.compose.runtime.MutableState

data class LiveScoreUpdate(
    val matchId: String = "",
    val battingTeam: String = "",
    val bowlingTeam: String = "",
    val innings1Runs: Int = 0,
    val innings1Wickets: Int = 0,
    val innings1Overs: String = "",
    val innings2Runs: Int = 0,
    val innings2Wickets: Int = 0,
    val innings2Overs: String = "",
    val currentOver: String = "",
    val runsToWin: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val status: String = "live",
    val strikerName: String = "",
    val strikerRuns: Int = 0,
    val strikerBalls: Int = 0,
    val striker4s: Int = 0,
    val striker6s: Int = 0,
    val nonStrikerName: String = "",
    val nonStrikerRuns: Int = 0,
    val nonStrikerBalls: Int = 0,
    val nonStriker4s: Int = 0,
    val nonStriker6s: Int = 0,
    val bowlerName: String = "",
    val bowlerOvers: String = "",
    val bowlerRuns: Int = 0,
    val bowlerWickets: Int = 0,
    val partnershipRuns: Int = 0,
    val partnershipBalls: Int = 0
)