package com.example.cricketscoringapp

data class BallEvent(
    val over: String,      // Over number (e.g., "2.3" means 2nd over, 3rd ball)
    val bowler: String,    // Name of the bowler
    val batsman: String,   // Name of the batsman who faced the ball
    val result: String     // Result of the ball (runs, dot, wicket, extra, etc.)
)