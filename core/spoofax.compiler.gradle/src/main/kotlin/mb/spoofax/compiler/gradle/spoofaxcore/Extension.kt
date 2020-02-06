@file:Suppress("UnstableApiUsage", "MemberVisibilityCanBePrivate")

package mb.spoofax.compiler.gradle.spoofaxcore

import mb.resource.DefaultResourceService
import mb.resource.fs.FSPath
import mb.resource.fs.FSResourceRegistry
import mb.spoofax.compiler.spoofaxcore.AdapterProjectCompiler
import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzerCompiler
import mb.spoofax.compiler.spoofaxcore.LanguageProjectCompiler
import mb.spoofax.compiler.spoofaxcore.ParserCompiler
import mb.spoofax.compiler.spoofaxcore.Shared
import mb.spoofax.compiler.spoofaxcore.StrategoRuntimeCompiler
import mb.spoofax.compiler.spoofaxcore.StylerCompiler
import mb.spoofax.compiler.util.TemplateCompiler
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
  internal val strategoRuntimeCompiler = StrategoRuntimeCompiler(templateCompiler)
  internal val constraintAnalyzerCompiler = ConstraintAnalyzerCompiler(templateCompiler)
  internal val languageProjectCompiler = LanguageProjectCompiler(templateCompiler, parserCompiler, stylerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler)
  internal val adapterProjectCompiler = AdapterProjectCompiler(templateCompiler, parserCompiler, stylerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler)


  internal val languageGradleProject: Property<Project> = objects.property()
  internal val adapterGradleProject: Property<Project> = objects.property()


  internal val shared: Shared by lazy {
    sharedSettings.finalizeValue()
    sharedSettings.get()
      .withPersistentProperties(persistentProperties)
      .baseDirectory(FSPath(baseDirectory))
      .build()
  }
}
