package com.example.cricketscoringapp

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun GoogleSheetSettingsPage() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    var authCode by remember { mutableStateOf("") }
    var newData by remember { mutableStateOf("") }
    val googleSheetsService = GoogleSheetsService()
    val dbHelper = CricketDatabaseHelper(context)

    // Check screen size and adjust the layout accordingly
    val isTablet = configuration.screenWidthDp >= 600

    Surface(
        modifier = Modifier.fillMaxSize(), // This makes the Surface fill the entire screen
        color = Color(10, 18, 32)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isTablet) 32.dp else 16.dp), // Increase padding for tablets
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "The Gotham City Scoresheet App",
                fontSize = if (isTablet) 32.sp else 20.sp,// Larger font size for tablets
                color = Color(255, 252, 228)
            )

            Image(
                painter = painterResource(id = R.drawable.updatedimage), // Replace with your app icon resource
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(if (isTablet) 400.dp else 300.dp) // Larger image size for tablets
                    .padding(8.dp)
            )

            // Button to trigger Google login
            Button(
                onClick = { googleSheetsService.authorize(context) },
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f), // Adjust button width for tablets
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(255, 252, 228) // Set the background color
                )
            ) {
                Text("Login with Google",
                    fontSize = if (isTablet) 26.sp else 22.sp,
                    color = Color(10, 18, 32)// Larger font size for tablets
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for the authentication code
            OutlinedTextField(
                value = authCode,
                onValueChange = { authCode = it },
                label = { Text("Enter Authorization Code") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Button to submit the authentication code
            Button(
                onClick = {
                    if (authCode.isNotEmpty()) {
                        //Toast.makeText(context, "Auth Code Submitted: $authCode", Toast.LENGTH_SHORT).show()

                        // Exchange the auth code for tokens and navigate to the main page
                        googleSheetsService.exchangeAuthorizationCodeForTokens(context,authCode)

                    } else {
                        Toast.makeText(context, "Authorization code cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f), // Adjust button width for tablets
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(255, 252, 228)
                )
            ) {
                Text("Submit Authorization Code",
                    fontSize = if (isTablet) 26.sp else 22.sp,
                    color = Color(10, 18, 32))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        // Read data asynchronously from Google Sheets
                        val data = googleSheetsService.readData(context)

                        // After fetching the data, show a Toast message with the size of the list
                        val listSize = data.size
                        val values = data.joinToString(separator = "\n") { it.joinToString(", ") }

                        Toast.makeText(
                            context, // Use your actual context
                            "List size: $listSize Values: $values",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f), // Adjust button width for tablets
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(255, 252, 228) // Light color for button
                )
            ) {
                Text(
                    text = "Get Data from Google Sheet",
                    fontSize = if (isTablet) 26.sp else 22.sp, // Adjust font size for tablets
                    color = Color(10, 18, 32) // Dark text color
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for the authentication code
            OutlinedTextField(
                value = newData,
                onValueChange = { newData = it },
                label = { Text("Data to add to sheet") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Button to submit the authentication code
            Button(
                onClick = {
                    if (newData.isNotEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                val dataToWrite = listOf(
                                    listOf<Any>(newData)
                                )
                                val rtnMessage = googleSheetsService.writeData(context,dataToWrite)

                                // Switch back to the Main thread to show Toast
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        rtnMessage,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f), // Adjust button width for tablets
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(255, 252, 228) // Light color for button
                )
            ) {
                Text("Add Data",
                    fontSize = if (isTablet) 26.sp else 22.sp,
                    color = Color(10, 18, 32))
            }
        }
    }
}

