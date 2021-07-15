package mb.str.task.debug;

import mb.common.region.Region;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class StrategoShowArgs implements Serializable {
    public final ResourceKey file;
    public final @Nullable ResourcePath rootDirectoryHint;
    public final @Nullable Region region;

    public StrategoShowArgs(ResourceKey file, @Nullable ResourcePath rootDirectoryHint, @Nullable Region region) {
        this.file = file;
        this.rootDirectoryHint = rootDirectoryHint;
        this.region = region;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final StrategoShowArgs that = (StrategoShowArgs)o;
        if(!file.equals(that.file)) return false;
        if(rootDirectoryHint != null ? !rootDirectoryHint.equals(that.rootDirectoryHint) : that.rootDirectoryHint != null)
            return false;
        return region != null ? region.equals(that.region) : that.region == null;
    }

    @Override public int hashCode() {
        int result = file.hashCode();
        result = 31 * result + (rootDirectoryHint != null ? rootDirectoryHint.hashCode() : 0);
        result = 31 * result + (region != null ? region.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return file + (region != null ? "@" + region : "");
    }
}
