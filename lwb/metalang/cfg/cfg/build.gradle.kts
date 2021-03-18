import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
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
    name("CFG")
    defaultClassPrefix("Cfg")
    defaultPackageId("mb.cfg")
  }
  compilerInput {
    withParser().run {
      startSymbol("Configuration")
    }
    withStyler()
    withStrategoRuntime()
  }
}
spoofax2BasedLanguageProject {
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime().run {
      copyCtree(true)
      copyClasses(false)
    }
    project.languageSpecificationDependency(GradleDependency.project(":cfg.spoofax2"))
  }
}

languageAdapterProject {
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime()
    project.configureCompilerInput()
  }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  val packageId = "mb.cfg"
  val taskPackageId = "$packageId.task"

  // Config object creation tasks.
  val toObject = TypeInfo.of(taskPackageId, "CfgToObject")
  val rootDirectoryToObject = TypeInfo.of(taskPackageId, "CfgRootDirectoryToObject")
  addTaskDefs(toObject, rootDirectoryToObject)

  // Manual multi-file check implementation.
  isMultiFile(true)
  val spoofaxTaskPackageId = "$taskPackageId.spoofax"
  baseCheckMultiTaskDef(spoofaxTaskPackageId, "GeneratedCfgCheckMulti")
  extendCheckMultiTaskDef(spoofaxTaskPackageId, "CfgCheckMulti")
}
