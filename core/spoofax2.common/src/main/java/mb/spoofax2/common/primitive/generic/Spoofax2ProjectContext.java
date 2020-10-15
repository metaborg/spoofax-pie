package mb.spoofax2.common.primitive.generic;

import mb.common.util.MultiMapView;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class Spoofax2ProjectContext implements Serializable {
    public final ResourcePath projectPath;
    public final MultiMapView<String, ResourcePath> languageIdToSourcePaths;
    public final MultiMapView<String, ResourcePath> languageIdToIncludePaths;

    public Spoofax2ProjectContext(
        ResourcePath projectPath,
        MultiMapView<String, ResourcePath> languageIdToSourcePaths,
        MultiMapView<String, ResourcePath> languageIdToIncludePaths
    ) {
        this.projectPath = projectPath;
        this.languageIdToSourcePaths = languageIdToSourcePaths;
        this.languageIdToIncludePaths = languageIdToIncludePaths;
    }

    public Spoofax2ProjectContext(ResourcePath projectPath) {
        this(projectPath, MultiMapView.of(), MultiMapView.of());
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Spoofax2ProjectContext that = (Spoofax2ProjectContext)o;
        return projectPath.equals(that.projectPath) && languageIdToSourcePaths.equals(that.languageIdToSourcePaths) && languageIdToIncludePaths.equals(that.languageIdToIncludePaths);
    }

    @Override public int hashCode() {
        return Objects.hash(projectPath, languageIdToSourcePaths, languageIdToIncludePaths);
    }

    @Override public String toString() {
        return "Spoofax2ProjectContext{" +
            "projectPath=" + projectPath +
            ", languageIdToSourcePaths=" + languageIdToSourcePaths +
            ", languageIdToIncludePaths=" + languageIdToIncludePaths +
            '}';
    }
}
