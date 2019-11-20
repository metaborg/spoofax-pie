package mb.spoofax.compiler.spoofaxcore;

import mb.resource.ResourceService;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

@Value.Enclosing
public class AllCompiler {
    private final LanguageProjectCompiler languageProjectCompiler;
    private final ParserCompiler parserCompiler;
    private final StylerCompiler stylerCompiler;
    private final StrategoRuntimeBuilderCompiler strategoRuntimeBuilderCompiler;

    public AllCompiler(LanguageProjectCompiler languageProjectCompiler, ParserCompiler parserCompiler, StylerCompiler stylerCompiler, StrategoRuntimeBuilderCompiler strategoRuntimeBuilderCompiler) {
        this.languageProjectCompiler = languageProjectCompiler;
        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.strategoRuntimeBuilderCompiler = strategoRuntimeBuilderCompiler;
    }

    public static AllCompiler fromClassLoaderResources(ResourceService resourceService) {
        final LanguageProjectCompiler languageProjectCompiler = LanguageProjectCompiler.fromClassLoaderResources(resourceService);
        final ParserCompiler parserCompiler = ParserCompiler.fromClassLoaderResources(resourceService);
        final StylerCompiler stylerCompiler = StylerCompiler.fromClassLoaderResources(resourceService);
        final StrategoRuntimeBuilderCompiler strategoRuntimeBuilderCompiler = StrategoRuntimeBuilderCompiler.fromClassLoaderResources(resourceService);
        return new AllCompiler(languageProjectCompiler, parserCompiler, stylerCompiler, strategoRuntimeBuilderCompiler);
    }

    public Output compile(Input input, Charset charset) throws IOException {
        final LanguageProjectCompiler.Output languageProject = languageProjectCompiler.compile(input.languageProject(), charset);
        final ParserCompiler.Output parserOutput = parserCompiler.compile(input.parser(), charset);
        final StylerCompiler.Output stylerOutput = stylerCompiler.compile(input.styler(), charset);
        final StrategoRuntimeBuilderCompiler.Output strategoRuntimeBuilderOutput = strategoRuntimeBuilderCompiler.compile(input.strategoRuntimeBuilder(), charset);
        return Output.builder()
            .languageProject(languageProject)
            .parser(parserOutput)
            .styler(stylerOutput)
            .strategoRuntimeBuilder(strategoRuntimeBuilderOutput)
            .build();
    }


    @Value.Immutable
    public interface Input {
        class Builder extends AllCompilerData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        LanguageProjectCompiler.Input languageProject();

        ParserCompiler.Input parser();

        StylerCompiler.Input styler();

        StrategoRuntimeBuilderCompiler.Input strategoRuntimeBuilder();
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends AllCompilerData.Output.Builder {}

        static Builder builder() {
            return new Builder();
        }


        LanguageProjectCompiler.Output languageProject();

        ParserCompiler.Output parser();

        StylerCompiler.Output styler();

        StrategoRuntimeBuilderCompiler.Output strategoRuntimeBuilder();
    }
}
