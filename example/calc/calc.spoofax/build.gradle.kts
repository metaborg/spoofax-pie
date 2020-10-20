import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.adapter.data.*
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

dependencies {
  testImplementation("org.metaborg:log.backend.slf4j")
  testImplementation("org.slf4j:slf4j-simple:1.7.30")
  testImplementation("org.metaborg:pie.runtime")
  testImplementation("com.google.jimfs:jimfs:1.1")
  testCompileOnly("org.checkerframework:checker-qual-android")
}

languageAdapterProject {
  languageProject.set(project(":calc"))
  compilerInput {
    withParser()
    withStyler()
    withConstraintAnalyzer()
    withStrategoRuntime()
    project.configureCompilerInput()
  }
}

fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  val packageId = "mb.calc.spoofax"

  // Tasks
  val taskPackageId = "$packageId.task"
  val toJava = TypeInfo.of(taskPackageId, "CalcToJava")
  addTaskDefs(toJava)

  // Debugging tasks
  val taskDebugPackageId = "$taskPackageId.debug"
  val showToJava = TypeInfo.of(taskDebugPackageId, "CalcShowToJava")
  addTaskDefs(showToJava)

  // Commands
  val commandPackageId = "$packageId.command"
  val showToJavaCommand = CommandDefRepr.builder()
    .type(commandPackageId, "CalcShowToJavaCommand")
    .taskDefType(showToJava)
    .argType(showToJava.appendToId(".Args"))
    .displayName("To Java")
    .description("Transforms the program to a Java implementation")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
    .addAllParams(listOf(
      ParamRepr.of("file", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.File))
    ))
    .build()
  addCommandDefs(showToJavaCommand)

  // Menu bindings
  val mainAndEditorMenu = listOf(
    MenuItemRepr.menu("Debug",
      MenuItemRepr.menu("Transformations",
        CommandActionRepr.builder().manualOnce(showToJavaCommand).fileRequired().enclosingProjectRequired().buildItem(),
        CommandActionRepr.builder().manualContinuous(showToJavaCommand).fileRequired().enclosingProjectRequired().buildItem()
      )
    )
  )
  addAllMainMenuItems(mainAndEditorMenu)
  addAllEditorContextMenuItems(mainAndEditorMenu)
  addResourceContextMenuItems(
    MenuItemRepr.menu("Debug",
      MenuItemRepr.menu("Transformations",
        CommandActionRepr.builder().manualOnce(showToJavaCommand).fileRequired().enclosingProjectRequired().buildItem()
      )
    )
  )
}
