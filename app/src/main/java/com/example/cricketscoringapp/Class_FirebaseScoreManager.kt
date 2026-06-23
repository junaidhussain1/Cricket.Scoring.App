package com.example.cricketscoringapp

import android.content.Context
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class FirebaseScoreManager private constructor(private val context: Context) {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val scoresRef = database.getReference("matches")

//    fun pushLiveScore(matchId: String, scoreUpdate: LiveScoreUpdate) {
//        scoresRef
//            .child(matchId)
//            .child("liveScore")
//            .setValue(scoreUpdate)
//            .addOnSuccessListener {
//                Log.d("Firebase", "Score updated successfully for match: $matchId")
//            }
//            .addOnFailureListener { error ->
//                Log.e("Firebase", "Failed to update score: ${error.message}", error)
//            }
//    }
    fun pushLiveScore(matchId: String, scoreUpdate: LiveScoreUpdate, context: Context, retryCount: Int = 0) {
        scoresRef
            .child(matchId)
            .child("liveScore")
            .setValue(scoreUpdate)
            .addOnSuccessListener {
                Log.d("Firebase", "Score updated successfully for match: $matchId")
            }
            .addOnFailureListener { error ->
                Log.e("Firebase", "Failed to update score: ${error.message}", error)

                if (retryCount < 3) {
                    Log.d("Firebase", "Retrying... attempt ${retryCount + 1}")
                    Handler(Looper.getMainLooper()).postDelayed({
                        pushLiveScore(matchId, scoreUpdate, context, retryCount + 1)
                    }, 2000)
                } else {
                    // All retries exhausted - show Toast on main thread
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            error.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Log.e("Firebase", "All retries failed for match: $matchId")
                }
            }
    }

    fun pushMatchComplete(matchId: String) {
        scoresRef
            .child(matchId)
            .child("status")
            .setValue("complete")
    }

    fun swapBatsmen(matchId: String) {
        val scoresRef = database.getReference("matches")
        val liveScoreRef = scoresRef.child(matchId).child("liveScore")

        // First read the current values
        liveScoreRef.get().addOnSuccessListener { snapshot ->
            val currentStrikerName      = snapshot.child("strikerName").getValue(String::class.java) ?: ""
            val currentStrikerRuns      = snapshot.child("strikerRuns").getValue(Int::class.java) ?: 0
            val currentStrikerBalls     = snapshot.child("strikerBalls").getValue(Int::class.java) ?: 0
            val currentStriker4s        = snapshot.child("striker4s").getValue(Int::class.java) ?: 0
            val currentStriker6s        = snapshot.child("striker6s").getValue(Int::class.java) ?: 0

            val currentNonStrikerName   = snapshot.child("nonStrikerName").getValue(String::class.java) ?: ""
            val currentNonStrikerRuns   = snapshot.child("nonStrikerRuns").getValue(Int::class.java) ?: 0
            val currentNonStrikerBalls  = snapshot.child("nonStrikerBalls").getValue(Int::class.java) ?: 0
            val currentNonStriker4s     = snapshot.child("nonStriker4s").getValue(Int::class.java) ?: 0
            val currentNonStriker6s     = snapshot.child("nonStriker6s").getValue(Int::class.java) ?: 0

            // Swap them
            val updates = hashMapOf<String, Any>(
                "strikerName"      to currentNonStrikerName,
                "strikerRuns"      to currentNonStrikerRuns,
                "strikerBalls"     to currentNonStrikerBalls,
                "striker4s"        to currentNonStriker4s,
                "striker6s"        to currentNonStriker6s,
                "nonStrikerName"   to currentStrikerName,
                "nonStrikerRuns"   to currentStrikerRuns,
                "nonStrikerBalls"  to currentStrikerBalls,
                "nonStriker4s"     to currentStriker4s,
                "nonStriker6s"     to currentStriker6s
            )

            liveScoreRef.updateChildren(updates)
        }
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

