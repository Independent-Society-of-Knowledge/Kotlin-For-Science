import java.util.*

plugins {
    kotlin("jvm") version "2.0.0"
    `kotlin-dsl`
    id("org.jetbrains.dokka") version "1.9.20"
    id("maven-publish")
}

version = Properties()
    .apply { load(projectDir.resolve("../gradle.properties").inputStream()) }
    .get("project.version") as String

group = "com.iskportal"


repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}
dependencies {
    implementation("com.squareup:kotlinpoet:1.18.1")
}

gradlePlugin {
    plugins {
        dependencies {
            implementation("dev.toastbits.kjna:plugin:0.0.5")
        }
        create("kfs-binder") {
            id = "com.iskportal.kfs.binder"
            implementationClass = "com.iskportal.kfs.binder.BinderPlugin"
        }
    }
}
publishing {
    repositories {
        mavenLocal()
    }
}