plugins {
    kotlin("multiplatform")
}
repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

kotlin{
    jvm("binder")
}