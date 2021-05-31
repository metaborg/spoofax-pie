package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.sdf3.Sdf3ConfigureException;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class StrategoConfigureException extends Exception {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R mainSourceDirectoryFail(ResourcePath mainSourceDirectory);

        R mainFileFail(ResourceKey mainFile);

        R includeDirectoryFail(ResourcePath includeDirectory);

        R builtinLibraryFail(String builtinLibraryName);

        R sdf3ConfigureFail(Sdf3ConfigureException sdf3ConfigureException);

        R sdf3SignatureGenerateFail(Exception cause);

        R sdf3PrettyPrinterGenerateFail(Exception cause);

        R sdf3ParenthesizerGenerateFail(Exception cause);

        R sdf3CompletionRuntimeGenerateFail(Exception cause);
    }

    public static StrategoConfigureException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(StrategoConfigureExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static StrategoConfigureException mainSourceDirectoryFail(ResourcePath sourceDirectory) {
        return StrategoConfigureExceptions.mainSourceDirectoryFail(sourceDirectory);
    }

    public static StrategoConfigureException mainFileFail(ResourceKey mainFile) {
        return StrategoConfigureExceptions.mainFileFail(mainFile);
    }

    public static StrategoConfigureException includeDirectoryFail(ResourcePath includeDirectory) {
        return StrategoConfigureExceptions.includeDirectoryFail(includeDirectory);
    }

    public static StrategoConfigureException builtinLibraryFail(String builtinLibraryName) {
        return StrategoConfigureExceptions.builtinLibraryFail(builtinLibraryName);
    }

    public static StrategoConfigureException sdf3ConfigureFail(Sdf3ConfigureException sdf3ConfigureException) {
        return withCause(StrategoConfigureExceptions.sdf3ConfigureFail(sdf3ConfigureException), sdf3ConfigureException);
    }

    public static StrategoConfigureException sdf3SignatureGenerateFail(Exception cause) {
        return withCause(StrategoConfigureExceptions.sdf3SignatureGenerateFail(cause), cause);
    }

    public static StrategoConfigureException sdf3PrettyPrinterGenerateFail(Exception cause) {
        return withCause(StrategoConfigureExceptions.sdf3PrettyPrinterGenerateFail(cause), cause);
    }

    public static StrategoConfigureException sdf3ParenthesizerGenerateFail(Exception cause) {
        return withCause(StrategoConfigureExceptions.sdf3ParenthesizerGenerateFail(cause), cause);
    }

    public static StrategoConfigureException sdf3CompletionRuntimeGenerateFail(Exception cause) {
        return withCause(StrategoConfigureExceptions.sdf3CompletionRuntimeGenerateFail(cause), cause);
    }

    private static StrategoConfigureException withCause(StrategoConfigureException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static StrategoConfigureExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return StrategoConfigureExceptions.cases();
    }

    public StrategoConfigureExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return StrategoConfigureExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .getLanguageCompilerConfigurationFail(cause -> "Getting language compiler configuration failed")
            .mainSourceDirectoryFail(mainSourceDirectory -> "Stratego main source directory '" + mainSourceDirectory + "' does not exist or is not a directory")
            .mainFileFail(mainFile -> "Stratego main file '" + mainFile + "' does not exist or is not a file")
            .includeDirectoryFail(includeDirectory -> "Stratego include directory '" + includeDirectory + "' does not exist or is not a directory")
            .builtinLibraryFail(builtinLibraryName -> "Stratego built-in library '" + builtinLibraryName + "' does not exist")
            .sdf3ConfigureFail(cause -> "Configuring SDF3 failed")
            .sdf3SignatureGenerateFail(cause -> "SDF3 to signature generator failed")
            .sdf3PrettyPrinterGenerateFail(cause -> "SDF3 to pretty-printer generator failed")
            .sdf3ParenthesizerGenerateFail(cause -> "SDF3 to parenthesizer generator failed")
            .sdf3CompletionRuntimeGenerateFail(cause -> "SDF3 to completion runtime generator failed")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
