package org.metaborg.spoofax.compiler.gradle.spoofaxcore

import mb.spoofax.compiler.spoofaxcore.LanguageProject
import mb.spoofax.compiler.spoofaxcore.Parser
import mb.spoofax.compiler.spoofaxcore.Shared
import mb.spoofax.compiler.spoofaxcore.Styler
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property

open class CompilerSettingsExtension(objects: ObjectFactory) {
  val sharedBuilder: Property<Shared.Builder> = objects.property()
  val parserBuilder: Property<Parser.Input.Builder> = objects.property()
  val stylerBuilder: Property<Styler.Input.Builder> = objects.property()
  val languageProjectBuilder: Property<LanguageProject.Input.Builder> = objects.property()

  init {
    sharedBuilder.convention(Shared.builder())
    parserBuilder.convention(Parser.Input.builder())
    languageProjectBuilder.convention(LanguageProject.Input.builder())
  }

//  internal val shared: Provider<Shared> = sharedBuilder.map { it.build() }
//  internal val parser: Provider<Parser.Input> = shared.flatMap { shared -> parserBuilder.map { it.shared(shared).build() } }
//  internal val styler: Provider<Styler.Input> = shared.flatMap { shared -> parser.flatMap { parser -> stylerBuilder.map { it.shared(shared).parser(parser).build() } } }
//  internal val languageProject: Provider<LanguageProject.Input> = shared.flatMap { shared -> parser.flatMap { parser -> languageProjectBuilder.map { it.shared(shared).parser(parser).build() } } }

  internal fun finalize(): CompilerSettings {
    sharedBuilder.finalizeValue()
    parserBuilder.finalizeValue()
    stylerBuilder.finalizeValue()
    languageProjectBuilder.finalizeValue()

    val shared = sharedBuilder.get().build()
    val parser = parserBuilder.get().shared(shared).build()

    return CompilerSettings(shared, parser)
  }
}

internal class CompilerSettings(
  val shared: Shared,
  val parser: Parser.Input
) {

}
