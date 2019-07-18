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
    private final TigerShowScopeGraph showScopeGraph;
    private final TigerShowDesugaredAst showDesugaredAst;


    @Inject public TigerInstance(
        TigerCheck check,
        TigerTokenize tokenize,
        TigerStyle style,

        TigerShowParsedAst showParsedAst,
        TigerShowPrettyPrintedText showPrettyPrintedText,
        TigerShowAnalyzedAst showAnalyzedAst,
        TigerShowScopeGraph showScopeGraph,
        TigerShowDesugaredAst showDesugaredAst
    ) {
        this.check = check;
        this.tokenize = tokenize;
        this.style = style;

        this.showParsedAst = showParsedAst;
        this.showPrettyPrintedText = showPrettyPrintedText;
        this.showAnalyzedAst = showAnalyzedAst;
        this.showScopeGraph = showScopeGraph;
        this.showDesugaredAst = showDesugaredAst;
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
        return CollectionView.of(showParsedAst, showPrettyPrintedText);
    }

    @Override public CollectionView<TransformDef> getAutoTransformDefs() {
        return CollectionView.of();
    }


    @Override public ListView<MenuItem> getMainMenuItems() {
        return ListView.of(
            new Menu("Debug",
                new Menu("Syntax",
                    onceTransformAction(showParsedAst), contTransformAction(showParsedAst),
                    onceTransformAction(showPrettyPrintedText), contTransformAction(showPrettyPrintedText)
                ),
                new Menu("Static Semantics",
                    onceTransformAction(showAnalyzedAst), contTransformAction(showAnalyzedAst),
                    onceTransformAction(showScopeGraph), contTransformAction(showScopeGraph)
                ),
                new Menu("Transformations",
                    onceTransformAction(showDesugaredAst), contTransformAction(showDesugaredAst)
                )
            )
        );
    }

    @Override public ListView<MenuItem> getResourceContextMenuItems() {
        return getMainMenuItems();
    }

    @Override public ListView<MenuItem> getEditorContextMenuItems() {
        return getMainMenuItems();
    }


    @Override public CollectionView<Shortcut> getShortcuts() {
        return CollectionView.of();
    }


    private static TransformAction transformAction(TransformDef transformDef, TransformExecutionType executionType, String suffix) {
        return new TransformAction(new TransformRequest(transformDef, executionType), transformDef.getDisplayName() + suffix);
    }

    private static TransformAction onceTransformAction(TransformDef transformDef) {
        return transformAction(transformDef, TransformExecutionType.OneShot, " (once)");
    }

    private static TransformAction contTransformAction(TransformDef transformDef) {
        return transformAction(transformDef, TransformExecutionType.Continuous, " (continuous)");
    }
}
