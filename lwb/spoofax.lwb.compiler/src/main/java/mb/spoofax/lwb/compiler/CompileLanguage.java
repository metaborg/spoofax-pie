package mb.spoofax.lwb.compiler;

import mb.cfg.CompileLanguageInput;
import mb.cfg.CompileLanguageShared;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.result.Result;
import mb.common.util.ADT;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.esv.CompileEsv;
import mb.spoofax.lwb.compiler.esv.EsvCompileException;
import mb.spoofax.lwb.compiler.sdf3.CompileSdf3;
import mb.spoofax.lwb.compiler.sdf3.Sdf3CompileException;
import mb.spoofax.lwb.compiler.statix.CompileStatix;
import mb.spoofax.lwb.compiler.statix.StatixCompileException;
import mb.spoofax.lwb.compiler.stratego.CompileStratego;
import mb.spoofax.lwb.compiler.stratego.StrategoCompileException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import javax.inject.Inject;

/**
 * Compiles a {@link CompileLanguageShared} by running the meta-language compilers.
 *
 * Takes an {@link CompileLanguageInput} describing the language specification and inputs for the meta-language
 * compilers. Meta-language compiler inputs are optional where absence causes the meta-language compiler to not be
 * executed.
 *
 * Produces a {@link Result} that is either an output with all {@link KeyedMessages messages} produced by the
 * meta-language compilers, or a {@link CompileException} when compilation fails.
 */
@Value.Enclosing
public class CompileLanguage implements TaskDef<CompileLanguageInput, Result<KeyedMessages, CompileLanguage.CompileException>> {
    private final CompileSdf3 compileSdf3;
    private final CompileEsv compileEsv;
    private final CompileStatix compileStatix;
    private final CompileStratego compileStratego;

    @Inject public CompileLanguage(
        CompileSdf3 compileSdf3,
        CompileEsv compileEsv,
        CompileStatix compileStatix,
        CompileStratego compileStratego
    ) {
        this.compileSdf3 = compileSdf3;
        this.compileEsv = compileEsv;
        this.compileStatix = compileStatix;
        this.compileStratego = compileStratego;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, CompileException> exec(ExecContext context, CompileLanguageInput input) {
        final ResourcePath rootDirectory = input.compileLanguageShared().languageProject().project().baseDirectory(); // HACK: get root directory from config for now.
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();

        final Result<KeyedMessages, CompileException> compileSdf3Result = context.require(compileSdf3, rootDirectory)
            .ifOk(messagesBuilder::addMessages)
            .mapErr(CompileLanguage.CompileException::sdf3CompileFail);
        if(compileSdf3Result.isErr()) {
            return compileSdf3Result;
        }

        final Result<KeyedMessages, CompileException> compileEsvResult = context.require(compileEsv, rootDirectory)
            .ifOk(messagesBuilder::addMessages)
            .mapErr(CompileLanguage.CompileException::esvCompileFail);
        if(compileEsvResult.isErr()) {
            return compileEsvResult;
        }

        final Result<KeyedMessages, CompileException> compileStatixResult = context.require(compileStatix, rootDirectory)
            .ifOk(messagesBuilder::addMessages)
            .mapErr(CompileLanguage.CompileException::statixCompileFail);
        if(compileStatixResult.isErr()) {
            return compileStatixResult;
        }

        final Result<KeyedMessages, CompileException> compileStrategoResult = context.require(compileStratego, rootDirectory)
            .ifOk(messagesBuilder::addMessages)
            .mapErr(CompileLanguage.CompileException::strategoCompileFail);
        if(compileStrategoResult.isErr()) {
            return compileStrategoResult;
        }

        return Result.ofOk(messagesBuilder.build());
    }


    @ADT
    public abstract static class CompileException extends Exception {
        public interface Cases<R> {
            R sdf3CompileFail(Sdf3CompileException sdf3CompileException);

            R esvCompileFail(EsvCompileException esvCompileException);

            R statixCompileFail(StatixCompileException statixCompileException);

            R strategoCompileFail(StrategoCompileException strategoCompileException);
        }

        public static CompileException sdf3CompileFail(Sdf3CompileException cause) {
            return withCause(CompileExceptions.sdf3CompileFail(cause), cause);
        }

        public static CompileException esvCompileFail(EsvCompileException cause) {
            return withCause(CompileExceptions.esvCompileFail(cause), cause);
        }

        public static CompileException statixCompileFail(StatixCompileException cause) {
            return withCause(CompileExceptions.statixCompileFail(cause), cause);
        }

        public static CompileException strategoCompileFail(StrategoCompileException cause) {
            return withCause(CompileExceptions.strategoCompileFail(cause), cause);
        }

        private static CompileException withCause(CompileException e, Exception cause) {
            e.initCause(cause);
            return e;
        }


        public abstract <R> R match(Cases<R> cases);

        public static CompileExceptions.CasesMatchers.TotalMatcher_Sdf3CompileFail cases() {
            return CompileExceptions.cases();
        }

        public CompileExceptions.CaseOfMatchers.TotalMatcher_Sdf3CompileFail caseOf() {
            return CompileExceptions.caseOf(this);
        }


        @Override public @NonNull String getMessage() {
            return cases()
                .sdf3CompileFail((cause) -> "SDF3 compiler failed")
                .esvCompileFail((cause) -> "ESV compiler failed")
                .statixCompileFail((cause) -> "Statix compiler failed")
                .strategoCompileFail((cause) -> "Stratego compiler failed")
                .apply(this);
        }

        @Override public Throwable fillInStackTrace() {
            return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
        }


        @Override public abstract int hashCode();

        @Override public abstract boolean equals(@Nullable Object obj);
    }
}
