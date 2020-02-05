@file:Suppress("UnstableApiUsage", "MemberVisibilityCanBePrivate")

package mb.spoofax.compiler.gradle.spoofaxcore

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

open class LanguageProjectCompilerSettings(
  val languageProject: LanguageProject.Builder = LanguageProject.builder(),
  val parser: ParserCompiler.LanguageProjectInput.Builder = ParserCompiler.LanguageProjectInput.builder(),
  val styler: StylerCompiler.LanguageProjectInput.Builder? = null, // Optional
  val strategoRuntime: StrategoRuntimeCompiler.LanguageProjectInput.Builder? = null, // Optional
  val constraintAnalyzer: ConstraintAnalyzerCompiler.LanguageProjectInput.Builder? = null, // Optional
  val compiler: LanguageProjectCompiler.Input.Builder = LanguageProjectCompiler.Input.builder()
) {
  internal fun createInput(shared: Shared, project: GradleProject): LanguageProjectCompiler.Input {
    val languageProject = this.languageProject.shared(shared).project(project).build()
    val parser = this.parser.shared(shared).languageProject(languageProject).build()
    val styler = if(this.styler != null) this.styler.shared(shared).languageProject(languageProject).build() else null
    val strategoRuntime = if(this.strategoRuntime != null) this.strategoRuntime.shared(shared).languageProject(languageProject).build() else null
    val constraintAnalyzer = if(this.constraintAnalyzer != null) this.constraintAnalyzer.shared(shared).languageProject(languageProject).build() else null
    val compiler = this.compiler.shared(shared).languageProject(languageProject).parser(parser)
    if(styler != null) {
      compiler.styler(styler)
    }
    if(strategoRuntime != null) {
      compiler.strategoRuntime(strategoRuntime)
    }
    if(constraintAnalyzer != null) {
      compiler.constraintAnalyzer(constraintAnalyzer)
    }
    return compiler.build()
  }
}

open class AdapterProjectCompilerSettings(
  val adapterProject: AdapterProject.Builder = AdapterProject.builder(),
  val parser: ParserCompiler.AdapterProjectInput.Builder = ParserCompiler.AdapterProjectInput.builder(),
  val styler: StylerCompiler.AdapterProjectInput.Builder? = null, // Optional
  val strategoRuntime: StrategoRuntimeCompiler.AdapterProjectInput.Builder? = null, // Optional
  val constraintAnalyzer: ConstraintAnalyzerCompiler.AdapterProjectInput.Builder? = null, // Optional
  val compiler: AdapterProjectCompiler.Input.Builder = AdapterProjectCompiler.Input.builder()
) {
  internal fun createInput(shared: Shared, languageProjectInput: LanguageProjectCompiler.Input, project: GradleProject): AdapterProjectCompiler.Input {
    val adapterProject = this.adapterProject.shared(shared).project(project).build()
    val parser = this.parser.shared(shared).adapterProject(adapterProject).languageProjectInput(languageProjectInput.parser()).build()
    val styler = if(this.styler != null) {
      if(!languageProjectInput.styler().isPresent) {
        throw GradleException("Styler adapter project input is present, but styler language project input is not")
      }
      this.styler.shared(shared).adapterProject(adapterProject).languageProjectInput(languageProjectInput.styler().get()).build()
    } else null
    val strategoRuntime = if(this.strategoRuntime != null) {
      if(!languageProjectInput.strategoRuntime().isPresent) {
        throw GradleException("Stratego runtime adapter project input is present, but Stratego runtime language project input is not")
      }
      this.strategoRuntime.languageProjectInput(languageProjectInput.strategoRuntime().get()).build()
    } else null
    val constraintAnalyzer = if(this.constraintAnalyzer != null) {
      if(!languageProjectInput.constraintAnalyzer().isPresent) {
        throw GradleException("Constraint analyzer adapter project input is present, but constraint analyzer runtime language project input is not")
      }
      this.constraintAnalyzer.shared(shared).adapterProject(adapterProject).languageProjectInput(languageProjectInput.constraintAnalyzer().get()).build()
    } else null
    val compiler = this.compiler.shared(shared).adapterProject(adapterProject).parser(parser)
    if(styler != null) {
      compiler.styler(styler)
    }
    if(strategoRuntime != null) {
      compiler.strategoRuntime(strategoRuntime)
    }
    if(constraintAnalyzer != null) {
      compiler.constraintAnalyzer(constraintAnalyzer)
    }
    return compiler.build()
  }
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
    this.languageProjectCompilerSettings.convention(LanguageProjectCompilerSettings())
    this.adapterProjectCompilerSettings.convention(AdapterProjectCompilerSettings())
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


  internal val shared: Shared by lazy {
    sharedSettings.finalizeValue()
    sharedSettings.get()
      .withPersistentProperties(persistentProperties)
      .baseDirectory(FSPath(baseDirectory))
      .build()
  }

  internal val languageProject: Property<GradleProject> = objects.property()
  internal val languageProjectCompilerInput: LanguageProjectCompiler.Input by lazy {
    languageProject.finalizeValue()
    languageProjectCompilerSettings.finalizeValue()
    if(!languageProject.isPresent) {
      throw GradleException("Language project has not been set")
    }
    languageProjectCompilerSettings.get().createInput(shared, languageProject.get())
  }

  internal val adapterProject: Property<GradleProject> = objects.property()
  internal val adapterProjectCompilerInput: AdapterProjectCompiler.Input by lazy {
    adapterProject.finalizeValue()
    adapterProjectCompilerSettings.finalizeValue()
    if(!adapterProject.isPresent) {
      throw GradleException("Adapter project has not been set")
    }
    adapterProjectCompilerSettings.get().createInput(shared, languageProjectCompilerInput, adapterProject.get())
  }
}
