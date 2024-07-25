package com.iskportal.kfs.binder.plugin.tasks

import com.github.javaparser.JavaParser
import com.iskportal.kfs.binder.plugin.dto.ProjectConfig
import dev.toastbits.kjna.plugin.KJnaJextractGenerateTask
import org.gradle.kotlin.dsl.withType
import java.io.File

fun doPoetry(clazz: Class<*>, projectConfig: ProjectConfig) =
    Poetry(
        clazz,
        projectConfig
    ).makePoet()



fun compileAndLoadTask(projectConfig: ProjectConfig) {
    val (kfsJavaSourceDir) = projectConfig.locations

    if (kfsJavaSourceDir.exists() && !kfsJavaSourceDir.listFiles().isNullOrEmpty()) {

        val classLoader = compileAndLoad(projectConfig)
        val packages = projectConfig.project.tasks.withType<KJnaJextractGenerateTask>().flatMap { it.packages.packages }

        val loadedClasses = packages
            .flatMap { it.headers.map { h -> "${it.package_name}.jextract.${h.class_name}" } }
            .map { classLoader.loadClass(it) }


        loadedClasses.forEach {
            doPoetry(it, projectConfig)
        }
    }
}