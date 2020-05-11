package mb.spoofax.compiler.util;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class GradleRepository implements Serializable {
    interface Cases<R> {
        R mavenCentral();

        R jcenter();

        R maven(String url);

        R gradlePluginPortal();
    }

    public static GradleRepository mavenCentral() {
        return GradleRepositories.mavenCentral();
    }

    public static GradleRepository jcenter() {
        return GradleRepositories.jcenter();
    }

    public static GradleRepository maven(String url) {
        return GradleRepositories.maven(url);
    }

    public static GradleRepository gradlePluginPortal() {
        return GradleRepositories.gradlePluginPortal();
    }


    public abstract <R> R match(Cases<R> cases);

    public GradleRepositories.CaseOfMatchers.TotalMatcher_MavenCentral caseOf() {
        return GradleRepositories.caseOf(this);
    }

    public String toKotlinCode() {
        return caseOf()
            .mavenCentral_("mavenCentral()")
            .jcenter_("jcenter()")
            .maven((url) -> "maven(\"" + url + "\")")
            .gradlePluginPortal_("gradlePluginPortal()")
            ;
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
