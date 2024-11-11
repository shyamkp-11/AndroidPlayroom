pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AndroidPlayroom"
// enables typesafe importing of module dependency inside project
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include (":githubplayroom")
include (":core:common")
include (":core:data")
include (":core:network")
include (":core:database")
include(":core:datastore")
include(":core:ui")
include(":geofenceplayroom")
