import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.adapter.data.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.*
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

val packageId = "mb.dynamix"
val taskPackageId = "$packageId.task"
val spoofaxTaskPackageId = "$taskPackageId.spoofax"
val debugTaskPackageId = "$taskPackageId.debug"
val commandPackageId = "$packageId.command"

languageProject {
  shared {
    name("Dynamix")
    defaultClassPrefix("Dynamix")
    defaultPackageId("mb.dynamix")
    addFileExtensions("dx")
  }
  compilerInput {
    withParser().run {
      startSymbol("DynamixProgram")
    }
    withStyler()
    withConstraintAnalyzer().run {
      enableNaBL2(false)
      enableStatix(true)
      multiFile(true)
    }
    withStrategoRuntime().run {
      addStrategyPackageIds("dynamix.strategies")
      addInteropRegisterersByReflection("dynamix.strategies.InteropRegisterer")
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
    project.languageSpecificationDependency(GradleDependency.project(":dynamix.spoofax2"))
  }
}

languageAdapterProject {
  compilerInput {
    withParser().run {
      // Wrap Parse task
      extendParseTaskDef(spoofaxTaskPackageId, "DynamixParseWrapper")
    }
    withStyler()
    withConstraintAnalyzer().run {
      // Wrap AnalyzeMulti and rename base task
      baseAnalyzeMultiTaskDef(spoofaxTaskPackageId, "BaseDynamixAnalyzeMulti")
      extendAnalyzeMultiTaskDef(spoofaxTaskPackageId, "DynamixAnalyzeMultiWrapper")
    }
    withStrategoRuntime()
    withReferenceResolution().run {
      resolveStrategy("editor-resolve")
    }
    withHover().run {
      hoverStrategy("editor-hover")
    }
    withGetSourceFiles().run {
      extendGetSourceFilesTaskDef(spoofaxTaskPackageId, "DynamixGetSourceFiles")
      baseGetSourceFilesTaskDef(spoofaxTaskPackageId, "BaseDynamixGetSourceFiles")
    }
    project.configureCompilerInput()
  }
}

fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  compositionGroup("mb.spoofax.lwb")

  // Symbols
  addLineCommentSymbols("//")
  addBlockCommentSymbols(BlockCommentSymbols("/*", "*/"))
  addBracketSymbols(BracketSymbols('[', ']'))
  addBracketSymbols(BracketSymbols('{', '}'))
  addBracketSymbols(BracketSymbols('(', ')'))

  baseComponent(packageId, "BaseDynamixComponent")
  extendComponent(packageId, "DynamixComponent")

  addTaskDefs(taskPackageId, "DynamixCompileModule")
  addTaskDefs(taskPackageId, "DynamixCompileProject")
  addTaskDefs(taskPackageId, "DynamixCompileAndMergeProject")
  addTaskDefs(taskPackageId, "DynamixPrettyPrint")

  isMultiFile(true)

  val showMerged = TypeInfo.of(debugTaskPackageId, "DynamixShowMerged")
  addTaskDefs(
    showMerged
  )

  val showMergedCommand = CommandDefRepr.builder()
    .type(commandPackageId, showMerged.id() + "Command")
    .taskDefType(showMerged)
    .argType(showMerged.appendToId(".Args"))
    .displayName("Show normalized merged form")
    .description("Shows the normalized and merged form in which definitions and calls are fully qualified")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
    .addAllParams(listOf(
      ParamRepr.of("rootDirectory", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)),
      ParamRepr.of("file", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.File))
    ))
    .build()
  val showMergedCommandMenuItem = CommandActionRepr.builder()
    .manualOnce(showMergedCommand)
    .fileRequired()
    .enclosingProjectRequired()
    .buildItem()

  val commands = listOf(
    showMergedCommand
  )
  addAllCommandDefs(commands)

  val menuItems = listOf(
    MenuItemRepr.menu("Evaluate", listOf(showMergedCommandMenuItem))
  )
  addAllMainMenuItems(menuItems)
  addAllEditorContextMenuItems(menuItems)
}
