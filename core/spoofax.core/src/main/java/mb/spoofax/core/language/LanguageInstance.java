package mb.spoofax.core.language;

import mb.common.message.KeyedMessages;
import mb.common.region.Region;
import mb.common.style.Styling;
import mb.common.token.Token;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.completions.common.CompletionResult;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
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

    /**
     * Creates a task that produces completions.
     *
     * @param resourceKey      the key of the resource in which completion is invoked
     * @param primarySelection the primary selection at which completion is invoked
     * @return a {@link CompletionResult}; or {@code null} when no completions could be generated
     */
    Task<@Nullable CompletionResult> createCompletionTask(ResourceKey resourceKey, Region primarySelection);

    Task<@Nullable KeyedMessages> createCheckTask(ResourcePath projectRoot);

    CollectionView<CommandDef<?>> getCommandDefs();

    CollectionView<AutoCommandRequest<?>> getAutoCommandRequests();


    CliCommand getCliCommand();


    ListView<MenuItem> getMainMenuItems();

    ListView<MenuItem> getResourceContextMenuItems();

    ListView<MenuItem> getEditorContextMenuItems();
}
