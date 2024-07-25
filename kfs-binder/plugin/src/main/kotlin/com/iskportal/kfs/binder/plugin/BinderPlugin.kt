package com.iskportal.kfs.binder.plugin

import com.github.javaparser.JavaParser
import com.iskportal.kfs.binder.plugin.dto.ProjectConfig
import com.iskportal.kfs.binder.plugin.tasks.Poetry
import com.iskportal.kfs.binder.plugin.tasks.compileAndLoadTask
import dev.toastbits.kjna.plugin.KJnaJextractGenerateTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import java.io.File

class BinderPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val projectConfig = ProjectConfig(project)


        val compileAndLoad = project.tasks.register("kfsCompileAndLoad", DefaultTask::class.java) {
            dependsOn(project.tasks.withType<KJnaJextractGenerateTask>())
            inputs.dir(projectConfig.locations.kfsJavaSourceDir)
            outputs.dir(projectConfig.locations.kfsClassDir)
            doLast{
                compileAndLoadTask(projectConfig)
            }

        }


        project.tasks.withType<KJnaJextractGenerateTask>() {
            finalizedBy(compileAndLoad)
        }
        project.tasks.named("generateKJnaBindings") {
            this.enabled = false
        }
    }



}

