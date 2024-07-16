package com.iskportal.kfs.binder

import dev.toastbits.kjna.plugin.KJnaJextractGenerateTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import java.lang.reflect.Modifier

class BinderPlugin : Plugin<Project> {
    override fun apply(project: Project) {


        lateinit var loadedClasses: List<Class<*>>


        val inspect = project.tasks.register("kfsPoetry") {
            doLast {
                loadedClasses.forEach {
                    doPoetry(project, it)
                }
            }
        }


        val compileAndLoad = project.tasks.register("kfsCompileAndLoad", DefaultTask::class.java) {
            doLast {
                val kfsBuildDir = project.layout.buildDirectory.dir("kfs").get().asFile
                val kfsJavaSourceDir = kfsBuildDir.resolve("src/java")
                val kfsClassDir = kfsBuildDir.resolve("classes")
                val classLoader = CompileAndLoadExtractedClass(kfsJavaSourceDir, kfsClassDir, project).compileAndLoad()
                val packages = project.tasks.withType<KJnaJextractGenerateTask>().flatMap { it.packages.packages }
                loadedClasses = packages
                    .flatMap { it.headers.map { h -> "${it.package_name}.jextract.${h.class_name}" } }
                    .map { classLoader.loadClass(it) }
            }
            finalizedBy(inspect)
        }


        project.tasks.withType<KJnaJextractGenerateTask>() {
            finalizedBy(compileAndLoad)
        }
    }

    fun doPoetry(project: Project, clazz: Class<*>) {
        clazz.declaredMethods
            .filter { Modifier.isStatic(it.modifiers) }
            .filterNot { it.isSynthetic }
            .filterNot { it.name.contains("$") }
            .forEach {
                println(it.name)
            }
    }

}

