package mb.sdf3.task.spec;

import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;

import java.io.Serializable;

public class Sdf3SpecConfig implements Serializable {
    public final ResourcePath rootDirectory;
    public final ResourceKey mainFile;
    public final ParseTableConfiguration parseTableConfig;

    public Sdf3SpecConfig(
        ResourcePath rootDirectory,
        ResourceKey mainFile,
        ParseTableConfiguration parseTableConfig
    ) {
        this.rootDirectory = rootDirectory;
        this.mainFile = mainFile;
        this.parseTableConfig = parseTableConfig;
    }

    public static Sdf3SpecConfig createDefault(ResourcePath projectDirectory) {
        final ResourcePath rootDirectory = projectDirectory.appendRelativePath("src");
        final ResourceKey mainFile = rootDirectory.appendRelativePath("start.sdf3");
        final ParseTableConfiguration parseTableConfig = createDefaultParseTableConfiguration();
        return new Sdf3SpecConfig(rootDirectory, mainFile, parseTableConfig);
    }

    public static ParseTableConfiguration createDefaultParseTableConfiguration() {
        return new ParseTableConfiguration(false, false, true, false, false, false);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Sdf3SpecConfig that = (Sdf3SpecConfig)o;
        if(!rootDirectory.equals(that.rootDirectory)) return false;
        if(!mainFile.equals(that.mainFile)) return false;
        return parseTableConfig.equals(that.parseTableConfig);
    }

    @Override public int hashCode() {
        int result = rootDirectory.hashCode();
        result = 31 * result + mainFile.hashCode();
        result = 31 * result + parseTableConfig.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Sdf3SpecConfig{" +
            "rootDirectory=" + rootDirectory +
            ", mainFile=" + mainFile +
            ", parseTableConfig=" + parseTableConfig +
            '}';
    }
}
