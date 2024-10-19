package com.example.cricketscoringapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class GoogleSheetsService {
    private val applicationName = "Cricket Scoring App"
    private val scopes = listOf(SheetsScopes.SPREADSHEETS)
    //private val SPREADSHEET_ID = "1z62HTf3OvhDLYrqVFwEKWoKY242Ja6yZpAG33S_XCmA"
    private val spreadsheetId = "1hoqVNgiQz6e2lkhZV_3ZizhFTlpoMa8QBLFXyhWR7c0" //JH Test Sheet

    private var authorizationCodeFlow: GoogleAuthorizationCodeFlow? = null
    private var redirectUri = "urn:ietf:wg:oauth:2.0:oob"

    // Initialize the HTTP transport and JSON factory
    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private val gsonFactory = GsonFactory.getDefaultInstance()

    // Function to handle the OAuth flow and request an authorization code
    fun authorize(context: Context) {
        // Load OAuth2 credentials file from res/raw (or wherever it’s located)
        val inputStream = context.resources.openRawResource(R.raw.clientsecret)  // Replace with your correct file
        val clientSecrets = GoogleClientSecrets.load(gsonFactory, InputStreamReader(inputStream))

        // Use FileDataStoreFactory to store OAuth tokens in the app's private directory
        val dataStoreDir = context.filesDir
        val dataStoreFactory = FileDataStoreFactory(dataStoreDir)

        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport, gsonFactory, clientSecrets, scopes
        )
            .setDataStoreFactory(dataStoreFactory)
            .setAccessType("offline")
            .build()

        authorizationCodeFlow = flow

        // Generate the authorization URL
        val authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri).build()

        // Open the browser to prompt the user to authorize the app
        openBrowserWithAuthorizationUrl(context,authorizationUrl)

        // Show the dialog for the user to input the authorization code
        showAuthorizationCodeInputDialog(context)
    }

    // Function to open the browser with the OAuth URL
    private fun openBrowserWithAuthorizationUrl(context: Context, authorizationUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(authorizationUrl)
        context.startActivity(intent)
    }

    // Function to show the input dialog for the authorization code
    private fun showAuthorizationCodeInputDialog(context: Context) {
        val input = EditText(context)  // Create the input field programmatically

        val dialog = AlertDialog.Builder(context)
            .setTitle("Enter Authorization Code")
            .setMessage("Please paste the authorization code from the browser here.")
            .setView(input)  // Set the input field in the dialog
            .setPositiveButton("Submit") { _, _ ->
                val authorizationCode = input.text.toString().trim()
                if (authorizationCode.isNotEmpty()) {
                    // Process the authorization code
                    exchangeAuthorizationCodeForTokens(context,authorizationCode)
                } else {
                    Toast.makeText(context, "Authorization code cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()  // Show the dialog
    }

    fun exchangeAuthorizationCodeForTokens(context: Context,authorizationCode: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use withContext(Dispatchers.IO) to run network operation
                val inputStream = context.resources.openRawResource(R.raw.clientsecret)  // Replace with your correct file
                val clientSecrets = GoogleClientSecrets.load(gsonFactory, InputStreamReader(inputStream))

                // Use FileDataStoreFactory to store OAuth tokens in the app's private directory
                val dataStoreDir = context.filesDir
                val dataStoreFactory = FileDataStoreFactory(dataStoreDir)

                val flow = GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, gsonFactory, clientSecrets, scopes
                )
                    .setDataStoreFactory(dataStoreFactory)
                    .setAccessType("offline")
                    .build()

                authorizationCodeFlow = flow

                // Perform the token request in a background thread
                val tokenResponse = flow.newTokenRequest(authorizationCode)
                    .setRedirectUri(redirectUri)
                    .execute()

                // Store the credential for future use
                flow.createAndStoreCredential(tokenResponse, "user")

                // Switch back to the main thread to update the UI (Toast)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Authorization Successful!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Switch back to the main thread to show error toast
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error exchanging authorization code: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun getSheetsService(context: Context): Sheets? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.resources.openRawResource(R.raw.clientsecret)  // Replace with your correct file
            val clientSecrets = GoogleClientSecrets.load(gsonFactory, InputStreamReader(inputStream))

            // Use FileDataStoreFactory to store OAuth tokens in the app's private directory
            val dataStoreDir = context.filesDir
            val dataStoreFactory = FileDataStoreFactory(dataStoreDir)

            val flow = GoogleAuthorizationCodeFlow.Builder(
                httpTransport, gsonFactory, clientSecrets, scopes
            )
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .build()

            authorizationCodeFlow = flow

            // Load credentials (ensure authorizationCodeFlow and context are initialized correctly)
            val credentials = authorizationCodeFlow?.let {
                val loadedCredential = it.loadCredential("user")
                if (loadedCredential == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No credentials found for the user", Toast.LENGTH_LONG).show()
                    }
                    return@withContext null
                }
                loadedCredential
            } ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "authorizationCodeFlow is null", Toast.LENGTH_LONG).show()
                }
                return@withContext null
            }

            // Create and return Sheets service instance
            return@withContext Sheets.Builder(httpTransport, gsonFactory, credentials)
                .setApplicationName(applicationName)
                .build()

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to initialize Sheets service: ${e.message}", Toast.LENGTH_LONG).show()
            }
            return@withContext null
        }
    }

    // Function to read data from Google Sheets
    suspend fun readData(context: Context,readRange: String): List<List<Any>> = withContext(Dispatchers.IO) {
        val sheetsService = getSheetsService(context) ?: return@withContext listOf()

        try {
            // Fetch data from the specified range in the spreadsheet
            val response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, readRange)
                .execute()

            // Return the fetched data
            return@withContext response.getValues() ?: listOf()
        } catch (e: Exception) {
            // Handle exceptions (e.g., network errors, API failures)
            e.printStackTrace()
            return@withContext listOf()  // Return an empty list in case of error
        }
    }

    // Function to write data to Google Sheets using coroutines
    suspend fun writeData(context: Context, writeRange: String, startRow:Int, endRow: Int, values: List<List<Any>>): String = withContext(Dispatchers.IO) {
        val sheetsService = getSheetsService(context) ?: return@withContext "Service initialization failed."

        try {

            val rangeForBC = "Data Raw!C$startRow:F$endRow"  // Adjust this to the correct range for columns B and C
            val existingDataResponse = sheetsService.spreadsheets().values().get(spreadsheetId, rangeForBC).execute()
            val existingDataBC = existingDataResponse.getValues() ?: return@withContext "Failed to retrieve existing data."

            val mergedData = values.mapIndexed { index, newRow ->
                val existingRowBC = existingDataBC.getOrNull(index) ?: listOf(null, null, null, null) // Ensure it has 4 elements
                newRow.take(2) + existingRowBC.take(4) + newRow.drop(6)
            }

            // Prepare the data to write
            val body = ValueRange().setValues(mergedData)

            // Update the specified range with the new data
            val response = sheetsService.spreadsheets().values()
                .update(spreadsheetId, writeRange, body)
                .setValueInputOption("RAW")
                .execute()

            // Check if the response contains updated values
            if (response != null) {
                val updatedRows = response.updatedRows
                val updatedColumns = response.updatedColumns
                val updatedCells = response.updatedCells

                // Return success information
                return@withContext "Update successful: $updatedRows rows, $updatedColumns columns, $updatedCells cells updated."
            } else {
                return@withContext "Update failed: No response from server."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Update failed: ${e.message}"
        }
    }
}