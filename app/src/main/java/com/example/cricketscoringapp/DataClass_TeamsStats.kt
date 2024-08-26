package com.example.cricketscoringapp

import androidx.compose.runtime.MutableState

data class TeamStats(
    var name: MutableState<String>,
    var overs: MutableState<Double>,
    var inningScore: MutableState<Int>,
    var inningWickets: MutableState<Int>,
    var active: MutableState<Boolean>
)