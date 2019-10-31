package mb.spoofax.compiler.spoofaxcore;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class JavaDependency implements Serializable {
    interface Cases<R> {
        R project(String projectPath);

        R module(Coordinate coordinate);
    }


    public abstract <R> R match(Cases<R> cases);


    public JavaDependencies.CaseOfMatchers.TotalMatcher_Project caseOf() {
        return JavaDependencies.caseOf(this);
    }


    public String toGradleDependency() {
        return caseOf()
            .project((projectPath) -> "project(\"" + projectPath + "\")")
            .module(Coordinate::gradleNotation);
    }


    public static JavaDependency project(String projectPath) {
        return JavaDependencies.project(projectPath);
    }

    public static JavaDependency module(Coordinate coordinate) {
        return JavaDependencies.module(coordinate);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
