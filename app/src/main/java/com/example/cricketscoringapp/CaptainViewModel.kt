package com.example.cricketscoringapp

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class CaptainViewModel : ViewModel() {

    // MutableStateList to hold captain names
    val captains = mutableStateListOf<String>()

    // Function to add captains to the list
    fun addCaptain(captain: String) {
        captains.add(captain)
    }

    // Function to remove a captain from the list
    fun removeCaptain(captain: String) {
        captains.remove(captain)
    }

    // Function to clear captains (optional)
    fun clearCaptains() {
        captains.clear()
    }
}
