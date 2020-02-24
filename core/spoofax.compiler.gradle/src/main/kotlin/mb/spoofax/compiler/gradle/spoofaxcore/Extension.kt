@file:Suppress("UnstableApiUsage", "MemberVisibilityCanBePrivate")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.resource.DefaultResourceService
import mb.resource.fs.FSPath
import mb.resource.fs.FSResourceRegistry
import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.util.TemplateCompiler
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*

open class SpoofaxCompilerExtension(objects: ObjectFactory, baseDirectory: File, persistentProperties: Properties) {
  val sharedSettings: Property<Shared.Builder> = objects.property()


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
  internal val completerCompiler = CompleterCompiler(templateCompiler)
  internal val strategoRuntimeCompiler = StrategoRuntimeCompiler(templateCompiler)
  internal val constraintAnalyzerCompiler = ConstraintAnalyzerCompiler(templateCompiler)
  internal val languageProjectCompiler = LanguageProjectCompiler(templateCompiler, parserCompiler, stylerCompiler, completerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler)
  internal val adapterProjectCompiler = AdapterProjectCompiler(templateCompiler, parserCompiler, stylerCompiler, completerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler)
  internal val cliProjectCompiler = CliProjectCompiler(templateCompiler)
  internal val eclipseExternaldepsProjectCompiler = EclipseExternaldepsProjectCompiler(templateCompiler)
  internal val eclipseProjectCompiler = EclipseProjectCompiler(templateCompiler)
  internal val intellijProjectCompiler = IntellijProjectCompiler(templateCompiler)

  internal val languageGradleProject: Property<Project> = objects.property()
  internal val adapterGradleProject: Property<Project> = objects.property()
  internal val eclipseExternaldepsGradleProject: Property<Project> = objects.property()

  internal val shared: Shared by lazy {
    sharedSettings.finalizeValue()
    sharedSettings.get()
      .withPersistentProperties(persistentProperties)
      .baseDirectory(FSPath(baseDirectory))
      .build()
  }

  internal val languageProjectCompilerExtension: LanguageProjectCompilerExtension by lazy {
    languageGradleProject.finalizeValue()
    if(!languageGradleProject.isPresent) {
      throw GradleException("Language project has not been set")
    }
    languageGradleProject.get().extensions.getByType<LanguageProjectCompilerExtension /* Type annotation required */>()
  }

  internal val adapterProjectCompilerExtension: AdapterProjectCompilerExtension by lazy {
    adapterGradleProject.finalizeValue()
    if(!adapterGradleProject.isPresent) {
      throw GradleException("Adapter project has not been set")
    }
    adapterGradleProject.get().extensions.getByType<AdapterProjectCompilerExtension /* Type annotation required */>()
  }

  internal val eclipseExternaldepsCompilerExtension: EclipseExternaldepsProjectCompilerExtension by lazy {
    eclipseExternaldepsGradleProject.finalizeValue()
    if(!eclipseExternaldepsGradleProject.isPresent) {
      throw GradleException("Eclipse externaldeps project has not been set")
    }
    eclipseExternaldepsGradleProject.get().extensions.getByType<EclipseExternaldepsProjectCompilerExtension /* Type annotation required */>()
  }
}
