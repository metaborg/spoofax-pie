package mb.spoofax.compiler.spoofax3.language;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;

@Value.Enclosing
public class Spoofax3LanguageProjectCompiler implements TaskDef<Spoofax3LanguageProjectCompiler.Input, Result<KeyedMessages, CompilerException>> {
    private final Spoofax3ParserLanguageCompiler parserCompiler;
    private final Spoofax3StrategoRuntimeLanguageCompiler strategoRuntimeCompiler;

    @Inject public Spoofax3LanguageProjectCompiler(
        Spoofax3ParserLanguageCompiler parserCompiler,
        Spoofax3StrategoRuntimeLanguageCompiler strategoRuntimeCompiler
    ) {
        this.parserCompiler = parserCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<KeyedMessages, CompilerException> exec(ExecContext context, Input input) throws Exception {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();

        final ArrayList<STask> strategoOriginTask = new ArrayList<>();

        if(input.parser().isPresent()) {
            strategoOriginTask.add(parserCompiler.createSupplier(input.parser().get()));
            final Result<KeyedMessages, CompilerException> result = context.require(parserCompiler, input.parser().get())
                .mapErr((e) -> CompilerException.parserCompilerFail(e));
            if(result.isErr()) {
                return result;
            }
            messagesBuilder.addMessages(result.get());
        }

        if(input.strategoRuntime().isPresent()) {
            final Result<KeyedMessages, CompilerException> result = context.require(strategoRuntimeCompiler, new Spoofax3StrategoRuntimeLanguageCompiler.Args(input.strategoRuntime().get(), strategoOriginTask))
                .map((n) -> KeyedMessages.of())
                .mapErr((e) -> CompilerException.strategoRuntimeCompilerFail(e));
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

        Optional<Spoofax3StrategoRuntimeLanguageCompiler.Input> strategoRuntime();


        default void syncTo(LanguageProjectCompilerInputBuilder builder) {
            parser().ifPresent((i) -> i.syncTo(builder.parser));
            strategoRuntime().ifPresent((i) -> i.syncTo(builder.strategoRuntime));
        }
    }
}
