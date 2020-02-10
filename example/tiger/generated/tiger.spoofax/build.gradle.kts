import mb.common.util.ListView
import mb.spoofax.compiler.cli.CliCommandRepr
import mb.spoofax.compiler.cli.CliParamRepr
import mb.spoofax.compiler.command.ArgProviderRepr
import mb.spoofax.compiler.command.AutoCommandDefRepr
import mb.spoofax.compiler.command.CommandDefRepr
import mb.spoofax.compiler.command.ParamRepr
import mb.spoofax.compiler.gradle.spoofaxcore.AdapterProjectCompilerSettings
import mb.spoofax.compiler.menu.MenuCommandActionRepr
import mb.spoofax.compiler.spoofaxcore.AdapterProjectCompiler
import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzerCompiler
import mb.spoofax.compiler.spoofaxcore.ParserCompiler
import mb.spoofax.compiler.spoofaxcore.StrategoRuntimeCompiler
import mb.spoofax.compiler.spoofaxcore.StylerCompiler
import mb.spoofax.compiler.util.StringUtil
import mb.spoofax.compiler.util.TypeInfo
import mb.spoofax.core.language.command.CommandContextType
import mb.spoofax.core.language.command.CommandExecutionType
import java.util.Optional

plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.adapter")
}

adapterProjectCompiler {
  settings.set(AdapterProjectCompilerSettings(
    parser = ParserCompiler.AdapterProjectInput.builder(),
    styler = StylerCompiler.AdapterProjectInput.builder(),
    strategoRuntime = StrategoRuntimeCompiler.AdapterProjectInput.builder(),
    constraintAnalyzer = ConstraintAnalyzerCompiler.AdapterProjectInput.builder(),
    compiler = run {
      val taskPackageId = "mb.tiger.spoofax.task"
      val commandPackageId = "mb.tiger.spoofax.command"

      val builder = AdapterProjectCompiler.Input.builder();

      // Tasks that are not called by the user, but are re-used by other tasks.
      val listDefNames = TypeInfo.of(taskPackageId, "TigerListDefNames")
      builder.addTaskDefs(listDefNames)
      val listLiteralVals = TypeInfo.of(taskPackageId, "TigerListLiteralVals")
      builder.addTaskDefs(listLiteralVals)

      // Show parsed/desugar/analyzed/pretty-printed tasks and commands
      val showArgs = TypeInfo.of(taskPackageId, "TigerShowArgs")
      val showParams = listOf(
        ParamRepr.of("resource", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context()),
        ParamRepr.of("region", TypeInfo.of("mb.common.region", "Region"), false, ArgProviderRepr.context())
      )
      val showParsedAst = TypeInfo.of(taskPackageId, "TigerShowParsedAst")
      builder.addTaskDefs(showParsedAst)
      val showParsedAstCommand = CommandDefRepr.builder()
        .type(commandPackageId, "TigerShowParsedAstCommand")
        .taskDefType(showParsedAst)
        .argType(showArgs)
        .displayName("Show parsed AST")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
        .addRequiredContextTypes(CommandContextType.Resource)
        .addAllParams(showParams)
        .build()
      builder.addCommandDefs(showParsedAstCommand)

      val showDesugaredAst = TypeInfo.of(taskPackageId, "TigerShowDesugaredAst")
      builder.addTaskDefs(showDesugaredAst)
      val showDesugaredAstCommand = CommandDefRepr.builder()
        .type(commandPackageId, "TigerShowDesugaredAstCommand")
        .taskDefType(showDesugaredAst)
        .argType(showArgs)
        .displayName("Show desugared AST")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
        .addRequiredContextTypes(CommandContextType.Resource)
        .addAllParams(showParams)
        .build()
      builder.addCommandDefs(showDesugaredAstCommand)

      val showAnalyzedAst = TypeInfo.of(taskPackageId, "TigerShowAnalyzedAst")
      builder.addTaskDefs(showAnalyzedAst)
      val showAnalyzedAstCommand = CommandDefRepr.builder()
        .type(commandPackageId, "TigerShowAnalyzedAstCommand")
        .taskDefType(showAnalyzedAst)
        .argType(showArgs)
        .displayName("Show analyzed AST")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
        .addRequiredContextTypes(CommandContextType.Resource)
        .addAllParams(showParams)
        .build()
      builder.addCommandDefs(showAnalyzedAstCommand)

      val showPrettyPrintedText = TypeInfo.of(taskPackageId, "TigerShowPrettyPrintedText")
      builder.addTaskDefs(showPrettyPrintedText)
      val showPrettyPrintedTextCommand = CommandDefRepr.builder()
        .type(commandPackageId, "TigerShowPrettyPrintedTextCommand")
        .taskDefType(showPrettyPrintedText)
        .argType(showArgs)
        .displayName("Show pretty-printed text")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
        .addRequiredContextTypes(CommandContextType.Resource)
        .addAllParams(showParams)
        .build()
      builder.addCommandDefs(showPrettyPrintedTextCommand)

      // Compilation tasks and commands
      val compileFile = TypeInfo.of(taskPackageId, "TigerCompileFile")
      builder.addTaskDefs(compileFile)
      val compileFileCommand = CommandDefRepr.builder()
        .type(TypeInfo.of(commandPackageId, "TigerCompileFileCommand"))
        .taskDefType(compileFile)
        .argType(taskPackageId, "TigerCompileFile.Args")
        .displayName("'Compile' file (list literals)")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous, CommandExecutionType.AutomaticContinuous)
        .addRequiredContextTypes(CommandContextType.File)
        .addParams("file", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, Optional.empty(), listOf(ArgProviderRepr.context()))
        .build()
      builder.addCommandDefs(compileFileCommand)
      builder.addAutoCommandDefs(AutoCommandDefRepr.of(compileFileCommand.type()))

      val compileDirectory = TypeInfo.of(taskPackageId, "TigerCompileDirectory")
      builder.addTaskDefs(compileDirectory)
      val compileDirectoryCommand = CommandDefRepr.builder()
        .type(TypeInfo.of(commandPackageId, "TigerCompileDirectoryCommand"))
        .taskDefType(compileDirectory)
        .argType(taskPackageId, "TigerCompileDirectory.Args")
        .displayName("'Compile' directory (list definition names)")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous, CommandExecutionType.AutomaticContinuous)
        .addRequiredContextTypes(CommandContextType.Directory)
        .addParams("dir", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, Optional.empty(), listOf(ArgProviderRepr.context()))
        .build()
      builder.addCommandDefs(compileDirectoryCommand)
      builder.addAutoCommandDefs(AutoCommandDefRepr.of(compileDirectoryCommand.type()))

      val altCompileFile = TypeInfo.of(taskPackageId, "TigerAltCompileFile")
      builder.addTaskDefs(altCompileFile)
      val altCompileFileCommand = CommandDefRepr.builder()
        .type(TypeInfo.of(commandPackageId, "TigerAltCompileFileCommand"))
        .taskDefType(altCompileFile)
        .argType(taskPackageId, "TigerAltCompileFile.Args")
        .displayName("'Alternative compile' file")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous, CommandExecutionType.AutomaticContinuous)
        .addRequiredContextTypes(CommandContextType.File)
        .addParams("file", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, Optional.empty(), listOf(ArgProviderRepr.context()))
        .addParams("listDefNames", TypeInfo.ofBoolean(), false, Optional.empty(), listOf(ArgProviderRepr.value("true")))
        .addParams("base64Encode", TypeInfo.ofBoolean(), false, Optional.empty(), listOf(ArgProviderRepr.value("false")))
        .addParams("compiledFileNameSuffix", TypeInfo.ofString(), true, Optional.empty(), listOf(ArgProviderRepr.value(StringUtil.doubleQuote("defnames.aterm"))))
        .build()
      builder.addCommandDefs(altCompileFileCommand)

      // CLI bindings
      builder.cliCommand(CliCommandRepr.builder()
        .name("tiger")
        .description("Tiger language command-line interface")
        .addSubCommands(
          CliCommandRepr.builder()
            .name("parse")
            .description("Parses Tiger sources and shows the parsed AST")
            .commandDefType(showParsedAstCommand.type())
            .addParams(
              CliParamRepr.positional("resource", 0, "FILE", "Source file to parse", null),
              CliParamRepr.option("region", ListView.of("-r", "--region"), false, null, "Region in source file to parse", null)
            )
            .build()
        )
        .build()
      )

      // Editor menu bindings
      builder.addEditorContextMenuItems(
        MenuCommandActionRepr.of(showParsedAstCommand.type(), CommandExecutionType.ManualOnce, "${showParsedAstCommand.displayName()} (once)"),
        MenuCommandActionRepr.of(showParsedAstCommand.type(), CommandExecutionType.ManualContinuous, "${showParsedAstCommand.displayName()} (continuous)"),
        MenuCommandActionRepr.of(showDesugaredAstCommand.type(), CommandExecutionType.ManualOnce, "${showDesugaredAstCommand.displayName()} (once)"),
        MenuCommandActionRepr.of(showDesugaredAstCommand.type(), CommandExecutionType.ManualContinuous, "${showDesugaredAstCommand.displayName()} (continuous)")
      )
    }
  ))
}
