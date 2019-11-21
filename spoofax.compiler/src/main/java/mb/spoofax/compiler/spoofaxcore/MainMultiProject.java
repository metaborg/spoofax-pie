package mb.spoofax.compiler.spoofaxcore;

import mb.resource.ResourceService;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

@Value.Enclosing
public class MainMultiProject {
    private final LanguageProject languageProject;
    private final Parser parser;
    private final Styler styler;
    private final StrategoRuntime strategoRuntime;
    private final ConstraintAnalyzer constraintAnalyzer;

    public MainMultiProject(
        LanguageProject languageProject,
        Parser parser,
        Styler styler,
        StrategoRuntime strategoRuntime,
        ConstraintAnalyzer constraintAnalyzer
    ) {
        this.languageProject = languageProject;
        this.parser = parser;
        this.styler = styler;
        this.strategoRuntime = strategoRuntime;
        this.constraintAnalyzer = constraintAnalyzer;
    }

    public static MainMultiProject fromClassLoaderResources(ResourceService resourceService) {
        final LanguageProject languageProject = LanguageProject.fromClassLoaderResources(resourceService);
        final Parser parser = Parser.fromClassLoaderResources(resourceService);
        final Styler styler = Styler.fromClassLoaderResources(resourceService);
        final StrategoRuntime strategoRuntime = StrategoRuntime.fromClassLoaderResources(resourceService);
        final ConstraintAnalyzer constraintAnalyzer = ConstraintAnalyzer.fromClassLoaderResources(resourceService);
        return new MainMultiProject(languageProject, parser, styler, strategoRuntime, constraintAnalyzer);
    }

    public Output compile(Input input, Charset charset) throws IOException {
        final LanguageProject.Output languageProject = this.languageProject.compile(input.languageProject(), charset);
        final Parser.Output parserOutput = parser.compile(input.parser(), charset);
        final Styler.Output stylerOutput = styler.compile(input.styler(), charset);
        final StrategoRuntime.Output strategoRuntimeBuilderOutput = strategoRuntime.compile(input.strategoRuntimeBuilder(), charset);
        final ConstraintAnalyzer.Output constraintAnalyzerOutput = constraintAnalyzer.compile(input.constraintAnalyzer(), charset);
        return Output.builder()
            .languageProject(languageProject)
            .parser(parserOutput)
            .styler(stylerOutput)
            .strategoRuntimeBuilder(strategoRuntimeBuilderOutput)
            .constraintAnalyzer(constraintAnalyzerOutput)
            .build();
    }


    @Value.Immutable
    public interface Input {
        class Builder extends MainMultiProjectData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        LanguageProject.Input languageProject();

        Parser.Input parser();

        Styler.Input styler();

        StrategoRuntime.Input strategoRuntimeBuilder();

        ConstraintAnalyzer.Input constraintAnalyzer();
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends MainMultiProjectData.Output.Builder {}

        static Builder builder() {
            return new Builder();
        }


        LanguageProject.Output languageProject();

        Parser.Output parser();

        Styler.Output styler();

        StrategoRuntime.Output strategoRuntimeBuilder();

        ConstraintAnalyzer.Output constraintAnalyzer();
    }
}
