package mb.spoofax.lwb.compiler;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.result.Result;
import mb.common.util.ADT;
import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.spoofax.lwb.compiler.metalang.CompileEsv;
import mb.spoofax.lwb.compiler.metalang.CompileSdf3;
import mb.spoofax.lwb.compiler.metalang.CompileStatix;
import mb.spoofax.lwb.compiler.metalang.CompileStratego;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageInput;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageShared;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Compiles a {@link CompileLanguageShared} by running the meta-language compilers.
 *
 * Takes an {@link CompileLanguageInput} describing the language specification and inputs for the
 * meta-language compilers. Meta-language compiler inputs are optional where absence causes the meta-language compiler
 * to not be executed.
 *
 * Produces a {@link Result} that is either an output with all {@link KeyedMessages messages} produced by the
 * meta-language compilers, or a {@link CompileException} when compilation fails.
 */
@Value.Enclosing
public class CompileLanguage implements TaskDef<CompileLanguageInput, Result<KeyedMessages, CompileLanguage.CompileException>> {
    private final CompileSdf3 parserCompiler;
    private final CompileEsv stylerCompiler;
    private final CompileStatix constraintAnalyzerCompiler;
    private final CompileStratego strategoRuntimeCompiler;

    @Inject public CompileLanguage(
        CompileSdf3 parserCompiler,
        CompileEsv stylerCompiler,
        CompileStatix constraintAnalyzerCompiler,
        CompileStratego strategoRuntimeCompiler
    ) {
        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, CompileException> exec(ExecContext context, CompileLanguageInput input) {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();

        final ArrayList<STask<?>> strategoOriginTask = new ArrayList<>();
        final ArrayList<Supplier<Result<IStrategoTerm, ?>>> esvAdditionalAstSuppliers = new ArrayList<>();

        if(input.sdf3().isPresent()) {
            strategoOriginTask.add(parserCompiler.createSupplier(input.sdf3().get()));
            final Result<CompileSdf3.Output, CompileException> result = context.require(parserCompiler, input.sdf3().get())
                .mapErr(CompileLanguage.CompileException::sdf3CompileFail);
            if(result.isErr()) {
                return result.map(CompileSdf3.Output::messages);
            }
            final CompileSdf3.Output output = result.get();
            messagesBuilder.addMessages(output.messages());
            esvAdditionalAstSuppliers.addAll(output.esvCompletionColorerAstSuppliers());
        }

        if(input.esv().isPresent()) {
            final CompileEsv.Args args = new CompileEsv.Args(input.esv().get(), esvAdditionalAstSuppliers);
            final Result<KeyedMessages, CompileException> result = context.require(stylerCompiler, args)
                .mapErr(CompileLanguage.CompileException::esvCompileFail);
            if(result.isErr()) {
                return result;
            }
            messagesBuilder.addMessages(result.get());
        }

        if(input.statix().isPresent()) {
            final Result<KeyedMessages, CompileException> result = context.require(constraintAnalyzerCompiler, input.statix().get())
                .mapErr(CompileLanguage.CompileException::statixCompileFail);
            if(result.isErr()) {
                return result;
            }
            messagesBuilder.addMessages(result.get());
        }

        if(input.stratego().isPresent()) {
            final Result<KeyedMessages, CompileException> result = context.require(strategoRuntimeCompiler, new CompileStratego.Args(input.stratego().get(), strategoOriginTask))
                .map((n) -> KeyedMessages.of())
                .mapErr(CompileLanguage.CompileException::strategoCompileFail);
            if(result.isErr()) {
                return result;
            }
            messagesBuilder.addMessages(result.get());
        }

        return Result.ofOk(messagesBuilder.build());
    }


    @ADT
    public abstract static class CompileException extends Exception {
        public interface Cases<R> {
            R sdf3CompileFail(CompileSdf3.Sdf3CompileException sdf3CompileException);

            R esvCompileFail(CompileEsv.EsvCompileException esvCompileException);

            R statixCompileFail(CompileStatix.StatixCompileException statixCompileException);

            R strategoCompileFail(CompileStratego.StrategoCompileException strategoCompileException);
        }

        public static CompileException sdf3CompileFail(CompileSdf3.Sdf3CompileException cause) {
            return withCause(CompileExceptions.sdf3CompileFail(cause), cause);
        }

        public static CompileException esvCompileFail(CompileEsv.EsvCompileException cause) {
            return withCause(CompileExceptions.esvCompileFail(cause), cause);
        }

        public static CompileException statixCompileFail(CompileStatix.StatixCompileException cause) {
            return withCause(CompileExceptions.statixCompileFail(cause), cause);
        }

        public static CompileException strategoCompileFail(CompileStratego.StrategoCompileException cause) {
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
                .sdf3CompileFail((cause) -> "Parser compiler failed")
                .esvCompileFail((cause) -> "Styler compiler failed")
                .statixCompileFail((cause) -> "Constraint analyzer compiler failed")
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
