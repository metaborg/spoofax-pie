import mb.common.util.ListView
import mb.spoofax.compiler.cli.CliCommandRepr
import mb.spoofax.compiler.cli.CliParamRepr
import mb.spoofax.compiler.command.ArgProviderRepr
import mb.spoofax.compiler.command.AutoCommandDefRepr
import mb.spoofax.compiler.command.CommandDefRepr
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

      val showArgs = TypeInfo.of(taskPackageId, "TigerShowArgs")
      val showParsedAst = TypeInfo.of(taskPackageId, "TigerShowParsedAst")
      val listDefNames = TypeInfo.of(taskPackageId, "TigerListDefNames")
      val listLiteralVals = TypeInfo.of(taskPackageId, "TigerListLiteralVals")
      val compileFile = TypeInfo.of(taskPackageId, "TigerCompileFile")
      val altCompileFile = TypeInfo.of(taskPackageId, "TigerAltCompileFile")

      val showParsedAstCommand = CommandDefRepr.builder()
        .type(commandPackageId, "TigerShowParsedAstCommand")
        .taskDefType(showParsedAst)
        .argType(showArgs)
        .displayName("Show parsed AST")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
        .addRequiredContextTypes(CommandContextType.Resource)
        .addParams("resource", TypeInfo.of("mb.resource", "ResourceKey"), true, Optional.empty(), listOf(ArgProviderRepr.context()))
        .addParams("region", TypeInfo.of("mb.common.region", "Region"), false, Optional.empty(), listOf(ArgProviderRepr.context()))
        .build()

      val compileFileCommand = CommandDefRepr.builder()
        .type(TypeInfo.of(commandPackageId, "TigerCompileFileCommand"))
        .taskDefType(compileFile)
        .argType(taskPackageId, "TigerCompileFile.Args")
        .displayName("'Compile' file (list literals)")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous, CommandExecutionType.AutomaticContinuous)
        .addRequiredContextTypes(CommandContextType.File)
        .addParams("file", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, Optional.empty(), listOf(ArgProviderRepr.context()))
        .build()

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

      AdapterProjectCompiler.Input.builder()
        .addTaskDefs(
          showParsedAst,
          listDefNames,
          listLiteralVals,
          compileFile,
          altCompileFile
        )
        .addCommandDefs(
          showParsedAstCommand,
          compileFileCommand,
          altCompileFileCommand
        )
        .addAutoCommandDefs(AutoCommandDefRepr.builder()
          .commandDef(compileFileCommand.type())
          .build()
        )
        .addAutoCommandDefs(AutoCommandDefRepr.builder()
          .commandDef(altCompileFileCommand.type())
          .putRawArgs("base64Encode", "true")
          .build()
        )
        .cliCommand(CliCommandRepr.builder()
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
        .addEditorContextMenuItems(
          MenuCommandActionRepr.builder()
            .commandDefType(showParsedAstCommand.type())
            .executionType(CommandExecutionType.ManualContinuous)
            .build()
        )
    }
  ))
}
