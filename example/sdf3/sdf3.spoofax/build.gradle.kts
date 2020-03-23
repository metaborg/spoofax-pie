import mb.spoofax.compiler.gradle.spoofaxcore.AdapterProjectCompilerSettings
import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.spoofaxcore.StrategoRuntimeCompiler.AdapterProjectInput.builder
import mb.spoofax.compiler.util.TypeInfo

plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.adapter")
  id("org.metaborg.gradle.config.junit-testing")
}

adapterProjectCompiler {
  settings.set(AdapterProjectCompilerSettings(
    parser = ParserCompiler.AdapterProjectInput.builder(),
    styler = StylerCompiler.AdapterProjectInput.builder(),
    completer = CompleterCompiler.AdapterProjectInput.builder(),
    strategoRuntime = builder(),
    constraintAnalyzer = ConstraintAnalyzerCompiler.AdapterProjectInput.builder(),
    compiler = run {
      val taskPackageId = "mb.sdf3.spoofax.task"
      val commandPackageId = "mb.sdf3.spoofax.command"

      val builder = AdapterProjectCompiler.Input.builder()

      val toCompletionColorer = TypeInfo.of(taskPackageId, "Sdf3ToCompletionColorer")
      builder.addTaskDefs(toCompletionColorer)

      builder
    }
  ))
}

dependencies {
  testAnnotationProcessor(platform("$group:spoofax.depconstraints:$version"))
  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testImplementation("org.metaborg:pie.runtime")
  testImplementation("org.metaborg:pie.dagger")
  testCompileOnly("org.checkerframework:checker-qual-android")
  testAnnotationProcessor("com.google.dagger:dagger-compiler")
}

tasks.test {
  // Show standard out and err in tests during development.
  testLogging {
    events(org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT, org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR)
    showStandardStreams = true
  }
}
