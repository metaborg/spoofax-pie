package org.metaborg.spoofax.compiler.gradle.spoofaxcore

import mb.resource.fs.FSPath
import mb.spoofax.compiler.util.GradleProject
import org.gradle.api.Project

fun Project.toGradleProject(): GradleProject {
  return GradleProject.builder()
    .coordinate(group.toString(), name, version.toString())
    .baseDirectory(FSPath(projectDir))
    .build()
}
