package com.example.cricketscoringapp

data class MatchState(
    val battingStats: List<Map<String, Any?>>,
    val bowlingStats: List<Map<String, Any?>>
)