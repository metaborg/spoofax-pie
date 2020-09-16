import mb.spoofax.compiler.adapter.*
import mb.spoofax.compiler.adapter.data.*
import mb.spoofax.compiler.gradle.plugin.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.*

plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.adapter")
}

languageAdapterProject {
  languageProject.set(project(":mod"))
  settings.set(AdapterProjectSettings().apply { builder.configureBuilder() })
}

fun AdapterProjectBuilder.configureBuilder() {
  withParser()
  withStyler()
  withStrategoRuntime()
  withConstraintAnalyzer()
  adapterProject.run { configureCompilerInput() }
}

fun AdapterProjectCompiler.Input.Builder.configureCompilerInput() {
  val taskPackageId = "mb.mod.spoofax.task"
  val commandPackageId = "mb.mod.spoofax.command"

  // Show scope graph
  val showScopeGraph = TypeInfo.of(taskPackageId, "ModShowScopeGraph")
  addTaskDefs(showScopeGraph)
  val showScopeGraphCommand = CommandDefRepr.builder()
    .type(commandPackageId, "ModShowScopeGraphCommand")
    .taskDefType(showScopeGraph)
    .argType(showScopeGraph.appendToId(".Args"))
    .displayName("Show scope graph")
    .description("Shows the scope graph for the program")
    .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
    .addAllParams(listOf(
      ParamRepr.of("project", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, ArgProviderRepr.enclosingContext(EnclosingCommandContextType.Project)),
      ParamRepr.of("file", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.File))
    ))
    .build()
  addCommandDefs(showScopeGraphCommand)

  // Menu bindings
  val mainAndEditorMenu = listOf(
    MenuItemRepr.menu("Debug",
      MenuItemRepr.menu("Static Semantics",
        CommandActionRepr.builder().manualOnce(showScopeGraphCommand).fileRequired().enclosingProjectRequired().buildItem(),
        CommandActionRepr.builder().manualContinuous(showScopeGraphCommand).fileRequired().enclosingProjectRequired().buildItem()
      )
    )
  )
  addAllMainMenuItems(mainAndEditorMenu)
  addAllEditorContextMenuItems(mainAndEditorMenu)
  addResourceContextMenuItems(
    MenuItemRepr.menu("Debug",
      MenuItemRepr.menu("Static Semantics",
        CommandActionRepr.builder().manualOnce(showScopeGraphCommand).fileRequired().enclosingProjectRequired().buildItem()
      )
    )
  )
}
