package mb.cfg;

import mb.common.util.ADT;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class DependencySource implements Serializable {
    interface Cases<R> {
        R coordinateRequirement(CoordinateRequirement coordinateRequirement);

        R coordinate(Coordinate coordinate);

        R path(String path);
    }

    public static DependencySource coordinateRequirement(CoordinateRequirement coordinateRequirement) {
        return DependencySources.coordinateRequirement(coordinateRequirement);
    }

    public static DependencySource coordinate(Coordinate coordinate) {
        return DependencySources.coordinate(coordinate);
    }

    public static DependencySource path(String path) {
        return DependencySources.path(path);
    }


    public abstract <R> R match(Cases<R> cases);

    public static DependencySources.CasesMatchers.TotalMatcher_CoordinateRequirement cases() {
        return DependencySources.cases();
    }

    public DependencySources.CaseOfMatchers.TotalMatcher_CoordinateRequirement caseOf() {
        return DependencySources.caseOf(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
