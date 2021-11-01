package mb.spoofax.lwb.compiler.statix;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3ConfigureException;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class StatixConfigureException extends Exception {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R mainSourceDirectoryFail(ResourcePath mainSourceDirectory);

        R mainFileFail(ResourceKey mainFile);

        R sdf3ConfigureFail(SpoofaxSdf3ConfigureException spoofaxSdf3ConfigureException);

        R sdf3ExtStatixGenInjFail(Exception cause);
    }

    public static StatixConfigureException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(StatixConfigureExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static StatixConfigureException mainSourceDirectoryFail(ResourcePath sourceDirectory) {
        return StatixConfigureExceptions.mainSourceDirectoryFail(sourceDirectory);
    }

    public static StatixConfigureException mainFileFail(ResourceKey mainFile) {
        return StatixConfigureExceptions.mainFileFail(mainFile);
    }

    public static StatixConfigureException sdf3ConfigureFail(SpoofaxSdf3ConfigureException spoofaxSdf3ConfigureException) {
        return withCause(StatixConfigureExceptions.sdf3ConfigureFail(spoofaxSdf3ConfigureException), spoofaxSdf3ConfigureException);
    }

    public static StatixConfigureException sdf3ExtStatixGenInjFail(Exception cause) {
        return withCause(StatixConfigureExceptions.sdf3ExtStatixGenInjFail(cause), cause);
    }

    private static StatixConfigureException withCause(StatixConfigureException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static StatixConfigureExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return StatixConfigureExceptions.cases();
    }

    public StatixConfigureExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return StatixConfigureExceptions.caseOf(this);
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
