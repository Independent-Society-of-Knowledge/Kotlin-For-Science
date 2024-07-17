package com.iskportal.kfs.binder.plugin

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URLClassLoader
import javax.tools.ToolProvider

class CompileAndLoadExtractedClass(
    val sourceDir: File,
    val classesDir: File,
    val project: Project
) {


    @TaskAction
    fun compileAndLoad(): URLClassLoader {
        val compiler = ToolProvider.getSystemJavaCompiler()
        val fileManager = compiler.getStandardFileManager(null, null, null)

        val sourceFiles = fileManager.getJavaFileObjectsFromFiles(
            project.fileTree(sourceDir).filter { it.extension == "java" }.files
        )

        val compilationTask = compiler.getTask(
            null, fileManager, null, listOf("-d", classesDir.absolutePath, "-parameters"),
            null, sourceFiles
        )

        if (!compilationTask.call()) {
            throw GradleException("Compilation failed")
        }

        // Load compiled classes
        val classLoader = URLClassLoader(arrayOf(classesDir.toURI().toURL()))

        return classLoader

    }
}