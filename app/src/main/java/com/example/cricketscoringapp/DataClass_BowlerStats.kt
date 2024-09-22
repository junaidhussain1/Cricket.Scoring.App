package com.example.cricketscoringapp

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

data class BowlerStats(
    var name: MutableState<String> = mutableStateOf(""),
    var over: MutableState<Double> = mutableDoubleStateOf(0.0),
    var maiden: MutableState<Int> = mutableIntStateOf(0),
    var runs: MutableState<Int> = mutableIntStateOf(0),
    var wickets: MutableState<Int> = mutableIntStateOf(0),
    var noballs: MutableState<Int> = mutableIntStateOf(0),
    var wides: MutableState<Int> = mutableIntStateOf(0),
    var byes: MutableState<Int> = mutableIntStateOf(0),
    var legbyes: MutableState<Int> = mutableIntStateOf(0),
    var fours: MutableState<Int> = mutableIntStateOf(0),
    var sixes: MutableState<Int> = mutableIntStateOf(0),
    var keepername: MutableState<String> = mutableStateOf(""),
    var overrecord: MutableState<String> = mutableStateOf("")
)