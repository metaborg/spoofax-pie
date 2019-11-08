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

    public AllCompiler(LanguageProjectCompiler languageProjectCompiler, ParserCompiler parserCompiler, StylerCompiler stylerCompiler) {
        this.languageProjectCompiler = languageProjectCompiler;
        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
    }

    public static AllCompiler fromClassLoaderResources(ResourceService resourceService) {
        final LanguageProjectCompiler languageProjectCompiler = LanguageProjectCompiler.fromClassLoaderResources(resourceService);
        final ParserCompiler parserCompiler = ParserCompiler.fromClassLoaderResources(resourceService);
        final StylerCompiler stylerCompiler = StylerCompiler.fromClassLoaderResources(resourceService);
        return new AllCompiler(languageProjectCompiler, parserCompiler, stylerCompiler);
    }

    public Output compile(Input input, Charset charset) throws IOException {
        final LanguageProjectCompiler.Output languageProject = languageProjectCompiler.compile(input.languageProject(), charset);
        final ParserCompiler.Output parser = parserCompiler.compile(input.parser(), charset);
        final StylerCompiler.Output styler = stylerCompiler.compile(input.styler(), charset);
        return Output.builder()
            .languageProject(languageProject)
            .parser(parser)
            .styler(styler)
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
    }
}
