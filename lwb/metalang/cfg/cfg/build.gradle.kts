import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

dependencies {
  api(project(":spoofax.lwb.compiler.cfg"))

  compileOnly("org.derive4j:derive4j-annotation")

  // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
  compileOnly("com.google.code.findbugs:jsr305")

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
