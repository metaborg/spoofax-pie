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
import mb.spoofax.core.language.cli.CliCommandItem;
import mb.spoofax.core.language.cli.CliCommandList;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.spoofax.core.language.menu.Menu;
import mb.spoofax.core.language.menu.MenuItem;
import mb.tiger.spoofax.taskdef.TigerCheck;
import mb.tiger.spoofax.taskdef.TigerStyle;
import mb.tiger.spoofax.taskdef.TigerTokenize;
import mb.tiger.spoofax.taskdef.command.TigerAltCompileFile;
import mb.tiger.spoofax.taskdef.command.TigerCompileDirectory;
import mb.tiger.spoofax.taskdef.command.TigerCompileFile;
import mb.tiger.spoofax.taskdef.command.TigerShowAnalyzedAst;
import mb.tiger.spoofax.taskdef.command.TigerShowDesugaredAst;
import mb.tiger.spoofax.taskdef.command.TigerShowParsedAst;
import mb.tiger.spoofax.taskdef.command.TigerShowPrettyPrintedText;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;

import static mb.spoofax.core.language.menu.CommandAction.ofManualContinuous;
import static mb.spoofax.core.language.menu.CommandAction.ofManualOnce;

public class TigerInstance implements LanguageInstance {
    private final static SetView<String> extensions = SetView.of("tig");

    private final TigerCheck check;
    private final TigerStyle style;
    private final TigerTokenize tokenize;
    private final TigerShowParsedAst showParsedAst;
    private final TigerShowPrettyPrintedText showPrettyPrintedText;
    private final TigerShowAnalyzedAst showAnalyzedAst;
    private final TigerShowDesugaredAst showDesugaredAst;
    private final TigerCompileFile compileFile;
    private final TigerAltCompileFile altCompileFile;
    private final TigerCompileDirectory compileDirectory;


    @Inject public TigerInstance(
        TigerCheck check,
        TigerTokenize tokenize,
        TigerStyle style,

        TigerShowParsedAst showParsedAst,
        TigerShowPrettyPrintedText showPrettyPrintedText,
        TigerShowAnalyzedAst showAnalyzedAst,
        TigerShowDesugaredAst showDesugaredAst,
        TigerCompileFile compileFile,
        TigerAltCompileFile altCompileFile,
        TigerCompileDirectory compileDirectory
    ) {
        this.check = check;
        this.tokenize = tokenize;
        this.style = style;

        this.showParsedAst = showParsedAst;
        this.showPrettyPrintedText = showPrettyPrintedText;
        this.showAnalyzedAst = showAnalyzedAst;
        this.showDesugaredAst = showDesugaredAst;
        this.compileFile = compileFile;
        this.altCompileFile = altCompileFile;
        this.compileDirectory = compileDirectory;
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
        return style.createTask(resourceKey);
    }

    @Override public Task<KeyedMessages> createCheckTask(ResourceKey resourceKey) {
        return check.createTask(resourceKey);
    }


    @Override public CollectionView<CommandDef<?>> getCommandDefs() {
        return CollectionView.<CommandDef<?>>of(
            showParsedAst,
            showPrettyPrintedText,
            showAnalyzedAst,
            showDesugaredAst,
            compileFile,
            altCompileFile,
            compileDirectory
        );
    }

    @Override public CollectionView<AutoCommandRequest<?>> getAutoCommandRequests() {
        return CollectionView.of(
            new AutoCommandRequest<Serializable>(compileFile),
            new AutoCommandRequest<Serializable>(compileDirectory)
        );
    }


    @Override public CliCommandItem getRootCliCommandItem() {
        return CliCommandList.of("tiger", "Tiger language command-line interface",
            showParsedAst.getCliCommandItem(),
            showPrettyPrintedText.getCliCommandItem(),
            showAnalyzedAst.getCliCommandItem(),
            showDesugaredAst.getCliCommandItem(),
            compileFile.getCliCommandItem(),
            altCompileFile.getCliCommandItem(),
            compileDirectory.getCliCommandItem()
        );
    }


    @Override public ListView<MenuItem> getMainMenuItems() {
        return getEditorContextMenuItems();
    }

    @Override public ListView<MenuItem> getResourceContextMenuItems() {
        return ListView.of(
            new Menu("Compile",
                ofManualOnce(compileFile),
                ofManualOnce(compileDirectory),
                ofManualOnce(altCompileFile, "- default"),
                ofManualOnce(altCompileFile, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))),
                ofManualOnce(altCompileFile, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))),
                ofManualOnce(altCompileFile, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt"))),
                ofManualContinuous(altCompileFile, "- default"),
                ofManualContinuous(altCompileFile, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))),
                ofManualContinuous(altCompileFile, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))),
                ofManualContinuous(altCompileFile, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt")))
            ),
            new Menu("Debug",
                new Menu("Syntax",
                    ofManualOnce(showParsedAst),
                    ofManualOnce(showPrettyPrintedText)
                ),
                new Menu("Static Semantics",
                    ofManualOnce(showAnalyzedAst)
                ),
                new Menu("Transformations",
                    ofManualOnce(showDesugaredAst)
                )
            )
        );
    }

    @Override public ListView<MenuItem> getEditorContextMenuItems() {
        return ListView.of(
            new Menu("Compile",
                ofManualOnce(compileFile),
                ofManualOnce(altCompileFile, "- default"),
                ofManualOnce(altCompileFile, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))),
                ofManualOnce(altCompileFile, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))),
                ofManualOnce(altCompileFile, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt"))),
                ofManualContinuous(altCompileFile, "- default"),
                ofManualContinuous(altCompileFile, "- list literal values instead", new RawArgs(MapView.of("listDefNames", false, "compiledFileNameSuffix", "litvals.aterm"))),
                ofManualContinuous(altCompileFile, "- base64 encode", new RawArgs(MapView.of("base64Encode", true, "compiledFileNameSuffix", "defnames_base64.txt"))),
                ofManualContinuous(altCompileFile, "- list literal values instead + base64 encode", new RawArgs(MapView.of("listDefNames", false, "base64Encode", true, "compiledFileNameSuffix", "litvals_base64.txt")))
            ),
            new Menu("Debug",
                new Menu("Syntax",
                    ofManualOnce(showParsedAst), ofManualContinuous(showParsedAst),
                    ofManualOnce(showPrettyPrintedText), ofManualContinuous(showPrettyPrintedText)
                ),
                new Menu("Static Semantics",
                    ofManualOnce(showAnalyzedAst), ofManualContinuous(showAnalyzedAst)
                ),
                new Menu("Transformations",
                    ofManualOnce(showDesugaredAst), ofManualContinuous(showDesugaredAst)
                )
            )
        );
    }
}
