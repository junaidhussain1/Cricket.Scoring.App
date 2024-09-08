package com.example.cricketscoringapp

//import com.google.api.client.extensions.android.http.AndroidHttp
import android.content.Context
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import java.io.File
import java.io.InputStreamReader
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily


@Composable
fun HomePage(navController: NavHostController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    // Check screen size and adjust the layout accordingly
    val isTablet = configuration.screenWidthDp >= 600

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
            painter = painterResource(id = R.drawable.designer), // Replace with your app icon resource
            contentDescription = "App Icon",
            modifier = Modifier
                .size(if (isTablet) 400.dp else 300.dp) // Larger image size for tablets
                .padding(8.dp)
        )

        Button(
            onClick = { navController.navigate("playermgt") },
            modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f), // Adjust button width for tablets
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(255, 252, 228) // Set the background color
                    )
        ) {
            Text(
                text = "Player Management",
                fontSize = if (isTablet) 26.sp else 22.sp,
                color = Color(8, 20, 28)// Larger font size for tablets
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { navController.navigate("newmatch") },
            modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(255, 252, 228) // Set the background color
            )
        ) {
            Text(
                text = "New Match",
                fontSize = if (isTablet) 26.sp else 22.sp,
                color = Color(8, 20, 28)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { navController.navigate("existingmatches") },
            modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(255, 252, 228) // Set the background color
            )
        ) {
            Text(
                text = "Existing Matches",
                fontSize = if (isTablet) 26.sp else 22.sp,
                color = Color(8, 20, 28)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                Toast.makeText(context, getDataFromSheet(context), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(255, 252, 228) // Set the background color
            )
        ) {
            Text(
                text = "Google Sheet Test",
                fontSize = if (isTablet) 26.sp else 22.sp,
                color = Color(8, 20, 28)

            )
        }
    }
}


fun getSheetsService(context: Context): Sheets {
    val jsonFactory = GsonFactory.getDefaultInstance()
    val transport =  GoogleNetHttpTransport.newTrustedTransport()

    val credentials = getCredentials(context) // Use the function from the previous setup
        //.createScoped(listOf("https://www.googleapis.com/auth/spreadsheets.readonly"))

    return Sheets.Builder(transport, jsonFactory, credentials)
        .setApplicationName("Your App Name")
        .build()
}

fun getDataFromSheet(context: Context) : String{
    val service = getSheetsService(context)
    val spreadsheetId = "196kJAI0SoRTozes3IafUYXuRh-SncXEsOPEoJ-SExrY"
    val range = "Test!A1"

    val response: ValueRange = service.spreadsheets().values()
        .get(spreadsheetId, range)
        .execute()

    val values = response.getValues()
    if (values != null && values.isNotEmpty()) {
        val cellValue = values[0][0] as String
        return cellValue
    } else {
        return "No data found."
    }
}

/*
fun accessGoogleSheetsAPI(context: Context): Sheets {
    val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
    val credentials = getCredentials(context) // Use the function from the previous setup
    return Sheets.Builder(httpTransport, jsonFactory, credentials)
        .setApplicationName("CricClient")
        .build()
}

fun getCellValue(context: Context): String? {
    // Replace this with your actual spreadsheet ID
    val spreadsheetId = "196kJAI0SoRTozes3IafUYXuRh-SncXEsOPEoJ-SExrY"

    // The range specifies the sheet name and the cell. In this case, Sheet "Test" and cell "A1"
    val range = "Test!A1"  // "Test" is the sheet name, and "A1" is the cell

    // Get the Sheets service instance
    val sheetsService = accessGoogleSheetsAPI(context)

    // Call the Sheets API to retrieve the value from the specified cell
    val response: ValueRange = sheetsService.spreadsheets().values()
        .get(spreadsheetId, range)
        .execute()

    // Get the value from the response
    val values = response.getValues()

    // Check if there is any value in the cell
    return if (values != null && values.isNotEmpty() && values[0].isNotEmpty()) {
        values[0][0].toString() // The value in cell A1
    } else {
        null // No value found in the cell
    }
}
 */

// Get OAuth 2.0 credentials for accessing Google Sheets
fun getCredentials(context: Context): Credential {
    // Define necessary constants
    val CREDENTIALS_FILE_PATH = "clientsecret.json"  // Replace with your JSON file name
    val TOKENS_DIRECTORY_PATH = "tokens"
    val APPLICATION_NAME = "CricClient"
    val SHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets.readonly"

    val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

    // Load client secrets
    val clientSecrets = loadClientSecrets(context)

    // Create data store directory for storing tokens
    val dataStoreDir = File(context.filesDir, TOKENS_DIRECTORY_PATH)
    val dataStoreFactory = FileDataStoreFactory(dataStoreDir)

    // Set up the authorization flow
    val flow = GoogleAuthorizationCodeFlow.Builder(
        httpTransport, jsonFactory, clientSecrets, listOf(SHEETS_SCOPE)
    )
        .setDataStoreFactory(dataStoreFactory)
        .setAccessType("offline")
        .build()

    // Authorize the user
    return AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
}

// Load client secrets from the raw resource folder
fun loadClientSecrets(context: Context): GoogleClientSecrets {
    val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
    val inputStream = context.resources.openRawResource(R.raw.clientsecret) // Replace with the correct file name
    return GoogleClientSecrets.load(jsonFactory, InputStreamReader(inputStream))
}
