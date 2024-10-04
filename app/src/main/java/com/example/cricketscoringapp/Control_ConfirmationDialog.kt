package com.example.cricketscoringapp

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.sp

@Composable
fun ConfirmationDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    AlertDialog(
        onDismissRequest = { onDismiss() },
        text = {
            Text(text = message, fontSize = if (isTablet) 30.sp else 20.sp)
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("Confirm", fontSize = if (isTablet) 30.sp else 20.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel", fontSize = if (isTablet) 30.sp else 20.sp)
            }
        }
    )
}
