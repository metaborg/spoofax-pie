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
import mb.tiger.spoofax.taskdef.transform.TigerParsedAst;
import mb.tiger.spoofax.taskdef.transform.TigerPrettyPrint;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.util.ArrayList;

public class TigerInstance implements LanguageInstance {
    private final static SetView<String> extensions = SetView.of("tig");

    private final TigerCheck check;
    private final TigerStyle style;
    private final TigerTokenize tokenize;
    private final TigerParsedAst astTransform;
    private final TigerPrettyPrint prettyPrintTransform;


    @Inject public TigerInstance(
        TigerCheck check,
        TigerTokenize tokenize,
        TigerStyle style,
        TigerParsedAst astTransform,
        TigerPrettyPrint prettyPrintTransform
    ) {
        this.check = check;
        this.tokenize = tokenize;
        this.style = style;
        this.astTransform = astTransform;
        this.prettyPrintTransform = prettyPrintTransform;
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
        return CollectionView.of(astTransform, prettyPrintTransform);
    }

    @Override public CollectionView<TransformDef> getAutoTransformDefs() {
        return CollectionView.of();
    }


    @Override public ListView<MenuItem> getMainMenuItems() {
        return ListView.of(
            new Menu("Syntax", ListView.of(
                onceTransformAction(astTransform), contTransformAction(astTransform),
                onceTransformAction(prettyPrintTransform), contTransformAction(prettyPrintTransform)
            )),
            new Menu("Static Semantics", ListView.of(

            )),
            new Menu("Transformations", ListView.of(

            ))
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
