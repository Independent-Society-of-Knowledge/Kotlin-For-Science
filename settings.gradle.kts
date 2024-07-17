pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        kotlin("multiplatform") version(extra["kotlin.version"] as String)
        id("maven-publish")
        id("org.jetbrains.dokka") version "1.9.20"
        id("dev.toastbits.kjna") version "0.0.5"
        id("com.iskportal.kfs.binder.plugin") version(extra["project.version"] as String)
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}


rootProject.name = "kfs"
include("kfs-gsl")
include("kfs-playground")
includeBuild("kfs-binder")
