package mb.spoofax.compiler.menu;

import mb.spoofax.compiler.command.CommandDefRepr;
import mb.spoofax.compiler.command.CommandRequestRepr;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.EditorFileType;
import mb.spoofax.core.language.command.EditorSelectionType;
import mb.spoofax.core.language.command.EnclosingCommandContextType;
import mb.spoofax.core.language.command.HierarchicalResourceType;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

@Value.Immutable
public interface CommandActionRepr extends Serializable {
    class Builder extends ImmutableCommandActionRepr.Builder {
        public Builder with(CommandDefRepr commandDef, CommandExecutionType executionType) {
            displayName(commandDef.displayName());
            description(commandDef.description());
            commandRequest(commandDef.request(executionType));
            return this;
        }

        public Builder with(CommandDefRepr commandDef, CommandExecutionType executionType, String displayName) {
            displayName(displayName);
            description(commandDef.description());
            commandRequest(commandDef.request(executionType));
            return this;
        }

        public Builder with(CommandDefRepr commandDef, CommandExecutionType executionType, Map<String, String> initialArgs) {
            displayName(commandDef.displayName());
            description(commandDef.description());
            commandRequest(commandDef.request(executionType, initialArgs));
            return this;
        }

        public Builder with(CommandDefRepr commandDef, CommandExecutionType executionType, String displayName, Map<String, String> initialArgs) {
            displayName(displayName);
            description(commandDef.description());
            commandRequest(commandDef.request(executionType, initialArgs));
            return this;
        }

        public Builder withSuffix(CommandDefRepr commandDef, CommandExecutionType executionType, String suffix) {
            return with(commandDef, executionType, commandDef.displayName() + suffix);
        }

        public Builder withSuffix(CommandDefRepr commandDef, CommandExecutionType executionType, String suffix, Map<String, String> initialArgs) {
            return with(commandDef, executionType, commandDef.displayName() + suffix, initialArgs);
        }


        public Builder manualOnce(CommandDefRepr commandDef) {
            return with(commandDef, CommandExecutionType.ManualOnce);
        }

        public Builder manualOnce(CommandDefRepr commandDef, String suffix) {
            return withSuffix(commandDef, CommandExecutionType.ManualOnce, " " + suffix);
        }

        public Builder manualOnce(CommandDefRepr commandDef, Map<String, String> initialArgs) {
            return with(commandDef, CommandExecutionType.ManualOnce, initialArgs);
        }

        public Builder manualOnce(CommandDefRepr commandDef, String suffix, Map<String, String> initialArgs) {
            return withSuffix(commandDef, CommandExecutionType.ManualOnce, " " + suffix, initialArgs);
        }


        public Builder manualContinuous(CommandDefRepr commandDef) {
            return withSuffix(commandDef, CommandExecutionType.ManualContinuous, " (continuous)");
        }

        public Builder manualContinuous(CommandDefRepr commandDef, String suffix) {
            return withSuffix(commandDef, CommandExecutionType.ManualContinuous, " " + suffix + " (continuous)");
        }

        public Builder manualContinuous(CommandDefRepr commandDef, Map<String, String> initialArgs) {
            return withSuffix(commandDef, CommandExecutionType.ManualContinuous, " (continuous)", initialArgs);
        }

        public Builder manualContinuous(CommandDefRepr commandDef, String suffix, Map<String, String> initialArgs) {
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


        public MenuItemRepr buildItem() {
            return MenuItemRepr.commandAction(build());
        }
    }

    static Builder builder() {
        return new Builder();
    }


    String displayName();

    String description();

    CommandRequestRepr commandRequest();

    Set<EditorSelectionType> requiredEditorSelectionTypes();

    Set<EditorFileType> requiredEditorFileTypes();

    Set<HierarchicalResourceType> requiredResourceTypes();

    Set<EnclosingCommandContextType> requiredEnclosingResourceTypes();
}
