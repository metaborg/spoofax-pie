package mb.spoofax.lwb.compiler.esv;

import mb.common.util.ADT;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class EsvResolvedDependency implements Serializable {
    interface Cases<R> {
        R sourceDirectory(ResourcePath sourceDirectory);
    }

    public static EsvResolvedDependency sourceDirectory(ResourcePath sourceDirectory) {
        return EsvResolvedDependencies.sourceDirectory(sourceDirectory);
    }


    public abstract <R> R match(EsvResolvedDependency.Cases<R> cases);

    public static EsvResolvedDependencies.CasesMatchers.TotalMatcher_SourceDirectory cases() {
        return EsvResolvedDependencies.cases();
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
