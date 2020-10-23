package mb.str.config;

import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class StrategoAnalyzeConfig implements Serializable {
    public final ResourcePath projectDir;
    public final ResourcePath mainFile;
    public final ListView<ResourcePath> includeDirs;
    public final ListView<String> builtinLibs;

    public StrategoAnalyzeConfig(
        ResourcePath projectDir,
        ResourcePath mainFile,
        ListView<ResourcePath> includeDirs,
        ListView<String> builtinLibs
    ) {
        this.projectDir = projectDir;
        this.mainFile = mainFile;
        this.includeDirs = includeDirs;
        this.builtinLibs = builtinLibs;
    }

    public StrategoAnalyzeConfig(ResourcePath projectDir) {
        this(projectDir, projectDir.appendRelativePath("src/main/str/main.str"), ListView.of(), defaultBuiltinLibs());
    }

    private static ListView<String> defaultBuiltinLibs() {
        return ListView.of("stratego-lib");
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final StrategoAnalyzeConfig cfg = (StrategoAnalyzeConfig)o;
        return projectDir.equals(cfg.projectDir) &&
            mainFile.equals(cfg.mainFile) &&
            includeDirs.equals(cfg.includeDirs) &&
            builtinLibs.equals(cfg.builtinLibs);
    }

    @Override public int hashCode() {
        return Objects.hash(projectDir, mainFile, includeDirs, builtinLibs);
    }

    @Override public String toString() {
        return "StrategoAnalyzeConfig{" +
            "projectDir=" + projectDir +
            ", mainFile=" + mainFile +
            ", includeDirs=" + includeDirs +
            ", builtinLibs=" + builtinLibs +
            '}';
    }
}
