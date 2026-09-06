package com.example.cricketscoringapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Define custom colors based on your app's palette
data class AppColors(
    val primaryDark: Color = Color(10, 18, 32),
    val primaryCream: Color = Color(255, 252, 228),
    val surfaceCream: Color = Color(255, 252, 228),
    val textOnDark: Color = Color(255, 252, 228),
    val textOnLight: Color = Color(10, 18, 32),
    val secondaryGray: Color = Color.Gray,
    val accentGreen: Color = Color(19, 207, 69)
)

// 2. Define adaptive dimensions (Tablet vs Phone)
data class AppDimensions(
    val headerSize: TextUnit,     
    val titleSize: TextUnit,      
    val bodySize: TextUnit,       
    val smallSize: TextUnit,      
    val microSize: TextUnit,      
    val listTextSize: TextUnit,   
    val matchListSize: TextUnit,
    val buttonTextSize: TextUnit, 
    val iconSize: Dp,             
    val imageSizeLarge: Dp,       
    val circleButtonSize: Int,    
    val circleButtonSizeSmall: Int, 
    val circleButtonDiameter: Dp, 
    val rowHeight: Dp             
)

val PhoneDimensions = AppDimensions(
    headerSize = 22.sp,
    titleSize = 20.sp,
    bodySize = 16.sp,
    smallSize = 14.sp,
    microSize = 10.sp,
    listTextSize = 10.sp,
    matchListSize = 20.sp, // Increased from 16.sp
    buttonTextSize = 16.sp,
    iconSize = 24.dp,
    imageSizeLarge = 300.dp,
    circleButtonSize = 40,
    circleButtonSizeSmall = 16,
    circleButtonDiameter = 80.dp,
    rowHeight = 30.dp
)

val TabletDimensions = AppDimensions(
    headerSize = 36.sp,
    titleSize = 26.sp,  
    bodySize = 26.sp,
    smallSize = 22.sp,
    microSize = 16.sp,
    listTextSize = 20.sp, 
    matchListSize = 26.sp, // Increased from 26.sp
    buttonTextSize = 22.sp,
    iconSize = 48.dp,
    imageSizeLarge = 400.dp,
    circleButtonSize = 50,
    circleButtonSizeSmall = 26,
    circleButtonDiameter = 120.dp,
    rowHeight = 44.dp
)

// 3. Create the Providers
internal val LocalAppColors = staticCompositionLocalOf { AppColors() }
internal val LocalAppDimensions = staticCompositionLocalOf { PhoneDimensions }

// 4. Object for easy access in Composable code
object CricketAppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val dimens: AppDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalAppDimensions.current
}

@Composable
fun ProvideCricketAppDesignSystem(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val dimensions = if (configuration.screenWidthDp >= 600) TabletDimensions else PhoneDimensions
    
    CompositionLocalProvider(
        LocalAppColors provides AppColors(),
        LocalAppDimensions provides dimensions,
        content = content
    )
}
