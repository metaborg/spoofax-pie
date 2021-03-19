package mb.statix;

import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class StatixConfig implements Serializable {
    public final ResourcePath projectPath;
    public final ListView<ResourcePath> sourcePaths;
    public final ListView<ResourcePath> includePaths;

    public StatixConfig(ResourcePath projectPath, ListView<ResourcePath> sourcePaths, ListView<ResourcePath> includePaths) {
        this.projectPath = projectPath;
        this.sourcePaths = sourcePaths;
        this.includePaths = includePaths;
    }

    public StatixConfig(ResourcePath projectPath, ListView<ResourcePath> sourcePaths) {
        this(projectPath, sourcePaths, ListView.of());
    }

    public StatixConfig(ResourcePath projectPath) {
        this(projectPath, ListView.of());
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final StatixConfig that = (StatixConfig)o;
        if(!projectPath.equals(that.projectPath)) return false;
        if(!sourcePaths.equals(that.sourcePaths)) return false;
        return includePaths.equals(that.includePaths);
    }

    @Override public int hashCode() {
        int result = projectPath.hashCode();
        result = 31 * result + sourcePaths.hashCode();
        result = 31 * result + includePaths.hashCode();
        return result;
    }

    @Override public String toString() {
        return "StatixConfig{" +
            "projectPath=" + projectPath +
            ", sourcePaths=" + sourcePaths +
            ", includePaths=" + includePaths +
            '}';
    }
}
