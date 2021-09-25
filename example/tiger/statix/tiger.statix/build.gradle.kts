import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.gradle.spoofax2.plugin.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.spoofax2.language.*
import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.util.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  testImplementation(compositeBuild("spoofax.test"))
  testCompileOnly("org.checkerframework:checker-qual-android")
}

languageProject {
  shared {
    name("Tiger")
    defaultPackageId("mb.tiger.statix")
  }
  compilerInput {
    withParser().run {
      startSymbol("Module")
    }
    withStyler()
    withConstraintAnalyzer().run {
      enableNaBL2(false)
      enableStatix(true)
      multiFile(false)
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
    project
      .languageSpecificationDependency(GradleDependency.project(":org.metaborg.lang.tiger.statix"))
  }
}

languageAdapterProject {
  compilerInput {
    withParser()
    withStyler()
    withConstraintAnalyzer()
    withStrategoRuntime()
//    withReferenceResolution().run {
//      resolveStrategy("editor-resolve")
//    }
//    withHover().run {
//      hoverStrategy("editor-hover")
//    }
    project.configureCompilerInput()
  }
}
fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
//  val packageId = "mb.cfg"
//  val taskPackageId = "$packageId.task"
//
//  addAdditionalModules(packageId, "CfgCustomizerModule");
//
//  // Config object creation tasks.
//  val normalize = TypeInfo.of(taskPackageId, "CfgNormalize")
//  val toObject = TypeInfo.of(taskPackageId, "CfgToObject")
//  val rootDirectoryToObject = TypeInfo.of(taskPackageId, "CfgRootDirectoryToObject")
//  addTaskDefs(normalize, toObject, rootDirectoryToObject)
//
//  // Manual multi-file check implementation.
//  isMultiFile(false)
//  val spoofaxTaskPackageId = "$taskPackageId.spoofax"
//  baseCheckTaskDef(spoofaxTaskPackageId, "BaseCfgCheck")
//  extendCheckTaskDef(spoofaxTaskPackageId, "CfgCheck")
}
