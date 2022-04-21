package mb.spoofax.core.language;

import mb.common.util.ADT;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class Export implements Serializable {
    interface Cases<R> {
        /**
         * Export of a single file by relative path. {@link ResourcePath#appendAsRelativePath(String) Append the
         * relative path} to get the exported file.
         *
         * @param relativePath Path to a file relative to the definition directory.
         */
        R file(String relativePath);

        /**
         * Export of the resources inside a directory by relative path. {@link ResourcePath#appendAsRelativePath(String)
         * Append the relative path} to get the exported directory
         *
         * @param relativePath Path to a directory relative to the definition directory.
         */
        R directory(String relativePath);
    }

    public static Export file(String relativePath) {
        return Exports.file(relativePath);
    }

    public static Export directory(String relativePath) {
        return Exports.directory(relativePath);
    }

    public abstract <R> R match(Cases<R> cases);

    public Exports.CaseOfMatchers.TotalMatcher_File caseOf() {
        return Exports.caseOf(this);
    }

    public String getRelativePath() {
        return Exports.getRelativePath(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
