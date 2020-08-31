import mb.spoofax.compiler.command.*
import mb.spoofax.compiler.gradle.spoofaxcore.*
import mb.spoofax.compiler.menu.*
import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.CommandContextType
import mb.spoofax.core.language.command.CommandExecutionType
import mb.spoofax.core.language.command.EnclosingCommandContextType

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.adapter")
}

dependencies {
  api("org.metaborg:stratego.build")
  api("org.metaborg:pie.task.java")

  // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
  compileOnly("com.google.code.findbugs:jsr305")

  testAnnotationProcessor(platform("$group:spoofax.depconstraints:$version"))
  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testImplementation("org.metaborg:pie.runtime")
  testImplementation("org.metaborg:pie.dagger")
  testImplementation("com.google.jimfs:jimfs:1.1")
  testCompileOnly("org.checkerframework:checker-qual-android")
  testAnnotationProcessor("com.google.dagger:dagger-compiler")
}

spoofaxAdapterProject {
  languageProject.set(project(":stratego"))
  settings.set(AdapterProjectSettings(
    parser = ParserCompiler.AdapterProjectInput.builder(),
    styler = StylerCompiler.AdapterProjectInput.builder(),
    completer = CompleterCompiler.AdapterProjectInput.builder(),
    strategoRuntime = StrategoRuntimeCompiler.AdapterProjectInput.builder(),

    builder = run {
      val packageId = "mb.str.spoofax"
      val incrPackageId = "$packageId.incr"
      val taskPackageId = "$packageId.task"
      val commandPackageId = "$packageId.command"

      val builder = AdapterProjectCompiler.Input.builder()

      builder.addAdditionalModules(packageId, "JavaTasksModule")
      builder.addAdditionalModules(incrPackageId, "StrategoIncrModule")

      // Manual component implementation
      builder.classKind(ClassKind.Extended)
      builder.genComponent(packageId, "GeneratedStrategoComponent")
      builder.manualComponent(packageId, "StrategoComponent")

      // Manual multifile check implementation
      builder.isMultiFile(true)
      builder.genCheckMultiTaskDef(taskPackageId, "GeneratedStrategoCheckMulti")
      builder.manualCheckMultiTaskDef(taskPackageId, "StrategoCheckMulti")
      builder.addTaskDefs(taskPackageId, "StrategoAnalyze")

      // Stratego incremental compiler task definitions
      val strBuildTaskPackageId = "mb.stratego.build.strincr"
      builder.addTaskDefs(strBuildTaskPackageId, "StrIncr")
      builder.addTaskDefs(strBuildTaskPackageId, "StrIncrAnalysis")
      builder.addTaskDefs(strBuildTaskPackageId, "Frontend")
      builder.addTaskDefs(strBuildTaskPackageId, "SubFrontend")
      builder.addTaskDefs(strBuildTaskPackageId, "LibFrontend")
      builder.addTaskDefs(strBuildTaskPackageId, "Backend")

      // Java compiler task definitions
      val pieTaskJavaPackageId = "mb.pie.task.java"
      builder.addTaskDefs(pieTaskJavaPackageId, "CompileJava")
      builder.addTaskDefs(pieTaskJavaPackageId, "CreateJar")

      // Compilation task definitions
      val compileToJava = TypeInfo.of(taskPackageId, "StrategoCompileToJava")
      val compileToJavaEditor = TypeInfo.of(taskPackageId, "StrategoEditorCompileToJava")
      builder.addTaskDefs(compileToJava, compileToJavaEditor)
      // Compilation commands
      val resourcePathType = TypeInfo.of("mb.resource.hierarchical", "ResourcePath")
      val compileToJavaCommand = CommandDefRepr.builder()
        .type(commandPackageId, compileToJavaEditor.id() + "Command")
        .taskDefType(compileToJavaEditor)
        .argType(compileToJavaEditor.appendToId(".Args"))
        .displayName("Compile to Java")
        .description("Compiles Stratego source files to Java source files")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce)
        .addAllParams(listOf(
          ParamRepr.of("projectDir", resourcePathType, true, ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)),
          ParamRepr.of("mainFile", resourcePathType, true, ArgProviderRepr.context(CommandContextType.File)),
          ParamRepr.of("includeDirs", TypeInfo.of("java.util", "ArrayList"), false, ArgProviderRepr.value("new java.util.ArrayList<>()")),
          ParamRepr.of("builtinLibs", TypeInfo.of("java.util", "ArrayList"), false, ArgProviderRepr.value("new java.util.ArrayList<>()")),
          ParamRepr.of("cacheDir", resourcePathType, false),
          ParamRepr.of("outputDir", resourcePathType, false),
          ParamRepr.of("outputJavaPackageId", TypeInfo.ofString(), false)
        ))
        .build()
      builder.addCommandDefs(compileToJavaCommand)

      // Show (debugging) task definitions
      val debugTaskPackageId = "$taskPackageId.debug"
      val showParsedAst = TypeInfo.of(debugTaskPackageId, "StrategoShowParsedAst")
      val showDesugaredAst = TypeInfo.of(debugTaskPackageId, "StrategoShowDesugaredAst")
      builder.addTaskDefs(showParsedAst, showDesugaredAst)
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
      builder.addAllCommandDefs(showCommands)

      // Menu bindings
      val mainAndEditorMenu = listOf(
        MenuItemRepr.menu("Compile",
          CommandActionRepr.builder().manualOnce(compileToJavaCommand).enclosingProjectRequired().fileRequired().buildItem()
        ),
        MenuItemRepr.menu("Debug",
          showCommands.flatMap { listOf(CommandActionRepr.builder().manualOnce(it).fileRequired().buildItem(), CommandActionRepr.builder().manualContinuous(it).fileRequired().buildItem()) }
        )
      )
      builder.addAllMainMenuItems(mainAndEditorMenu)
      builder.addAllEditorContextMenuItems(mainAndEditorMenu)
      builder.addResourceContextMenuItems(
        MenuItemRepr.menu("Compile",
          CommandActionRepr.builder().manualOnce(compileToJavaCommand).enclosingProjectRequired().fileRequired().buildItem()
        ),
        MenuItemRepr.menu("Debug",
          showCommands.flatMap { listOf(CommandActionRepr.builder().manualOnce(it).fileRequired().buildItem()) }
        )
      )

      builder
    }
  ))
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
