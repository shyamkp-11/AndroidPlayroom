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

rootProject.name = "GithubPlayroom"
// enables typesafe importing of module dependency inside project
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include (":app")
include (":core:common")
include (":core:data")
include (":core:network")
include (":core:database")
include(":core:datastore")
include(":core:ui")
