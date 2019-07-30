package mb.tiger.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.style.Styling;
import mb.common.token.Token;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.menu.Menu;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.core.language.menu.TransformAction;
import mb.spoofax.core.language.shortcut.Shortcut;
import mb.spoofax.core.language.transform.TransformDef;
import mb.spoofax.core.language.transform.TransformExecutionType;
import mb.spoofax.core.language.transform.TransformRequest;
import mb.tiger.spoofax.taskdef.TigerCheck;
import mb.tiger.spoofax.taskdef.TigerStyle;
import mb.tiger.spoofax.taskdef.TigerTokenize;
import mb.tiger.spoofax.taskdef.transform.*;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.ArrayList;

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


    @Override public CollectionView<TransformDef> getTransformDefs() {
        return CollectionView.of(
            showParsedAst,
            showPrettyPrintedText,
            showAnalyzedAst,
            showDesugaredAst,
            compileFile,
            altCompileFile,
            compileDirectory
        );
    }

    @Override public CollectionView<TransformDef> getAutoTransformDefs() {
        return CollectionView.of(
            compileFile,
            compileDirectory
        );
    }


    @Override public ListView<MenuItem> getMainMenuItems() {
        return getEditorContextMenuItems();
    }

    @Override public ListView<MenuItem> getResourceContextMenuItems() {
        return ListView.of(
            new Menu("Compile",
                manualOnceTransformAction(compileFile),
                manualOnceTransformAction(compileDirectory),
                manualOnceTransformAction(altCompileFile),
                manualContinuousTransformAction(altCompileFile)
            ),
            new Menu("Debug",
                new Menu("Syntax",
                    manualOnceTransformAction(showParsedAst),
                    manualOnceTransformAction(showPrettyPrintedText)
                ),
                new Menu("Static Semantics",
                    manualOnceTransformAction(showAnalyzedAst)
                ),
                new Menu("Transformations",
                    manualOnceTransformAction(showDesugaredAst)
                )
            )
        );
    }

    @Override public ListView<MenuItem> getEditorContextMenuItems() {
        return ListView.of(
            new Menu("Compile",
                manualOnceTransformAction(compileFile),
                manualOnceTransformAction(altCompileFile),
                manualContinuousTransformAction(altCompileFile)
            ),
            new Menu("Debug",
                new Menu("Syntax",
                    manualOnceTransformAction(showParsedAst), manualContinuousTransformAction(showParsedAst),
                    manualOnceTransformAction(showPrettyPrintedText), manualContinuousTransformAction(showPrettyPrintedText)
                ),
                new Menu("Static Semantics",
                    manualOnceTransformAction(showAnalyzedAst), manualContinuousTransformAction(showAnalyzedAst)
                ),
                new Menu("Transformations",
                    manualOnceTransformAction(showDesugaredAst), manualContinuousTransformAction(showDesugaredAst)
                )
            )
        );
    }


    @Override public CollectionView<Shortcut> getShortcuts() {
        return CollectionView.of();
    }


    private static TransformAction transformAction(TransformDef transformDef, TransformExecutionType executionType, String suffix) {
        return new TransformAction(new TransformRequest(transformDef, executionType), transformDef.getDisplayName() + suffix);
    }

    private static TransformAction transformAction(TransformDef transformDef, TransformExecutionType executionType) {
        return transformAction(transformDef, executionType, "");
    }

    private static TransformAction manualOnceTransformAction(TransformDef transformDef) {
        return transformAction(transformDef, TransformExecutionType.ManualOnce, "");
    }

    private static TransformAction manualContinuousTransformAction(TransformDef transformDef) {
        return transformAction(transformDef, TransformExecutionType.ManualContinuous, " (continuous)");
    }
}
