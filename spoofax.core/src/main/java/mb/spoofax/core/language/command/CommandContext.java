package mb.spoofax.core.language.command;

import mb.common.region.Selection;
import mb.resource.ResourceKey;
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


    public Optional<ResourcePathWithKind> getResourcePathWithKind() {
        return Optional.ofNullable(resourcePath);
    }

    public Optional<ResourceKey> getResourceKey() {
        return Optional.ofNullable(resourceKey);
    }

    public Optional<Selection> getSelection() {
        return Optional.ofNullable(selection);
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
