package mb.spoofax.core.language.command;

import mb.common.region.Region;
import mb.common.region.Selection;
import mb.common.region.Selections;
import mb.common.util.EnumSetView;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public class CommandContext implements Serializable {
    private final @Nullable ResourcePathWithKind resourcePath;
    private final @Nullable ResourceKey resourceKey;
    private final @Nullable Selection selection;


    public CommandContext() {
        this.resourcePath = null;
        this.resourceKey = null;
        this.selection = null;
    }

    public CommandContext(ResourcePathWithKind resourcePath) {
        this.resourcePath = resourcePath;
        this.resourceKey = resourcePath.getPath();
        this.selection = null;
    }

    public CommandContext(ResourcePathWithKind resourcePath, Selection selection) {
        this.resourcePath = resourcePath;
        this.resourceKey = resourcePath.getPath();
        this.selection = selection;
    }

    public CommandContext(ResourceKey resourceKey) {
        this.resourcePath = null;
        this.resourceKey = resourceKey;
        this.selection = null;
    }

    public CommandContext(ResourceKey resourceKey, Selection selection) {
        this.resourcePath = null;
        this.resourceKey = resourceKey;
        this.selection = selection;
    }

    public CommandContext(@Nullable ResourcePathWithKind resourcePath, @Nullable ResourceKey resourceKey, @Nullable Selection selection) {
        this.resourcePath = resourcePath;
        this.resourceKey = resourceKey;
        this.selection = selection;
    }

    public static CommandContext ofProject(ResourcePath project) {
        return new CommandContext(ResourcePathWithKinds.project(project));
    }

    public static CommandContext ofDirectory(ResourcePath directory) {
        return new CommandContext(ResourcePathWithKinds.directory(directory));
    }

    public static CommandContext ofFile(ResourcePath file) {
        return new CommandContext(ResourcePathWithKinds.file(file));
    }

    public static CommandContext ofFile(ResourcePath file, Selection selection) {
        return new CommandContext(ResourcePathWithKinds.file(file), selection);
    }

    public static CommandContext ofFile(ResourcePath file, Region region) {
        return new CommandContext(ResourcePathWithKinds.file(file), Selections.region(region));
    }

    public static CommandContext ofFile(ResourcePath file, int offset) {
        return new CommandContext(ResourcePathWithKinds.file(file), Selections.offset(offset));
    }

    public static CommandContext ofResource(ResourceKey resource) {
        return new CommandContext(resource);
    }

    public static CommandContext ofResource(ResourceKey resource, Selection selection) {
        return new CommandContext(resource, selection);
    }

    public static CommandContext ofResource(ResourceKey resource, Region region) {
        return new CommandContext(resource, Selections.region(region));
    }

    public static CommandContext ofResource(ResourceKey resource, int offset) {
        return new CommandContext(resource, Selections.offset(offset));
    }


    public Optional<ResourcePathWithKind> getResourcePathWithKind() {
        return Optional.ofNullable(resourcePath);
    }

    public Optional<ResourceKey> getResourceKey() {
        return Optional.ofNullable(resourceKey);
    }

    public Optional<Selection> getSelection() {
        return Optional.ofNullable(selection);
    }


    public boolean isSupportedBy(EnumSetView<CommandContextType> types) {
        if(types.contains(CommandContextType.Project)) {
            if(resourcePath == null) return false;
            return resourcePath.caseOf()
                .project_(true)
                .directory_(false)
                .file_(false);
        } else if(types.contains(CommandContextType.Directory)) {
            if(resourcePath == null) return false;
            return resourcePath.caseOf()
                .project_(false)
                .directory_(true)
                .file_(false);
        } else if(types.contains(CommandContextType.File)) {
            if(resourcePath == null) return false;
            return resourcePath.caseOf()
                .project_(false)
                .directory_(false)
                .file_(true);
        } else if(types.contains(CommandContextType.Resource) && resourceKey == null) {
            return false;
        } else if(types.contains(CommandContextType.Region)) {
            if(selection == null) return false;
            return selection.caseOf()
                .region_(true)
                .offset_(false);
        } else if(types.contains(CommandContextType.Offset)) {
            if(selection == null) return false;
            return selection.caseOf()
                .region_(false)
                .offset_(true);
        }
        return true;
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final CommandContext other = (CommandContext) obj;
        return Objects.equals(resourcePath, other.resourcePath) &&
            Objects.equals(resourceKey, other.resourceKey) &&
            Objects.equals(selection, other.selection);
    }

    @Override public int hashCode() {
        return Objects.hash(resourcePath, resourceKey, selection);
    }

    @Override public String toString() {
        return "CommandContext{" +
            "resourcePath=" + resourcePath +
            ", resourceKey=" + resourceKey +
            ", selection=" + selection +
            '}';
    }
}
