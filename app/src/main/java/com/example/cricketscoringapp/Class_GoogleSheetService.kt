package com.example.cricketscoringapp

import android.content.Context
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import java.io.File
import java.io.InputStreamReader

class GoogleSheetsService(private val context: Context) {
    private val APPLICATION_NAME = "Cricket Scoring App"
    private val SCOPES = listOf(SheetsScopes.SPREADSHEETS)
    private val SPREADSHEET_ID = "1z62HTf3OvhDLYrqVFwEKWoKY242Ja6yZpAG33S_XCmA"
    private val RANGE = "Sheet1!A1:A1"
    private val AUTH_CODE_REQUEST = 1001

    private var authorizationCodeFlow: GoogleAuthorizationCodeFlow? = null
    private var redirectUri = "urn:ietf:wg:oauth:2.0:oob"

    // Initialize the HTTP transport and JSON factory
    private val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
    private val JSON_FACTORY = JacksonFactory.getDefaultInstance()

    // Function to handle the OAuth flow and request an authorization code
    fun authorize() {
        // Load OAuth2 credentials file from res/raw (or wherever it’s located)
        val inputStream = context.resources.openRawResource(R.raw.clientsecret)  // Replace with your correct file
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))

        // Use FileDataStoreFactory to store OAuth tokens in the app's private directory
        val dataStoreDir = context.filesDir
        val dataStoreFactory = FileDataStoreFactory(dataStoreDir)

        val flow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES
        )
            .setDataStoreFactory(dataStoreFactory)
            .setAccessType("offline")
            .build()

        authorizationCodeFlow = flow

        // Generate the authorization URL
        val authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri).build()

        // Open the browser to prompt the user to authorize the app
        openBrowserWithAuthorizationUrl(authorizationUrl)

        // Show the dialog for the user to input the authorization code
        showAuthorizationCodeInputDialog()
    }

    // Function to open the browser with the OAuth URL
    private fun openBrowserWithAuthorizationUrl(authorizationUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(authorizationUrl)
        context.startActivity(intent)
    }

    // Function to show the input dialog for the authorization code
    private fun showAuthorizationCodeInputDialog() {
        val input = EditText(context)  // Create the input field programmatically

        val dialog = AlertDialog.Builder(context)
            .setTitle("Enter Authorization Code")
            .setMessage("Please paste the authorization code from the browser here.")
            .setView(input)  // Set the input field in the dialog
            .setPositiveButton("Submit") { _, _ ->
                val authorizationCode = input.text.toString().trim()
                if (authorizationCode.isNotEmpty()) {
                    // Process the authorization code
                    exchangeAuthorizationCodeForTokens(authorizationCode)
                } else {
                    Toast.makeText(context, "Authorization code cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()  // Show the dialog
    }

    // Function to exchange the authorization code for OAuth tokens
    fun exchangeAuthorizationCodeForTokens(authorizationCode: String) {
        try {
            val flow = authorizationCodeFlow ?: return
            val tokenResponse = flow.newTokenRequest(authorizationCode)
                .setRedirectUri(redirectUri)
                .execute()

            // Store the credential for future use
            flow.createAndStoreCredential(tokenResponse, "user")
            Toast.makeText(context, "Authorization Successful!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error exchanging authorization code: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Example function to interact with Google Sheets API (replace with actual usage)
    fun getSheetsService(): Sheets? {
        authorize()  // Initiate authorization when needed

        val credentials = authorizationCodeFlow?.loadCredential("user") ?: return null

        return Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    // Function to read data from Google Sheets
    fun readData(): List<List<Any>> {
        val sheetsService = getSheetsService() ?: return listOf()

        // Fetch data from the specified range in the spreadsheet
        val response = sheetsService.spreadsheets().values()
            .get(SPREADSHEET_ID, RANGE)
            .execute()

        // Return the fetched data
        return response.getValues() ?: listOf()
    }

    // Function to write data to Google Sheets
    fun writeData(values: List<List<Any>>) {
        val sheetsService = getSheetsService() ?: return

        // Prepare the data to write
        val body = ValueRange().setValues(values)

        // Update the specified range with the new data
        sheetsService.spreadsheets().values()
            .update(SPREADSHEET_ID, RANGE, body)
            .setValueInputOption("RAW")
            .execute()

        Toast.makeText(context, "Data written to Google Sheets successfully.", Toast.LENGTH_SHORT).show()
    }
}