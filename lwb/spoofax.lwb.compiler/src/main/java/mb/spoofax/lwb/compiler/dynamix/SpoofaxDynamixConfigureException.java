package mb.spoofax.lwb.compiler.dynamix;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3ConfigureException;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Configuration exception for Dynamix in the context of the Spoofax LWB compiler.
 */
@ADT
public abstract class SpoofaxDynamixConfigureException extends Exception {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R mainSourceDirectoryFail(ResourcePath mainSourceDirectory);

        R mainFileFail(ResourceKey mainFile);

        R sdf3ConfigureFail(SpoofaxSdf3ConfigureException spoofaxSdf3ConfigureException);

        R sdf3ExtDynamixGenInjFail(Exception cause);
    }

    public static SpoofaxDynamixConfigureException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(SpoofaxDynamixConfigureExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static SpoofaxDynamixConfigureException mainSourceDirectoryFail(ResourcePath sourceDirectory) {
        return SpoofaxDynamixConfigureExceptions.mainSourceDirectoryFail(sourceDirectory);
    }

    public static SpoofaxDynamixConfigureException mainFileFail(ResourceKey mainFile) {
        return SpoofaxDynamixConfigureExceptions.mainFileFail(mainFile);
    }

    public static SpoofaxDynamixConfigureException sdf3ConfigureFail(SpoofaxSdf3ConfigureException spoofaxSdf3ConfigureException) {
        return withCause(SpoofaxDynamixConfigureExceptions.sdf3ConfigureFail(spoofaxSdf3ConfigureException), spoofaxSdf3ConfigureException);
    }

    public static SpoofaxDynamixConfigureException sdf3ExtDynamixGenInjFail(Exception cause) {
        return withCause(SpoofaxDynamixConfigureExceptions.sdf3ExtDynamixGenInjFail(cause), cause);
    }

    private static SpoofaxDynamixConfigureException withCause(SpoofaxDynamixConfigureException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static SpoofaxDynamixConfigureExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return SpoofaxDynamixConfigureExceptions.cases();
    }

    public SpoofaxDynamixConfigureExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return SpoofaxDynamixConfigureExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .getLanguageCompilerConfigurationFail((cause) -> "Getting language compiler configuration failed")
            .mainSourceDirectoryFail((mainSourceDirectory) -> "Dynamix main source directory '" + mainSourceDirectory + "' does not exist or is not a directory")
            .mainFileFail((mainFile) -> "Dynamix main file '" + mainFile + "' does not exist or is not a file")
            .sdf3ConfigureFail(cause -> "Configuring SDF3 failed")
            .sdf3ExtDynamixGenInjFail(cause -> "SDF3 to Dynamix signature generator failed")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
