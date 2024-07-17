import java.util.*

plugins {
    `kotlin-dsl`
    id("org.jetbrains.dokka")
    id("maven-publish")
}

version = Properties()
    .apply { load(projectDir.resolve("../../gradle.properties").inputStream()) }
    .get("project.version") as String

group = "com.iskportal"


repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}
dependencies {
    implementation("com.squareup:kotlinpoet:1.18.1")
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.26.1")
    implementation(project(":lib"))
}

gradlePlugin {
    plugins {
        dependencies {
            implementation("dev.toastbits.kjna:plugin:0.0.5")
        }
        create("kfs-binder") {
            id = "com.iskportal.kfs.binder.plugin"
            implementationClass = "com.iskportal.kfs.binder.plugin.BinderPlugin"
        }
    }
}
publishing {
    repositories {
        mavenLocal()
    }
}