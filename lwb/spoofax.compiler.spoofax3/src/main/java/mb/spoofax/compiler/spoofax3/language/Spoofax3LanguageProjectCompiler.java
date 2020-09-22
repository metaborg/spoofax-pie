package mb.spoofax.compiler.spoofax3.language;

import mb.common.result.AggregateException;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;

@Value.Enclosing
public class Spoofax3LanguageProjectCompiler implements TaskDef<Spoofax3LanguageProjectCompiler.Input, Result<None, AggregateException>> {
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

    @Override public Result<None, AggregateException> exec(ExecContext context, Input input) throws Exception {
        final ArrayList<Exception> exceptions = new ArrayList<>();
        input.parser().ifPresent(i -> context.require(parserCompiler, i).ifErr(exceptions::add));
        input.strategoRuntime().ifPresent(i -> context.require(strategoRuntimeCompiler, i).ifErr(exceptions::add));
        if(!exceptions.isEmpty()) {
            return Result.ofErr(new AggregateException(new ArrayList<>(), exceptions));
        }
        return Result.ofOk(None.instance);
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends Spoofax3LanguageProjectCompilerData.Input.Builder {}

        static Builder builder() { return new Input.Builder(); }


        /// Sub-inputs

        Optional<Spoofax3ParserLanguageCompiler.Input> parser();

        Optional<Spoofax3StrategoRuntimeLanguageCompiler.Input> strategoRuntime();


        ResourcePath generatedResourcesDirectory();

        ResourcePath generatedJavaSourcesDirectory();


        default void syncTo(LanguageProjectCompilerInputBuilder builder) {
            parser().ifPresent((i) -> i.syncTo(builder.parser));
            strategoRuntime().ifPresent((i) -> i.syncTo(builder.strategoRuntime));
        }
    }
}
