package mb.spoofax.compiler.gradle

import mb.common.util.Properties
import mb.resource.ResourceRuntimeException
import mb.resource.ResourceService
import mb.resource.fs.FSPath
import mb.resource.hierarchical.ResourcePath
import mb.spoofax.compiler.util.*
import mb.spoofax.core.Coordinate
import mb.spoofax.core.Version
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.*
import java.io.File
import java.io.IOException

fun Project.toSpoofaxCompilerProject(): GradleProject {
  return GradleProject.builder()
    .coordinate(Coordinate(group.toString(), name, Version.parse(version.toString())))
    .baseDirectory(FSPath(projectDir))
    .build()
  // TODO: set src/main and build directory
}

// Do not inline this function because it disables a Gradle plugin classpath optimization.
fun <E : Any> Project.whenFinalized(clazz: Class<E>, closure: () -> Unit) {
  try {
    extensions.getByType(clazz)
  } catch(e: UnknownDomainObjectException) {
    afterEvaluate {
      extensions.getByType(clazz)
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

val Project.lockFile: File get() = projectDir.resolve("spoofaxc.lock")

fun Project.loadLockFileProperties(): Properties {
  val file = lockFile
  val properties = Properties()
  if(file.exists()) {
    file.bufferedReader().use {
      try {
        properties.load(it)
      } catch(e: IOException) {
        logger.warn("Failed to load properties from lock file '$file'", e)
      }
    }
  }
  return properties
}

fun Project.saveLockFileProperties(properties: Properties) {
  val file = lockFile
  file.parentFile.mkdirs()
  file.createNewFile()
  file.bufferedWriter().use {
    try {
      properties.storeWithoutDate(it)
      it.flush()
    } catch(e: IOException) {
      project.logger.warn("Failed to store properties to lock file '$file'", e)
    }
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
  project.logger.info("$project: Added dependency to $configurationName: ${this.dependency.toKotlinCode()}")
  return dependency
}

fun GradleDependency.toGradleDependency(project: Project): Dependency {
  return caseOf()
    .project<Dependency> { project.dependencies.project(it) }
    .module { project.dependencies.create(it.groupId, it.artifactId, it.versionRequirement?.toString()) }
    .files { project.dependencies.create(project.files(it)) }
}
