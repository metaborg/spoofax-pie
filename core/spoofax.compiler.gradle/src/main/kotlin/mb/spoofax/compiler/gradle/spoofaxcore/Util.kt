package mb.spoofax.compiler.gradle.spoofaxcore

import mb.resource.ResourceService
import mb.resource.fs.FSPath
import mb.spoofax.compiler.util.GradleConfiguredDependency
import mb.spoofax.compiler.util.GradleDependency
import mb.spoofax.compiler.util.GradleProject
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.project

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

fun GradleConfiguredDependency.addToDependencies(project: Project): Dependency {
  val configurationName = caseOf()
    .api_("api")
    .implementation_("implementation")
    .compileOnly_("compileOnly")
    .runtimeOnly_("runtimeOnly")
    .testImplementation_("testImplementation")
    .testCompileOnly_("testCompileOnly")
    .testRuntimeOnly_("testRuntimeOnly")
    .annotationProcessor_("annotationProcessor")
    .testAnnotationProcessor_("testAnnotationProcessor")
  val dependency = this.dependency.toGradleDependency(project)
  project.dependencies.add(configurationName, dependency)
  return dependency
}

fun GradleDependency.toGradleDependency(project: Project): Dependency {
  return caseOf()
    .project<Dependency> { project.dependencies.project(it) }
    .module { project.dependencies.module(it.toGradleNotation()) }
    .files { project.dependencies.create(project.files(it)) }
}
