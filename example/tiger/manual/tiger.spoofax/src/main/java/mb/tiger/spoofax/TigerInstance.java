package mb.tiger.spoofax;

import mb.common.codecompletion.CodeCompletionResult;
import mb.common.editor.HoverResult;
import mb.common.editor.ReferenceResolutionResult;
import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.common.style.Styling;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.common.util.SetView;
import mb.jsglr.common.JSGLRTokens;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecException;
import mb.pie.api.Session;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.common.BlockCommentSymbols;
import mb.spoofax.common.BracketSymbols;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.Version;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.ResourceExports;
import mb.spoofax.core.language.cli.CliCommand;
import mb.spoofax.core.language.cli.CliParam;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.EditorFileType;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.spoofax.core.language.menu.CommandAction;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.core.language.taskdef.NoneHoverTaskDef;
import mb.spoofax.core.language.taskdef.NoneResolveTaskDef;
import mb.spt.api.parse.ParseResult;
import mb.spt.api.parse.TestableParse;
import mb.tiger.spoofax.command.TigerCompileDirectoryCommand;
import mb.tiger.spoofax.command.TigerCompileFileAltCommand;
import mb.tiger.spoofax.command.TigerCompileFileCommand;
import mb.tiger.spoofax.command.TigerInlineMethodCallCommand;
import mb.tiger.spoofax.command.TigerShowAnalyzedAstCommand;
import mb.tiger.spoofax.command.TigerShowDesugaredAstCommand;
import mb.tiger.spoofax.command.TigerShowParsedAstCommand;
import mb.tiger.spoofax.command.TigerShowPrettyPrintedTextCommand;
import mb.tiger.spoofax.task.TigerCheck;
import mb.tiger.spoofax.task.TigerCheckAggregator;
import mb.tiger.spoofax.task.TigerIdeTokenize;
import mb.tiger.spoofax.task.TigerInlineMethodCall;
import mb.tiger.spoofax.task.reusable.TigerParse;
import mb.tiger.spoofax.task.reusable.TigerStyle;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

public class TigerInstance implements LanguageInstance, TestableParse {
    public final static SetView<String> extensions = SetView.of("tig");

    private final TigerParse parse;
    private final TigerCheck check;
    private final TigerCheckAggregator checkAggregate;
    private final TigerStyle style;
    private final TigerIdeTokenize tokenize;
    private final NoneResolveTaskDef resolve;
    private final NoneHoverTaskDef hover;

    private final TigerShowParsedAstCommand showParsedAstCommand;
    private final TigerShowPrettyPrintedTextCommand showPrettyPrintedTextCommand;
    private final TigerShowAnalyzedAstCommand showAnalyzedAstCommand;
    private final TigerShowDesugaredAstCommand showDesugaredAstCommand;
    private final TigerCompileFileCommand compileFileCommand;
    private final TigerCompileFileAltCommand altCompileFileCommand;
    private final TigerCompileDirectoryCommand compileDirectoryCommand;
    private final TigerInlineMethodCallCommand inlineMethodCallCommand;

    private final CollectionView<CommandDef<?>> commandDefs;
    private final CollectionView<AutoCommandRequest<?>> autoCommandDefs;


