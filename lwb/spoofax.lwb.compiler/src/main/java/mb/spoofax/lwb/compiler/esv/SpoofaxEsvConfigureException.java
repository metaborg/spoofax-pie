package mb.spoofax.lwb.compiler.esv;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3ConfigureException;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Error for ESV configuration task in the context of the Spoofax LWB compiler.
 */
@ADT
public abstract class SpoofaxEsvConfigureException extends Exception {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R mainSourceDirectoryFail(ResourcePath mainSourceDirectory);

        R mainFileFail(ResourceKey mainFile);

        R includeDirectoryFail(ResourcePath includeDirectory);

        R sdf3ConfigureFail(SpoofaxSdf3ConfigureException spoofaxSdf3ConfigureException);
    }

    public static SpoofaxEsvConfigureException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(SpoofaxEsvConfigureExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static SpoofaxEsvConfigureException mainSourceDirectoryFail(ResourcePath sourceDirectory) {
        return SpoofaxEsvConfigureExceptions.mainSourceDirectoryFail(sourceDirectory);
    }

    public static SpoofaxEsvConfigureException mainFileFail(ResourceKey mainFile) {
        return SpoofaxEsvConfigureExceptions.mainFileFail(mainFile);
    }

    public static SpoofaxEsvConfigureException includeDirectoryFail(ResourcePath includeDirectory) {
        return SpoofaxEsvConfigureExceptions.includeDirectoryFail(includeDirectory);
    }

    public static SpoofaxEsvConfigureException sdf3ConfigureFail(SpoofaxSdf3ConfigureException spoofaxSdf3ConfigureException) {
        return withCause(SpoofaxEsvConfigureExceptions.sdf3ConfigureFail(spoofaxSdf3ConfigureException), spoofaxSdf3ConfigureException);
    }

    private static SpoofaxEsvConfigureException withCause(SpoofaxEsvConfigureException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static SpoofaxEsvConfigureExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return SpoofaxEsvConfigureExceptions.cases();
    }

    public SpoofaxEsvConfigureExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return SpoofaxEsvConfigureExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .getLanguageCompilerConfigurationFail((cause) -> "Getting language compiler configuration failed")
            .mainSourceDirectoryFail((mainSourceDirectory) -> "ESV main source directory '" + mainSourceDirectory + "' does not exist or is not a directory")
            .mainFileFail((mainFile) -> "ESV main file '" + mainFile + "' does not exist or is not a file")
            .includeDirectoryFail((includeDirectory) -> "ESV include directory '" + includeDirectory + "' does not exist or is not a directory")
            .sdf3ConfigureFail((cause) -> "Failed to configure SDF3")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
