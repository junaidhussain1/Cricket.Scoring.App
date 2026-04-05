package com.example.cricketscoringapp

import android.app.Application
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        // Sign in anonymously
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    Log.d("Firebase", "Signed in anonymously: ${it.user?.uid}")
                }
                .addOnFailureListener { error ->
                    Log.e("Firebase", "Anonymous sign in failed: ${error.message}")
                }
        }
    }
}