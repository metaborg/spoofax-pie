package mb.spoofax.core.language;

import mb.common.style.Styling;
import mb.common.token.Token;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.cli.CliCommand;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.menu.MenuItem;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;

public interface LanguageInstance {
    String getDisplayName();

    SetView<String> getFileExtensions();


    Task<@Nullable ArrayList<? extends Token<?>>> createTokenizeTask(ResourceKey resourceKey);

    Task<@Nullable Styling> createStyleTask(ResourceKey resourceKey);

    LanguageInspection getInspection();


    CollectionView<CommandDef<?>> getCommandDefs();

    CollectionView<AutoCommandRequest<?>> getAutoCommandRequests();


    CliCommand getCliCommand();


    ListView<MenuItem> getMainMenuItems();

    ListView<MenuItem> getResourceContextMenuItems();

    ListView<MenuItem> getEditorContextMenuItems();
}
