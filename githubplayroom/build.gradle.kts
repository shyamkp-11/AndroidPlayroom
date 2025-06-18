plugins {
    alias(libs.plugins.shyampatel.android.application)
    alias(libs.plugins.shyampatel.android.application.compose)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    alias(libs.plugins.gms.google.services)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.shyampatel.android.application.firebase)
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
    }

    buildTypes {
        all {
            buildConfigField(
                "String",
                "APP_NAME_GITHUBAPP",
                "\"${properties["APP_NAME_GITHUBAPP"]}\""
            )
            buildConfigField(
                "String",
                "GITHUB_BASE_URL",
                "\"${properties["GITHUB_BASE_URL"]}\""
            )
            buildConfigField(
                "String",
                "GITHUB_BASE_URL_GRAPHQL",
                "\"${properties["GITHUB_BASE_URL_GRAPHQL"]}\""
            )
            buildConfigField(
                "String",
                "APP_SERVER_BASE_URL",
                "\"${properties["APP_SERVER_BASE_URL"]}\""
            )
            buildConfigField(
                "String",
                "WEB_AUTHENTICATION_MODE",
                "\"${properties["WEB_AUTHENTICATION_MODE"]}\""
            )
            val file = project.file("githubplayroom_private_pkcs8.pem")
            val key = file.readText()
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace(System.lineSeparator(), "")
            buildConfigField(
                "String",
                "JWT_KEY",
                "\"$key\""
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    namespace = "com.shyampatel.githubplayroom"
}

dependencies {

    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.work.runtime)
    implementation(libs.coil.kt)
    implementation(libs.coil.kt.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.koin.androidx.startup)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.material)
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
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