package mb.str.config;

import mb.common.util.ListView;
import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.util.StrategoGradualSetting;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class StrategoAnalyzeConfig implements Serializable {
    public final ResourcePath rootDirectory;
    public final ResourcePath mainFile;
    public final ListView<ResourcePath> includeDirs;
    public final ListView<String> builtinLibs;
    public final StrategoGradualSetting gradualTypingSetting;
    public final ListView<STask<?>> sourceFileOrigins;

    public StrategoAnalyzeConfig(
        ResourcePath rootDirectory,
        ResourcePath mainFile,
        ListView<ResourcePath> includeDirs,
        ListView<String> builtinLibs,
        StrategoGradualSetting gradualTypingSetting,
        ListView<STask<?>> sourceFileOrigins
    ) {
        this.rootDirectory = rootDirectory;
        this.mainFile = mainFile;
        this.includeDirs = includeDirs;
        this.builtinLibs = builtinLibs;
        this.gradualTypingSetting = gradualTypingSetting;
        this.sourceFileOrigins = sourceFileOrigins;
    }

    public static StrategoAnalyzeConfig createDefault(ResourcePath rootDirectory) {
        return new StrategoAnalyzeConfig(
            rootDirectory,
            StrategoConfig.defaultMainFile(rootDirectory),
            ListView.of(),
            StrategoConfig.defaultBuiltinLibs(),
            StrategoConfig.defaultGradualTypingSetting(),
            ListView.of()
        );
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final StrategoAnalyzeConfig that = (StrategoAnalyzeConfig)o;
        if(!rootDirectory.equals(that.rootDirectory)) return false;
        if(!mainFile.equals(that.mainFile)) return false;
        if(!includeDirs.equals(that.includeDirs)) return false;
        if(!builtinLibs.equals(that.builtinLibs)) return false;
        if(gradualTypingSetting != that.gradualTypingSetting) return false;
        return sourceFileOrigins.equals(that.sourceFileOrigins);
    }

    @Override public int hashCode() {
        int result = rootDirectory.hashCode();
        result = 31 * result + mainFile.hashCode();
        result = 31 * result + includeDirs.hashCode();
        result = 31 * result + builtinLibs.hashCode();
        result = 31 * result + gradualTypingSetting.hashCode();
        result = 31 * result + sourceFileOrigins.hashCode();
        return result;
    }

    @Override public String toString() {
        return "StrategoAnalyzeConfig{" +
            "rootDirectory=" + rootDirectory +
            ", mainFile=" + mainFile +
            ", includeDirs=" + includeDirs +
            ", builtinLibs=" + builtinLibs +
            ", gradualTypingSetting=" + gradualTypingSetting +
            ", sourceFileOrigins=" + sourceFileOrigins +
            '}';
    }
}
