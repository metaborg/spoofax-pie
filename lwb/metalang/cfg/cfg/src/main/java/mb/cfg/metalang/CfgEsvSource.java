package mb.cfg.metalang;

import mb.common.util.ADT;
import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Optional;

/**
 * Configuration for ESV sources in the context of the CFG meta-language.
 */
@ADT
public abstract class CfgEsvSource implements Serializable {
    interface Cases<R> {
        R files(
            ResourcePath mainSourceDirectory,
            ResourcePath mainFile,
            ListView<ResourcePath> includeDirectories,
            boolean includeLibSpoofax2Exports,
            ResourcePath libSpoofax2UnarchiveDirectory
        );

        R prebuilt(ResourcePath esvAtermFile);
    }

    public static CfgEsvSource files(
        ResourcePath mainSourceDirectory,
        ResourcePath mainFile,
        ListView<ResourcePath> includeDirectories,
        boolean includeLibSpoofax2Exports,
        ResourcePath libSpoofax2UnarchiveDirectory
    ) {
        return CfgEsvSources.files(mainSourceDirectory, mainFile, includeDirectories, includeLibSpoofax2Exports, libSpoofax2UnarchiveDirectory);
    }

    public static CfgEsvSource prebuilt(ResourcePath inputFile) {
        return CfgEsvSources.prebuilt(inputFile);
    }


    public abstract <R> R match(Cases<R> cases);

    public static CfgEsvSources.CasesMatchers.TotalMatcher_Files cases() {
        return CfgEsvSources.cases();
    }

    public CfgEsvSources.CaseOfMatchers.TotalMatcher_Files caseOf() {
        return CfgEsvSources.caseOf(this);
    }

    public Optional<ResourcePath> getMainSourceDirectory() {
        return CfgEsvSources.getMainSourceDirectory(this);
    }

    public Optional<ResourcePath> getMainFile() {
        return CfgEsvSources.getMainFile(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
