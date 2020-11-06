import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.adapter.data.*
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.gradle.spoofax2.plugin.*
import mb.spoofax.compiler.language.*
import mb.spoofax.compiler.spoofax2.language.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofax2.language")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

dependencies {
  api("org.metaborg:stratego.build")
  api("org.metaborg:pie.task.java")
  api("org.metaborg:pie.task.archive")

  // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
  compileOnly("com.google.code.findbugs:jsr305")

  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testImplementation("org.metaborg:pie.runtime")
  testImplementation("com.google.jimfs:jimfs:1.1")
  testCompileOnly("org.checkerframework:checker-qual-android")
}

languageProject {
  shared {
    name("Stratego")
    defaultClassPrefix("Stratego")
    defaultPackageId("mb.str")
  }
  compilerInput {
    withParser().run {
      startSymbol("Module")
    }
    withStyler()
    withStrategoRuntime().run {
      addInteropRegisterersByReflection("org.metaborg.meta.lang.stratego.trans.InteropRegisterer")
    }
  }
}
spoofax2BasedLanguageProject {
  compilerInput {
    withParser()
    withStyler()
    withStrategoRuntime().run {
      copyCtree(false)
      copyClasses(true)
    }

    // Use group ID "org.metaborg.bootstraphack" when building as part of devenv (not standalone).
    val spoofax2GroupId = if(gradle.parent?.rootProject?.name == "spoofax3.root") "org.metaborg" else "org.metaborg.bootstraphack"
    val spoofax2Version = System.getProperty("spoofax2Version")
    project.languageSpecificationDependency(GradleDependency.module("$spoofax2GroupId:org.metaborg.meta.lang.stratego:$spoofax2Version"))
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
  val packageId = "mb.str"
  val incrPackageId = "$packageId.incr"
  val configPackageId = "$packageId.config"
  val taskPackageId = "$packageId.task"
  val commandPackageId = "$packageId.command"

  // Custom component and additional modules
  baseComponent(packageId, "GeneratedStrategoComponent")
  extendComponent(packageId, "StrategoComponent")
  addAdditionalModules(packageId, "JavaTasksModule")
  addAdditionalModules(incrPackageId, "StrategoIncrModule")
  addAdditionalModules(configPackageId, "StrategoConfigModule")

  // Manual multifile check implementation
  isMultiFile(true)
  baseCheckMultiTaskDef(taskPackageId, "GeneratedStrategoCheckMulti")
  extendCheckMultiTaskDef(taskPackageId, "StrategoCheckMulti")
  addTaskDefs(taskPackageId, "StrategoAnalyze")

  // Utility task definitions
  addTaskDefs(taskPackageId, "StrategoPrettyPrint")

  // Stratego incremental compiler task definitions
  val strBuildTaskPackageId = "mb.stratego.build.strincr"
  addTaskDefs(strBuildTaskPackageId, "StrIncr")
  addTaskDefs(strBuildTaskPackageId, "StrIncrAnalysis")
  addTaskDefs(strBuildTaskPackageId, "Frontend")
  addTaskDefs(strBuildTaskPackageId, "SubFrontend")
  addTaskDefs(strBuildTaskPackageId, "LibFrontend")
  addTaskDefs(strBuildTaskPackageId, "Backend")

  // Java compiler task definitions
  val pieTaskJavaPackageId = "mb.pie.task.java"
  val pieTaskArchivePackageId = "mb.pie.task.archive"
  addTaskDefs(pieTaskJavaPackageId, "CompileJava")
  addTaskDefs(pieTaskArchivePackageId, "ArchiveToJar")

  // Compilation task definitions
  val compileToJava = TypeInfo.of(taskPackageId, "StrategoCompileToJava")
  val compileToJavaEditor = TypeInfo.of(taskPackageId, "StrategoEditorCompileToJava")
  addTaskDefs(compileToJava, compileToJavaEditor)
  // Compilation commands
  val resourcePathType = TypeInfo.of("mb.resource.hierarchical", "ResourcePath")
  val compileToJavaCommand = CommandDefRepr.builder()
    .type(commandPackageId, compileToJavaEditor.id() + "Command")
    .taskDefType(compileToJavaEditor)
    .argType("mb.str.config", "StrategoCompileConfig")
    .displayName("Compile to Java")
    .description("Compiles Stratego source files to Java source files")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce)
    .addAllParams(listOf(
      ParamRepr.of("projectDir", resourcePathType, true, ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)),
      ParamRepr.of("mainFile", resourcePathType, true, ArgProviderRepr.context(CommandContextType.File)),
      ParamRepr.of("includeDirs", TypeInfo.of("mb.common.util", "ListView"), false, ArgProviderRepr.value("mb.common.util.ListView.of()")),
      ParamRepr.of("builtinLibs", TypeInfo.of("mb.common.util", "ListView"), false, ArgProviderRepr.value("mb.common.util.ListView.of()")),
      ParamRepr.of("cacheDir", resourcePathType, false),
      ParamRepr.of("outputDir", resourcePathType, true),
      ParamRepr.of("outputJavaPackageId", TypeInfo.ofString(), true)
    ))
    .build()
  addCommandDefs(compileToJavaCommand)

  // Show (debugging) task definitions
  val debugTaskPackageId = "$taskPackageId.debug"
  val showParsedAst = TypeInfo.of(debugTaskPackageId, "StrategoShowParsedAst")
  val showDesugaredAst = TypeInfo.of(debugTaskPackageId, "StrategoShowDesugaredAst")
  addTaskDefs(showParsedAst, showDesugaredAst)
  // Show (debugging) commands
  fun showCommand(taskDefType: TypeInfo, resultName: String) = CommandDefRepr.builder()
    .type(commandPackageId, taskDefType.id() + "Command")
    .taskDefType(taskDefType)
    .argType(TypeInfo.of(debugTaskPackageId, "StrategoShowArgs"))
    .displayName("Show $resultName")
    .description("Shows the $resultName of the file")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
    .addAllParams(listOf(
      ParamRepr.of("key", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.File)),
      ParamRepr.of("region", TypeInfo.of("mb.common.region", "Region"), false)
    ))
    .build()

  val showParsedAstCommand = showCommand(showParsedAst, "parsed AST")
  val showDesugaredAstCommand = showCommand(showDesugaredAst, "desugared AST")
  val showCommands = listOf(
    showParsedAstCommand,
    showDesugaredAstCommand
  )
  addAllCommandDefs(showCommands)

  // Menu bindings
  val mainAndEditorMenu = listOf(
    MenuItemRepr.menu("Compile",
      CommandActionRepr.builder().manualOnce(compileToJavaCommand).enclosingProjectRequired().fileRequired().buildItem()
    ),
    MenuItemRepr.menu("Debug",
      showCommands.flatMap { listOf(CommandActionRepr.builder().manualOnce(it).fileRequired().buildItem(), CommandActionRepr.builder().manualContinuous(it).fileRequired().buildItem()) }
    )
  )
  addAllMainMenuItems(mainAndEditorMenu)
  addAllEditorContextMenuItems(mainAndEditorMenu)
  addResourceContextMenuItems(
    MenuItemRepr.menu("Compile",
      CommandActionRepr.builder().manualOnce(compileToJavaCommand).enclosingProjectRequired().fileRequired().buildItem()
    ),
    MenuItemRepr.menu("Debug",
      showCommands.flatMap { listOf(CommandActionRepr.builder().manualOnce(it).fileRequired().buildItem()) }
    )
  )
}

// Additional dependencies which are injected into tests.
val classPathInjection = configurations.create("classPathInjection")
dependencies {
  classPathInjection(platform("$group:spoofax.depconstraints:$version"))
  classPathInjection("org.metaborg:org.strategoxt.strj")
}

tasks.test {
  // Pass classPathInjection to tests in the form of system properties
  dependsOn(classPathInjection)
  doFirst {
    // Wrap in doFirst to properly defer dependency resolution to the task execution phase.
    systemProperty("classPath", classPathInjection.resolvedConfiguration.resolvedArtifacts.map { it.file }.joinToString(File.pathSeparator))
  }
}
