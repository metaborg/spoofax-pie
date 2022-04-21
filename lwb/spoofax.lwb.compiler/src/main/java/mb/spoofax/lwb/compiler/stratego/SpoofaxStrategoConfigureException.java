package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.definition.ResolveDependenciesException;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3ConfigureException;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Configure exception for Stratego in the context of the Spoofax LWB compiler.
 */
@ADT
public abstract class SpoofaxStrategoConfigureException extends Exception {
    public interface Cases<R> {
        R getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException);

        R mainSourceDirectoryFail(ResourcePath mainSourceDirectory);

        R mainFileFail(ResourceKey mainFile);

        R includeDirectoryFail(ResourcePath includeDirectory);

        R resolveIncludeFail(ResolveDependenciesException resolveDependenciesException);

        R builtinLibraryFail(String builtinLibraryName);

        R sdf3ConfigureFail(SpoofaxSdf3ConfigureException sdf3ConfigureException);

        R sdf3SignatureGenerateFail(Exception cause);

        R sdf3PrettyPrinterGenerateFail(Exception cause);

        R sdf3ParenthesizerGenerateFail(Exception cause);

        R sdf3CompletionRuntimeGenerateFail(Exception cause);

        R sdf3ExtStatixGenInjFail(Exception cause);
    }

    public static SpoofaxStrategoConfigureException getLanguageCompilerConfigurationFail(CfgRootDirectoryToObjectException cfgRootDirectoryToObjectException) {
        return withCause(SpoofaxStrategoConfigureExceptions.getLanguageCompilerConfigurationFail(cfgRootDirectoryToObjectException), cfgRootDirectoryToObjectException);
    }

    public static SpoofaxStrategoConfigureException mainSourceDirectoryFail(ResourcePath sourceDirectory) {
        return SpoofaxStrategoConfigureExceptions.mainSourceDirectoryFail(sourceDirectory);
    }

    public static SpoofaxStrategoConfigureException mainFileFail(ResourceKey mainFile) {
        return SpoofaxStrategoConfigureExceptions.mainFileFail(mainFile);
    }

    public static SpoofaxStrategoConfigureException includeDirectoryFail(ResourcePath includeDirectory) {
        return SpoofaxStrategoConfigureExceptions.includeDirectoryFail(includeDirectory);
    }

    public static SpoofaxStrategoConfigureException resolveIncludeFail(ResolveDependenciesException cause) {
        return withCause(SpoofaxStrategoConfigureExceptions.resolveIncludeFail(cause), cause);
    }

    public static SpoofaxStrategoConfigureException builtinLibraryFail(String builtinLibraryName) {
        return SpoofaxStrategoConfigureExceptions.builtinLibraryFail(builtinLibraryName);
    }

    public static SpoofaxStrategoConfigureException sdf3ConfigureFail(SpoofaxSdf3ConfigureException cause) {
        return withCause(SpoofaxStrategoConfigureExceptions.sdf3ConfigureFail(cause), cause);
    }

    public static SpoofaxStrategoConfigureException sdf3SignatureGenerateFail(Exception cause) {
        return withCause(SpoofaxStrategoConfigureExceptions.sdf3SignatureGenerateFail(cause), cause);
    }

    public static SpoofaxStrategoConfigureException sdf3PrettyPrinterGenerateFail(Exception cause) {
        return withCause(SpoofaxStrategoConfigureExceptions.sdf3PrettyPrinterGenerateFail(cause), cause);
    }

    public static SpoofaxStrategoConfigureException sdf3ParenthesizerGenerateFail(Exception cause) {
        return withCause(SpoofaxStrategoConfigureExceptions.sdf3ParenthesizerGenerateFail(cause), cause);
    }

    public static SpoofaxStrategoConfigureException sdf3CompletionRuntimeGenerateFail(Exception cause) {
        return withCause(SpoofaxStrategoConfigureExceptions.sdf3CompletionRuntimeGenerateFail(cause), cause);
    }

    public static SpoofaxStrategoConfigureException sdf3ExtStatixGenInjFail(Exception cause) {
        return withCause(SpoofaxStrategoConfigureExceptions.sdf3ExtStatixGenInjFail(cause), cause);
    }

    private static SpoofaxStrategoConfigureException withCause(SpoofaxStrategoConfigureException e, Exception cause) {
        e.initCause(cause);
        return e;
    }


    public abstract <R> R match(Cases<R> cases);

    public static SpoofaxStrategoConfigureExceptions.CasesMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail cases() {
        return SpoofaxStrategoConfigureExceptions.cases();
    }

    public SpoofaxStrategoConfigureExceptions.CaseOfMatchers.TotalMatcher_GetLanguageCompilerConfigurationFail caseOf() {
        return SpoofaxStrategoConfigureExceptions.caseOf(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .getLanguageCompilerConfigurationFail(cause -> "Getting language compiler configuration failed")
            .mainSourceDirectoryFail(mainSourceDirectory -> "Stratego main source directory '" + mainSourceDirectory + "' does not exist or is not a directory")
            .mainFileFail(mainFile -> "Stratego main file '" + mainFile + "' does not exist or is not a file")
            .includeDirectoryFail(includeDirectory -> "Stratego include directory '" + includeDirectory + "' does not exist or is not a directory")
            .resolveIncludeFail(cause -> "Resolving compile-time dependency to Stratego includes failed")
            .builtinLibraryFail(builtinLibraryName -> "Stratego built-in library '" + builtinLibraryName + "' does not exist")
            .sdf3ConfigureFail(cause -> "Configuring SDF3 failed")
            .sdf3SignatureGenerateFail(cause -> "SDF3 to signature generator failed")
            .sdf3PrettyPrinterGenerateFail(cause -> "SDF3 to pretty-printer generator failed")
            .sdf3ParenthesizerGenerateFail(cause -> "SDF3 to parenthesizer generator failed")
            .sdf3CompletionRuntimeGenerateFail(cause -> "SDF3 to completion runtime generator failed")
            .sdf3ExtStatixGenInjFail(cause -> "SDF3 to Statix injection explication/implication generator failed")
            ;
    }

    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);
}
