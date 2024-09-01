plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.cricketscoringapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cricketscoringapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    //implementation(libs.identity.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Retrofit for networking
    //implementation("com.squareup.retrofit2:retrofit:2.9.0")
    //implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    // Coroutine support for networking
//    implementation(libs.kotlinx.coroutines.android)
//
//    implementation(libs.google.api.client.android)
//    implementation(libs.google.api.services.sheets)
//    implementation(libs.google.oauth.client.jetty)
//    implementation(libs.google.auth.library.oauth2.http)
//
//    implementation(libs.gradle)
//
//    implementation(libs.androidx.core.ktx.v1100)

    // Google APIs Client Library for Java
    //implementation(libs.google.api.client)

    // OAuth2 for handling authentication and authorization
    //implementation(libs.google.oauth.client.jetty)

    // Google Sheets API specific library

    //implementation(libs.google.api.services.sheets)
    //implementation(libs.google.api.services.sheet)

    implementation("androidx.compose.material:material:1.6.8")

    implementation("com.google.api-client:google-api-client:2.6.0")
    implementation("com.google.api-client:google-api-client-android:2.6.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.36.0")
    implementation("com.google.http-client:google-http-client-gson:1.44.2")
    implementation(libs.google.api.services.sheets)


//      implementation("com.google.api-client:google-api-client-android:2.6.0")
//      implementation(libs.google.api.services.sheets)
      implementation("com.google.auth:google-auth-library-oauth2-http:1.24.1")

    // Optional, for easier JSON handling if needed
    implementation(libs.gson)

}