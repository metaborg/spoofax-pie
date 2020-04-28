import mb.spoofax.compiler.command.ArgProviderRepr
import mb.spoofax.compiler.command.CommandDefRepr
import mb.spoofax.compiler.command.ParamRepr
import mb.spoofax.compiler.gradle.spoofaxcore.AdapterProjectCompilerSettings
import mb.spoofax.compiler.menu.CommandActionRepr
import mb.spoofax.compiler.menu.MenuItemRepr
import mb.spoofax.compiler.spoofaxcore.AdapterProjectCompiler
import mb.spoofax.compiler.spoofaxcore.CompleterCompiler
import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzerCompiler
import mb.spoofax.compiler.spoofaxcore.ParserCompiler
import mb.spoofax.compiler.spoofaxcore.StrategoRuntimeCompiler.AdapterProjectInput.builder
import mb.spoofax.compiler.spoofaxcore.StylerCompiler
import mb.spoofax.compiler.util.TypeInfo
import mb.spoofax.core.language.command.CommandContextType
import mb.spoofax.core.language.command.EnclosingCommandContextType
import mb.spoofax.core.language.command.CommandExecutionType

plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.adapter")
}

adapterProjectCompiler {
  settings.set(AdapterProjectCompilerSettings(
    parser = ParserCompiler.AdapterProjectInput.builder(),
    styler = StylerCompiler.AdapterProjectInput.builder(),
    completer = CompleterCompiler.AdapterProjectInput.builder(),
    strategoRuntime = builder(),
    constraintAnalyzer = ConstraintAnalyzerCompiler.AdapterProjectInput.builder(),
    compiler = run {
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
