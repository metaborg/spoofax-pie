package mb.spoofax.compiler.spoofax3.language;

import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Optional;

@Value.Enclosing
public class Spoofax3LanguageProjectCompiler implements TaskDef<Spoofax3LanguageProjectCompiler.Input, None> {
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

    @Override public None exec(ExecContext context, Input input) throws Exception {
        input.parser().ifPresent(i -> context.require(parserCompiler, i));
        input.strategoRuntime().ifPresent(i -> context.require(strategoRuntimeCompiler, i));
        return None.instance;
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
