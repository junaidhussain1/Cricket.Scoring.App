package com.example.cricketscoringapp

data class BallEvent(
    val over: String,
    val bowler: String,
    val batsman: String,
    val result: String,
    val resultText: String,
    val isOverSummary: Boolean = false,
    val overRuns: Int = 0,
    val overExtras: Int = 0,
    val totalScore: String = ""
)