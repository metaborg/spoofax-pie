package mb.spoofax.compiler.spoofaxcore;

import mb.resource.ResourceService;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

@Value.Enclosing @ImmutablesStyle
public class AllCompiler {
    private final LanguageProjectCompiler languageProjectCompiler;
    private final ParserCompiler parserCompiler;

    public AllCompiler(LanguageProjectCompiler languageProjectCompiler, ParserCompiler parserCompiler) {
        this.languageProjectCompiler = languageProjectCompiler;
        this.parserCompiler = parserCompiler;
    }

    public static AllCompiler fromClassLoaderResources(ResourceService resourceService) {
        final LanguageProjectCompiler languageProjectCompiler = LanguageProjectCompiler.fromClassLoaderResources(resourceService);
        final ParserCompiler parserCompiler = ParserCompiler.fromClassLoaderResources(resourceService);
        return new AllCompiler(languageProjectCompiler, parserCompiler);
    }

    public Output compile(Input input, Charset charset) throws IOException {
        final LanguageProjectCompiler.Output languageProject = languageProjectCompiler.compile(input.languageProject(), charset);
        final ParserCompiler.Output parser = parserCompiler.compile(input.parser(), charset);
        return Output.builder().languageProject(languageProject).parser(parser).build();
    }


    @Value.Immutable
    public interface Input {
        class Builder extends AllCompilerData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        LanguageProjectCompiler.Input languageProject();

        ParserCompiler.Input parser();
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends AllCompilerData.Output.Builder {}

        static Builder builder() {
            return new Builder();
        }


        LanguageProjectCompiler.Output languageProject();

        ParserCompiler.Output parser();
    }
}
