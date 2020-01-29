package org.metaborg.spoofax.compiler.gradle.spoofaxcore

import mb.spoofax.compiler.util.GradleProject
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.*

open class RootPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val persistentProperties = Properties() // TODO: load from file
    val extension = SpoofaxCompilerExtension(project.objects, persistentProperties)
    project.extensions.add(SpoofaxCompilerExtension.id, extension)
    project.subprojects {
      project.extensions.add(SpoofaxCompilerExtension.id, extension)
    }
    project.afterEvaluate {
      afterEvaluate(this)
    }
  }

  private fun afterEvaluate(project: Project) {
    // TODO: store persistent properties
  }
}
