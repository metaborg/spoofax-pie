package org.metaborg.spoofax.compiler.gradle.spoofaxcore

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.*

open class RootPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val persistentProperties = Properties() // TODO: load from file
    val extension = SpoofaxCompilerExtension(project.objects, project.projectDir, persistentProperties)
    extension.rootProject.set(project.providers.provider { project.toGradleProject() })
    project.extensions.add(SpoofaxCompilerExtension.id, extension)
    project.subprojects {
      extensions.add(SpoofaxCompilerExtension.id, extension)
    }
    project.afterEvaluate {
      afterEvaluate(this)
    }
  }

  private fun afterEvaluate(project: Project) {
    // TODO: store persistent properties
  }
}
