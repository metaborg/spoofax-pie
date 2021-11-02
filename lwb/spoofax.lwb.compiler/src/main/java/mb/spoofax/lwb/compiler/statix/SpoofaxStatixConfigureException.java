package mb.spoofax.lwb.compiler.statix;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3ConfigureException;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Configuration exception for Statix in the context of the Spoofax LWB compiler.
 */
@ADT
public abstract class SpoofaxStatixConfigureException extends Exception {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R mainSourceDirectoryFail(ResourcePath mainSourceDirectory);

        R mainFileFail(ResourceKey mainFile);

        R sdf3ConfigureFail(SpoofaxSdf3ConfigureException spoofaxSdf3ConfigureException);

        R sdf3ExtStatixGenInjFail(Exception cause);
    }

    public static SpoofaxStatixConfigureException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(SpoofaxStatixConfigureExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static SpoofaxStatixConfigureException mainSourceDirectoryFail(ResourcePath sourceDirectory) {
        return SpoofaxStatixConfigureExceptions.mainSourceDirectoryFail(sourceDirectory);
    }

    public static SpoofaxStatixConfigureException mainFileFail(ResourceKey mainFile) {
        return SpoofaxStatixConfigureExceptions.mainFileFail(mainFile);
    }

    public static SpoofaxStatixConfigureException sdf3ConfigureFail(SpoofaxSdf3ConfigureException spoofaxSdf3ConfigureException) {
        return withCause(SpoofaxStatixConfigureExceptions.sdf3ConfigureFail(spoofaxSdf3ConfigureException), spoofaxSdf3ConfigureException);
    }

    public static SpoofaxStatixConfigureException sdf3ExtStatixGenInjFail(Exception cause) {
        return withCause(SpoofaxStatixConfigureExceptions.sdf3ExtStatixGenInjFail(cause), cause);
    }

    private static SpoofaxStatixConfigureException withCause(SpoofaxStatixConfigureException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static SpoofaxStatixConfigureExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return SpoofaxStatixConfigureExceptions.cases();
    }

    public SpoofaxStatixConfigureExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return SpoofaxStatixConfigureExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .getLanguageCompilerConfigurationFail((cause) -> "Getting language compiler configuration failed")
            .mainSourceDirectoryFail((mainSourceDirectory) -> "Statix main source directory '" + mainSourceDirectory + "' does not exist or is not a directory")
            .mainFileFail((mainFile) -> "Statix main file '" + mainFile + "' does not exist or is not a file")
            .sdf3ConfigureFail(cause -> "Configuring SDF3 failed")
            .sdf3ExtStatixGenInjFail(cause -> "SDF3 to Statix signature generator failed")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
