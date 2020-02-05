@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.resource.ResourceService
import mb.spoofax.compiler.spoofaxcore.LanguageProjectCompiler
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.*

open class LanguagePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.getByType<SpoofaxCompilerExtension>()
    extension.languageProject.set(project.toSpoofaxCompilerProject())
    project.afterEvaluate {
      afterEvaluate(this, extension)
    }
  }

  private fun afterEvaluate(project: Project, extension: SpoofaxCompilerExtension) {
    val compiler = extension.languageProjectCompiler
    val input = extension.languageProjectCompilerInput
    val resourceService = extension.resourceService
    project.configureGeneratedSources(input.languageProject().project(), resourceService)
    extension.languageProjectCompiler.getDependencies(input).forEach {
      it.addToDependencies(project)
    }
    configureCompilerTask(project, input, compiler, resourceService)
    configureCopySpoofaxLanguageTasks(project, input, compiler)
  }

  private fun configureCompilerTask(
    project: Project,
    input: LanguageProjectCompiler.Input,
    compiler: LanguageProjectCompiler,
    resourceService: ResourceService
  ) {
    val compileTask = project.tasks.register("spoofaxCompileLanguageProject") {
      group = "spoofax compiler"
      inputs.property("input", input)
      outputs.files(input.providedFiles().map { resourceService.toLocalFile(it) })
      doLast {
        compiler.compile(input)
      }
    }
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }

  private fun configureCopySpoofaxLanguageTasks(
    project: Project,
    input: LanguageProjectCompiler.Input,
    compiler: LanguageProjectCompiler
  ) {
    val destinationPackage = input.languageProject().packagePath()
    val includeStrategoClasses = input.strategoRuntime().map { it.copyClasses() }.orElse(false)
    val includeStrategoJavastratClasses = input.strategoRuntime().map { it.copyJavaStrategyClasses() }.orElse(false)
    val copyResources = compiler.getCopyResources(input)

    // Create language specification dependency and 'spoofaxLanguage' configuration that contains this dependency.
    val configuration = project.configurations.create("spoofaxLanguage") {
      val dependency: Dependency = input.languageSpecificationDependency().caseOf()
        .project { configureSpoofaxLanguageDependency(project.dependencies.project(it)) }
        .module { configureSpoofaxLanguageDependency(project.dependencies.module(it.toGradleNotation()) as ModuleDependency) }
        .files { project.dependencies.create(project.files(it)) }
      dependencies.add(dependency)
    }

    // Unpack the '.spoofax-language' archive.
    val unpackSpoofaxLanguageDir = "${project.buildDir}/unpackedSpoofaxLanguage/"
    val unpackSpoofaxLanguageTask = project.tasks.register<Sync>("unpackSpoofaxLanguage") {
      dependsOn(configuration)
      from({ configuration.map { project.zipTree(it) } })  /* Closure inside `from` to defer evaluation until task execution time */
      into(unpackSpoofaxLanguageDir)

      val allCopyResources = copyResources.toMutableList()
      if(includeStrategoClasses) {
        allCopyResources.add("target/metaborg/stratego.jar")
      }
      if(includeStrategoJavastratClasses) {
        allCopyResources.add("target/metaborg/stratego-javastrat.jar")
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
    val strategoJavastratCopySpec = project.copySpec {
      from(project.zipTree("$unpackSpoofaxLanguageDir/target/metaborg/stratego-javastrat.jar"))
      exclude("META-INF")
    }
    val copyMainTask = project.tasks.register<Copy>("copyMainResources") {
      dependsOn(unpackSpoofaxLanguageTask)
      into(project.the<SourceSetContainer>()["main"].java.outputDir)
      into(destinationPackage) { with(resourcesCopySpec) }
      if(includeStrategoClasses) {
        into(".") { with(strategoCopySpec) }
      }
      if(includeStrategoJavastratClasses) {
        into(".") { with(strategoJavastratCopySpec) }
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
      if(includeStrategoJavastratClasses) {
        into(".") { with(strategoJavastratCopySpec) }
      }
    }
    project.tasks.getByName(JavaPlugin.TEST_CLASSES_TASK_NAME).dependsOn(copyTestTask)
  }
}

private fun configureSpoofaxLanguageDependency(dependency: ModuleDependency): Dependency {
  dependency.targetConfiguration = Dependency.DEFAULT_CONFIGURATION
  dependency.isTransitive = false // Don't care about transitive dependencies, just want the '.spoofax-language' artifact.
  dependency.artifact {
    name = dependency.name
    type = "spoofax-language"
    extension = "spoofax-language"
  }
  return dependency
}
