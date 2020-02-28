package mb.spoofax.core.language.menu;

import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.core.language.command.EditorFileType;
import mb.spoofax.core.language.command.EditorSelectionType;
import mb.spoofax.core.language.command.EnclosingCommandContextType;
import mb.spoofax.core.language.command.HierarchicalResourceType;
import mb.spoofax.core.language.command.arg.RawArgs;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
public interface CommandAction {
    class Builder extends ImmutableCommandAction.Builder {
        public Builder with(CommandDef<?> commandDef, CommandExecutionType executionType) {
            displayName(commandDef.getDisplayName());
            description(commandDef.getDescription());
            commandRequest(commandDef.request(executionType));
            return this;
        }

        public Builder with(CommandDef<?> commandDef, CommandExecutionType executionType, String displayName) {
            displayName(displayName);
            description(commandDef.getDescription());
            commandRequest(commandDef.request(executionType));
            return this;
        }

        public Builder with(CommandDef<?> commandDef, CommandExecutionType executionType, RawArgs initialArgs) {
            displayName(commandDef.getDisplayName());
            description(commandDef.getDescription());
            commandRequest(commandDef.request(executionType, initialArgs));
            return this;
        }

        public Builder with(CommandDef<?> commandDef, CommandExecutionType executionType, String displayName, RawArgs initialArgs) {
            displayName(displayName);
            description(commandDef.getDescription());
            commandRequest(commandDef.request(executionType, initialArgs));
            return this;
        }

        public Builder withSuffix(CommandDef<?> commandDef, CommandExecutionType executionType, String suffix) {
            return with(commandDef, executionType, commandDef.getDisplayName() + suffix);
        }

        public Builder withSuffix(CommandDef<?> commandDef, CommandExecutionType executionType, String suffix, RawArgs initialArgs) {
            return with(commandDef, executionType, commandDef.getDisplayName() + suffix, initialArgs);
        }


        public Builder manualOnce(CommandDef<?> commandDef) {
            return with(commandDef, CommandExecutionType.ManualOnce);
        }

        public Builder manualOnce(CommandDef<?> commandDef, String suffix) {
            return withSuffix(commandDef, CommandExecutionType.ManualOnce, " " + suffix);
        }

        public Builder manualOnce(CommandDef<?> commandDef, RawArgs initialArgs) {
            return with(commandDef, CommandExecutionType.ManualOnce, initialArgs);
        }

        public Builder manualOnce(CommandDef<?> commandDef, String suffix, RawArgs initialArgs) {
            return withSuffix(commandDef, CommandExecutionType.ManualOnce, " " + suffix, initialArgs);
        }


        public Builder manualContinuous(CommandDef<?> commandDef) {
            return withSuffix(commandDef, CommandExecutionType.ManualContinuous, " (continuous)");
        }

        public Builder manualContinuous(CommandDef<?> commandDef, String suffix) {
            return withSuffix(commandDef, CommandExecutionType.ManualContinuous, " " + suffix + " (continuous)");
        }

        public Builder manualContinuous(CommandDef<?> commandDef, RawArgs initialArgs) {
            return withSuffix(commandDef, CommandExecutionType.ManualContinuous, " (continuous)", initialArgs);
        }

        public Builder manualContinuous(CommandDef<?> commandDef, String suffix, RawArgs initialArgs) {
            return withSuffix(commandDef, CommandExecutionType.ManualContinuous, " " + suffix + " (continuous)", initialArgs);
        }


        public Builder fileRequired() {
            addRequiredEditorFileTypes(EditorFileType.HierarchicalResource);
            addRequiredResourceTypes(HierarchicalResourceType.File);
            return this;
        }

        public Builder directoryRequired() {
            addRequiredResourceTypes(HierarchicalResourceType.Directory);
            return this;
        }

        public Builder projectRequired() {
            addRequiredResourceTypes(HierarchicalResourceType.Project);
            return this;
        }

        public Builder enclosingDirectoryRequired() {
            addRequiredEnclosingResourceTypes(EnclosingCommandContextType.Directory);
            return this;
        }

        public Builder enclosingProjectRequired() {
            addRequiredEnclosingResourceTypes(EnclosingCommandContextType.Project);
            return this;
        }


        public MenuItem buildItem() {
            return MenuItem.commandAction(build());
        }
    }

    static Builder builder() { return new Builder(); }


    String displayName();

    String description();

    CommandRequest commandRequest();

    /**
     * Gets whether this command action, in editor context menus, should only be shown when the text selection of the
     * editor is of a type in this set. An empty set indicates that there is no requirement.
     */
    Set<EditorSelectionType> requiredEditorSelectionTypes();

    /**
     * Gets whether this command action, in editor context menus, should only be shown when the file of the editor is of
     * a type in this set. An empty set indicates that there is no requirement.
     */
    Set<EditorFileType> requiredEditorFileTypes();

    /**
     * Gets whether this command action, in resource context menus, should only be shown when the selected resource is
     * of a type in this set. An empty set indicates that there is no requirement.
     */
    Set<HierarchicalResourceType> requiredResourceTypes();

    /**
     * Gets whether this command action, in context menus, should only be shown when the enclosing resource of the
     * resource in context is of a type in this set. An empty set indicates that there is no requirement.
     */
    Set<EnclosingCommandContextType> requiredEnclosingResourceTypes();
}
