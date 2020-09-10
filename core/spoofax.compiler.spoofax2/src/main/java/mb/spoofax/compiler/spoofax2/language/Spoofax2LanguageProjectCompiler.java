package mb.spoofax.compiler.spoofax2.language;

import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.util.GradleDependency;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class Spoofax2LanguageProjectCompiler implements TaskDef<Spoofax2LanguageProjectCompiler.Input, None> {
    private final Spoofax2ParserLanguageCompiler parserCompiler;
    private final Spoofax2StylerLanguageCompiler stylerCompiler;
    private final Spoofax2ConstraintAnalyzerLanguageCompiler constraintAnalyzerCompiler;
    private final Spoofax2MultilangAnalyzerLanguageCompiler multilangAnalyzerCompiler;
    private final Spoofax2StrategoRuntimeLanguageCompiler strategoRuntimeCompiler;


    @Inject public Spoofax2LanguageProjectCompiler(
        Spoofax2ParserLanguageCompiler parserCompiler,
        Spoofax2StylerLanguageCompiler stylerCompiler,
        Spoofax2ConstraintAnalyzerLanguageCompiler constraintAnalyzerCompiler,
        Spoofax2MultilangAnalyzerLanguageCompiler multilangAnalyzerCompiler,
        Spoofax2StrategoRuntimeLanguageCompiler strategoRuntimeCompiler
    ) {
        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
        this.multilangAnalyzerCompiler = multilangAnalyzerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws Exception {
        input.parser().ifPresent((i) -> context.require(parserCompiler, i));
        input.styler().ifPresent((i) -> context.require(stylerCompiler, i));
        input.constraintAnalyzer().ifPresent((i) -> context.require(constraintAnalyzerCompiler, i));
        input.multilangAnalyzer().ifPresent((i) -> context.require(multilangAnalyzerCompiler, i));
        input.strategoRuntime().ifPresent((i) -> context.require(strategoRuntimeCompiler, i));
        return None.instance;
    }


    public HashSet<String> getCopyResources(Input input) {
        final HashSet<String> copyResources = new HashSet<>(input.additionalCopyResources());
        input.parser().ifPresent((i) -> parserCompiler.getCopyResources(i).addAllTo(copyResources));
        input.styler().ifPresent((i) -> stylerCompiler.getCopyResources(i).addAllTo(copyResources));
        input.constraintAnalyzer().ifPresent((i) -> constraintAnalyzerCompiler.getCopyResources(i).addAllTo(copyResources));
        input.multilangAnalyzer().ifPresent((i) -> multilangAnalyzerCompiler.getCopyResources(i).addAllTo(copyResources));
        input.strategoRuntime().ifPresent((i) -> strategoRuntimeCompiler.getCopyResources(i).addAllTo(copyResources));
        return copyResources;
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends Spoofax2LanguageProjectCompilerData.Input.Builder {}

        static Builder builder() { return new Input.Builder(); }


        /// Sub-inputs

        Optional<Spoofax2ParserLanguageCompiler.Input> parser();

        Optional<Spoofax2StylerLanguageCompiler.Input> styler();

        Optional<Spoofax2ConstraintAnalyzerLanguageCompiler.Input> constraintAnalyzer();

        Optional<Spoofax2MultilangAnalyzerLanguageCompiler.Input> multilangAnalyzer();

        Optional<Spoofax2StrategoRuntimeLanguageCompiler.Input> strategoRuntime();


        /// Configuration

        GradleDependency languageSpecificationDependency();

        List<String> additionalCopyResources();
    }
}
