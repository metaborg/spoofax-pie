package mb.spoofax.lwb.compiler.definition;

import mb.cfg.DependencySource;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.util.ADT;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Optional;

@ADT
public abstract class ResolveDependenciesException extends Exception {
    public interface Cases<R> {
        R getConfigurationFail(ResourcePath rootDirectory, CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R languageDefinitionOrComponentNotFoundFail(DependencySource dependencySource, Coordinate coordinate);

        R languageDefinitionOrComponentNotFoundOrMultipleFail(DependencySource dependencySource, CoordinateRequirement coordinateRequirement);

        R noResourcesComponentFail(DependencySource dependencySource, Coordinate coordinate);

        R noLanguageComponentFail(DependencySource dependencySource, Coordinate coordinate);

        R noResourceExportsFail(DependencySource dependencySource, Coordinate coordinate, String metaLanguageName);

        R resolveFromComponentFail(DependencySource dependencySource, Coordinate coordinate, IOException ioException);

        R configureFail(DependencySource dependencySource, ResourcePath rootDirectory, String metaLanguageName, Exception exception);

        R noConfigurationFail(DependencySource dependencySource, ResourcePath rootDirectory, String metaLanguageName);
    }

    public static ResolveDependenciesException getConfigurationFail(ResourcePath rootDirectory, CfgRootDirectoryToObjectException cause) {
        return withCause(ResolveDependenciesExceptions.getConfigurationFail(rootDirectory, cause), cause);
    }

    public static ResolveDependenciesException languageDefinitionOrComponentNotFoundFail(DependencySource dependencySource, Coordinate coordinate) {
        return ResolveDependenciesExceptions.languageDefinitionOrComponentNotFoundFail(dependencySource, coordinate);
    }

    public static ResolveDependenciesException languageDefinitionOrComponentNotFoundOrMultipleFail(DependencySource dependencySource, CoordinateRequirement coordinateRequirement) {
        return ResolveDependenciesExceptions.languageDefinitionOrComponentNotFoundOrMultipleFail(dependencySource, coordinateRequirement);
    }

    public static ResolveDependenciesException noResourcesComponentFail(DependencySource dependencySource, Coordinate coordinate) {
        return ResolveDependenciesExceptions.noResourcesComponentFail(dependencySource, coordinate);
    }

    public static ResolveDependenciesException noLanguageComponentFail(DependencySource dependencySource, Coordinate coordinate) {
        return ResolveDependenciesExceptions.noLanguageComponentFail(dependencySource, coordinate);
    }

    public static ResolveDependenciesException noResourceExportsFail(DependencySource dependencySource, Coordinate coordinate, String metaLanguageName) {
        return ResolveDependenciesExceptions.noResourceExportsFail(dependencySource, coordinate, metaLanguageName);
    }

    public static ResolveDependenciesException resolveFromComponentFail(DependencySource dependencySource, Coordinate coordinate, IOException cause) {
        return withCause(ResolveDependenciesExceptions.resolveFromComponentFail(dependencySource, coordinate, cause), cause);
    }

    public static ResolveDependenciesException configureFail(DependencySource dependencySource, ResourcePath rootDirectory, String metaLanguageName, Exception cause) {
        return withCause(ResolveDependenciesExceptions.configureFail(dependencySource, rootDirectory, metaLanguageName, cause), cause);
    }

    public static ResolveDependenciesException noConfigurationFail(DependencySource dependencySource, ResourcePath rootDirectory, String metaLanguageName) {
        return ResolveDependenciesExceptions.noConfigurationFail(dependencySource, rootDirectory, metaLanguageName);
    }

    private static ResolveDependenciesException withCause(ResolveDependenciesException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static ResolveDependenciesExceptions.CasesMatchers.TotalMatcher_GetConfigurationFail cases() {
        return ResolveDependenciesExceptions.cases();
    }

    public ResolveDependenciesExceptions.CaseOfMatchers.TotalMatcher_GetConfigurationFail caseOf() {
        return ResolveDependenciesExceptions.caseOf(this);
    }

    public Optional<DependencySource> getDependencySource() {
        return ResolveDependenciesExceptions.getDependencySource(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .getConfigurationFail((rootDirectory, e) -> "Cannot resolve dependencies to includes; getting configuration for language definition '" + rootDirectory + "' failed")
            .languageDefinitionOrComponentNotFoundFail((source, coordinate) -> messagePrefix(source) + "no language definition nor language component was found for coordinate '" + coordinate + "'")
            .languageDefinitionOrComponentNotFoundOrMultipleFail((source, coordinateRequirement) -> messagePrefix(source) + "no language definition nor language component was found, or multiple were found, for coordinate requirement '" + coordinateRequirement + "'")
            .noResourcesComponentFail((source, coordinate) -> messagePrefix(source) + "component with coordinates '" + coordinate + "' has no resources subcomponent")
            .noLanguageComponentFail((source, coordinate) -> messagePrefix(source) + "component with coordinates '" + coordinate + "' has no language subcomponent")
            .noResourceExportsFail((source, coordinate, metaLanguageName) -> messagePrefix(source) + "component with coordinates '" + coordinate + "' does not export " + metaLanguageName + " resources")
            .resolveFromComponentFail((source, coordinate, e) -> messagePrefix(source) + "resolving from component with coordinates '" + coordinate + "' failed unexpectedly")
            .configureFail((source, rootDirectory, metaLanguageName, e) -> messagePrefix(source) + "configuring " + metaLanguageName + " for language definition '" + rootDirectory + "' failed")
            .noConfigurationFail((source, rootDirectory, metaLanguageName) -> messagePrefix(source) + metaLanguageName + " is not configured for language definition '" + rootDirectory + "'")
            ;
    }

    private String messagePrefix(DependencySource source) {
        return "Cannot resolve dependency '" + source + "'; ";
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
