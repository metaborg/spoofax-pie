import mb.spoofax.compiler.command.*
import mb.spoofax.compiler.gradle.spoofaxcore.*
import mb.spoofax.compiler.menu.*
import mb.spoofax.compiler.spoofaxcore.*
import mb.spoofax.compiler.util.*
import mb.spoofax.core.language.command.CommandContextType
import mb.spoofax.core.language.command.CommandExecutionType
import mb.spoofax.core.language.command.EnclosingCommandContextType

plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.adapter")
}

spoofaxAdapterProject {
  languageProject.set(project(":mod"))
  settings.set(AdapterProjectSettings(
    parser = ParserCompiler.AdapterProjectInput.builder(),
    styler = StylerCompiler.AdapterProjectInput.builder(),
    completer = CompleterCompiler.AdapterProjectInput.builder(),
    strategoRuntime = StrategoRuntimeCompiler.AdapterProjectInput.builder(),
    constraintAnalyzer = ConstraintAnalyzerCompiler.AdapterProjectInput.builder(),

    builder = run {
      val taskPackageId = "mb.mod.spoofax.task"
      val commandPackageId = "mb.mod.spoofax.command"

      val builder = AdapterProjectCompiler.Input.builder();

      // Show scope graph
      val showScopeGraph = TypeInfo.of(taskPackageId, "ModShowScopeGraph")
      builder.addTaskDefs(showScopeGraph)
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
      builder.addCommandDefs(showScopeGraphCommand)


      // Menu bindings
      val mainAndEditorMenu = listOf(
        MenuItemRepr.menu("Debug",
          MenuItemRepr.menu("Static Semantics",
            CommandActionRepr.builder().manualOnce(showScopeGraphCommand).fileRequired().enclosingProjectRequired().buildItem(),
            CommandActionRepr.builder().manualContinuous(showScopeGraphCommand).fileRequired().enclosingProjectRequired().buildItem()
          )
        )
      )
      builder.addAllMainMenuItems(mainAndEditorMenu)
      builder.addAllEditorContextMenuItems(mainAndEditorMenu)
      builder.addResourceContextMenuItems(
        MenuItemRepr.menu("Debug",
          MenuItemRepr.menu("Static Semantics",
            CommandActionRepr.builder().manualOnce(showScopeGraphCommand).fileRequired().enclosingProjectRequired().buildItem()
          )
        )
      )
    }
  ))
}
