package mb.cfg.metalang;

import mb.common.util.ADT;
import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Optional;

/**
 * Configuration for Statix sources in the context of CFG.
 */
@ADT
public abstract class CfgStatixSource implements Serializable {
    interface Cases<R> {
        R files(
            ResourcePath mainSourceDirectory,
            ResourcePath mainFile,
            ListView<ResourcePath> includeDirectories
        );

        R prebuilt(ResourcePath specAtermDirectory);
    }

    public static CfgStatixSource files(
        ResourcePath mainSourceDirectory,
        ResourcePath mainFile,
        ListView<ResourcePath> includeDirectories
    ) {
        return CfgStatixSources.files(mainSourceDirectory, mainFile, includeDirectories);
    }

    public static CfgStatixSource prebuilt(ResourcePath specAtermDirectory) {
        return CfgStatixSources.prebuilt(specAtermDirectory);
    }


    public abstract <R> R match(Cases<R> cases);

    public static CfgStatixSources.CasesMatchers.TotalMatcher_Files cases() {
        return CfgStatixSources.cases();
    }

    public CfgStatixSources.CaseOfMatchers.TotalMatcher_Files caseOf() {
        return CfgStatixSources.caseOf(this);
    }

    public Optional<ResourcePath> getMainSourceDirectory() {
        return CfgStatixSources.getMainSourceDirectory(this);
    }

    public Optional<ResourcePath> getMainFile() {
        return CfgStatixSources.getMainFile(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
