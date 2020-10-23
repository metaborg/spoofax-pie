package mb.str.config;

import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class StrategoCompileConfig implements Serializable {
    public final ResourcePath projectDir;
    public final ResourcePath mainFile;
    public final ListView<ResourcePath> includeDirs;
    public final ListView<String> builtinLibs;
    public final @Nullable ResourcePath cacheDir;
    public final ResourcePath outputDir;
    public final String outputJavaPackageId;

    public StrategoCompileConfig(
        ResourcePath projectDir,
        ResourcePath mainFile,
        ListView<ResourcePath> includeDirs,
        ListView<String> builtinLibs,
        @Nullable ResourcePath cacheDir,
        ResourcePath outputDir,
        String outputJavaPackageId
    ) {
        this.projectDir = projectDir;
        this.mainFile = mainFile;
        this.includeDirs = includeDirs;
        this.builtinLibs = builtinLibs;
        this.cacheDir = cacheDir;
        this.outputDir = outputDir;
        this.outputJavaPackageId = outputJavaPackageId;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final StrategoCompileConfig that = (StrategoCompileConfig)o;
        return projectDir.equals(that.projectDir) &&
            mainFile.equals(that.mainFile) &&
            includeDirs.equals(that.includeDirs) &&
            builtinLibs.equals(that.builtinLibs) &&
            Objects.equals(cacheDir, that.cacheDir) &&
            outputDir.equals(that.outputDir) &&
            outputJavaPackageId.equals(that.outputJavaPackageId);
    }

    @Override public int hashCode() {
        return Objects.hash(projectDir, mainFile, includeDirs, builtinLibs, cacheDir, outputDir, outputJavaPackageId);
    }

    @Override public String toString() {
        return "StrategoCompileConfig{" +
            "projectDir=" + projectDir +
            ", mainFile=" + mainFile +
            ", includeDirs=" + includeDirs +
            ", builtinLibs=" + builtinLibs +
            ", cacheDir=" + cacheDir +
            ", outputDir=" + outputDir +
            ", outputJavaPackageId='" + outputJavaPackageId + '\'' +
            '}';
    }
}
