package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.DependencySource;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.util.ADT;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;

@ADT
public abstract class SpoofaxStrategoResolveIncludesException extends Exception {
    public interface Cases<R> {
        R languageDefinitionOrComponentNotFoundFail(DependencySource dependencySource, Coordinate coordinate);

        R languageDefinitionOrComponentNotFoundOrMultipleFail(DependencySource dependencySource, CoordinateRequirement coordinateRequirement);

        R noResourcesComponentFail(DependencySource dependencySource, Coordinate coordinate);

        R noLanguageComponentFail(DependencySource dependencySource, Coordinate coordinate);

        R getClassLoaderResourcesLocationsFail(DependencySource dependencySource, Coordinate coordinate, IOException ioException);

        R noStrategoResourceExportsFail(DependencySource dependencySource, Coordinate coordinate);

        R strategoConfigureFail(DependencySource dependencySource, ResourcePath rootDirectory, SpoofaxStrategoConfigureException spoofaxStrategoConfigureException);

        R getConfigurationFail(DependencySource dependencySource, ResourcePath rootDirectory, CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R noStrategoConfiguration(DependencySource dependencySource, ResourcePath rootDirectory);
    }

    public static SpoofaxStrategoResolveIncludesException languageDefinitionOrComponentNotFoundFail(DependencySource dependencySource, Coordinate coordinate) {
        return SpoofaxStrategoResolveIncludesExceptions.languageDefinitionOrComponentNotFoundFail(dependencySource, coordinate);
    }

    public static SpoofaxStrategoResolveIncludesException languageDefinitionOrComponentNotFoundOrMultipleFail(DependencySource dependencySource, CoordinateRequirement coordinateRequirement) {
        return SpoofaxStrategoResolveIncludesExceptions.languageDefinitionOrComponentNotFoundOrMultipleFail(dependencySource, coordinateRequirement);
    }

    public static SpoofaxStrategoResolveIncludesException noResourcesComponentFail(DependencySource dependencySource, Coordinate coordinate) {
        return SpoofaxStrategoResolveIncludesExceptions.noResourcesComponentFail(dependencySource, coordinate);
    }

    public static SpoofaxStrategoResolveIncludesException noLanguageComponentFail(DependencySource dependencySource, Coordinate coordinate) {
        return SpoofaxStrategoResolveIncludesExceptions.noLanguageComponentFail(dependencySource, coordinate);
    }

    public static SpoofaxStrategoResolveIncludesException getClassLoaderResourcesLocationsFail(DependencySource dependencySource, Coordinate coordinate, IOException cause) {
        return withCause(SpoofaxStrategoResolveIncludesExceptions.getClassLoaderResourcesLocationsFail(dependencySource, coordinate, cause), cause);
    }

    public static SpoofaxStrategoResolveIncludesException noStrategoResourceExportsFail(DependencySource dependencySource, Coordinate coordinate) {
        return SpoofaxStrategoResolveIncludesExceptions.noStrategoResourceExportsFail(dependencySource, coordinate);
    }

    public static SpoofaxStrategoResolveIncludesException strategoConfigureFail(DependencySource dependencySource, ResourcePath rootDirectory, SpoofaxStrategoConfigureException cause) {
        return withCause(SpoofaxStrategoResolveIncludesExceptions.strategoConfigureFail(dependencySource, rootDirectory, cause), cause);
    }

    public static SpoofaxStrategoResolveIncludesException getConfigurationFail(DependencySource dependencySource, ResourcePath rootDirectory, CfgRootDirectoryToObjectException cause) {
        return withCause(SpoofaxStrategoResolveIncludesExceptions.getConfigurationFail(dependencySource, rootDirectory, cause), cause);
    }

    public static SpoofaxStrategoResolveIncludesException noStrategoConfiguration(DependencySource dependencySource, ResourcePath rootDirectory) {
        return SpoofaxStrategoResolveIncludesExceptions.noStrategoConfiguration(dependencySource, rootDirectory);
    }

    private static SpoofaxStrategoResolveIncludesException withCause(SpoofaxStrategoResolveIncludesException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static SpoofaxStrategoResolveIncludesExceptions.CasesMatchers.TotalMatcher_LanguageDefinitionOrComponentNotFoundFail cases() {
        return SpoofaxStrategoResolveIncludesExceptions.cases();
    }

    public SpoofaxStrategoResolveIncludesExceptions.CaseOfMatchers.TotalMatcher_LanguageDefinitionOrComponentNotFoundFail caseOf() {
        return SpoofaxStrategoResolveIncludesExceptions.caseOf(this);
    }

    public DependencySource getDependencySource() {
        return SpoofaxStrategoResolveIncludesExceptions.getDependencySource(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .languageDefinitionOrComponentNotFoundFail((source, coordinate) -> messagePrefix() + "no language definition nor language component was found for coordinate '" + coordinate + "'")
            .languageDefinitionOrComponentNotFoundOrMultipleFail((source, coordinateRequirement) -> messagePrefix() + "no language definition nor language component was found, or multiple were found, for coordinate requirement '" + coordinateRequirement + "'")
            .noResourcesComponentFail((source, coordinate) -> messagePrefix() + "component with coordinates '" + coordinate + "' has no resources subcomponent")
            .noLanguageComponentFail((source, coordinate) -> messagePrefix() + "component with coordinates '" + coordinate + "' has no language subcomponent")
            .getClassLoaderResourcesLocationsFail((source, coordinate, e) -> messagePrefix() + "getting classloader resource locations failed")
            .noStrategoResourceExportsFail((source, coordinate) -> messagePrefix() + "component with coordinates '" + coordinate + "' does not export Stratego resources")
            .strategoConfigureFail((source, rootDirectory, e) -> messagePrefix() + "configuring Stratego for language definition '" + rootDirectory + "' failed")
            .getConfigurationFail((source, rootDirectory, e) -> messagePrefix() + "getting configuration for language definition '" + rootDirectory + "' failed")
            .noStrategoConfiguration((source, rootDirectory) -> messagePrefix() + "Stratego is not configured for language definition '" + rootDirectory + "'")
            ;
    }

    private String messagePrefix() {
        return "Cannot resolve dependency '" + getDependencySource() + "' to Stratego exports; ";
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
