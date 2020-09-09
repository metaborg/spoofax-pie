@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.spoofax.compiler.platform.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*

open class CliProjectSettings(
  val builder: CliProjectCompiler.Input.Builder = CliProjectCompiler.Input.builder()
) {
  internal fun finalize(gradleProject: Project, adapterProject: Project): CliProjectFinalized {
    val adapterProjectExtension: AdapterProjectExtension = adapterProject.extensions.getByType()
    val adapterProjectFinalized = adapterProjectExtension.finalized
    val languageProjectFinalized = adapterProjectFinalized.languageProjectFinalized

    val shared = languageProjectFinalized.shared
    val project = gradleProject.toSpoofaxCompilerProject()
    val input = this.builder
      .shared(shared)
      .project(project)
      .packageId(CliProjectCompiler.Input.Builder.defaultPackageId(shared))
      .adapterProjectCompilerInput(adapterProjectFinalized.input)
      .build()

    return CliProjectFinalized(input, languageProjectFinalized)
  }
}

open class CliProjectExtension(project: Project) {
  val adapterProject: Property<Project> = project.objects.property()
  val settings: Property<CliProjectSettings> = project.objects.property()

  init {
    settings.convention(CliProjectSettings())
  }

  companion object {
    internal const val id = "spoofaxCliProject"
    private const val name = "Spoofax language CLI project"
  }

  internal val adapterProjectFinalized: Project by lazy {
    project.logger.debug("Finalizing $name's adapter project reference in $project")
    adapterProject.finalizeValue()
    if(!adapterProject.isPresent) {
      throw GradleException("$name's adapter project reference in $project has not been set")
    }
    adapterProject.get()
  }

  internal val finalized: CliProjectFinalized by lazy {
    project.logger.debug("Finalizing $name settings in $project")
    settings.finalizeValue()
    if(!settings.isPresent) {
      throw GradleException("$name settings in $project have not been set")
    }
    settings.get().finalize(project, adapterProjectFinalized)
  }
}

internal class CliProjectFinalized(
  val input: CliProjectCompiler.Input,
  languageProjectFinalized: LanguageProjectFinalized
) {
  val pie = languageProjectFinalized.pie
  val resourceService = languageProjectFinalized.resourceService
  val compiler = languageProjectFinalized.component.cliProjectCompiler
}

@Suppress("unused")
open class CliPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = CliProjectExtension(project)
    project.extensions.add(CliProjectExtension.id, extension)

    project.plugins.apply(ApplicationPlugin::class.java)

    project.afterEvaluate {
      extension.adapterProjectFinalized.whenAdapterProjectFinalized {
        configure(project, extension.finalized)
      }
    }
  }

  private fun configure(project: Project, finalized: CliProjectFinalized) {
    configureProject(project, finalized)
    configureCompileTask(project, finalized)
    configureExecutableJarTask(project)
  }

  private fun configureProject(project: Project, finalized: CliProjectFinalized) {
    val input = finalized.input
    project.configureGeneratedSources(project.toSpoofaxCompilerProject(), finalized.resourceService)
    finalized.compiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
    project.configure<JavaApplication> {
      mainClassName = input.main().qualifiedId()
    }
  }

  private fun configureCompileTask(project: Project, finalized: CliProjectFinalized) {
    val input = finalized.input
    val compileTask = project.tasks.register("spoofaxCompileCliProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { finalized.resourceService.toLocalFile(it) })

      doLast {
        project.deleteGenSourceSpoofaxDirectory(input.project(), finalized.resourceService)
        finalized.pie.newSession().use { session ->
          session.require(finalized.compiler.createTask(input))
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureExecutableJarTask(project: Project) {
    val executableJarTask = project.tasks.register<Jar>("executableJar") {
      val runtimeClasspath by project.configurations
      dependsOn(runtimeClasspath)

      archiveClassifier.set("executable")

      with(project.tasks.getByName<Jar>(JavaPlugin.JAR_TASK_NAME))
      from({
        // Closure inside to defer evaluation until task execution time.
        runtimeClasspath.filter { it.exists() }.map {
          @Suppress("IMPLICIT_CAST_TO_ANY") // Implicit cast to Any is fine, as from takes Any's.
          if(it.isDirectory) it else project.zipTree(it)
        }
      })

      doFirst { // Delay setting Main-Class attribute to just before execution, to ensure that mainClassName is set.
        manifest {
          @Suppress("UnstableApiUsage")
          attributes["Main-Class"] = project.the<JavaApplication>().mainClassName
        }
      }
    }
    project.tasks.named(BasePlugin.ASSEMBLE_TASK_NAME).configure { dependsOn(executableJarTask) }

    // Create an artifact for the executable JAR.
    val executableJarArtifact = project.artifacts.add(Dependency.DEFAULT_CONFIGURATION, executableJarTask) {
      classifier = "executable"
    }

    // Publish primary artifact from the Java component, and publish executable JAR and ZIP distribution as secondary artifacts.
    project.plugins.withType(MavenPublishPlugin::class.java) {
      project.configure<PublishingExtension> {
        publications {
          create<MavenPublication>("JavaApplication") {
            from(project.components["java"])
            artifact(executableJarArtifact)
            artifact(project.tasks.getByName("distZip"))
          }
        }
      }
    }
  }
}
