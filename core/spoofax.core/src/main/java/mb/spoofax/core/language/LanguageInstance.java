package mb.spoofax.core.language;

import mb.common.codecompletion.CodeCompletionResult;
import mb.common.editor.HoverResult;
import mb.common.editor.ReferenceResolutionResult;
import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.common.style.Styling;
import mb.common.token.Tokens;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.common.BracketSymbols;
import mb.spoofax.common.BlockCommentSymbols;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.language.cli.CliCommand;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.menu.MenuItem;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface LanguageInstance {
    Coordinate getCoordinate();

    String getDisplayName();

    SetView<String> getFileExtensions();


    Task<? extends Option<? extends Tokens<?>>> createTokenizeTask(ResourceKey resourceKey);

    Task<Option<Styling>> createStyleTask(ResourceKey file, @Nullable ResourcePath rootDirectoryHint);

    @Deprecated
    default Task<Option<Styling>> createStyleTask(ResourceKey file) {
        return createStyleTask(file, null);
    }

    Task<KeyedMessages> createCheckOneTask(ResourceKey file, @Nullable ResourcePath rootDirectoryHint);

    Task<KeyedMessages> createCheckTask(ResourcePath rootDirectory);


    /**
     * Creates a task that produces completions.
     *
     * @param primarySelection  the primary selection at which completion is invoked
     * @param file              the key of the resource in which completion is invoked
     * @param rootDirectoryHint the root directory of the project; or {@code null} when not specified
     * @return a {@link CodeCompletionResult} result; or an exception when no completions could be generated
     */
    Option<Task<Result<CodeCompletionResult, ?>>> createCodeCompletionTask(Region primarySelection, ResourceKey file, @Nullable ResourcePath rootDirectoryHint);

    Task<Option<ReferenceResolutionResult>> createResolveTask(ResourcePath rootDirectory, ResourceKey file, Region region);

    Task<Option<HoverResult>> createHoverTask(ResourcePath rootDirectory, ResourceKey file, Region region);


    CollectionView<CommandDef<?>> getCommandDefs();

    CollectionView<AutoCommandRequest<?>> getAutoCommandRequests();


    CliCommand getCliCommand();


    ListView<MenuItem> getMainMenuItems();

    ListView<MenuItem> getResourceContextMenuItems();

    ListView<MenuItem> getEditorContextMenuItems();


    ListView<String> getLineCommentSymbols();

    ListView<BlockCommentSymbols> getBlockCommentSymbols();

    ListView<BracketSymbols> getBracketSymbols();
}
