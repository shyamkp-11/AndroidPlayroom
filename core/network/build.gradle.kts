plugins {
    alias(libs.plugins.shyampatel.android.library)
    alias(libs.plugins.apollo)
    id("kotlinx-serialization")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.shyampatel.core.network"
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        all{
            buildConfigField("String", "APP_SERVER_BASE_URL", "\"${properties["APP_SERVER_BASE_URL"]}\"")
            buildConfigField("String", "APP_SERVER_TOKEN", "\"${properties["APP_SERVER_TOKEN"]}\"")

        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
apollo {
    service("service") {
        packageName.set("com.shyampatel.network.graphql")
        introspection {
            endpointUrl.set("https://api.github.com/graphql")
            headers.put("Authorization", "Add token here for apollo plugin introspection")
            schemaFile.set(file("src/main/graphql/schema.graphqls"))
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
    implementation(libs.apollo.runtime)
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