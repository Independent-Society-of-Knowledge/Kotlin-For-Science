pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        kotlin("multiplatform") version "2.0.0"
        id("maven-publish")
        id("org.jetbrains.dokka") version "1.9.20"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}


rootProject.name = "kfs-binder"

include("plugin")
include("lib")