package mb.spoofax.compiler.util;

import mb.common.util.ADT;
import mb.common.util.ListView;
import mb.spoofax.core.CoordinateRequirement;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.stream.Collectors;

@ADT
public abstract class GradleDependency implements Serializable {
    interface Cases<R> {
        R project(String projectPath);

        R module(CoordinateRequirement coordinateRequirement);

        R files(ListView<String> filePaths);
    }

    public static GradleDependency project(String projectPath) {
        return GradleDependencies.project(projectPath);
    }

    public static GradleDependency module(CoordinateRequirement coordinateRequirement) {
        return GradleDependencies.module(coordinateRequirement);
    }

    public static GradleDependency module(String gradleNotation) {
        return GradleDependencies.module(CoordinateRequirement.parse(gradleNotation)
            .unwrapOrElseThrow(() -> new IllegalArgumentException("Cannot parse '" + gradleNotation + "' into a coordinate requirement")));
    }

    public static GradleDependency files(ListView<String> filePaths) {
        return GradleDependencies.files(filePaths);
    }

    public static GradleDependency files(String... filePaths) {
        return GradleDependencies.files(ListView.of(filePaths));
    }


    public abstract <R> R match(Cases<R> cases);

    public GradleDependencies.CaseOfMatchers.TotalMatcher_Project caseOf() {
        return GradleDependencies.caseOf(this);
    }

    public String toKotlinCode() {
        return caseOf()
            .project((projectPath) -> "project(\"" + projectPath + "\")")
            .module((coordinateRequirement) -> "\"" + coordinateRequirement + "\"")
            .files((filePaths) -> "files(" + filePaths.stream().map((s) -> "\"" + s + "\"").collect(Collectors.joining(", ")) + ")");
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
