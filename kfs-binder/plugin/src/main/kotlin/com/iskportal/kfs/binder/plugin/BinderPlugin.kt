package com.iskportal.kfs.binder.plugin

import com.github.javaparser.JavaParser
import dev.toastbits.kjna.plugin.KJnaJextractGenerateTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import java.io.File
import kotlin.jvm.optionals.toList

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
                if (kfsJavaSourceDir.exists() && kfsJavaSourceDir.listFiles().isNotEmpty()) {

                    val classLoader =
                        CompileAndLoadExtractedClass(kfsJavaSourceDir, kfsClassDir, project).compileAndLoad()
                    val packages = project.tasks.withType<KJnaJextractGenerateTask>().flatMap { it.packages.packages }
                    val loadedClasses = packages
                        .flatMap { it.headers.map { h -> "${it.package_name}.jextract.${h.class_name}" } }
                        .map { it to getComments(kfsJavaSourceDir, it) }
                        .map { classLoader.loadClass(it.first) to it.second }



                    loadedClasses.forEach {
                        doPoetry(project, it.first, kfsSrcDir, it.second)
                    }
                }
            }

        }


        project.tasks.withType<KJnaJextractGenerateTask>() {
            finalizedBy(compileAndLoad)
        }
        project.tasks.named("generateKJnaBindings") {
            this.enabled = false
        }
    }

    fun doPoetry(project: Project, clazz: Class<*>, srcDir: File, second: Unit) =
        Poetry(
            project,
            clazz,
            srcDir
        ).makePoet()

    private fun getComments(kfsJavaSourceDir: File, className: String) {
        val parsed = JavaParser().parse(kfsJavaSourceDir.resolve("${className.replace(".", "/")}.java"))
        parsed.commentsCollection.get()
        // TODO work later on parsing the source, grabbing the java docs
        // they contain header signature for C functions,
        // using that information with some KMP magic i can
        // replace MemorySegments ( which are C-Pointers ) with arrays which are multiplatform
        // i also should attach those documents to kotlin classes
    }

}

