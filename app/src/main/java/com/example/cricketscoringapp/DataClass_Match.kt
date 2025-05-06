package com.example.cricketscoringapp

data class Match(val matchId: String,
                 val firstBattingTeamCaptain: String,
                 val secondBattingTeamCaptain: String,
                 val winningTeamCaptain: String,
                 val noOfOversAside: Int,
                 val noOfPlayersAside: Int,
                 val isStarted: Boolean,
                 val isFinished: Boolean,
                 val isSynced: Boolean)