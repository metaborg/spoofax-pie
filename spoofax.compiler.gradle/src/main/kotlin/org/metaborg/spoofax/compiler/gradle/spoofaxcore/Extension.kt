@file:Suppress("UnstableApiUsage")

package org.metaborg.spoofax.compiler.gradle.spoofaxcore

import mb.resource.DefaultResourceService
import mb.resource.fs.FSPath
import mb.resource.fs.FSResourceRegistry
import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.util.GradleProject
import mb.spoofax.compiler.util.TemplateCompiler
import org.gradle.api.GradleException
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*

open class LanguageProjectCompilerSettings {
  val languageProject: LanguageProject.Builder = LanguageProject.builder()
  val parser: ParserCompiler.LanguageProjectInput.Builder = ParserCompiler.LanguageProjectInput.builder()
  val styler: StylerCompiler.LanguageProjectInput.Builder? = null // Optional
  val strategoRuntime: StrategoRuntimeCompiler.LanguageProjectInput.Builder? = null // Optional
  val constraintAnalyzer: ConstraintAnalyzerCompiler.LanguageProjectInput.Builder? = null // Optional
  val compiler: LanguageProjectCompiler.Input.Builder = LanguageProjectCompiler.Input.builder()
}

open class AdapterProjectCompilerSettings {
  val adapterProject: AdapterProject.Builder = AdapterProject.builder()
  val parser: ParserCompiler.AdapterProjectInput.Builder = ParserCompiler.AdapterProjectInput.builder()
  val styler: StylerCompiler.AdapterProjectInput.Builder? = null // Optional
  val strategoRuntime: StrategoRuntimeCompiler.AdapterProjectInput.Builder? = null // Optional
  val constraintAnalyzer: ConstraintAnalyzerCompiler.AdapterProjectInput.Builder? = null // Optional
  val compiler: AdapterProjectCompiler.Input.Builder = AdapterProjectCompiler.Input.builder()
}

open class SpoofaxCompilerExtension(objects: ObjectFactory, baseDirectory: File, persistentProperties: Properties) {
  val sharedSettings: Property<Shared.Builder> = objects.property()
  val languageProjectCompilerSettings: Property<LanguageProjectCompilerSettings> = objects.property()
  val adapterProjectCompilerSettings: Property<AdapterProjectCompilerSettings> = objects.property()

  companion object {
    internal const val id = "spoofaxCompiler"
  }

  init {
    this.sharedSettings.convention(Shared.builder())
  }

  internal val resourceService = DefaultResourceService(FSResourceRegistry())
  internal val charset = StandardCharsets.UTF_8
  internal val templateCompiler = TemplateCompiler(Shared::class.java, resourceService, charset)
  internal val parserCompiler = ParserCompiler(templateCompiler)
  internal val stylerCompiler = StylerCompiler(templateCompiler)
  internal val strategoRuntimeCompiler = StrategoRuntimeCompiler(templateCompiler)
  internal val constraintAnalyzerCompiler = ConstraintAnalyzerCompiler(templateCompiler)
  internal val languageProjectCompiler = LanguageProjectCompiler(templateCompiler, parserCompiler, stylerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler)
  internal val adapterProjectCompiler = AdapterProjectCompiler(templateCompiler, parserCompiler, stylerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler)

  internal val rootProject: Property<GradleProject> = objects.property()
  internal val languageProject: Property<GradleProject> = objects.property()
  internal val adapterProject: Property<GradleProject> = objects.property()
  internal val cliProject: Property<GradleProject> = objects.property()
  internal val eclipseExternaldepsProject: Property<GradleProject> = objects.property()
  internal val eclipseProject: Property<GradleProject> = objects.property()
  internal val intellijProject: Property<GradleProject> = objects.property()

  internal val finalized: CompilerSettings by lazy {

    parserLanguageProjectInputBuilder.finalizeValue()
    stylerBuilder.finalizeValue()
    strategoRuntimeBuilder.finalizeValue()
    constraintAnalyzerBuilder.finalizeValue()
    languageProjectCompilerInputBuilder.finalizeValue()

    rootProject.finalizeValue()
    languageProject.finalizeValue()
    adapterProject.finalizeValue()
    cliProject.finalizeValue()
    eclipseExternaldepsProject.finalizeValue()
    eclipseProject.finalizeValue()
    intellijProject.finalizeValue()

    val shared = this.sharedSettings.get()
      .withPersistentProperties(persistentProperties)
      .baseDirectory(FSPath(baseDirectory))
      .build()

    val languageProjectCompilerInput = languageProjectCompilerInputBuilder.ifPresent {
      if(!parserLanguageProjectInputBuilder.isPresent) {
        throw GradleException("Cannot create language project compiler input: languageProjectCompilerInputBuilder property is set, but parserLanguageProjectInputBuilder is not")
      }
      val parser = parserLanguageProjectInputBuilder.get().shared(shared).build()
      val styler = stylerBuilder.ifPresent { it.shared(shared).parser(parser).build() } elseReturn { null }
      val strategoRuntime = strategoRuntimeBuilder.ifPresent { it.shared(shared).build() } elseReturn { null }
      val constraintAnalyzer = constraintAnalyzerBuilder.ifPresent { it.shared(shared)/*.parser(parser)*/.build() } elseReturn { null }
    }


    val languageProjectBuilder = languageProjectCompilerInputBuilder.get().shared(shared).parser(parser)
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

    val adapterProjectInput = adapterProjectBuilder.ifPresent {
//      it.shared(shared).parser(parser)
//      if(styler != null) {
//        it.styler(styler)
//      }
//      if(strategoRuntime != null) {
//        it.strategoRuntime(strategoRuntime)
//      }
//      if(constraintAnalyzer != null) {
//        it.constraintAnalyzer(constraintAnalyzer)
//      }
      it.build()
    } elseReturn {
      null
    }

    CompilerSettings(shared, languageProject, adapterProjectInput)
  }
}

internal data class CompilerSettings(
  val shared: Shared,
  val languageProjectInput: LanguageProjectCompiler.Input,
  val adapterProjectInput: AdapterProjectCompiler.Input?
)
