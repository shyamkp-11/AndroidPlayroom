plugins {
    alias(libs.plugins.shyampatel.android.application)
    alias(libs.plugins.shyampatel.android.application.compose)
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
    }
    namespace = "com.shyampatel.geofenceplayroom"
}

dependencies {

    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.ui)
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