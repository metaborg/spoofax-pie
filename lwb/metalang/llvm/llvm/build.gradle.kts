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

  compileOnly("org.derive4j:derive4j-annotation")

  // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
  compileOnly("com.google.code.findbugs:jsr305")

  compileOnly("org.checkerframework:checker-qual-android")
  compileOnly("org.immutables:value-annotations")
  annotationProcessor("org.immutables:value")
  annotationProcessor("org.derive4j:derive4j")
}

val packageId = "mb.llvm"
val taskPackageId = "$packageId.task"
val spoofaxTaskPackageId = "$taskPackageId.spoofax"
val debugTaskPackageId = "$taskPackageId.debug"
val commandPackageId = "$packageId.command"

languageProject {
  shared {
    name("LLVM")
    defaultClassPrefix("LLVM")
    defaultPackageId("mb.llvm")
    addFileExtensions("ll")
  }
  compilerInput {
    withParser().run {
      startSymbol("Start")
    }
    withStyler()
    withConstraintAnalyzer().run {
      enableNaBL2(false)
      enableStatix(true)
      multiFile(false)
    }
    withStrategoRuntime().run {
      addStrategyPackageIds("llvm.strategies")
      addInteropRegisterersByReflection("llvm.strategies.InteropRegisterer")
    }
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
      copyClasses(true)
    }
    project.languageSpecificationDependency(GradleDependency.project(":llvm.spoofax2"))
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
    withGetSourceFiles()
    project.configureCompilerInput()
  }
}

fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  compositionGroup("mb.spoofax.lwb")

  // Symbols
  addLineCommentSymbols(";")
  addBracketSymbols(BracketSymbols('[', ']'))
  addBracketSymbols(BracketSymbols('{', '}'))
  addBracketSymbols(BracketSymbols('(', ')'))

  isMultiFile(false)
}