    @Inject public TigerInstance(
        TigerParse parse,
        TigerCheck check,
        TigerCheckAggregator checkAggregate,
        TigerStyle style,
        TigerIdeTokenize tokenize,
        NoneResolveTaskDef resolve,
        NoneHoverTaskDef hover,

        TigerShowParsedAstCommand showParsedAstCommand,
        TigerShowPrettyPrintedTextCommand showPrettyPrintedTextCommand,
        TigerShowAnalyzedAstCommand showAnalyzedAstCommand,
        TigerShowDesugaredAstCommand showDesugaredAstCommand,
        TigerCompileFileCommand compileFileCommand,
        TigerCompileFileAltCommand altCompileFileCommand,
        TigerCompileDirectoryCommand compileDirectoryCommand,
        TigerInlineMethodCallCommand inlineMethodCallCommand,

        Set<CommandDef<?>> commandDefs,
        Set<AutoCommandRequest<?>> autoCommandDefs
    ) {
        this.parse = parse;
        this.check = check;
        this.checkAggregate = checkAggregate;
        this.style = style;
        this.tokenize = tokenize;
        this.resolve = resolve;
        this.hover = hover;

        this.showParsedAstCommand = showParsedAstCommand;
        this.showPrettyPrintedTextCommand = showPrettyPrintedTextCommand;
        this.showAnalyzedAstCommand = showAnalyzedAstCommand;
        this.showDesugaredAstCommand = showDesugaredAstCommand;
        this.compileFileCommand = compileFileCommand;
        this.altCompileFileCommand = altCompileFileCommand;
        this.compileDirectoryCommand = compileDirectoryCommand;
        this.inlineMethodCallCommand = inlineMethodCallCommand;

        this.commandDefs = CollectionView.copyOf(commandDefs);
        this.autoCommandDefs = CollectionView.copyOf(autoCommandDefs);
    }


    @Override public Coordinate getCoordinate() {
        return new Coordinate("org.metaborg", "tiger", new Version(0, 1, 0));
    }

    @Override public String getDisplayName() {
        return "Tiger";
    }

    @Override public SetView<String> getFileExtensions() {
        return extensions;
    }


    @Override public Task<Option<JSGLRTokens>> createTokenizeTask(ResourceKey resourceKey) {
        return tokenize.createTask(resourceKey);
    }

    @Override
    public Task<Option<Styling>> createStyleTask(ResourceKey resourceKey, @Nullable ResourcePath rootDirectoryHint) {
        return style.createTask(parse.inputBuilder().withFile(resourceKey).rootDirectoryHint(Optional.ofNullable(rootDirectoryHint)).buildRecoverableTokensSupplier().map(Result::ok));
    }

    @Override
    public Option<Task<Result<CodeCompletionResult, ?>>> createCodeCompletionTask(Region primarySelection, ResourceKey resourceKey, @Nullable ResourcePath rootDirectoryHint) {
        return Option.ofNone();
    }

    @Override
    public Task<KeyedMessages> createCheckOneTask(ResourceKey file, @Nullable ResourcePath rootDirectoryHint) {
        return check.createTask(new TigerCheck.Input(file, rootDirectoryHint));
    }

    @Override
    public Task<KeyedMessages> createCheckTask(ResourcePath projectRoot) {
        return checkAggregate.createTask(projectRoot);
    }

    @Override
    public Task<Option<ReferenceResolutionResult>> createResolveTask(ResourcePath rootDirectory, ResourceKey file, Region region) {
        return resolve.createTask(NoneResolveTaskDef.Args.Empty);
    }

