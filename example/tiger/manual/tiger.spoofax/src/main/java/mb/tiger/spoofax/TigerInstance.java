package mb.tiger.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.style.Styling;
import mb.common.token.Token;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.common.util.SetView;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.cli.CliCommand;
import mb.spoofax.core.language.cli.CliParam;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.spoofax.core.language.menu.Menu;
import mb.spoofax.core.language.menu.MenuItem;
import mb.tiger.spoofax.command.TigerAltCompileFileCommand;
import mb.tiger.spoofax.command.TigerCompileDirectoryCommand;
import mb.tiger.spoofax.command.TigerCompileFileCommand;
import mb.tiger.spoofax.command.TigerShowAnalyzedAstCommand;
import mb.tiger.spoofax.command.TigerShowDesugaredAstCommand;
import mb.tiger.spoofax.command.TigerShowParsedAstCommand;
import mb.tiger.spoofax.command.TigerShowPrettyPrintedTextCommand;
import mb.tiger.spoofax.task.TigerCheck;
import mb.tiger.spoofax.task.TigerParse;
import mb.tiger.spoofax.task.TigerStyle;
import mb.tiger.spoofax.task.TigerTokenize;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Set;

import static mb.spoofax.core.language.menu.CommandAction.ofManualContinuous;
import static mb.spoofax.core.language.menu.CommandAction.ofManualOnce;

public class TigerInstance implements LanguageInstance {
    private final static SetView<String> extensions = SetView.of("tig");

    private final TigerParse parse;
    private final TigerCheck tigerCheck;
    private final TigerStyle style;
    private final TigerTokenize tokenize;

    private final TigerShowParsedAstCommand showParsedAstCommand;
    private final TigerShowPrettyPrintedTextCommand showPrettyPrintedTextCommand;
    private final TigerShowAnalyzedAstCommand showAnalyzedAstCommand;
    private final TigerShowDesugaredAstCommand showDesugaredAstCommand;
    private final TigerCompileFileCommand compileFileCommand;
    private final TigerAltCompileFileCommand altCompileFileCommand;
    private final TigerCompileDirectoryCommand compileDirectoryCommand;

    private final CollectionView<CommandDef<?>> commandDefs;
    private final CollectionView<AutoCommandRequest<?>> autoCommandDefs;


    @Inject public TigerInstance(
        TigerParse parse,
        TigerCheck tigerCheck,
        TigerStyle style,
        TigerTokenize tokenize,

        TigerShowParsedAstCommand showParsedAstCommand,
        TigerShowPrettyPrintedTextCommand showPrettyPrintedTextCommand,
        TigerShowAnalyzedAstCommand showAnalyzedAstCommand,
        TigerShowDesugaredAstCommand showDesugaredAstCommand,
        TigerCompileFileCommand compileFileCommand,
        TigerAltCompileFileCommand altCompileFileCommand,
        TigerCompileDirectoryCommand compileDirectoryCommand,

        Set<CommandDef<?>> commandDefs,
        Set<AutoCommandRequest<?>> autoCommandDefs
    ) {
        this.parse = parse;
        this.tigerCheck = tigerCheck;
        this.style = style;
        this.tokenize = tokenize;

        this.showParsedAstCommand = showParsedAstCommand;
        this.showPrettyPrintedTextCommand = showPrettyPrintedTextCommand;
        this.showAnalyzedAstCommand = showAnalyzedAstCommand;
        this.showDesugaredAstCommand = showDesugaredAstCommand;
        this.compileFileCommand = compileFileCommand;
        this.altCompileFileCommand = altCompileFileCommand;
        this.compileDirectoryCommand = compileDirectoryCommand;

        this.commandDefs = CollectionView.copyOf(commandDefs);
        this.autoCommandDefs = CollectionView.copyOf(autoCommandDefs);
    }


    @Override public String getDisplayName() {
        return "Tiger";
    }

    @Override public SetView<String> getFileExtensions() {
        return extensions;
    }


    @Override public Task<@Nullable ArrayList<? extends Token<?>>> createTokenizeTask(ResourceKey resourceKey) {
        return tokenize.createTask(resourceKey);
    }

    @Override public Task<@Nullable Styling> createStyleTask(ResourceKey resourceKey) {
        return style.createTask(parse.createTokensProvider(resourceKey));
    }

    @Override public Task<KeyedMessages> createCheckTask(ResourceKey resourceKey) {
        return tigerCheck.createTask(resourceKey);
    }


    @Override public CollectionView<CommandDef<?>> getCommandDefs() {
        return commandDefs;
    }

    @Override public CollectionView<AutoCommandRequest<?>> getAutoCommandRequests() {
        return autoCommandDefs;
    }


