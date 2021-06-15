package mb.str.config;

import mb.common.util.ListView;
import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.ModuleIdentifier;
import mb.stratego.build.util.StrategoGradualSetting;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class StrategoAnalyzeConfig implements Serializable {
    public final ResourcePath rootDirectory;
    public final ModuleIdentifier mainModule;
    public final ListView<ResourcePath> includeDirs;
    public final ListView<BuiltinLibraryIdentifier> builtinLibs;
    public final StrategoGradualSetting gradualTypingSetting;
    public final ListView<STask<?>> sourceFileOrigins;

    public StrategoAnalyzeConfig(
        ResourcePath rootDirectory,
        ModuleIdentifier mainModule,
        ListView<ResourcePath> includeDirs,
        ListView<BuiltinLibraryIdentifier> builtinLibs,
        StrategoGradualSetting gradualTypingSetting,
        ListView<STask<?>> sourceFileOrigins
    ) {
        this.rootDirectory = rootDirectory;
        this.mainModule = mainModule;
        this.includeDirs = includeDirs;
        this.builtinLibs = builtinLibs;
        this.gradualTypingSetting = gradualTypingSetting;
        this.sourceFileOrigins = sourceFileOrigins;
    }

    public StrategoAnalyzeConfig(
        ResourcePath rootDirectory,
        ResourcePath mainFile,
        ListView<ResourcePath> includeDirs,
        ListView<BuiltinLibraryIdentifier> builtinLibs,
        StrategoGradualSetting gradualTypingSetting,
        ListView<STask<?>> sourceFileOrigins
    ) {
        this(
            rootDirectory,
            StrategoConfig.fromRootDirectoryAndMainFile(rootDirectory, mainFile),
            includeDirs,
            builtinLibs,
            gradualTypingSetting,
            sourceFileOrigins
        );
    }

    public static StrategoAnalyzeConfig createDefault(ResourcePath rootDirectory) {
        return new StrategoAnalyzeConfig(
            rootDirectory,
            StrategoConfig.defaultMainModule(rootDirectory),
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
        if(!mainModule.equals(that.mainModule)) return false;
        if(!includeDirs.equals(that.includeDirs)) return false;
        if(!builtinLibs.equals(that.builtinLibs)) return false;
        if(gradualTypingSetting != that.gradualTypingSetting) return false;
        return sourceFileOrigins.equals(that.sourceFileOrigins);
    }

    @Override public int hashCode() {
        int result = rootDirectory.hashCode();
        result = 31 * result + mainModule.hashCode();
        result = 31 * result + includeDirs.hashCode();
        result = 31 * result + builtinLibs.hashCode();
        result = 31 * result + gradualTypingSetting.hashCode();
        result = 31 * result + sourceFileOrigins.hashCode();
        return result;
    }

    @Override public String toString() {
        return "StrategoAnalyzeConfig{" +
            "rootDirectory=" + rootDirectory +
            ", mainModule=" + mainModule +
            ", includeDirs=" + includeDirs +
            ", builtinLibs=" + builtinLibs +
            ", gradualTypingSetting=" + gradualTypingSetting +
            ", sourceFileOrigins=" + sourceFileOrigins +
            '}';
    }
}