    @Override
    public Task<Option<HoverResult>> createHoverTask(ResourcePath rootDirectory, ResourceKey file, Region region) {
        return hover.createTask(NoneHoverTaskDef.Args.Empty);
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
            )),
            CliCommand.of("inline-method-call", "Inlines a method call", inlineMethodCallCommand, ListView.of(
                CliParam.positional("resource", 0, "FILE", "Source file to inline"),
                CliParam.option("region", ListView.of("-r", "--region"), false, "REGION", "Region in source file to inline")
            )))
        );
    }


    @Override public ListView<MenuItem> getMainMenuItems() {
        return getEditorContextMenuItems();
    }

    @Override public ListView<MenuItem> getResourceContextMenuItems() {
        return ListView.of(
            MenuItem.menu("Compile",
                CommandAction.builder().manualOnce(compileFileCommand).fileRequired().buildItem(),
                CommandAction.builder().manualOnce(compileDirectoryCommand).directoryRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- default").fileRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))).fileRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))).fileRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt"))).fileRequired().buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- default").buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))).fileRequired().buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))).fileRequired().buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt"))).fileRequired().buildItem()
            ),
            MenuItem.menu("Debug",
                MenuItem.menu("Syntax",
                    CommandAction.builder().manualOnce(showParsedAstCommand).buildItem(),
                    CommandAction.builder().manualOnce(showPrettyPrintedTextCommand).buildItem()
                ),
                MenuItem.menu("Static Semantics",
                    CommandAction.builder().manualOnce(showAnalyzedAstCommand).buildItem()
                ),
                MenuItem.menu("Transformations",
                    CommandAction.builder().manualOnce(showDesugaredAstCommand).buildItem(),
                    CommandAction.builder().manualOnce(inlineMethodCallCommand).buildItem()
                )
            )
        );
    }

    @Override public ListView<MenuItem> getEditorContextMenuItems() {
        return ListView.of(
            MenuItem.menu("Compile",
                CommandAction.builder().manualOnce(compileFileCommand).fileRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- default").fileRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))).fileRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))).fileRequired().buildItem(),
                CommandAction.builder().manualOnce(altCompileFileCommand, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt"))).fileRequired().buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- default").fileRequired().buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))).fileRequired().buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))).fileRequired().buildItem(),
                CommandAction.builder().manualContinuous(altCompileFileCommand, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt"))).fileRequired().buildItem()
            ),
            MenuItem.menu("Debug",
                MenuItem.menu("Syntax",
                    CommandAction.builder().manualOnce(showParsedAstCommand).buildItem(),
                    CommandAction.builder().manualContinuous(showParsedAstCommand).buildItem(),
                    CommandAction.builder().manualOnce(showPrettyPrintedTextCommand).buildItem(),
                    CommandAction.builder().manualContinuous(showPrettyPrintedTextCommand).buildItem()
                ),
                MenuItem.menu("Static Semantics",
                    CommandAction.builder().manualOnce(showAnalyzedAstCommand).buildItem(),
                    CommandAction.builder().manualContinuous(showAnalyzedAstCommand).buildItem()
                ),
                MenuItem.menu("Transformations",
                    CommandAction.builder().manualOnce(showDesugaredAstCommand).buildItem(),
                    CommandAction.builder().manualContinuous(showDesugaredAstCommand).buildItem()
                )
            )
        );
    }

    @Override public ListView<String> getLineCommentSymbols() {
        return ListView.of();
    }

    @Override public ListView<BlockCommentSymbols> getBlockCommentSymbols() {
        return ListView.of();
    }

    @Override public ListView<BracketSymbols> getBracketSymbols() {
        return ListView.of();
    }


    @Override public ResourceExports getResourceExports() {
        return id -> ListView.of(); // Nothing to export
    }


    @Override
    public Result<ParseResult, ?> testParse(Session session, ResourceKey resource, @Nullable ResourcePath rootDirectoryHint) throws InterruptedException {
        final Result<JsglrParseOutput, JsglrParseException> result;
        try {
            result = session.requireWithoutObserving(parse.createTask(JsglrParseTaskInput.builder()
                .withFile(resource)
                .rootDirectoryHint(Optional.ofNullable(rootDirectoryHint))
                .build()
            ));
            return result.mapOrElse(
                o -> Result.ofOk(new ParseResult(true, o.recovered, o.ambiguous, o.messages)),
                e -> e.caseOf()
                    .parseFail_(Result.ofOk(new ParseResult()))
                    .otherwise_(Result.ofErr(e))
            );
        } catch(ExecException e) {
            return Result.ofErr(e);
        }
    }

    @Override
    public Result<IStrategoTerm, ?> testParseToAterm(Session session, ResourceKey resource, @Nullable ResourcePath rootDirectoryHint) throws InterruptedException {
        try {
            return session.requireWithoutObserving(parse.createTask(JsglrParseTaskInput.builder()
                .withFile(resource)
                .rootDirectoryHint(Optional.ofNullable(rootDirectoryHint))
                .build()
            )).map(r -> r.ast);
        } catch(ExecException e) {
            return Result.ofErr(e);
        }
    }
}
