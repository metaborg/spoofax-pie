package mb.spoofax.lwb.compiler.sdf3;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.definition.ResolveDependenciesException;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Configure exception for SDF3 in the context of the Spoofax LWB compiler.
 */
@ADT
public abstract class SpoofaxSdf3ConfigureException extends Exception {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R mainSourceDirectoryFail(ResourcePath mainSourceDirectory);

        R mainFileFail(ResourceKey mainFile);

        R includeDirectoryFail(ResourcePath includeDirectory);

        R resolveIncludeFail(ResolveDependenciesException resolveDependenciesException);
    }

    public static SpoofaxSdf3ConfigureException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(SpoofaxSdf3ConfigureExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static SpoofaxSdf3ConfigureException mainSourceDirectoryFail(ResourcePath sourceDirectory) {
        return SpoofaxSdf3ConfigureExceptions.mainSourceDirectoryFail(sourceDirectory);
    }

    public static SpoofaxSdf3ConfigureException mainFileFail(ResourceKey mainFile) {
        return SpoofaxSdf3ConfigureExceptions.mainFileFail(mainFile);
    }

    public static SpoofaxSdf3ConfigureException includeDirectoryFail(ResourcePath includeDirectory) {
        return SpoofaxSdf3ConfigureExceptions.includeDirectoryFail(includeDirectory);
    }

    public static SpoofaxSdf3ConfigureException resolveIncludeFail(ResolveDependenciesException cause) {
        return withCause(SpoofaxSdf3ConfigureExceptions.resolveIncludeFail(cause), cause);
    }

    private static SpoofaxSdf3ConfigureException withCause(SpoofaxSdf3ConfigureException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static SpoofaxSdf3ConfigureExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return SpoofaxSdf3ConfigureExceptions.cases();
    }

    public SpoofaxSdf3ConfigureExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return SpoofaxSdf3ConfigureExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .getLanguageCompilerConfigurationFail((cause) -> "Getting language compiler configuration failed")
            .mainSourceDirectoryFail((mainSourceDirectory) -> "SDF3 main source directory '" + mainSourceDirectory + "' does not exist or is not a directory")
            .mainFileFail((mainFile) -> "SDF3 main file '" + mainFile + "' does not exist or is not a file")
            .includeDirectoryFail(includeDirectory -> "Stratego include directory '" + includeDirectory + "' does not exist or is not a directory")
            .resolveIncludeFail(cause -> "Resolving compile-time dependency to Stratego includes failed")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
