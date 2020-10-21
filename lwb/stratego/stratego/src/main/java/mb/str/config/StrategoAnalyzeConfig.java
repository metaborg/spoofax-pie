package mb.str.config;

import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class StrategoAnalyzeConfig implements Serializable {
    public final ResourcePath projectDir;
    public final ResourcePath mainFile;
    public final ArrayList<ResourcePath> includeDirs;
    public final ArrayList<String> builtinLibs;

    public StrategoAnalyzeConfig(
        ResourcePath projectDir,
        ResourcePath mainFile,
        ArrayList<ResourcePath> includeDirs,
        ArrayList<String> builtinLibs
    ) {
        this.projectDir = projectDir;
        this.mainFile = mainFile;
        this.includeDirs = includeDirs;
        this.builtinLibs = builtinLibs;
    }

    public StrategoAnalyzeConfig(ResourcePath projectDir) {
        this(projectDir, projectDir.appendRelativePath("src/main/str/main.str"), new ArrayList<>(), defaultBuiltinLibs());
    }

    private static ArrayList<String> defaultBuiltinLibs() {
        final ArrayList<String> builtinLibs = new ArrayList<String>();
        builtinLibs.add("stratego-lib");
        return builtinLibs;
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
