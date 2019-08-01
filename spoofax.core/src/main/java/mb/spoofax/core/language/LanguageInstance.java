package mb.spoofax.core.language;

import mb.common.message.KeyedMessages;
import mb.common.style.Styling;
import mb.common.token.Token;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.core.language.shortcut.Shortcut;
import mb.spoofax.core.language.transform.TransformDef;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Set;

public interface LanguageInstance {
    String getDisplayName();

    SetView<String> getFileExtensions();


    Task<@Nullable ArrayList<? extends Token<?>>> createTokenizeTask(ResourceKey resourceKey);

    Task<@Nullable Styling> createStyleTask(ResourceKey resourceKey);

    Task<KeyedMessages> createCheckTask(ResourceKey resourceKey);


    CollectionView<TransformDef<?>> getTransformDefs();

    CollectionView<TransformDef<?>> getAutoTransformDefs();


    ListView<MenuItem> getMainMenuItems();

    ListView<MenuItem> getResourceContextMenuItems();

    ListView<MenuItem> getEditorContextMenuItems();


    CollectionView<Shortcut> getShortcuts();
}
