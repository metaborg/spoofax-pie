@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.plugin

import mb.spoofax.compiler.dagger.*
import mb.spoofax.compiler.gradle.*
import mb.spoofax.compiler.platform.*
import mb.spoofax.core.platform.ResourceServiceComponent
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*
import org.jetbrains.intellij.IntelliJPlugin
import org.jetbrains.intellij.IntelliJPluginExtension

open class IntellijProjectCompilerExtension(project: Project) {
  val adapterProject: Property<Project> = project.objects.property()
  val compilerInput: Property<IntellijProjectCompiler.Input.Builder> = project.objects.property()

  fun compilerInput(closure: IntellijProjectCompiler.Input.Builder.() -> Unit) {
    compilerInput.get().closure()
  }

  init {
    compilerInput.convention(IntellijProjectCompiler.Input.builder())
  }

  companion object {
    internal const val id = "languageIntellijProject"
    private const val name = "language IntelliJ project"
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

  internal val compilerInputFinalized: IntellijProjectCompiler.Input by lazy {
    project.logger.debug("Finalizing $name's compiler input in $project")
    compilerInput.finalizeValue()
    val shared = languageProjectExtension.sharedFinalized
    compilerInput.get()
      .shared(shared)
      .project(project.toSpoofaxCompilerProject())
      .packageId(IntellijProjectCompiler.Input.Builder.defaultPackageId(shared))
      .adapterProjectCompilerInput(adapterProjectExtension.compilerInputFinalized)
      .build()
  }
}

open class IntellijPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = IntellijProjectCompilerExtension(project)
    project.extensions.add(IntellijProjectCompilerExtension.id, extension)

    project.plugins.apply(JavaLibraryPlugin::class.java)
    project.pluginManager.apply("org.jetbrains.intellij")

    // Disable some IntelliJ plugin functionality to increase incrementality.
    project.configure<IntelliJPluginExtension> {
      instrumentCode = false
    }
    project.tasks.getByName("buildSearchableOptions").enabled = false

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
    input: IntellijProjectCompiler.Input
  ) {
    configureProject(project, resourceServiceComponent, component, input)
    configureCompilerTask(project, resourceServiceComponent, component, input)
  }

  private fun configureProject(
    project: Project,
    resourceServiceComponent: ResourceServiceComponent,
    component: SpoofaxCompilerComponent,
    input: IntellijProjectCompiler.Input
  ) {
    project.addMainJavaSourceDirectory(input.generatedJavaSourcesDirectory(), resourceServiceComponent.resourceService)
    project.addMainResourceDirectory(input.generatedResourcesDirectory(), resourceServiceComponent.resourceService)
    component.intellijProjectCompiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
    project.dependencies.add("implementation", input.adapterProjectDependency().toGradleDependency(project), closureOf<ModuleDependency> {
      exclude(group = "org.slf4j") // Exclude slf4j, as IntelliJ has its own special version of it.
    })
  }

  private fun configureCompilerTask(
    project: Project,
    resourceServiceComponent: ResourceServiceComponent,
    component: SpoofaxCompilerComponent,
    input: IntellijProjectCompiler.Input
  ) {
    val compileTask = project.tasks.register("compileIntellijProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { resourceServiceComponent.resourceService.toLocalFile(it) })

      doLast {
        project.deleteDirectory(input.generatedJavaSourcesDirectory(), resourceServiceComponent.resourceService)
        project.deleteDirectory(input.generatedResourcesDirectory(), resourceServiceComponent.resourceService)
        synchronized(component.pie) {
          component.pie.newSession().use { session ->
            session.require(component.intellijProjectCompiler.createTask(input))
          }
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)

    // Make all of IntelliJ's tasks depend on our task, because we generate Java code and a plugin.xml file.
    project.tasks.getByName(IntelliJPlugin.BUILD_PLUGIN_TASK_NAME).dependsOn(compileTask)
    project.tasks.getByName(IntelliJPlugin.PATCH_PLUGIN_XML_TASK_NAME).dependsOn(compileTask)
    project.tasks.getByName(IntelliJPlugin.PREPARE_SANDBOX_TASK_NAME).dependsOn(compileTask)
    project.tasks.getByName(IntelliJPlugin.RUN_IDE_TASK_NAME).dependsOn(compileTask)
    project.tasks.getByName(IntelliJPlugin.PUBLISH_PLUGIN_TASK_NAME).dependsOn(compileTask)
    project.tasks.getByName(IntelliJPlugin.VERIFY_PLUGIN_TASK_NAME).dependsOn(compileTask)
  }
}
