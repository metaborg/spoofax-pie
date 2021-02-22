@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofax2.plugin

import mb.pie.dagger.DaggerPieComponent
import mb.pie.dagger.PieComponent
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.spoofax2.dagger.*
import mb.spoofax.compiler.spoofax2.language.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.*

open class Spoofax2LanguageProjectExtension(project: Project) {
  val compilerInput: Property<Spoofax2LanguageProjectCompilerInputBuilder> = project.objects.property()

  fun compilerInput(closure: Spoofax2LanguageProjectCompilerInputBuilder.() -> Unit) {
    compilerInput.get().closure()
  }

  init {
    compilerInput.convention(Spoofax2LanguageProjectCompilerInputBuilder())
  }

  companion object {
    internal const val id = "spoofax2BasedLanguageProject"
    private const val name = "Spoofax2-based language project"
  }

  val compilerInputFinalized: Spoofax2LanguageProjectCompiler.Input by lazy {
    project.logger.debug("Finalizing $name's compiler input in $project")
    compilerInput.finalizeValue()
    compilerInput.get().build()
  }
}

@Suppress("unused")
open class Spoofax2LanguagePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    // Apply Spoofax 2 Gradle base plugin to make its configurations and variants available.
    project.plugins.apply(mb.spoofax.gradle.plugin.SpoofaxBasePlugin::class)

    // First apply the language plugin to make its extension available.
    project.plugins.apply("org.metaborg.spoofax.compiler.gradle.language")
    val languageProjectExtension = project.extensions.getByType<LanguageProjectExtension>()

    val components = languageProjectExtension.components
    val component = DaggerSpoofax2CompilerComponent.builder()
      .loggerComponent(components.loggerComponent)
      .resourceServiceComponent(components.resourceServiceComponent)
      .build()
    val pieComponent = DaggerPieComponent.builder()
      .loggerComponent(components.loggerComponent)
      .resourceServiceComponent(components.resourceServiceComponent)
      .pieModule(components.pieComponent.createChildModule(component))
      .build()

    val extension = Spoofax2LanguageProjectExtension(project)
    project.extensions.add(Spoofax2LanguageProjectExtension.id, extension)

    // Add a configuration closure to the language project that syncs our finalized input to their builder.
    languageProjectExtension.compilerInput { extension.compilerInputFinalized.syncTo(this) }

    project.afterEvaluate {
      project.whenLanguageProjectFinalized {
        configure(project, component, pieComponent, extension.compilerInputFinalized, languageProjectExtension.compilerInputFinalized)
      }
    }
  }

  private fun configure(
    project: Project,
    component: Spoofax2CompilerComponent,
    pieComponent: PieComponent,
    input: Spoofax2LanguageProjectCompiler.Input,
    sharedInput: LanguageProjectCompiler.Input
  ) {
    configureCompileTask(project, component, pieComponent, input)
    configureCopySpoofaxLanguageTasks(project, component, input, sharedInput)
  }

  private fun configureCompileTask(
    project: Project,
    component: Spoofax2CompilerComponent,
    pieComponent: PieComponent,
    input: Spoofax2LanguageProjectCompiler.Input
  ) {
    val compileTask = project.tasks.register("compileSpoofax2BasedLanguageProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.upToDateWhen { true } // No outputs

      doLast {
        pieComponent.pie.newSession().use { session ->
          session.require(component.spoofax2LanguageProjectCompiler.createTask(input))
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureCopySpoofaxLanguageTasks(
    project: Project,
    component: Spoofax2CompilerComponent,
    input: Spoofax2LanguageProjectCompiler.Input,
    sharedInput: LanguageProjectCompiler.Input
  ) {
    val destinationPackage = sharedInput.languageProject().packagePath()
    val includeStrategoClasses = input.strategoRuntime().map { it.copyClasses() }.orElse(false)
    val copyResources = component.spoofax2LanguageProjectCompiler.getCopyResources(input)

    // Add language dependency.
    val languageDependency = input.languageSpecificationDependency().caseOf()
      .project<Dependency> { project.dependencies.project(it) }
      .module { project.dependencies.create(it.toGradleNotation()) }
      .files { project.dependencies.create(project.files(it)) }
    project.dependencies.add("compileLanguage", languageDependency)

    // Unpack the '.spoofax-language' archive.
    val languageFiles = project.configurations.getByName("languageFiles")
    val unpackSpoofaxLanguageDir = "${project.buildDir}/unpackedSpoofaxLanguage/"
    val unpackSpoofaxLanguageTask = project.tasks.register<Sync>("unpackSpoofaxLanguage") {
      inputs.property("input", input)
      dependsOn(languageFiles)
      from({ languageFiles.map { project.zipTree(it) } })  /* Closure inside `from` to defer evaluation until task execution time */
      into(unpackSpoofaxLanguageDir)

      val allCopyResources = copyResources.toMutableList()
      if(includeStrategoClasses) {
        allCopyResources.add("target/metaborg/stratego.jar")
      }
      include(allCopyResources)
    }

    // Copy resources into `mainSourceSet.java.outputDir` and `testSourceSet.java.outputDir`, so they end up in the target package.
    val resourcesCopySpec = project.copySpec {
      from(unpackSpoofaxLanguageDir)
      include(copyResources)
    }
    val strategoCopySpec = project.copySpec {
      from(project.zipTree("$unpackSpoofaxLanguageDir/target/metaborg/stratego.jar"))
      exclude("META-INF")
    }
    val copyMainTask = project.tasks.register<Copy>("copyMainResources") {
      dependsOn(unpackSpoofaxLanguageTask)
      into(project.the<SourceSetContainer>()["main"].java.outputDir)
      into(destinationPackage) { with(resourcesCopySpec) }
      if(includeStrategoClasses) {
        into(".") { with(strategoCopySpec) }
      }
    }
    project.tasks.getByName(JavaPlugin.CLASSES_TASK_NAME).dependsOn(copyMainTask)
    val copyTestTask = project.tasks.register<Copy>("copyTestResources") {
      dependsOn(unpackSpoofaxLanguageTask)
      into(project.the<SourceSetContainer>()["test"].java.outputDir)
      into(destinationPackage) { with(resourcesCopySpec) }
      if(includeStrategoClasses) {
        into(".") { with(strategoCopySpec) }
      }
    }
    project.tasks.getByName(JavaPlugin.TEST_CLASSES_TASK_NAME).dependsOn(copyTestTask)
  }
}
