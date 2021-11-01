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
public abstract class EsvConfigureException extends Exception {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R mainSourceDirectoryFail(ResourcePath mainSourceDirectory);

        R mainFileFail(ResourceKey mainFile);

        R includeDirectoryFail(ResourcePath includeDirectory);

        R sdf3ConfigureFail(SpoofaxSdf3ConfigureException spoofaxSdf3ConfigureException);
    }

    public static EsvConfigureException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(EsvConfigureExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static EsvConfigureException mainSourceDirectoryFail(ResourcePath sourceDirectory) {
        return EsvConfigureExceptions.mainSourceDirectoryFail(sourceDirectory);
    }

    public static EsvConfigureException mainFileFail(ResourceKey mainFile) {
        return EsvConfigureExceptions.mainFileFail(mainFile);
    }

    public static EsvConfigureException includeDirectoryFail(ResourcePath includeDirectory) {
        return EsvConfigureExceptions.includeDirectoryFail(includeDirectory);
    }

    public static EsvConfigureException sdf3ConfigureFail(SpoofaxSdf3ConfigureException spoofaxSdf3ConfigureException) {
        return withCause(EsvConfigureExceptions.sdf3ConfigureFail(spoofaxSdf3ConfigureException), spoofaxSdf3ConfigureException);
    }

    private static EsvConfigureException withCause(EsvConfigureException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static EsvConfigureExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return EsvConfigureExceptions.cases();
    }

    public EsvConfigureExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return EsvConfigureExceptions.caseOf(this);
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
