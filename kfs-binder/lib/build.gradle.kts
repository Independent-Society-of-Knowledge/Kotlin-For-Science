import java.util.*

plugins {
    kotlin("multiplatform")
    id("maven-publish")
}
repositories {
    mavenLocal()
    mavenCentral()
}
val properties = Properties()
    .apply { load(projectDir.resolve("../../gradle.properties").inputStream()) }

version = properties["project.version"]!!
group = "com.iskportal.kfs.binder"


kotlin {
    jvm()
    sourceSets.commonMain {
        dependencies {
            implementation("org.jetbrains.kotlin:kotlin-stdlib:${properties["kotlin.version"]!!}")
            implementation("org.jetbrains.kotlin:kotlin-reflect:${properties["kotlin.version"]!!}")
        }
    }
}