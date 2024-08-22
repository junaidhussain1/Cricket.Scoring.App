//package com.example.cricketscoringapp
//
//import retrofit2.Retrofit
//import retrofit2.converter.moshi.MoshiConverterFactory
//import retrofit2.http.GET
//import retrofit2.http.Path
//import retrofit2.http.Query
//
//interface GoogleSheetsService {
//    @GET("v4/spreadsheets/{spreadsheetId}/values/{range}")
//    suspend fun getSheetData(
//        @Path("spreadsheetId") spreadsheetId: String,
//        @Path("range") range: String,
//        @Query("key") apiKey: String
//    ): GoogleSheetResponse
//}
//
//object RetrofitInstance {
//    val retrofit: Retrofit = Retrofit.Builder()
//        .baseUrl("https://sheets.googleapis.com/")
//        .addConverterFactory(MoshiConverterFactory.create())
//        .build()
//
//    val googleSheetsService: GoogleSheetsService = retrofit.create(GoogleSheetsService::class.java)
//}
