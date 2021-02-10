@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.plugin

import mb.spoofax.compiler.dagger.*
import mb.spoofax.compiler.gradle.*
import mb.spoofax.compiler.platform.*
import mb.spoofax.core.platform.ResourceServiceComponent
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

open class CliProjectExtension(project: Project) {
  val adapterProject: Property<Project> = project.objects.property()
  val compilerInput: Property<CliProjectCompiler.Input.Builder> = project.objects.property()

  fun compilerInput(closure: CliProjectCompiler.Input.Builder.() -> Unit) {
    compilerInput.get().closure()
  }

  init {
    compilerInput.convention(CliProjectCompiler.Input.builder())
  }

  companion object {
    internal const val id = "languageCliProject"
    private const val name = "language CLI project"
  }

  internal val adapterProjectFinalized: Project by lazy {
    project.logger.debug("Finalizing $name's adapter project reference in $project")
    adapterProject.finalizeValue()
    if(!adapterProject.isPresent) {
      throw GradleException("$name's adapter project reference in $project has not been set")
    }
    adapterProject.get()
  }
  internal val adapterProjectExtension get() = adapterProjectFinalized.extensions.getByType<AdapterProjectExtension>()
  internal val languageProjectExtension get() = adapterProjectExtension.languageProjectExtension

  internal val compilerInputFinalized: CliProjectCompiler.Input by lazy {
    project.logger.debug("Finalizing $name's compiler input in $project")
    compilerInput.finalizeValue()
    val shared = languageProjectExtension.sharedFinalized
    compilerInput.get()
      .shared(shared)
      .project(project.toSpoofaxCompilerProject())
      .packageId(CliProjectCompiler.Input.Builder.defaultPackageId(shared))
      .adapterProjectCompilerInput(adapterProjectExtension.compilerInputFinalized)
      .build()
  }
}

@Suppress("unused")
open class CliPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = CliProjectExtension(project)
    project.extensions.add(CliProjectExtension.id, extension)

    project.plugins.apply(ApplicationPlugin::class.java)

    project.afterEvaluate {
      extension.adapterProjectFinalized.whenAdapterProjectFinalized {
        configure(project, extension.languageProjectExtension.resourceServiceComponent, extension.languageProjectExtension.component, extension.compilerInputFinalized)
      }
    }
  }

  private fun configure(
    project: Project,
    resourceServiceComponent: ResourceServiceComponent,
    component: SpoofaxCompilerComponent,
    input: CliProjectCompiler.Input
  ) {
    configureProject(project, resourceServiceComponent, component, input)
    configureCompileTask(project, resourceServiceComponent, component, input)
    configureExecutableJarTask(project)
  }

  private fun configureProject(
    project: Project,
    resourceServiceComponent: ResourceServiceComponent,
    component: SpoofaxCompilerComponent,
    input: CliProjectCompiler.Input
  ) {
    project.addMainJavaSourceDirectory(input.generatedJavaSourcesDirectory(), resourceServiceComponent.resourceService)
    component.cliProjectCompiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
    project.configure<JavaApplication> {
      @Suppress("DEPRECATION") // Use deprecated property to stay compatible with Gradle 5.6.4
      mainClassName = input.main().qualifiedId()
    }
  }

  private fun configureCompileTask(
    project: Project,
    resourceServiceComponent: ResourceServiceComponent,
    component: SpoofaxCompilerComponent,
    input: CliProjectCompiler.Input
  ) {
    val compileTask = project.tasks.register("compileCliProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { resourceServiceComponent.resourceService.toLocalFile(it) })

      doLast {
        project.deleteDirectory(input.generatedJavaSourcesDirectory(), resourceServiceComponent.resourceService)
        synchronized(component.pie) {
          component.pie.newSession().use { session ->
            session.require(component.cliProjectCompiler.createTask(input))
          }
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
          @Suppress("UnstableApiUsage", "DEPRECATION") // Use deprecated property to stay compatible with Gradle 5.6.4
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
