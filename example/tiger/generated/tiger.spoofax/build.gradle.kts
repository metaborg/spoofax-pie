import mb.common.util.ListView
import mb.spoofax.compiler.cli.CliCommandRepr
import mb.spoofax.compiler.cli.CliParamRepr
import mb.spoofax.compiler.command.ArgProviderRepr
import mb.spoofax.compiler.command.AutoCommandDefRepr
import mb.spoofax.compiler.command.CommandDefRepr
import mb.spoofax.compiler.command.ParamRepr
import mb.spoofax.compiler.gradle.spoofaxcore.AdapterProjectCompilerSettings
import mb.spoofax.compiler.menu.MenuCommandActionRepr
import mb.spoofax.compiler.menu.MenuRepr
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
        .description("Shows the parsed abstract syntax tree of the program")
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
        .description("Shows the desugared abstract syntax tree of the program")
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
        .description("Shows the analyzed abstract syntax tree of the program")
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
        .description("Shows a pretty-printed version of the program")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
        .addRequiredContextTypes(CommandContextType.Resource)
        .addAllParams(showParams)
        .build()
      builder.addCommandDefs(showPrettyPrintedTextCommand)

      val showScopeGraph = TypeInfo.of(taskPackageId, "TigerShowScopeGraph")
      builder.addTaskDefs(showScopeGraph)
      val showScopeGraphCommand = CommandDefRepr.builder()
        .type(commandPackageId, "TigerShowScopeGraphCommand")
        .taskDefType(showScopeGraph)
        .argType(showArgs)
        .displayName("Show scope graph")
        .description("Shows the scope graph for the program")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
        .addRequiredContextTypes(CommandContextType.Resource)
        .addAllParams(showParams)
        .build()
      builder.addCommandDefs(showScopeGraphCommand)

      // Compilation tasks and commands
      val compileFile = TypeInfo.of(taskPackageId, "TigerCompileFile")
      builder.addTaskDefs(compileFile)
      val compileFileCommand = CommandDefRepr.builder()
        .type(TypeInfo.of(commandPackageId, "TigerCompileFileCommand"))
        .taskDefType(compileFile)
        .argType(compileFile.appendToId(".Args"))
        .displayName("'Compile' file (list literals)")
        .description("Compiles the file (lists literals) and shows the compiled file")
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
        .argType(compileDirectory.appendToId(".Args"))
        .displayName("'Compile' directory (list definition names)")
        .description("Compiles the directory (list definition names) and shows the compiled file")
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
        .argType(altCompileFile.appendToId(".Args"))
        .displayName("'Alternative compile' file")
        .description("Compiles the file in an alternative way and shows the compiled file")
        .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous, CommandExecutionType.AutomaticContinuous)
        .addRequiredContextTypes(CommandContextType.File)
        .addParams("file", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, Optional.empty(), listOf(ArgProviderRepr.context()))
        .addParams("listDefNames", TypeInfo.ofBoolean(), false, Optional.empty(), listOf(ArgProviderRepr.value("true")))
        .addParams("base64Encode", TypeInfo.ofBoolean(), false, Optional.empty(), listOf(ArgProviderRepr.value("false")))
        .addParams("compiledFileNameSuffix", TypeInfo.ofString(), true, Optional.empty(), listOf(ArgProviderRepr.value(StringUtil.doubleQuote("defnames.aterm"))))
        .build()
      builder.addCommandDefs(altCompileFileCommand)

      // CLI bindings
      fun showParams(operation: String) = listOf(
        CliParamRepr.positional("resource", 0, "FILE", "Source file to $operation"),
        CliParamRepr.option("region", ListView.of("-r", "--region"), false, "REGION", "Region in source file to $operation")
      )
      builder.cliCommand(CliCommandRepr.of(
        "tiger",
        "Tiger language command-line interface",
        CliCommandRepr.of("parse", showParsedAstCommand.type(), showParams("parse")),
        CliCommandRepr.of("pretty-print", showPrettyPrintedTextCommand.type(), showParams("pretty-print")),
        CliCommandRepr.of("analyze", showAnalyzedAstCommand.type(), showParams("analyze")),
        CliCommandRepr.of("desugar", showDesugaredAstCommand.type(), showParams("desugar")),
        CliCommandRepr.of("scope-graph", showScopeGraphCommand.type(), showParams("show the scope graph for")),
        CliCommandRepr.of("compile-file", compileFileCommand.type(),
          CliParamRepr.positional("file", 0, "FILE", "File to compile")
        ),
        CliCommandRepr.of("alt-compile-file", altCompileFileCommand.type(),
          CliParamRepr.positional("file", 0, "FILE", "File to compile"),
          CliParamRepr.option("listDefNames", ListView.of("-l", "--no-defnames"), true, "", "Whether to list definition names intead of literal values"),
          CliParamRepr.option("base64Encode", ListView.of("-b", "--base64"), false, "", "Whether to Base64 encode the result"),
          CliParamRepr.option("compiledFileNameSuffix", ListView.of("-s", "--suffix"), false, "SUFFIX", "Suffix to append to the compiled file name")
        ),
        CliCommandRepr.of("compile-dir", compileDirectoryCommand.type(),
          CliParamRepr.positional("dir", 0, "DIR", "Directory to compile")
        )
      ))

      // Menu bindings
      fun CommandDefRepr.action(execType: CommandExecutionType, suffix: String = "", initialArgs: Map<String, String> = mapOf()) = MenuCommandActionRepr.of(type(), execType, "${displayName()}$suffix", initialArgs)
      fun CommandDefRepr.actionOnce(suffix: String = "", initialArgs: Map<String, String> = mapOf()) = action(CommandExecutionType.ManualOnce, "$suffix (once)", initialArgs)
      fun CommandDefRepr.actionCont(suffix: String = "", initialArgs: Map<String, String> = mapOf()) = action(CommandExecutionType.ManualContinuous, "$suffix (continuous)", initialArgs)
      val altCompileFileActions = listOf(
        altCompileFileCommand.actionOnce("- default"),
        altCompileFileCommand.actionOnce("- list literal values instead", mapOf(Pair("listDefNames", "false"), Pair("compiledFileNameSuffix", "\"litvals.aterm\""))),
        altCompileFileCommand.actionOnce("- base64 encode", mapOf(Pair("base64Encode", "true"), Pair("compiledFileNameSuffix", "\"defnames_base64.txt\""))),
        altCompileFileCommand.actionOnce("- list literal values instead + base64 encode", mapOf(Pair("listDefNames", "false"), Pair("base64Encode", "true"), Pair("compiledFileNameSuffix", "\"litvals_base64.txt\""))),
        altCompileFileCommand.actionCont("- default"),
        altCompileFileCommand.actionCont("- list literal values instead", mapOf(Pair("listDefNames", "false"), Pair("compiledFileNameSuffix", "\"litvals.aterm\""))),
        altCompileFileCommand.actionCont("- base64 encode", mapOf(Pair("base64Encode", "true"), Pair("compiledFileNameSuffix", "\"defnames_base64.txt\""))),
        altCompileFileCommand.actionCont("- list literal values instead + base64 encode", mapOf(Pair("listDefNames", "false"), Pair("base64Encode", "true"), Pair("compiledFileNameSuffix", "\"litvals_base64.txt\"")))
      )
      val mainAndEditorMenu = listOf(
        MenuRepr.of("Compile", listOf(
          compileFileCommand.action(CommandExecutionType.ManualOnce)
        ) + altCompileFileActions),
        MenuRepr.of("Debug",
          MenuRepr.of("Syntax", showParsedAstCommand.actionOnce(), showParsedAstCommand.actionCont()),
          MenuRepr.of("Static Semantics",
            showAnalyzedAstCommand.actionOnce(), showAnalyzedAstCommand.actionCont(),
            showScopeGraphCommand.actionOnce(), showScopeGraphCommand.actionCont()
          ),
          MenuRepr.of("Transformations", showDesugaredAstCommand.actionOnce(), showDesugaredAstCommand.actionCont())
        )
      )
      builder.addAllMainMenuItems(mainAndEditorMenu)
      builder.addAllEditorContextMenuItems(mainAndEditorMenu)
      builder.addResourceContextMenuItems(
        MenuRepr.of("Compile", listOf(
          compileFileCommand.action(CommandExecutionType.ManualOnce),
          compileDirectoryCommand.action(CommandExecutionType.ManualOnce)
        ) + altCompileFileActions),
        MenuRepr.of("Debug",
          MenuRepr.of("Syntax", showParsedAstCommand.actionOnce()),
          MenuRepr.of("Static Semantics",
            showAnalyzedAstCommand.actionOnce(),
            showScopeGraphCommand.actionOnce()
          ),
          MenuRepr.of("Transformations", showDesugaredAstCommand.actionOnce())
        )
      )
    }
  ))
}
