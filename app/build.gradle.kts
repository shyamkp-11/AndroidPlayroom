plugins {
    alias(libs.plugins.shyampatel.android.application)
    alias(libs.plugins.shyampatel.android.application.compose)
}
android {

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.shyampatel.githubplayroom"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    namespace =  "com.shyampatel.githubplayroom"
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
}