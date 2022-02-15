import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.util.*
import mb.spoofax.common.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  api(compositeBuild("spoofax.common"))
  api(compositeBuild("spoofax.compiler"))
  api(compositeBuild("spoofax.compiler.dagger"))

  compileOnly("org.derive4j:derive4j-annotation")

  // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
  compileOnly("com.google.code.findbugs:jsr305")

  compileOnly("org.checkerframework:checker-qual-android")
  compileOnly("org.immutables:value-annotations")
  annotationProcessor("org.immutables:value")
  annotationProcessor("org.derive4j:derive4j")
}

languageProject {
  shared {
    name("Dynamix")
    defaultClassPrefix("Dynamix")
    defaultPackageId("mb.dynamix")
  }
  compilerInput {
    withParser().run {
      startSymbol("Start")
    }
    withStyler()
    withConstraintAnalyzer().run {
      enableNaBL2(false)
      enableStatix(true)
      multiFile(false) // TODO(molenzwiebel): Dynamix multi-file support
    }
    withStrategoRuntime()
  }
}
spoofax2BasedLanguageProject {
  compilerInput {
    withParser()
    withStyler()
    withConstraintAnalyzer().run {
      copyStatix(true)
    }
    withStrategoRuntime().run {
      copyCtree(true)
      copyClasses(false)
    }
    project.languageSpecificationDependency(GradleDependency.project(":dynamix.spoofax2"))
  }
}

languageAdapterProject {
  compilerInput {
    withParser()
    withStyler()
    withConstraintAnalyzer()
    withStrategoRuntime()
    withReferenceResolution().run {
      resolveStrategy("editor-resolve")
    }
    withHover().run {
      hoverStrategy("editor-hover")
    }
    project.configureCompilerInput()
  }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  val packageId = "mb.dynamix"
  val taskPackageId = "$packageId.task"

  // Symbols
  addLineCommentSymbols("//")
  addBlockCommentSymbols(BlockCommentSymbols("/*", "*/"))
  addBracketSymbols(BracketSymbols('[', ']'))
  addBracketSymbols(BracketSymbols('{', '}'))
  addBracketSymbols(BracketSymbols('(', ')'))

  baseComponent(packageId, "BaseDynamixComponent")
  extendComponent(packageId, "DynamixComponent")

  /*// Manual multi-file check implementation.
  isMultiFile(false) // TODO(molenzwiebel): Dynamix multi-file support
  val spoofaxTaskPackageId = "$taskPackageId.spoofax"
  baseCheckTaskDef(spoofaxTaskPackageId, "BaseDynamixCheck")
  extendCheckTaskDef(spoofaxTaskPackageId, "DynamixCheck")*/
  isMultiFile(false)
}
