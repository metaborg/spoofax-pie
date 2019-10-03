package mb.spoofax.compiler.spoofaxcore;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class GradleDependency {
    interface Cases<R> {
        R project(String projectPath);

        R module(String notation);
    }


    public abstract <R> R match(Cases<R> cases);


    public GradleDependencies.CaseOfMatchers.TotalMatcher_Project caseOf() {
        return GradleDependencies.caseOf(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
