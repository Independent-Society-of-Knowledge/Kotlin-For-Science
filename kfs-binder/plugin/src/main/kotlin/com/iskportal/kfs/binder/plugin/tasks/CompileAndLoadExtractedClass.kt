package com.iskportal.kfs.binder.plugin.tasks

import com.iskportal.kfs.binder.plugin.dto.ProjectConfig
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URLClassLoader
import javax.tools.ToolProvider


fun compileAndLoad(projectConfig: ProjectConfig): URLClassLoader {

    val sourceDir = projectConfig.locations.kfsJavaSourceDir
    val classesDir = projectConfig.locations.kfsClassDir

    val compiler = ToolProvider.getSystemJavaCompiler()
    val fileManager = compiler.getStandardFileManager(null, null, null)

    val sourceFiles = fileManager.getJavaFileObjectsFromFiles(
        projectConfig.project.fileTree(sourceDir).filter { it.extension == "java" }.files
    )

    val compilationTask = compiler.getTask(
        null, fileManager, null,
        listOf(
            "-d", classesDir.absolutePath,
            "-parameters",
        ),
        null, sourceFiles
    )

    if (!compilationTask.call()) {
        throw GradleException("Compilation failed")
    }

    // Load compiled classes
    val classLoader = URLClassLoader(arrayOf(classesDir.toURI().toURL()))

    return classLoader

}