package com.example.cricketscoringapp

import androidx.compose.runtime.MutableState

data class BowlerStats(
    var name: MutableState<String>,
    var over: MutableState<Double>,
    var maiden: MutableState<Int>,
    var runs: MutableState<Int>,
    var wickets: MutableState<Int>
)