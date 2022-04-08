package mb.spoofax.lwb.compiler.stratego;

import mb.common.util.ADT;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.Serializable;

@ADT
public abstract class StrategoResolvedDependency implements Serializable {
    interface Cases<R> {
        R sourceDirectory(ResourcePath sourceDirectory);

        R compiledLibrary(ResourcePath str2libFile, SetView<File> javaClassPaths);
    }

    public static StrategoResolvedDependency sourceDirectory(ResourcePath sourceDirectory) {
        return StrategoResolvedDependencies.sourceDirectory(sourceDirectory);
    }

    public static StrategoResolvedDependency compiledLibrary(ResourcePath str2libFile, SetView<File> javaClassPaths) {
        return StrategoResolvedDependencies.compiledLibrary(str2libFile, javaClassPaths);
    }


    public abstract <R> R match(StrategoResolvedDependency.Cases<R> cases);

    public static StrategoResolvedDependencies.CasesMatchers.TotalMatcher_SourceDirectory cases() {
        return StrategoResolvedDependencies.cases();
    }

    public StrategoResolvedDependencies.CaseOfMatchers.TotalMatcher_SourceDirectory caseOf() {
        return StrategoResolvedDependencies.caseOf(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
