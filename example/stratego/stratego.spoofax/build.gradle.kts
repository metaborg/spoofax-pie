import mb.spoofax.compiler.command.*
import mb.spoofax.compiler.menu.*
import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.util.*
import mb.spoofax.compiler.gradle.spoofaxcore.*
import mb.spoofax.core.language.command.CommandContextType
import mb.spoofax.core.language.command.CommandExecutionType
import mb.spoofax.core.language.command.EnclosingCommandContextType

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.adapter")
}

spoofaxAdapterProject {
  languageProject.set(project(":stratego"))
  settings.set(AdapterProjectSettings(
    parser = ParserCompiler.AdapterProjectInput.builder(),
    styler = StylerCompiler.AdapterProjectInput.builder(),
    completer = CompleterCompiler.AdapterProjectInput.builder(),
    strategoRuntime = StrategoRuntimeCompiler.AdapterProjectInput.builder(),

    builder = run {
      val packageId = "mb.stratego.spoofax"
      val taskPackageId = "$packageId.task"
      val commandPackageId = "$packageId.command"

      val builder = AdapterProjectCompiler.Input.builder()
      builder
    }
  ))
}
