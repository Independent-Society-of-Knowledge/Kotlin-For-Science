package com.iskportal.kfs.binder

import dev.toastbits.kjna.plugin.KJnaJextractGenerateTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import java.io.File

class BinderPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val kfsBuildDir = project.layout.buildDirectory.dir("kfs").get().asFile
        val kfsClassDir = kfsBuildDir.resolve("classes")
        val kfsSrcDir = kfsBuildDir.resolve("src")
        val kfsJavaSourceDir = kfsSrcDir.resolve("java")


        val compileAndLoad = project.tasks.register("kfsCompileAndLoad", DefaultTask::class.java) {
            dependsOn(project.tasks.withType<KJnaJextractGenerateTask>())
            inputs.dir(kfsJavaSourceDir)
            outputs.dir(kfsClassDir)
            doLast {
                if(kfsJavaSourceDir.exists() && kfsJavaSourceDir.listFiles().isNotEmpty()){

                    val classLoader = CompileAndLoadExtractedClass(kfsJavaSourceDir, kfsClassDir, project).compileAndLoad()
                    val packages = project.tasks.withType<KJnaJextractGenerateTask>().flatMap { it.packages.packages }
                    val loadedClasses = packages
                        .flatMap { it.headers.map { h -> "${it.package_name}.jextract.${h.class_name}" } }
                        .map { classLoader.loadClass(it) }

                    loadedClasses.forEach {
                        doPoetry(project, it, kfsSrcDir)
                    }
                }
                kfsJavaSourceDir.deleteRecursively()
            }

        }


        project.tasks.withType<KJnaJextractGenerateTask>() {
            finalizedBy(compileAndLoad)
        }
        project.tasks.named("generateKJnaBindings") {
            this.enabled = false
        }
    }

    fun doPoetry(project: Project, clazz: Class<*>, srcDir: File) =
        Poetry(
            project,
            clazz,
            srcDir
        ).makePoet()

}

