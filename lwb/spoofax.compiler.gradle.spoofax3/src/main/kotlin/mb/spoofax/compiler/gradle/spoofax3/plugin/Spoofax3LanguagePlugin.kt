@file:Suppress("UnstableApiUsage")

package mb.spoofax.compiler.gradle.spoofax3.plugin

import mb.sdf3.spoofax.DaggerSdf3Component
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.gradle.spoofax3.*
import mb.spoofax.compiler.spoofax3.language.*
import mb.spoofax.core.platform.DaggerPlatformComponent
import mb.str.spoofax.DaggerStrategoComponent
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*

open class Spoofax3LanguageProjectExtension(project: Project) {
  val compilerInput: Property<Spoofax3LanguageProjectCompilerInputBuilder> = project.objects.property()

  fun compilerInput(closure: Spoofax3LanguageProjectCompilerInputBuilder.() -> Unit) {
    compilerInput.get().closure()
  }

  init {
    compilerInput.convention(Spoofax3LanguageProjectCompilerInputBuilder())
  }

  companion object {
    internal const val id = "spoofax3BasedLanguageProject"
    private const val name = "Spoofax3-based language project"
  }

  val compilerInputFinalized: Spoofax3LanguageProjectCompiler.Input by lazy {
    project.logger.debug("Finalizing $name's compiler input in $project")
    compilerInput.finalizeValue()
    compilerInput.get().build()
  }
}

@Suppress("unused")
open class Spoofax3LanguagePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    // First apply the language plugin to make its extension available.
    project.plugins.apply("org.metaborg.spoofax.compiler.gradle.language")
    val languageProjectExtension = project.extensions.getByType<LanguageProjectExtension>()

    val platformComponent = DaggerPlatformComponent.builder().build()
    val component = DaggerSpoofax3CompilerGradleComponent.builder()
      .spoofax3CompilerGradleModule(Spoofax3CompilerGradleModule(languageProjectExtension.component.resourceService, languageProjectExtension.component.pie))
      // OPTO: cache instantiation of the SDF3 and Stratego components?
      .sdf3Component(DaggerSdf3Component.builder().platformComponent(platformComponent).build())
      .strategoComponent(DaggerStrategoComponent.builder().platformComponent(platformComponent).build())
      .build()

    val extension = Spoofax3LanguageProjectExtension(project)
    project.extensions.add(Spoofax3LanguageProjectExtension.id, extension)

    // Add a configuration closure to the language project that syncs our finalized input to their builder.
    languageProjectExtension.compilerInput { extension.compilerInputFinalized.syncTo(this) }

    project.afterEvaluate {
      configure(project, component, extension.compilerInputFinalized)
    }
  }

  private fun configure(
    project: Project,
    component: Spoofax3CompilerGradleComponent,
    input: Spoofax3LanguageProjectCompiler.Input
  ) {
    configureCompileTask(project, component, input)
  }

  private fun configureCompileTask(
    project: Project,
    component: Spoofax3CompilerGradleComponent,
    input: Spoofax3LanguageProjectCompiler.Input
  ) {
    val compileTask = project.tasks.register("compileLanguageProject") {
      group = "spoofax compiler"
      inputs.property("input", input)

      doLast {
        component.pie.newSession().use { session ->
          session.require(component.spoofax3LanguageProjectCompiler.createTask(input))
        }
      }
    }

    // Make compileJava depend on our task, because we generate Java code.
    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(compileTask)
  }
}
