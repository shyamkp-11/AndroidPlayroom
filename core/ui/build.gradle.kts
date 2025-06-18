plugins {
    alias(libs.plugins.shyampatel.android.library)
    alias(libs.plugins.shyampatel.android.library.compose)
}

android {
    namespace = "com.shyampatel.ui"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    implementation(projects.core.data)
    api(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.iconsExtended)
    api(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    api(platform(libs.koin.bom))
    api(libs.koin.android)
    api(libs.koin.compose)
    api(libs.accompanist.permissions)
}