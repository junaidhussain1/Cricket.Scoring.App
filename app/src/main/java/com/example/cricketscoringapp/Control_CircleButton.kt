package com.example.cricketscoringapp

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CircleButton(
    label: String,
    fontSize: Int,
    onClick: () -> Unit
) {
    // Determine the color based on the label
    val buttonColor = when (label) {
        "WICKET" -> Color.Red // Red color for "WICKET"
        "UNDO" -> Color.Gray  // Gray color for "UNDO"
        else -> MaterialTheme.colorScheme.primary // Default color if label is not recognized
    }

    Button(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(80.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = MaterialTheme.colorScheme.onPrimary // Ensure text color contrasts well with button color
        )
    ) {
        Text(
            text = label,
            fontSize = fontSize.sp,
            color = MaterialTheme.colorScheme.onPrimary, // Ensure text color is suitable for the button color
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)  // Aligns text vertically center
                .wrapContentWidth(Alignment.CenterHorizontally) // Centers text horizontally
        )
    }
}