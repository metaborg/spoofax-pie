package mb.statix.task;

import mb.common.util.ListView;
import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

public class StatixConfig implements Serializable {
    public final ResourcePath rootDirectory;
    public final ResourcePath mainFile;
    public final ListView<ResourcePath> sourcePaths;
    public final ListView<ResourcePath> includePaths;
    public final ListView<STask<?>> sourceFileOrigins;

    public StatixConfig(
        ResourcePath rootDirectory,
        ResourcePath mainFile,
        ListView<ResourcePath> sourcePaths,
        ListView<ResourcePath> includePaths,
        ListView<STask<?>> sourceFileOrigins) {
        this.rootDirectory = rootDirectory;
        this.mainFile = mainFile;
        this.sourcePaths = sourcePaths;
        this.includePaths = includePaths;
        this.sourceFileOrigins = sourceFileOrigins;
    }

    public static StatixConfig createDefault(ResourcePath rootDirectory) {
        final ResourcePath sourceDirectory = rootDirectory.appendRelativePath("src");
        return new StatixConfig(rootDirectory, sourceDirectory.appendRelativePath("main.stx"), ListView.of(sourceDirectory), ListView.of(), ListView.of());
    }

    public ArrayList<ResourcePath> sourceAndIncludePaths() {
        final ArrayList<ResourcePath> sourceAndIncludePaths = new ArrayList<>();
        sourcePaths.addAllTo(sourceAndIncludePaths);
        includePaths.addAllTo(sourceAndIncludePaths);
        return sourceAndIncludePaths;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final StatixConfig that = (StatixConfig)o;
        if(!rootDirectory.equals(that.rootDirectory)) return false;
        if(!mainFile.equals(that.mainFile)) return false;
        if(!sourcePaths.equals(that.sourcePaths)) return false;
        if(!includePaths.equals(that.includePaths)) return false;
        return sourceFileOrigins.equals(that.sourceFileOrigins);
    }

    @Override public int hashCode() {
        int result = rootDirectory.hashCode();
        result = 31 * result + mainFile.hashCode();
        result = 31 * result + sourcePaths.hashCode();
        result = 31 * result + includePaths.hashCode();
        result = 31 * result + sourceFileOrigins.hashCode();
        return result;
    }

    @Override public String toString() {
        return "StatixConfig{" +
            "rootDirectory=" + rootDirectory +
            ", mainFile=" + mainFile +
            ", sourcePaths=" + sourcePaths +
            ", includePaths=" + includePaths +
            ", sourceFileOrigins=" + sourceFileOrigins +
            '}';
    }
}
