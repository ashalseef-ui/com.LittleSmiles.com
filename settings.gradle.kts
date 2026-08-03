pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "com.LittleSmiles.com"
include(":app")
include(":core:model")
include(":core:common")
include(":core:ui")
include(":core:navigation")
include(":data")
include(":feature:parent-hub")
include(":feature:learning")
include(":feature:auth")
include(":feature:games")
include(":feature:menu")
include(":feature:loading")
