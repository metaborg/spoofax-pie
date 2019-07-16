package mb.tiger.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.style.Styling;
import mb.common.token.Token;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.AstResult;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.core.language.shortcut.Shortcut;
import mb.spoofax.core.language.transform.TransformDef;
import mb.tiger.spoofax.taskdef.TigerCheck;
import mb.tiger.spoofax.taskdef.TigerGetAST;
import mb.tiger.spoofax.taskdef.TigerStyle;
import mb.tiger.spoofax.taskdef.TigerTokenize;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;

public class TigerInstance implements LanguageInstance {
    private final static SetView<String> extensions = SetView.of("tig");

    private final TigerGetAST getAst;
    private final TigerCheck check;
    private final TigerStyle style;
    private final TigerTokenize tokenize;


    @Inject public TigerInstance(
        TigerGetAST getAst,
        TigerCheck check,
        TigerTokenize tokenize,
        TigerStyle style
    ) {
        this.getAst = getAst;
        this.check = check;
        this.tokenize = tokenize;
        this.style = style;
    }


    @Override public String getDisplayName() {
        return "Tiger";
    }

    @Override public SetView<String> getFileExtensions() {
        return extensions;
    }


    @Override public Task<AstResult> createGetAstTask(ResourceKey resourceKey) {
        return getAst.createTask(resourceKey);
    }

    @Override public Task<KeyedMessages> createCheckTask(ResourceKey resourceKey) {
        return check.createTask(resourceKey);
    }

    @Override public Task<@Nullable Styling> createStyleTask(ResourceKey resourceKey) {
        return style.createTask(resourceKey);
    }

    @Override public Task<@Nullable ArrayList<Token>> createTokenizeTask(ResourceKey resourceKey) {
        return tokenize.createTask(resourceKey);
    }


    @Override public CollectionView<TransformDef> getTransformDefs() {
        return new CollectionView<>(Collections.emptyList());
    }

    @Override public CollectionView<TransformDef> getAutoTransformDefs() {
        return new CollectionView<>(Collections.emptyList());
    }

    @Override public ListView<MenuItem> getMainMenus() {
        return new ListView<>(Collections.emptyList());
    }

    @Override public ListView<MenuItem> getResourceContextMenus() {
        return new ListView<>(Collections.emptyList());
    }

    @Override public ListView<MenuItem> getEditorContextMenus() {
        return new ListView<>(Collections.emptyList());
    }

    @Override public CollectionView<Shortcut> getShortcuts() {
        return new CollectionView<>(Collections.emptyList());
    }
}
