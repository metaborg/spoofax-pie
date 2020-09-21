package mb.spoofax.compiler.gradle

import mb.resource.ResourceRuntimeException
import mb.resource.ResourceService
import mb.resource.fs.FSPath
import mb.resource.hierarchical.ResourcePath
import mb.spoofax.compiler.util.*
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.*

fun Project.toSpoofaxCompilerProject(): GradleProject {
  return GradleProject.builder()
    .coordinate(Coordinate.of(group.toString(), name, version.toString()))
    .baseDirectory(FSPath(projectDir))
    .build()
  // TODO: set src/main and build directory
}

inline fun <reified E : Any> Project.whenFinalized(crossinline closure: () -> Unit) {
  try {
    extensions.getByType<E>()
  } catch(e: UnknownDomainObjectException) {
    afterEvaluate {
      extensions.getByType<E>()
      closure()
    }
    return
  }
  closure()
}

fun Project.addMainJavaSourceDirectory(directory: ResourcePath, resourceService: ResourceService) {
  configure<SourceSetContainer> {
    named("main") {
      java {
        srcDir(resourceService.toLocalFile(directory)
          ?: throw GradleException("Cannot configure java sources directory, directory '$directory' is not on the local filesystem"))
      }
    }
  }
}

fun Project.addMainResourceDirectory(directory: ResourcePath, resourceService: ResourceService) {
  configure<SourceSetContainer> {
    named("main") {
      resources {
        srcDir(resourceService.toLocalFile(directory)
          ?: throw GradleException("Cannot configure resources directory, directory '$directory' is not on the local filesystem"))
      }
    }
  }
}

fun Project.deleteDirectory(directory: ResourcePath, resourceService: ResourceService) {
  try {
    val genSourceDir = resourceService.getHierarchicalResource(directory)
    genSourceDir.delete(true)
  } catch(e: ResourceRuntimeException) {
    project.logger.warn("Failed to delete directory", e)
  }
}

fun GradleConfiguredDependency.addToDependencies(project: Project): Dependency {
  val (configurationName, isPlatform) = caseOf()
    .api_("api" to false)
    .implementation_("implementation" to false)
    .compileOnly_("compileOnly" to false)
    .runtimeOnly_("runtimeOnly" to false)
    .testImplementation_("testImplementation" to false)
    .testCompileOnly_("testCompileOnly" to false)
    .testRuntimeOnly_("testRuntimeOnly" to false)
    .annotationProcessor_("annotationProcessor" to false)
    .testAnnotationProcessor_("testAnnotationProcessor" to false)
    .apiPlatform_("api" to true)
    .implementationPlatform_("implementation" to true)
    .annotationProcessorPlatform_("annotationProcessor" to true)
  var dependency = this.dependency.toGradleDependency(project)
  if(isPlatform) {
    dependency = project.dependencies.platform(dependency)
  }
  project.dependencies.add(configurationName, dependency)
  return dependency
}

fun GradleDependency.toGradleDependency(project: Project): Dependency {
  return caseOf()
    .project<Dependency> { project.dependencies.project(it) }
    .module { project.dependencies.create(it.groupId(), it.artifactId(), it.version().orElse(null)) }
    .files { project.dependencies.create(project.files(it)) }
}
