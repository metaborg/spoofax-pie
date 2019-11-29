package mb.spoofax.compiler.util;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class GradleConfiguredDependency implements Serializable {
    interface Cases<R> {
        R api(GradleDependency dependency);

        R implementation(GradleDependency dependency);

        R compileOnly(GradleDependency dependency);

        R runtimeOnly(GradleDependency dependency);


        R testImplementation(GradleDependency dependency);

        R testCompileOnly(GradleDependency dependency);

        R testRuntimeOnly(GradleDependency dependency);


        R annotationProcessor(GradleDependency dependency);

        R testAnnotationProcessor(GradleDependency dependency);
    }


    public abstract <R> R match(Cases<R> cases);


    public GradleDependency getDependency() {
        return GradleConfiguredDependencies.getDependency(this);
    }

    public GradleConfiguredDependencies.CaseOfMatchers.TotalMatcher_Api caseOf() {
        return GradleConfiguredDependencies.caseOf(this);
    }

    public String toKotlinCode() {
        return caseOf()
            .api((dependency) -> "api(" + dependency.toKotlinCode() + ")")
            .implementation((dependency) -> "implementation(" + dependency.toKotlinCode() + ")")
            .compileOnly((dependency) -> "compileOnly(" + dependency.toKotlinCode() + ")")
            .runtimeOnly((dependency) -> "runtimeOnly(" + dependency.toKotlinCode() + ")")
            .testImplementation((dependency) -> "testImplementation(" + dependency.toKotlinCode() + ")")
            .testCompileOnly((dependency) -> "testCompileOnly(" + dependency.toKotlinCode() + ")")
            .testRuntimeOnly((dependency) -> "testRuntimeOnly(" + dependency.toKotlinCode() + ")")
            .annotationProcessor((dependency) -> "annotationProcessor(" + dependency.toKotlinCode() + ")")
            .testAnnotationProcessor((dependency) -> "testAnnotationProcessor(" + dependency.toKotlinCode() + ")")
            ;
    }


    public static GradleConfiguredDependency api(GradleDependency dependency) {
        return GradleConfiguredDependencies.api(dependency);
    }

    public static GradleConfiguredDependency implementation(GradleDependency dependency) {
        return GradleConfiguredDependencies.implementation(dependency);
    }

    public static GradleConfiguredDependency compileOnly(GradleDependency dependency) {
        return GradleConfiguredDependencies.compileOnly(dependency);
    }

    public static GradleConfiguredDependency runtimeOnly(GradleDependency dependency) {
        return GradleConfiguredDependencies.runtimeOnly(dependency);
    }

    public static GradleConfiguredDependency testImplementation(GradleDependency dependency) {
        return GradleConfiguredDependencies.testImplementation(dependency);
    }

    public static GradleConfiguredDependency testCompileOnly(GradleDependency dependency) {
        return GradleConfiguredDependencies.testCompileOnly(dependency);
    }

    public static GradleConfiguredDependency testRuntimeOnly(GradleDependency dependency) {
        return GradleConfiguredDependencies.testRuntimeOnly(dependency);
    }

    public static GradleConfiguredDependency annotationProcessor(GradleDependency dependency) {
        return GradleConfiguredDependencies.annotationProcessor(dependency);
    }

    public static GradleConfiguredDependency testAnnotationProcessor(GradleDependency dependency) {
        return GradleConfiguredDependencies.testAnnotationProcessor(dependency);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
