package mb.spoofax.compiler.spoofax3.language;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

@Value.Enclosing
public class Spoofax3LanguageProjectCompiler implements TaskDef<Spoofax3LanguageProjectCompiler.Input, Result<KeyedMessages, CompilerException>> {
    private final Spoofax3ParserLanguageCompiler parserCompiler;
    private final Spoofax3StylerLanguageCompiler stylerCompiler;
    private final Spoofax3ConstraintAnalyzerLanguageCompiler constraintAnalyzerCompiler;
    private final Spoofax3StrategoRuntimeLanguageCompiler strategoRuntimeCompiler;

    @Inject public Spoofax3LanguageProjectCompiler(
        Spoofax3ParserLanguageCompiler parserCompiler,
        Spoofax3StylerLanguageCompiler stylerCompiler,
        Spoofax3ConstraintAnalyzerLanguageCompiler constraintAnalyzerCompiler,
        Spoofax3StrategoRuntimeLanguageCompiler strategoRuntimeCompiler
    ) {
        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<KeyedMessages, CompilerException> exec(ExecContext context, Input input) throws Exception {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();

        final ArrayList<STask<?>> strategoOriginTask = new ArrayList<>();
        final ArrayList<Supplier<Result<IStrategoTerm, ?>>> esvAdditionalAstSuppliers = new ArrayList<>();

        if(input.parser().isPresent()) {
            strategoOriginTask.add(parserCompiler.createSupplier(input.parser().get()));
            final Result<Spoofax3ParserLanguageCompiler.Output, CompilerException> result = context.require(parserCompiler, input.parser().get())
                .mapErr(CompilerException::parserCompilerFail);
            if(result.isErr()) {
                return result.map(Spoofax3ParserLanguageCompiler.Output::messages);
            }
            final Spoofax3ParserLanguageCompiler.Output output = result.get();
            messagesBuilder.addMessages(output.messages());
            esvAdditionalAstSuppliers.addAll(output.esvCompletionColorerAstSuppliers());
        }

        if(input.styler().isPresent()) {
            final Spoofax3StylerLanguageCompiler.Args args = new Spoofax3StylerLanguageCompiler.Args(input.styler().get(), esvAdditionalAstSuppliers);
            final Result<KeyedMessages, CompilerException> result = context.require(stylerCompiler, args)
                .mapErr(CompilerException::stylerCompilerFail);
            if(result.isErr()) {
                return result;
            }
            messagesBuilder.addMessages(result.get());
        }

        if(input.constraintAnalyzer().isPresent()) {
            final Result<KeyedMessages, CompilerException> result = context.require(constraintAnalyzerCompiler, input.constraintAnalyzer().get())
                .mapErr(CompilerException::constraintAnalyzerCompilerFail);
            if(result.isErr()) {
                return result;
            }
            messagesBuilder.addMessages(result.get());
        }

        if(input.strategoRuntime().isPresent()) {
            final Result<KeyedMessages, CompilerException> result = context.require(strategoRuntimeCompiler, new Spoofax3StrategoRuntimeLanguageCompiler.Args(input.strategoRuntime().get(), strategoOriginTask))
                .map((n) -> KeyedMessages.of())
                .mapErr(CompilerException::strategoRuntimeCompilerFail);
            if(result.isErr()) {
                return result;
            }
            messagesBuilder.addMessages(result.get());
        }

        return Result.ofOk(messagesBuilder.build());
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends Spoofax3LanguageProjectCompilerData.Input.Builder {}

        static Builder builder() { return new Input.Builder(); }


        /// Project

        Spoofax3LanguageProject spoofax3LanguageProject();


        /// Sub-inputs

        Optional<Spoofax3ParserLanguageCompiler.Input> parser();

        Optional<Spoofax3StylerLanguageCompiler.Input> styler();

        Optional<Spoofax3ConstraintAnalyzerLanguageCompiler.Input> constraintAnalyzer();

        Optional<Spoofax3StrategoRuntimeLanguageCompiler.Input> strategoRuntime();


        default void savePersistentProperties(Properties properties) {
            parser().ifPresent((i) -> i.savePersistentProperties(properties));
        }


        default void syncTo(LanguageProjectCompilerInputBuilder builder) {
            parser().ifPresent((i) -> i.syncTo(builder.parser));
            styler().ifPresent((i) -> i.syncTo(builder.styler));
            constraintAnalyzer().ifPresent((i) -> i.syncTo(builder.constraintAnalyzer));
            strategoRuntime().ifPresent((i) -> i.syncTo(builder.strategoRuntime));
        }
    }
}
