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
public abstract class ResolveIncludesException extends Exception {
    public interface Cases<R> {
        R getConfigurationFail(ResourcePath rootDirectory, CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R languageDefinitionOrComponentNotFoundFail(DependencySource dependencySource, Coordinate coordinate);

        R languageDefinitionOrComponentNotFoundOrMultipleFail(DependencySource dependencySource, CoordinateRequirement coordinateRequirement);

        R noResourcesComponentFail(DependencySource dependencySource, Coordinate coordinate);

        R noLanguageComponentFail(DependencySource dependencySource, Coordinate coordinate);

        R getClassLoaderResourcesLocationsFail(DependencySource dependencySource, Coordinate coordinate, IOException ioException);

        R noResourceExportsFail(DependencySource dependencySource, Coordinate coordinate, String metaLanguageName);

        R configureFail(DependencySource dependencySource, ResourcePath rootDirectory, String metaLanguageName, Exception exception);

        R noConfigurationFail(DependencySource dependencySource, ResourcePath rootDirectory, String metaLanguageName);
    }

    public static ResolveIncludesException getConfigurationFail(ResourcePath rootDirectory, CfgRootDirectoryToObjectException cause) {
        return withCause(ResolveIncludesExceptions.getConfigurationFail(rootDirectory, cause), cause);
    }

    public static ResolveIncludesException languageDefinitionOrComponentNotFoundFail(DependencySource dependencySource, Coordinate coordinate) {
        return ResolveIncludesExceptions.languageDefinitionOrComponentNotFoundFail(dependencySource, coordinate);
    }

    public static ResolveIncludesException languageDefinitionOrComponentNotFoundOrMultipleFail(DependencySource dependencySource, CoordinateRequirement coordinateRequirement) {
        return ResolveIncludesExceptions.languageDefinitionOrComponentNotFoundOrMultipleFail(dependencySource, coordinateRequirement);
    }

    public static ResolveIncludesException noResourcesComponentFail(DependencySource dependencySource, Coordinate coordinate) {
        return ResolveIncludesExceptions.noResourcesComponentFail(dependencySource, coordinate);
    }

    public static ResolveIncludesException noLanguageComponentFail(DependencySource dependencySource, Coordinate coordinate) {
        return ResolveIncludesExceptions.noLanguageComponentFail(dependencySource, coordinate);
    }

    public static ResolveIncludesException getClassLoaderResourcesLocationsFail(DependencySource dependencySource, Coordinate coordinate, IOException cause) {
        return withCause(ResolveIncludesExceptions.getClassLoaderResourcesLocationsFail(dependencySource, coordinate, cause), cause);
    }

    public static ResolveIncludesException noResourceExportsFail(DependencySource dependencySource, Coordinate coordinate, String metaLanguageName) {
        return ResolveIncludesExceptions.noResourceExportsFail(dependencySource, coordinate, metaLanguageName);
    }

    public static ResolveIncludesException configureFail(DependencySource dependencySource, ResourcePath rootDirectory, String metaLanguageName, Exception cause) {
        return withCause(ResolveIncludesExceptions.configureFail(dependencySource, rootDirectory, metaLanguageName, cause), cause);
    }

    public static ResolveIncludesException noConfigurationFail(DependencySource dependencySource, ResourcePath rootDirectory, String metaLanguageName) {
        return ResolveIncludesExceptions.noConfigurationFail(dependencySource, rootDirectory, metaLanguageName);
    }

    private static ResolveIncludesException withCause(ResolveIncludesException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static ResolveIncludesExceptions.CasesMatchers.TotalMatcher_GetConfigurationFail cases() {
        return ResolveIncludesExceptions.cases();
    }

    public ResolveIncludesExceptions.CaseOfMatchers.TotalMatcher_GetConfigurationFail caseOf() {
        return ResolveIncludesExceptions.caseOf(this);
    }

    public Optional<DependencySource> getDependencySource() {
        return ResolveIncludesExceptions.getDependencySource(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .getConfigurationFail((rootDirectory, e) -> "Cannot resolve dependencies to includes; getting configuration for language definition '" + rootDirectory + "' failed")
            .languageDefinitionOrComponentNotFoundFail((source, coordinate) -> messagePrefix(source) + "no language definition nor language component was found for coordinate '" + coordinate + "'")
            .languageDefinitionOrComponentNotFoundOrMultipleFail((source, coordinateRequirement) -> messagePrefix(source) + "no language definition nor language component was found, or multiple were found, for coordinate requirement '" + coordinateRequirement + "'")
            .noResourcesComponentFail((source, coordinate) -> messagePrefix(source) + "component with coordinates '" + coordinate + "' has no resources subcomponent")
            .noLanguageComponentFail((source, coordinate) -> messagePrefix(source) + "component with coordinates '" + coordinate + "' has no language subcomponent")
            .getClassLoaderResourcesLocationsFail((source, coordinate, e) -> messagePrefix(source) + "getting classloader resource locations failed")
            .noResourceExportsFail((source, coordinate, metaLanguageName) -> messagePrefix(source) + "component with coordinates '" + coordinate + "' does not export " + metaLanguageName + " resources")
            .configureFail((source, rootDirectory, metaLanguageName, e) -> messagePrefix(source) + "configuring " + metaLanguageName + " for language definition '" + rootDirectory + "' failed")
            .noConfigurationFail((source, rootDirectory, metaLanguageName) -> messagePrefix(source) + metaLanguageName + " is not configured for language definition '" + rootDirectory + "'")
            ;
    }

    private String messagePrefix(DependencySource source) {
        return "Cannot resolve dependency '" + source + "' to includes; ";
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
