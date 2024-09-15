package com.example.cricketscoringapp

import androidx.compose.runtime.MutableState

data class BatsmanStats(
    var name: MutableState<String>,
    var runs: MutableState<Int>,
    var balls: MutableState<Int>,
    var fours: MutableState<Int>,
    var sixes: MutableState<Int>,
    var active: MutableState<Boolean>

)

