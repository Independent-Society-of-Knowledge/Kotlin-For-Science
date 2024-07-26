package com.iskportal.kfs.binder.plugin.tasks

import com.github.javaparser.JavaParser
import com.iskportal.kfs.binder.plugin.dto.ProjectConfig
import dev.toastbits.kjna.plugin.KJnaJextractGenerateTask
import org.gradle.kotlin.dsl.withType
import org.slf4j.LoggerFactory
import java.io.File

fun doPoetry(clazz: Class<*>, projectConfig: ProjectConfig) =
    Poetry(
        clazz,
        projectConfig
    ).makePoet()

val logger = LoggerFactory.getLogger("kfs")


fun compileAndLoadTask(projectConfig: ProjectConfig) {
    val (kfsJavaSourceDir) = projectConfig.locations

    if (kfsJavaSourceDir.exists() && !kfsJavaSourceDir.listFiles().isNullOrEmpty()) {

        logger.info("compiling jextract sources")
        val classLoader = compileAndLoad(projectConfig)
        logger.info("jextract sources compiled in")
        val packages = projectConfig.project.tasks.withType<KJnaJextractGenerateTask>().flatMap { it.packages.packages }

        val loadedClasses = packages
            .flatMap { it.headers.map { h -> "${it.package_name}.jextract.${h.class_name}" } }
            .map { classLoader.loadClass(it) }

        logger.info("loaded classes for inspection : {}", loadedClasses)

        loadedClasses
            .parallelStream()
            .forEach {
                logger.info("doing poetry for: {}", it)
                doPoetry(it, projectConfig)
            }
    }
}