package mb.spoofax.compiler.util;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class GradleAddDependency implements Serializable {
    interface Cases<R> {
        R api(GradleDependency dependency);

        R implementation(GradleDependency dependency);
    }


    public abstract <R> R match(Cases<R> cases);


    public GradleDependency getDependency() {
        return GradleAddDependencies.getDependency(this);
    }

    public GradleAddDependencies.CaseOfMatchers.TotalMatcher_Api caseOf() {
        return GradleAddDependencies.caseOf(this);
    }

    public String toKotlinCode() {
        return caseOf()
            .api((dependency) -> "api(" + dependency.toKotlinCode() + ")")
            .implementation((dependency) -> "implementation(" + dependency.toKotlinCode() + ")");
    }


    public static GradleAddDependency api(GradleDependency dependency) {
        return GradleAddDependencies.api(dependency);
    }

    public static GradleAddDependency implementation(GradleDependency dependency) {
        return GradleAddDependencies.implementation(dependency);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
