package mb.spoofax.compiler.gradle.spoofaxcore

import mb.resource.ResourceService
import mb.resource.fs.FSPath
import mb.spoofax.compiler.util.GradleProject
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.configure

fun Project.toSpoofaxCompilerProject(): GradleProject {
  return GradleProject.builder()
    .coordinate(group.toString(), name, version.toString())
    .baseDirectory(FSPath(projectDir))
    .build()
}

fun Project.configureGeneratedSources(project: GradleProject, resourceService: ResourceService) {
  configure<SourceSetContainer> {
    named("main") {
      java {
        val dir = project.genSourceSpoofaxJavaDirectory()
        srcDir(resourceService.toLocalFile(dir)
          ?: throw GradleException("Cannot configure java sources directory, directory '$dir' is not on the local filesystem"))
      }
      resources {
        val dir = project.genSourceSpoofaxResourcesDirectory()
        srcDir(resourceService.toLocalFile(dir)
          ?: throw GradleException("Cannot configure resources directory, directory '$dir' is not on the local filesystem"))
      }
    }
  }
}
