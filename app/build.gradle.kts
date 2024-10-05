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
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains:annotations:23.0.0")
            exclude(group = "com.intellij", module = "annotations")
        }
    }

}

dependencies {
    implementation(libs.google.api.client)
    implementation(libs.google.oauth.client.jetty.v1321)
    implementation(libs.google.api.services.sheets.vv4rev6141250)
    implementation(libs.google.api.client.v1315)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.compiler)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.security.crypto.ktx)
    //implementation(libs.androidx.room.compiler)
    //implementation(libs.identity.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Additional dependencies
    //implementation("com.google.api-client:google-api-client-android:2.6.0")
    //implementation("com.google.oauth-client:google-oauth-client:1.36.0")
    //implementation("com.google.oauth-client:google-oauth-client-java6:1.36.0")
    //implementation("com.google.oauth-client:google-oauth-client-jetty:1.36.0")
    //implementation("com.google.api-client:google-api-client-android:2.6.0")

    implementation(libs.google.api.services.tasks)
    implementation(libs.google.api.client.android.v1230)
    implementation(libs.google.http.client.gson)
    //implementation(libs.play.services.identity)
    implementation(libs.google.api.client.v1321)
    implementation(libs.google.oauth.client.jetty.v1321)
    implementation(libs.google.api.services.sheets)
    implementation(libs.androidx.material.icons.extended)
    implementation("com.google.android.gms:play-services-auth:20.5.0")
    implementation(libs.google.oauth.client.jetty.v1315)
    implementation(libs.google.api.services.sheets.vv4rev6141250)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.google.http.client.jackson2)
    implementation(libs.google.api.client)
    implementation(libs.google.oauth.client.jetty.v1321)
    implementation(libs.google.api.services.sheets.vv4rev6141250)
}