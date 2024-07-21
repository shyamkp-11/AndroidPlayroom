plugins {
    alias(libs.plugins.shyampatel.android.library)
    id("kotlinx-serialization")
}

android {
    namespace = "com.shyampatel.core.network"
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField(
            "String",
            "CLIENT_ID",
            "\"${project.findProperty("CLIENT_ID")}\""
        )
        buildConfigField(
            "String",
            "CLIENT_SECRET",
            "\"${project.findProperty("CLIENT_SECRET")}\""
        )
        buildConfigField(
            "String",
            "APP_NAME",
            "\"${project.findProperty("APP_NAME")}\""
        )
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
}

dependencies {
    implementation(projects.core.common)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation (libs.androidx.core.ktx)
    implementation(libs.retrofit.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.coil.kt)
    implementation(libs.coil.kt.svg)
}