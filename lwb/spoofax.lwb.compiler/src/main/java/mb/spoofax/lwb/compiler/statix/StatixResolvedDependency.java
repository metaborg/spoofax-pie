package mb.spoofax.lwb.compiler.statix;

import mb.common.util.ADT;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.sdf3.Sdf3ResolvedDependencies;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class StatixResolvedDependency implements Serializable {
    interface Cases<R> {
        R sourceDirectory(ResourcePath sourceDirectory);
    }

    public static StatixResolvedDependency sourceDirectory(ResourcePath sourceDirectory) {
        return StatixResolvedDependencies.sourceDirectory(sourceDirectory);
    }


    public abstract <R> R match(StatixResolvedDependency.Cases<R> cases);

    public static StatixResolvedDependencies.CasesMatchers.TotalMatcher_SourceDirectory cases() {
        return StatixResolvedDependencies.cases();
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
