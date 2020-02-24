import mb.spoofax.compiler.command.ArgProviderRepr
import mb.spoofax.compiler.command.CommandDefRepr
import mb.spoofax.compiler.command.ParamRepr
import mb.spoofax.compiler.gradle.spoofaxcore.AdapterProjectCompilerSettings
import mb.spoofax.compiler.menu.MenuCommandActionRepr
import mb.spoofax.compiler.menu.MenuRepr
import mb.spoofax.compiler.spoofaxcore.AdapterProjectCompiler
import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzerCompiler
import mb.spoofax.compiler.spoofaxcore.ParserCompiler
import mb.spoofax.compiler.spoofaxcore.StrategoRuntimeCompiler.AdapterProjectInput.builder
import mb.spoofax.compiler.spoofaxcore.StylerCompiler
import mb.spoofax.compiler.spoofaxcore.CompleterCompiler
import mb.spoofax.compiler.util.TypeInfo
import mb.spoofax.core.language.command.CommandContextType
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
        .addRequiredContextTypes(CommandContextType.Project, CommandContextType.Resource)
        .addAllParams(listOf(
          ParamRepr.of("project", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, ArgProviderRepr.context()),
          ParamRepr.of("file", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context())
        ))
        .build()
      builder.addCommandDefs(showScopeGraphCommand)


      // Menu bindings
      fun CommandDefRepr.action(execType: CommandExecutionType, suffix: String = "", initialArgs: Map<String, String> = mapOf()) = MenuCommandActionRepr.of(type(), execType, "${displayName()}$suffix", initialArgs)
      fun CommandDefRepr.actionOnce(suffix: String = "", initialArgs: Map<String, String> = mapOf()) = action(CommandExecutionType.ManualOnce, "$suffix (once)", initialArgs)
      fun CommandDefRepr.actionCont(suffix: String = "", initialArgs: Map<String, String> = mapOf()) = action(CommandExecutionType.ManualContinuous, "$suffix (continuous)", initialArgs)
      val mainAndEditorMenu = listOf(
        MenuRepr.of("Debug",
          MenuRepr.of("Static Semantics",
            showScopeGraphCommand.actionOnce(), showScopeGraphCommand.actionCont()
          )
        )
      )
      builder.addAllMainMenuItems(mainAndEditorMenu)
      builder.addAllEditorContextMenuItems(mainAndEditorMenu)
      builder.addResourceContextMenuItems(
        MenuRepr.of("Debug",
          MenuRepr.of("Static Semantics", showScopeGraphCommand.actionOnce())
        )
      )
    }
  ))
}
