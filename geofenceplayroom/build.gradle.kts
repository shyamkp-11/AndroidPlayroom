plugins {
    alias(libs.plugins.shyampatel.android.application)
    alias(libs.plugins.shyampatel.android.application.compose)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    defaultConfig {
        applicationId = "com.shyampatel.geofenceplayroom"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    namespace = "com.shyampatel.geofenceplayroom"
}

dependencies {

    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.ui)
    implementation(libs.maps.compose)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation (libs.androidx.core.ktx)
    implementation (libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.koin.compose)
    implementation (libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.coil.kt)
    implementation(libs.coil.kt.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
//    implementation(libs.androidx.lifecycle.runtime.ktx)
//    androidTestImplementation(libs.androidx.ui.test.junit4)
}

secrets {
    // To add your Maps API key to this project:
    // 1. If the secrets.properties file does not exist, create it in the same folder as the local.properties file.
    // 2. Add this line, where YOUR_API_KEY is your API key:
    //        MAPS_API_KEY=YOUR_API_KEY
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default.
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}