package mb.spoofax.lwb.compiler.esv;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class EsvConfigureException extends Exception {
    public interface Cases<R> {
        R mainSourceDirectoryFail(ResourcePath mainSourceDirectory);

        R mainFileFail(ResourceKey mainFile);

        R includeDirectoryFail(ResourcePath includeDirectory);

        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);
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

    public static EsvConfigureException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(EsvConfigureExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    private static EsvConfigureException withCause(EsvConfigureException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static EsvConfigureExceptions.CasesMatchers.TotalMatcher_MainSourceDirectoryFail cases() {
        return EsvConfigureExceptions.cases();
    }

    public EsvConfigureExceptions.CaseOfMatchers.TotalMatcher_MainSourceDirectoryFail caseOf() {
        return EsvConfigureExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .mainSourceDirectoryFail((mainSourceDirectory) -> "ESV main source directory '" + mainSourceDirectory + "' does not exist or is not a directory")
            .mainFileFail((mainFile) -> "ESV main file '" + mainFile + "' does not exist or is not a file")
            .includeDirectoryFail((includeDirectory) -> "ESV include directory '" + includeDirectory + "' does not exist or is not a directory")
            .getLanguageCompilerConfigurationFail((cause) -> "Getting language compiler configuration failed")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
