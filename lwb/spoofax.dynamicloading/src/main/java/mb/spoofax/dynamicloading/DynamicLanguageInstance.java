package mb.spoofax.dynamicloading;

import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.style.Styling;
import mb.common.token.Tokens;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.completions.common.CompletionResult;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.cli.CliCommand;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.menu.MenuItem;
import mb.spoofax.dynamicloading.task.DynamicStyle;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DynamicLanguageInstance implements LanguageInstance {
    private final String displayName;
    private final SetView<String> fileExtensions;
    private final DynamicStyle dynamicStyle;


    public DynamicLanguageInstance(
        String displayName,
        SetView<String> fileExtensions,
        DynamicStyle dynamicStyle
    ) {
        this.displayName = displayName;
        this.fileExtensions = fileExtensions;
        this.dynamicStyle = dynamicStyle;
    }


    @Override public String getDisplayName() {
        return displayName;
    }

    @Override public SetView<String> getFileExtensions() {
        return fileExtensions;
    }


    @Override
    public Task<? extends Option<? extends Tokens<?>>> createTokenizeTask(ResourceKey resourceKey) {
        return null;
    }

    @Override
    public Task<Option<Styling>> createStyleTask(ResourceKey resourceKey) {
        return dynamicStyle.createTask(resourceKey);
    }

    @Override
    public Task<@Nullable CompletionResult> createCompletionTask(ResourceKey resourceKey, Region primarySelection) {
        return null;
    }

    @Override
    public Task<KeyedMessages> createCheckTask(ResourcePath projectRoot) {
        return null;
    }


    @Override public CollectionView<CommandDef<?>> getCommandDefs() {
        return CollectionView.of();
    }

    @Override public CollectionView<AutoCommandRequest<?>> getAutoCommandRequests() {
        return CollectionView.of();
    }

    @Override public CliCommand getCliCommand() {
        return CliCommand.of("nope");
    }

    @Override public ListView<MenuItem> getMainMenuItems() {
        return ListView.of();
    }

    @Override public ListView<MenuItem> getResourceContextMenuItems() {
        return ListView.of();
    }

    @Override public ListView<MenuItem> getEditorContextMenuItems() {
        return ListView.of();
    }
}
