// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id 'com.android.application'
    id 'kotlin-android'
}

android {
    compileSdkVersion 33

    defaultConfig {
        applicationId "com.example.yourapp"
        minSdkVersion 21
        targetSdkVersion 33
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    implementation 'com.google.android.gms:play-services-auth:20.3.0'
    implementation 'com.google.api-client:google-api-client-android:1.33.0'
    implementation 'com.google.apis:google-api-services-sheets:v4-rev614-1.25.0'
    implementation 'com.squareup.okhttp3:okhttp:4.9.3'

    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    implementation 'androidx.core:core-ktx:1.10.1'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.navigation:navigation-fragment-ktx:2.6.0'
    implementation 'androidx.navigation:navigation-ui-ktx:2.6.0'
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
