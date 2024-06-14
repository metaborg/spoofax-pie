package mb.sdf3.task.spec;

import mb.common.option.Option;
import mb.common.util.ListView;
import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.util.SeparatorUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;
import org.metaborg.util.tuple.Tuple2;

import java.io.Serializable;

public class Sdf3SpecConfig implements Serializable {
    public final ResourcePath rootDirectory;
    public final ResourcePath mainSourceDirectory;
    public final ResourcePath mainFile;
    public final ListView<ResourcePath> includeDirectories;
    public final ListView<STask<?>> sourceFileOrigins;
    public final ParseTableConfiguration parseTableConfig;
    public final Option<Tuple2<String, String>> placeholders;

    public Sdf3SpecConfig(
        ResourcePath rootDirectory,
        ResourcePath mainSourceDirectory,
        ResourcePath mainFile,
        ListView<ResourcePath> includeDirectories,
        ListView<STask<?>> sourceFileOrigins,
        ParseTableConfiguration parseTableConfig,
        Option<Tuple2<String, String>> placeholders) {
        this.rootDirectory = rootDirectory;
        this.mainSourceDirectory = mainSourceDirectory;
        this.mainFile = mainFile;
        this.includeDirectories = includeDirectories;
        this.sourceFileOrigins = sourceFileOrigins;
        this.parseTableConfig = parseTableConfig;
        this.placeholders = placeholders;
    }

    public String getMainModuleName() {
        return SeparatorUtil.convertCurrentToUnixSeparator(mainSourceDirectory.relativize(mainFile.removeLeafExtension()));
    }

    public static Sdf3SpecConfig createDefault(ResourcePath rootDirectory, ListView<ResourcePath> includeDirectories, ListView<STask<?>> sourceFileOrigins) {
        final ResourcePath mainSourceDirectory = rootDirectory.appendRelativePath("src");
        final ResourcePath mainFile = mainSourceDirectory.appendRelativePath("start.sdf3");
        final ParseTableConfiguration parseTableConfig = createDefaultParseTableConfiguration();
        return new Sdf3SpecConfig(rootDirectory, mainSourceDirectory, mainFile, includeDirectories, sourceFileOrigins, parseTableConfig,
            Option.ofNone());
    }

    public static Sdf3SpecConfig createDefault(ResourcePath rootDirectory, ListView<ResourcePath> includeDirectories) {
        return createDefault(rootDirectory, includeDirectories, ListView.of());
    }

    public static Sdf3SpecConfig createDefault(ResourcePath rootDirectory) {
        return createDefault(rootDirectory, ListView.of());
    }

    public static ParseTableConfiguration createDefaultParseTableConfiguration() {
        return new ParseTableConfiguration(false, false, true, false, false, false);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Sdf3SpecConfig that = (Sdf3SpecConfig)o;
        if(!rootDirectory.equals(that.rootDirectory)) return false;
        if(!mainSourceDirectory.equals(that.mainSourceDirectory)) return false;
        if(!mainFile.equals(that.mainFile)) return false;
        if(!includeDirectories.equals(that.includeDirectories)) return false;
        if(!sourceFileOrigins.equals(that.sourceFileOrigins)) return false;
        if(!parseTableConfig.equals(that.parseTableConfig)) return false;
        return placeholders.equals(that.placeholders);
    }

    @Override public int hashCode() {
        int result = rootDirectory.hashCode();
        result = 31 * result + mainSourceDirectory.hashCode();
        result = 31 * result + mainFile.hashCode();
        result = 31 * result + includeDirectories.hashCode();
        result = 31 * result + sourceFileOrigins.hashCode();
        result = 31 * result + parseTableConfig.hashCode();
        result = 31 * result + placeholders.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Sdf3SpecConfig{" +
            "rootDirectory=" + rootDirectory +
            ", mainSourceDirectory=" + mainSourceDirectory +
            ", mainFile=" + mainFile +
            ", includeDirectories=" + includeDirectories +
            ", sourceFileOrigins=" + sourceFileOrigins +
            ", parseTableConfig=" + parseTableConfig +
            ", placeholders=" + placeholders +
            '}';
    }
}
