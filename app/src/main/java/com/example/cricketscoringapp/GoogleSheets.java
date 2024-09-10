package com.example.cricketscoringapp;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GoogleSheets extends AppCompatActivity {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    private ActivityResultLauncher<Intent> accountPickerLauncher;
    private CompletableFuture<GoogleAccountCredential> credentialFuture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ActivityResultLauncher
        accountPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                if (accountName != null) {
                    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, SCOPES);
                    credential.setSelectedAccountName(accountName);
                    credentialFuture.complete(credential); // Complete the future with the selected credential

                    // Call the Google Sheets API after credentials are obtained
                    String resultMessage = doCallGoogleSheetAPI();
                    // You can handle the result here, for example, by displaying it in the UI
                    System.out.println(resultMessage);  // Replace with appropriate UI handling code
                } else {
                    credentialFuture.completeExceptionally(new Exception("No account selected"));
                }
            } else {
                credentialFuture.completeExceptionally(new Exception("Account selection failed"));
            }
        });

        // Start the process to get credentials
        getCredentials();
    }

    private void getCredentials() {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, SCOPES);
        Intent signInIntent = credential.newChooseAccountIntent();
        credentialFuture = new CompletableFuture<>();
        accountPickerLauncher.launch(signInIntent);
    }

    public String doCallGoogleSheetAPI() {
        try {
            final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport(); // Use AndroidHttp for Android-specific transport
            final String spreadsheetId = "196kJAI0SoRTozes3IafUYXuRh-SncXEsOPEoJ-SExrY";
            final String range = "Test!A1";

            // Get the GoogleAccountCredential for authorization
            GoogleAccountCredential credential = credentialFuture.get(); // Wait for the result

            // Build the Sheets service with the Android GoogleAccountCredential
            Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Make the API call
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                return "No data found.";
            } else {
                for (List<Object> row : values) {
                    return row.get(0).toString();  // Returning the first value found in the row
                }
            }
        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException e) {
            // Catch and return any exceptions
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            // Catch any other potential runtime exceptions (like NullPointerException)
            return "Error: " + e.toString();
        }
        return "Unknown error occurred.";
    }
}
