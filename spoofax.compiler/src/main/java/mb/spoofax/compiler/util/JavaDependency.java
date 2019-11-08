package mb.spoofax.compiler.util;

import mb.common.util.ADT;
import mb.common.util.ListView;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.stream.Collectors;

@ADT
public abstract class JavaDependency implements Serializable {
    interface Cases<R> {
        R project(String projectPath);

        R module(Coordinate coordinate);

        R files(ListView<String> filePaths);
    }


    public abstract <R> R match(Cases<R> cases);


    public JavaDependencies.CaseOfMatchers.TotalMatcher_Project caseOf() {
        return JavaDependencies.caseOf(this);
    }

    public String toGradleKotlinDependencyCode() {
        return caseOf()
            .project((projectPath) -> "project(\"" + projectPath + "\")")
            .module((coordinate) -> "\"" + coordinate.gradleNotation() + "\"")
            .files((filePaths) -> "files(" + filePaths.stream().map((s) -> "\"" + s + "\"").collect(Collectors.joining(", ")) + ")");
    }


    public static JavaDependency project(String projectPath) {
        return JavaDependencies.project(projectPath);
    }

    public static JavaDependency module(Coordinate coordinate) {
        return JavaDependencies.module(coordinate);
    }

    public static JavaDependency module(String gradleNotation) {
        return JavaDependencies.module(Coordinate.fromGradleNotation(gradleNotation));
    }

    public static JavaDependency files(ListView<String> filePaths) {
        return JavaDependencies.files(filePaths);
    }

    public static JavaDependency files(String... filePaths) {
        return JavaDependencies.files(ListView.of(filePaths));
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
