package mb.spoofax.compiler.util;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class GradleConfiguredBundleDependency implements Serializable {
    interface Cases<R> {
        R bundleApi(GradleDependency dependency);

        R bundleImplementation(GradleDependency dependency);

        R bundleEmbedApi(GradleDependency dependency);

        R bundleEmbedImplementation(GradleDependency dependency);

        R bundleTargetPlatformApi(String name, @Nullable String version);

        R bundleTargetPlatformImplementation(String name, @Nullable String version);
    }

    public static GradleConfiguredBundleDependency bundleApi(GradleDependency dependency) {
        return GradleConfiguredBundleDependencies.bundleApi(dependency);
    }

    public static GradleConfiguredBundleDependency bundleImplementation(GradleDependency dependency) {
        return GradleConfiguredBundleDependencies.bundleImplementation(dependency);
    }

    public static GradleConfiguredBundleDependency bundleEmbedApi(GradleDependency dependency) {
        return GradleConfiguredBundleDependencies.bundleEmbedApi(dependency);
    }

    public static GradleConfiguredBundleDependency bundleEmbedImplementation(GradleDependency dependency) {
        return GradleConfiguredBundleDependencies.bundleEmbedImplementation(dependency);
    }

    public static GradleConfiguredBundleDependency bundleTargetPlatformApi(String name, @Nullable String version) {
        return GradleConfiguredBundleDependencies.bundleTargetPlatformApi(name, version);
    }

    public static GradleConfiguredBundleDependency bundleTargetPlatformImplementation(String name, @Nullable String version) {
        return GradleConfiguredBundleDependencies.bundleTargetPlatformImplementation(name, version);
    }


    public abstract <R> R match(Cases<R> cases);

    public GradleConfiguredBundleDependencies.CaseOfMatchers.TotalMatcher_BundleApi caseOf() {
        return GradleConfiguredBundleDependencies.caseOf(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
