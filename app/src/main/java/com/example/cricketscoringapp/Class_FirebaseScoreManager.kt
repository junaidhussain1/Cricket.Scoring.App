package com.example.cricketscoringapp

import android.content.Context
import com.google.firebase.database.FirebaseDatabase

class FirebaseScoreManager private constructor(private val context: Context) {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val scoresRef = database.getReference("matches")

    fun pushLiveScore(matchId: String, scoreUpdate: LiveScoreUpdate) {
        scoresRef
            .child(matchId)
            .child("liveScore")
            .setValue(scoreUpdate)
    }

    fun pushMatchComplete(matchId: String) {
        scoresRef
            .child(matchId)
            .child("status")
            .setValue("complete")
    }

    companion object {
        @Volatile
        private var instance: FirebaseScoreManager? = null

        fun getInstance(context: Context): FirebaseScoreManager {
            return instance ?: synchronized(this) {
                instance ?: FirebaseScoreManager(context).also { instance = it }
            }
        }
    }
}