    @Override public CliCommand getCliCommand() {
        return CliCommand.of("tiger", "Tiger language command-line interface", ListView.of(
            CliCommand.of("parse", "Parses Tiger sources and shows the parsed AST", showParsedAstCommand, ListView.of(
                CliParam.positional("resource", 0, "FILE", "Source file to parse"),
                CliParam.option("region", ListView.of("-r", "--region"), false, "REGION", "Region in source file to parse")
            )),
            CliCommand.of("pretty-print", "Pretty-prints Tiger sources", showPrettyPrintedTextCommand, ListView.of(
                CliParam.positional("resource", 0, "FILE", "Source file to pretty-print"),
                CliParam.option("region", ListView.of("-r", "--region"), false, "REGION", "Region in source file to pretty-print")
            )),
            CliCommand.of("analyze", "Analyzes Tiger sources and shows the analyzed AST", showAnalyzedAstCommand, ListView.of(
                CliParam.positional("resource", 0, "FILE", "Source file to analyze"),
                CliParam.option("region", ListView.of("-r", "--region"), false, "REGION", "Region in source file to analyze")
            )),
            CliCommand.of("desugar", "Desugars Tiger sources and shows the desugared AST", showDesugaredAstCommand, ListView.of(
                CliParam.positional("resource", 0, "FILE", "Source file to desugar"),
                CliParam.option("region", ListView.of("-r", "--region"), false, "REGION", "Region in source file to desugar")
            )),
            CliCommand.of("compile-file", "Compiles Tiger sources and shows the compiled file", compileFileCommand, ListView.of(
                CliParam.positional("file", 0, "FILE", "File to compile")
            )),
            CliCommand.of("alt-compile-file", "Compiles Tiger sources in an alternative way and shows the compiled file", altCompileFileCommand, ListView.of(
                CliParam.positional("file", 0, "FILE", "File to compile"),
                CliParam.option("listDefNames", ListView.of("-l", "--no-defnames"), true, "", "Whether to list definition names intead of literal values"),
                CliParam.option("base64Encode", ListView.of("-b", "--base64"), false, "", "Whether to Base64 encode the result"),
                CliParam.option("compiledFileNameSuffix", ListView.of("-s", "--suffix"), false, "SUFFIX", "Suffix to append to the compiled file name")
            )),
            CliCommand.of("compile-dir", "Compiles Tiger sources in given directory and shows the compiled file", compileDirectoryCommand, ListView.of(
                CliParam.positional("dir", 0, "DIR", "Directory to compile")
            )))
        );
    }


    @Override public ListView<MenuItem> getMainMenuItems() {
        return getEditorContextMenuItems();
    }

    @Override public ListView<MenuItem> getResourceContextMenuItems() {
        return ListView.of(
            new Menu("Compile",
                ofManualOnce(compileFileCommand),
                ofManualOnce(compileDirectoryCommand),
                ofManualOnce(altCompileFileCommand, "- default"),
                ofManualOnce(altCompileFileCommand, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))),
                ofManualOnce(altCompileFileCommand, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))),
                ofManualOnce(altCompileFileCommand, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt"))),
                ofManualContinuous(altCompileFileCommand, "- default"),
                ofManualContinuous(altCompileFileCommand, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))),
                ofManualContinuous(altCompileFileCommand, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))),
                ofManualContinuous(altCompileFileCommand, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt")))
            ),
            new Menu("Debug",
                new Menu("Syntax",
                    ofManualOnce(showParsedAstCommand),
                    ofManualOnce(showPrettyPrintedTextCommand)
                ),
                new Menu("Static Semantics",
                    ofManualOnce(showAnalyzedAstCommand)
                ),
                new Menu("Transformations",
                    ofManualOnce(showDesugaredAstCommand)
                )
            )
        );
    }

    @Override public ListView<MenuItem> getEditorContextMenuItems() {
        return ListView.of(
            new Menu("Compile",
                ofManualOnce(compileFileCommand),
                ofManualOnce(altCompileFileCommand, "- default"),
                ofManualOnce(altCompileFileCommand, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))),
                ofManualOnce(altCompileFileCommand, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))),
                ofManualOnce(altCompileFileCommand, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt"))),
                ofManualContinuous(altCompileFileCommand, "- default"),
                ofManualContinuous(altCompileFileCommand, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))),
                ofManualContinuous(altCompileFileCommand, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))),
                ofManualContinuous(altCompileFileCommand, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt")))
            ),
            new Menu("Debug",
                new Menu("Syntax",
                    ofManualOnce(showParsedAstCommand), ofManualContinuous(showParsedAstCommand),
                    ofManualOnce(showPrettyPrintedTextCommand), ofManualContinuous(showPrettyPrintedTextCommand)
                ),
                new Menu("Static Semantics",
                    ofManualOnce(showAnalyzedAstCommand), ofManualContinuous(showAnalyzedAstCommand)
                ),
                new Menu("Transformations",
                    ofManualOnce(showDesugaredAstCommand), ofManualContinuous(showDesugaredAstCommand)
                )
            )
        );
    }
}
