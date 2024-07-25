package com.iskportal.kfs.binder.plugin.dto

import java.io.File

data class Locations(
    val kfsBuildDir: File
) {
    val kfsClassDir = kfsBuildDir.resolve("classes")
    val kfsSrcDir = kfsBuildDir.resolve("src")
    val kfsJavaSourceDir = kfsSrcDir.resolve("java")
}