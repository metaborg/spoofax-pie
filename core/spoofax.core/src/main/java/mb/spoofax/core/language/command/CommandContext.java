package mb.spoofax.core.language.command;

import mb.common.region.Region;
import mb.common.region.Selection;
import mb.common.region.Selections;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class CommandContext implements Serializable {
    protected final @Nullable ResourcePathWithKind resourcePath;
    protected final @Nullable ResourceKey resourceKey;
    protected final @Nullable Selection selection;


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


    public @Nullable CommandContext getEnclosing(CommandContextType type) {
        if(type == CommandContextType.DirectoryPath) {
            if(resourcePath == null) return null;
            final @Nullable ResourcePath parent = resourcePath.getPath().getParent();
            if(parent == null) return null;
            return CommandContext.ofDirectory(parent);
        }
        return null;
    }


    public boolean supports(CommandContextType type) {
        switch(type) {
            case ProjectPath:
                if(resourcePath == null) return false;
                return resourcePath.caseOf()
                    .project_(true)
                    .directory_(false)
                    .file_(false);
            case DirectoryPath:
                if(resourcePath == null) return false;
                return resourcePath.caseOf()
                    .project_(false)
                    .directory_(true)
                    .file_(false);
            case FilePath:
                if(resourcePath == null) return false;
                return resourcePath.caseOf()
                    .project_(false)
                    .directory_(false)
                    .file_(true);
            case ResourcePath:
                return resourcePath != null;
            case ResourceKey:
                return resourceKey != null;
            case Region:
                if(selection == null) return false;
                return selection.caseOf()
                    .region_(true)
                    .offset_(false);
            case Offset:
                if(selection == null) return false;
                return selection.caseOf()
                    .region_(false)
                    .offset_(true);
        }
        return false;
    }

    public boolean supports(EditorFileType type) {
        switch(type) {
            case HierarchicalResource:
                return resourcePath != null;
            case Resource:
                return resourceKey != null;
        }
        return false;
    }

    public boolean supportsAnyEditorFileType(Collection<EditorFileType> types) {
        if(types.isEmpty()) return true;
        for(EditorFileType type : types) {
            if(supports(type)) return true;
        }
        return false;
    }

    public boolean supports(EditorSelectionType type) {
        switch(type) {
            case Region:
                if(selection == null) return false;
                return selection.caseOf()
                    .region_(true)
                    .offset_(false);
            case Offset:
                if(selection == null) return false;
                return selection.caseOf()
                    .region_(false)
                    .offset_(true);
        }
        return false;
    }

    public boolean supportsAnyEditorSelectionType(Collection<EditorSelectionType> types) {
        if(types.isEmpty()) return true;
        for(EditorSelectionType type : types) {
            if(supports(type)) return true;
        }
        return false;
    }

    public boolean supports(HierarchicalResourceType type) {
        switch(type) {
            case Project:
                if(resourcePath == null) return false;
                return resourcePath.caseOf()
                    .project_(true)
                    .directory_(false)
                    .file_(false);
            case Directory:
                if(resourcePath == null) return false;
                return resourcePath.caseOf()
                    .project_(false)
                    .directory_(true)
                    .file_(false);
            case File:
                if(resourcePath == null) return false;
                return resourcePath.caseOf()
                    .project_(false)
                    .directory_(false)
                    .file_(true);
        }
        return false;
    }

    public boolean supportsAnyHierarchicalResourceType(Collection<HierarchicalResourceType> types) {
        if(types.isEmpty()) return true;
        for(HierarchicalResourceType type : types) {
            if(supports(type)) return true;
        }
        return false;
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final CommandContext other = (CommandContext)obj;
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
