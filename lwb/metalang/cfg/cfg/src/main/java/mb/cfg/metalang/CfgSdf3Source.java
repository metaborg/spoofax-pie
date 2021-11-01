package mb.cfg.metalang;

import mb.common.util.ADT;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Optional;

/**
 * Configuration for SDF3 sources in the context of the CFG meta-language.
 */
@ADT
public abstract class CfgSdf3Source implements Serializable {
    interface Cases<R> {
        R files(ResourcePath mainSourceDirectory, ResourcePath mainFile);

        R prebuilt(ResourcePath inputParseTableAtermFile, ResourcePath inputParseTablePersistedFile);
    }

    public static CfgSdf3Source files(ResourcePath mainSourceDirectory, ResourcePath mainFile) {
        return CfgSdf3Sources.files(mainSourceDirectory, mainFile);
    }

    public static CfgSdf3Source prebuilt(ResourcePath inputParseTableAtermFile, ResourcePath inputParseTablePersistedFile) {
        return CfgSdf3Sources.prebuilt(inputParseTableAtermFile, inputParseTablePersistedFile);
    }


    public abstract <R> R match(Cases<R> cases);

    public static CfgSdf3Sources.CasesMatchers.TotalMatcher_Files cases() {
        return CfgSdf3Sources.cases();
    }

    public CfgSdf3Sources.CaseOfMatchers.TotalMatcher_Files caseOf() {
        return CfgSdf3Sources.caseOf(this);
    }

    public Optional<ResourcePath> getMainSourceDirectory() {
        return CfgSdf3Sources.getMainSourceDirectory(this);
    }

    public Optional<ResourcePath> getMainFile() {
        return CfgSdf3Sources.getMainFile(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
