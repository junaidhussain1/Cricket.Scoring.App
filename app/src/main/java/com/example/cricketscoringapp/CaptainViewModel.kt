package com.example.cricketscoringapp

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class CaptainViewModel : ViewModel() {

    // MutableStateList to hold captain names
    val captains = mutableStateListOf<Player>()

    val team1 = mutableStateListOf<Player>()
    val team2 = mutableStateListOf<Player>()

    // Function to add captains to the list
    fun addCaptain(player: Player) {
        captains.add(player)
    }

    // Function to remove a captain from the list
    fun removeCaptain(player: Player) {
        captains.remove(player)
    }

    // Function to clear captains (optional)
    fun clearCaptains() {
        captains.clear()
    }

    fun addTeam1Player(player: Player) {
        team1.add(player)
    }

    fun removeTeam1Player(player: Player) {
        team1.remove(player)
    }

    fun clearTeam1() {
        team1.clear()
    }

    fun addTeam2Player(player: Player) {
        team2.add(player)
    }

    fun removeTeam2Player(player: Player) {
        team2.remove(player)
    }

    fun clearTeam2() {
        team2.clear()
    }
}
