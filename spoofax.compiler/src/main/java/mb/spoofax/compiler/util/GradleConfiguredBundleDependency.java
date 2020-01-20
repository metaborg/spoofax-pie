package mb.spoofax.compiler.util;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

import static mb.spoofax.compiler.util.StringUtil.doubleQuote;

@ADT
public abstract class GradleConfiguredBundleDependency implements Serializable {
    interface Cases<R> {
        R bundle(GradleDependency dependency, boolean reexport);

        R embeddingBundle(GradleDependency dependency, boolean reexport);

        R targetPlatform(String name, @Nullable String version, boolean reexport);
    }

    public static GradleConfiguredBundleDependency bundle(GradleDependency dependency, boolean reexport) {
        return GradleConfiguredBundleDependencies.bundle(dependency, reexport);
    }

    public static GradleConfiguredBundleDependency embeddingBundle(GradleDependency dependency, boolean reexport) {
        return GradleConfiguredBundleDependencies.embeddingBundle(dependency, reexport);
    }

    public static GradleConfiguredBundleDependency targetPlatform(String name, @Nullable String version, boolean reexport) {
        return GradleConfiguredBundleDependencies.targetPlatform(name, version, reexport);
    }


    public abstract <R> R match(Cases<R> cases);

    public GradleConfiguredBundleDependencies.CaseOfMatchers.TotalMatcher_Bundle caseOf() {
        return GradleConfiguredBundleDependencies.caseOf(this);
    }

    public String toKotlinCode() {
        return caseOf()
            .bundle((dependency, reexport) -> "requireBundle(" + dependency.toKotlinCode() + ", " + reexport + ")")
            .embeddingBundle((dependency, reexport) -> "requireEmbeddingBundle(" + dependency.toKotlinCode() + ", " + reexport + ")")
            .targetPlatform((name, version, reexport) -> "requireTargetPlatform(" + doubleQuote(name) + ", " + (version != null ? doubleQuote(version) : "null") + ", " + reexport + ")")
            ;
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
