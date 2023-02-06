package mb.spoofax.lwb.compiler.sdf3;

import mb.common.util.ADT;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class Sdf3ResolvedDependency implements Serializable {
    interface Cases<R> {
        R sourceDirectory(ResourcePath sourceDirectory);
    }

    public static Sdf3ResolvedDependency sourceDirectory(ResourcePath sourceDirectory) {
        return Sdf3ResolvedDependencies.sourceDirectory(sourceDirectory);
    }


    public abstract <R> R match(Sdf3ResolvedDependency.Cases<R> cases);

    public static Sdf3ResolvedDependencies.CasesMatchers.TotalMatcher_SourceDirectory cases() {
        return Sdf3ResolvedDependencies.cases();
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
