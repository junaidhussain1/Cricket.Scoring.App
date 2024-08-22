package com.example.cricketscoringapp

import com.squareup.moshi.Json

data class GoogleSheetResponse(
    @Json(name = "range") val range: String,
    @Json(name = "values") val values: List<List<String>>
)
