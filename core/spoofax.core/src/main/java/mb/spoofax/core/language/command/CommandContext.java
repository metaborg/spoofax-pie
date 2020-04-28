package mb.spoofax.core.language.command;

import mb.common.region.Region;
import mb.common.region.Selection;
import mb.common.region.Selections;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

public class CommandContext implements Serializable {
    protected final @Nullable ResourcePathWithKind resourcePath;
    protected final @Nullable ResourceKey resourceKey;
    protected final @Nullable Selection selection;
    protected final HashMap<EnclosingCommandContextType, CommandContext> enclosingContexts = new HashMap<>();


    public CommandContext(@Nullable ResourcePathWithKind resourcePath, @Nullable ResourceKey resourceKey, @Nullable Selection selection) {
        this.resourcePath = resourcePath;
        this.resourceKey = resourceKey;
        this.selection = selection;
    }

    public CommandContext(ResourcePathWithKind resourcePath, Selection selection) {
        this(resourcePath, resourcePath.getPath(), selection);
    }

    public CommandContext(ResourcePathWithKind resourcePath) {
        this(resourcePath, resourcePath.getPath(), null);
    }

    public CommandContext(ResourceKey resourceKey, Selection selection) {
        this(null, resourceKey, selection);
    }

    public CommandContext(ResourceKey resourceKey) {
        this(null, resourceKey, null);
    }

    public CommandContext() {
        this(null, null, null);
    }


    public static CommandContext ofProject(ResourcePath project) {
        return new CommandContext(ResourcePathWithKind.project(project));
    }

    public static CommandContext ofDirectory(ResourcePath directory) {
        return new CommandContext(ResourcePathWithKind.directory(directory));
    }

    public static CommandContext ofFile(ResourcePath file) {
        return new CommandContext(ResourcePathWithKind.file(file));
    }

    public static CommandContext ofFile(ResourcePath file, Selection selection) {
        return new CommandContext(ResourcePathWithKind.file(file), selection);
    }

    public static CommandContext ofFile(ResourcePath file, Region region) {
        return new CommandContext(ResourcePathWithKind.file(file), Selections.region(region));
    }

    public static CommandContext ofFile(ResourcePath file, int offset) {
        return new CommandContext(ResourcePathWithKind.file(file), Selections.offset(offset));
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


    public @Nullable CommandContext getEnclosing(EnclosingCommandContextType type) {
        return enclosingContexts.get(type);
    }

    public void setEnclosing(EnclosingCommandContextType type, CommandContext context) {
        enclosingContexts.put(type, context);
    }


    public boolean supports(CommandContextType type) {
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


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final CommandContext that = (CommandContext)o;
        return Objects.equals(resourcePath, that.resourcePath) &&
            Objects.equals(resourceKey, that.resourceKey) &&
            Objects.equals(selection, that.selection) &&
            enclosingContexts.equals(that.enclosingContexts);
    }

    @Override public int hashCode() {
        return Objects.hash(resourcePath, resourceKey, selection, enclosingContexts);
    }

    @Override public String toString() {
        return "CommandContext{" +
            "resourcePath=" + resourcePath +
            ", resourceKey=" + resourceKey +
            ", selection=" + selection +
            ", enclosingContexts=" + enclosingContexts +
            '}';
    }
}
