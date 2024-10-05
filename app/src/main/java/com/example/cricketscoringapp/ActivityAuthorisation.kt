package com.example.cricketscoringapp

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class AuthorizationActivity : Activity() {

    private lateinit var authCodeInput: EditText
    private lateinit var submitButton: Button

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a linear layout to hold the views
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Create the TextView for instructions
        val instructionsText = TextView(this).apply {
            text = "Enter Authorization Code"
            textSize = 18f
            gravity = Gravity.CENTER
        }

        // Create the EditText for authorization code input
        authCodeInput = EditText(this).apply {
            hint = "Authorization Code"
            setPadding(16, 16, 16, 16)
        }

        // Create the submit Button
        submitButton = Button(this).apply {
            text = "Submit"
            setOnClickListener {
                val authorizationCode = authCodeInput.text.toString().trim()

                if (authorizationCode.isNotEmpty()) {
                    // Pass the authorization code back to the calling activity
                    submitAuthorizationCode(authorizationCode)
                } else {
                    Toast.makeText(this@AuthorizationActivity, "Authorization code cannot be empty.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Add views to the layout
        layout.addView(instructionsText)
        layout.addView(authCodeInput)
        layout.addView(submitButton)

        // Set the dynamically created layout as the content view
        setContentView(layout)
    }

    private fun submitAuthorizationCode(authorizationCode: String) {
        // You can pass the authorization code back to your GoogleSheetsService
        val intent = intent
        intent.putExtra("AUTH_CODE", authorizationCode)
        setResult(Activity.RESULT_OK, intent)
        finish() // Close the activity and return the result
    }
}
