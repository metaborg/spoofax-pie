package mb.spoofax.core.language.command;

import mb.common.region.Selection;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CommandContextBuilder {
    private @Nullable ResourcePathWithKind resourcePath;
    private @Nullable ResourceKey resourceKey;
    private @Nullable Selection selection;


    public void setResourcePath(@Nullable ResourcePathWithKind resourcePath) {
        this.resourcePath = resourcePath;
    }

    public void setResourceKey(@Nullable ResourceKey resourceKey) {
        this.resourceKey = resourceKey;
    }

    public void setSelection(@Nullable Selection selection) {
        this.selection = selection;
    }

    public void clear() {
        this.resourcePath = null;
        this.resourceKey = null;
        this.selection = null;
    }


    public CommandContext build() {
        return new CommandContext(resourcePath, (resourceKey == null && resourcePath != null) ? resourcePath.getPath() : resourceKey, selection);
    }
}
