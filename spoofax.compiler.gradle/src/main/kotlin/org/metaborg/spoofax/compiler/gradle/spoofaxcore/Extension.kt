package org.metaborg.spoofax.compiler.gradle.spoofaxcore

import mb.resource.DefaultResourceService
import mb.resource.fs.FSPath
import mb.resource.fs.FSResourceRegistry
import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.util.GradleProject
import mb.spoofax.compiler.util.TemplateCompiler
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*

open class SpoofaxCompilerExtension(objects: ObjectFactory, baseDirectory: File, persistentProperties: Properties) {
  val sharedBuilder: Property<Shared.Builder> = objects.property()
  val parserBuilder: Property<Parser.Input.Builder> = objects.property()
  val stylerBuilder: Property<Styler.Input.Builder> = objects.property()
  val strategoRuntimeBuilder: Property<StrategoRuntime.Input.Builder> = objects.property()
  val constraintAnalyzerBuilder: Property<ConstraintAnalyzer.Input.Builder> = objects.property()

  val languageProjectBuilder: Property<LanguageProject.Input.Builder> = objects.property()


  companion object {
    internal const val id = "spoofaxCompiler"
  }

  init {
    sharedBuilder.convention(Shared.builder())
    parserBuilder.convention(Parser.Input.builder())
    languageProjectBuilder.convention(LanguageProject.Input.builder())
  }

  internal val resourceService = DefaultResourceService(FSResourceRegistry())
  internal val charset = StandardCharsets.UTF_8
  internal val templateCompiler = TemplateCompiler(Shared::class.java, resourceService, charset)
  internal val parserCompiler = Parser(templateCompiler)
  internal val stylerCompiler = Styler(templateCompiler)
  internal val strategoRuntimeCompiler = StrategoRuntime(templateCompiler)
  internal val constraintAnalyzerCompiler = ConstraintAnalyzer(templateCompiler)
  internal val rootProjectCompiler = RootProject(templateCompiler)
  internal val languageProjectCompiler = LanguageProject(templateCompiler, parserCompiler, stylerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler)


  internal val rootProject: Property<GradleProject> = objects.property()
  internal val languageProject: Property<GradleProject> = objects.property()

  internal val finalized: CompilerSettings by lazy {
    sharedBuilder.finalizeValue()
    parserBuilder.finalizeValue()
    stylerBuilder.finalizeValue()
    strategoRuntimeBuilder.finalizeValue()
    constraintAnalyzerBuilder.finalizeValue()
    languageProjectBuilder.finalizeValue()

    rootProject.finalizeValue()

    val shared = sharedBuilder.get()
      .withPersistentProperties(persistentProperties)
      .baseDirectory(FSPath(baseDirectory))
      .rootProject(rootProject.get())
      .languageProject(languageProject.get())
      .build()
    val parser = parserBuilder.get().shared(shared).build()
    val styler = stylerBuilder.orNull?.shared(shared)?.parser(parser)?.build()
    val strategoRuntime = strategoRuntimeBuilder.orNull?.shared(shared)?.build()
    val constraintAnalyzer = constraintAnalyzerBuilder.orNull?.shared(shared)?.parser(parser)?.build()

    val languageProjectBuilder = languageProjectBuilder.get().shared(shared).parser(parser)
    if(styler != null) {
      languageProjectBuilder.styler(styler)
    }
    if(strategoRuntime != null) {
      languageProjectBuilder.strategoRuntime(strategoRuntime)
    }
    if(constraintAnalyzer != null) {
      languageProjectBuilder.constraintAnalyzer(constraintAnalyzer)
    }
    val languageProject = languageProjectBuilder.build()

    CompilerSettings(shared, languageProject)
  }
}

internal data class CompilerSettings(
  val shared: Shared,
  val languageProjectInput: LanguageProject.Input
)
