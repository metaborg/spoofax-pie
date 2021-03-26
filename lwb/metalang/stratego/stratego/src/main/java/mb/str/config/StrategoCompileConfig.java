package mb.str.config;

import mb.common.util.ListView;
import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.util.StrategoGradualSetting;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.cmd.Arguments;

import java.io.Serializable;

public class StrategoCompileConfig implements Serializable {
    public final ResourcePath rootDirectory;
    public final ResourcePath mainFile;
    public final ListView<ResourcePath> includeDirs;
    public final ListView<String> builtinLibs;
    public final StrategoGradualSetting gradualTypingSetting;
    public final Arguments extraCompilerArguments;
    public final ListView<STask<?>> sourceFileOrigins;

    public final @Nullable ResourcePath cacheDir;
    public final ResourcePath outputDir;
    public final String outputJavaPackageId;

    public StrategoCompileConfig(
        ResourcePath rootDirectory,
        ResourcePath mainFile,
        ListView<ResourcePath> includeDirs,
        ListView<String> builtinLibs,
        StrategoGradualSetting gradualTypingSetting,
        Arguments extraCompilerArguments,
        ListView<STask<?>> sourceFileOrigins,

        @Nullable ResourcePath cacheDir,
        ResourcePath outputDir,
        String outputJavaPackageId
    ) {
        this.rootDirectory = rootDirectory;
        this.mainFile = mainFile;
        this.includeDirs = includeDirs;
        this.builtinLibs = builtinLibs;
        this.gradualTypingSetting = gradualTypingSetting;
        this.extraCompilerArguments = extraCompilerArguments;
        this.sourceFileOrigins = sourceFileOrigins;

        this.cacheDir = cacheDir;
        this.outputDir = outputDir;
        this.outputJavaPackageId = outputJavaPackageId;
    }

    public static StrategoCompileConfig createDefault(ResourcePath rootDirectory, ResourcePath outputDir, String outputJavaPackageId) {
        return new StrategoCompileConfig(
            rootDirectory,
            StrategoConfig.defaultMainFile(rootDirectory),
            ListView.of(),
            StrategoConfig.defaultBuiltinLibs(),
            StrategoConfig.defaultGradualTypingSetting(),
            new Arguments(),
            ListView.of(),
            null,
            outputDir,
            outputJavaPackageId
        );
    }

    public StrategoAnalyzeConfig toAnalyzeConfig() {
        return new StrategoAnalyzeConfig(rootDirectory, mainFile, includeDirs, builtinLibs, gradualTypingSetting, sourceFileOrigins);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final StrategoCompileConfig that = (StrategoCompileConfig)o;
        if(!rootDirectory.equals(that.rootDirectory)) return false;
        if(!mainFile.equals(that.mainFile)) return false;
        if(!includeDirs.equals(that.includeDirs)) return false;
        if(!builtinLibs.equals(that.builtinLibs)) return false;
        if(gradualTypingSetting != that.gradualTypingSetting) return false;
        if(!extraCompilerArguments.equals(that.extraCompilerArguments)) return false;
        if(!sourceFileOrigins.equals(that.sourceFileOrigins)) return false;
        if(cacheDir != null ? !cacheDir.equals(that.cacheDir) : that.cacheDir != null) return false;
        if(!outputDir.equals(that.outputDir)) return false;
        return outputJavaPackageId.equals(that.outputJavaPackageId);
    }

    @Override public int hashCode() {
        int result = rootDirectory.hashCode();
        result = 31 * result + mainFile.hashCode();
        result = 31 * result + includeDirs.hashCode();
        result = 31 * result + builtinLibs.hashCode();
        result = 31 * result + gradualTypingSetting.hashCode();
        result = 31 * result + extraCompilerArguments.hashCode();
        result = 31 * result + sourceFileOrigins.hashCode();
        result = 31 * result + (cacheDir != null ? cacheDir.hashCode() : 0);
        result = 31 * result + outputDir.hashCode();
        result = 31 * result + outputJavaPackageId.hashCode();
        return result;
    }

    @Override public String toString() {
        return "StrategoCompileConfig{" +
            "rootDirectory=" + rootDirectory +
            ", mainFile=" + mainFile +
            ", includeDirs=" + includeDirs +
            ", builtinLibs=" + builtinLibs +
            ", gradualTypingSetting=" + gradualTypingSetting +
            ", extraCompilerArguments=" + extraCompilerArguments +
            ", sourceFileOrigins=" + sourceFileOrigins +
            ", cacheDir=" + cacheDir +
            ", outputDir=" + outputDir +
            ", outputJavaPackageId='" + outputJavaPackageId + '\'' +
            '}';
    }
}
