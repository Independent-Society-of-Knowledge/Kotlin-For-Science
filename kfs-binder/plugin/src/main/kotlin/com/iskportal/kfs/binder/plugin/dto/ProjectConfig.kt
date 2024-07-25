package com.iskportal.kfs.binder.plugin.dto

import org.gradle.api.Project

data class ProjectConfig(
    val project: Project,
) {
    val locations = Locations(project.layout.buildDirectory.dir("kfs").get().asFile)
}