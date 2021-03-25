package mb.spoofax.lwb.compiler.sdf3;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.sdf3.Sdf3ConfigureExceptions;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class Sdf3ConfigureException extends Exception {
    public interface Cases<R> {
        R mainSourceDirectoryFail(ResourcePath mainSourceDirectory);

        R mainFileFail(ResourceKey mainFile);


        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);
    }

    public static Sdf3ConfigureException mainSourceDirectoryFail(ResourcePath sourceDirectory) {
        return Sdf3ConfigureExceptions.mainSourceDirectoryFail(sourceDirectory);
    }

    public static Sdf3ConfigureException mainFileFail(ResourceKey mainFile) {
        return Sdf3ConfigureExceptions.mainFileFail(mainFile);
    }

    public static Sdf3ConfigureException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(Sdf3ConfigureExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    private static Sdf3ConfigureException withCause(Sdf3ConfigureException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static Sdf3ConfigureExceptions.CasesMatchers.TotalMatcher_MainSourceDirectoryFail cases() {
        return Sdf3ConfigureExceptions.cases();
    }

    public Sdf3ConfigureExceptions.CaseOfMatchers.TotalMatcher_MainSourceDirectoryFail caseOf() {
        return Sdf3ConfigureExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .mainSourceDirectoryFail((mainSourceDirectory) -> "SDF3 main source directory '" + mainSourceDirectory + "' does not exist or is not a directory")
            .mainFileFail((mainFile) -> "SDF3 main file '" + mainFile + "' does not exist or is not a file")
            .getLanguageCompilerConfigurationFail((cause) -> "Getting language compiler configuration failed")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
