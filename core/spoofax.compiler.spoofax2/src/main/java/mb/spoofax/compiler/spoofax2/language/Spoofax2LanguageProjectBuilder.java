package mb.spoofax.compiler.spoofax2.language;

import mb.spoofax.compiler.language.LanguageProjectBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

/**
 * Facade for consistently building a {@link Spoofax2LanguageProjectCompiler.Input} instance, and synchronizing that
 * input with a {@link LanguageProjectBuilder}.
 */
public class Spoofax2LanguageProjectBuilder {
    public Spoofax2ParserLanguageCompiler.Input.@Nullable Builder parser = null;
    public Spoofax2StylerLanguageCompiler.Input.@Nullable Builder styler = null;
    public Spoofax2ConstraintAnalyzerLanguageCompiler.Input.@Nullable Builder constraintAnalyzer = null;
    public Spoofax2MultilangAnalyzerLanguageCompiler.Input.@Nullable Builder multilangAnalyzer = null;
    public Spoofax2StrategoRuntimeLanguageCompiler.Input.@Nullable Builder strategoRuntime = null;
    public Spoofax2LanguageProjectCompiler.Input.Builder languageProject = Spoofax2LanguageProjectCompiler.Input.builder();


    public void withParser() {
        parser = Spoofax2ParserLanguageCompiler.Input.builder();
    }

    public void withParser(Function<Spoofax2ParserLanguageCompiler.Input.Builder, Spoofax2ParserLanguageCompiler.Input.Builder> f) {
        parser = f.apply(Spoofax2ParserLanguageCompiler.Input.builder());
    }


    public void withStyler() {
        styler = Spoofax2StylerLanguageCompiler.Input.builder();
    }

    public void withStyler(Function<Spoofax2StylerLanguageCompiler.Input.Builder, Spoofax2StylerLanguageCompiler.Input.Builder> f) {
        styler = f.apply(Spoofax2StylerLanguageCompiler.Input.builder());
    }


    public void withConstraintAnalyzer() {
        constraintAnalyzer = Spoofax2ConstraintAnalyzerLanguageCompiler.Input.builder();
    }

    public void withConstraintAnalyzer(Function<Spoofax2ConstraintAnalyzerLanguageCompiler.Input.Builder, Spoofax2ConstraintAnalyzerLanguageCompiler.Input.Builder> f) {
        constraintAnalyzer = f.apply(Spoofax2ConstraintAnalyzerLanguageCompiler.Input.builder());
    }


    public void withMultilangAnalyzer() {
        multilangAnalyzer = Spoofax2MultilangAnalyzerLanguageCompiler.Input.builder();
    }

    public void withMultilangAnalyzer(Function<Spoofax2MultilangAnalyzerLanguageCompiler.Input.Builder, Spoofax2MultilangAnalyzerLanguageCompiler.Input.Builder> f) {
        multilangAnalyzer = f.apply(Spoofax2MultilangAnalyzerLanguageCompiler.Input.builder());
    }


    public void withStrategoRuntime() {
        strategoRuntime = Spoofax2StrategoRuntimeLanguageCompiler.Input.builder();
    }

    public void withStrategoRuntime(Function<Spoofax2StrategoRuntimeLanguageCompiler.Input.Builder, Spoofax2StrategoRuntimeLanguageCompiler.Input.Builder> f) {
        strategoRuntime = f.apply(Spoofax2StrategoRuntimeLanguageCompiler.Input.builder());
    }



    public Spoofax2LanguageProjectCompiler.Input build() {
        final Spoofax2ParserLanguageCompiler.@Nullable Input parser = buildParser();
        if(parser != null) languageProject.parser(parser);
        final Spoofax2StylerLanguageCompiler.@Nullable Input styler = buildStyler();
        if(styler != null) languageProject.styler(styler);
        final Spoofax2ConstraintAnalyzerLanguageCompiler.@Nullable Input constraintAnalyzer = buildConstraintAnalyzer();
        if(constraintAnalyzer != null) languageProject.constraintAnalyzer(constraintAnalyzer);
        final Spoofax2MultilangAnalyzerLanguageCompiler.@Nullable Input multilangAnalyzer = buildMultilangAnalyzer();
        if(multilangAnalyzer != null) languageProject.multilangAnalyzer(multilangAnalyzer);
        final Spoofax2StrategoRuntimeLanguageCompiler.@Nullable Input strategoRuntime = buildStrategoRuntime();
        if(strategoRuntime != null) languageProject.strategoRuntime(strategoRuntime);
        return languageProject.build();
    }

    private Spoofax2ParserLanguageCompiler.@Nullable Input buildParser() {
        if(parser != null) return parser.build();
        return null;
    }

    private Spoofax2StylerLanguageCompiler.@Nullable Input buildStyler() {
        if(styler != null) return styler.build();
        return null;
    }

    private Spoofax2ConstraintAnalyzerLanguageCompiler.@Nullable Input buildConstraintAnalyzer() {
        if(constraintAnalyzer != null) return constraintAnalyzer.build();
        return null;
    }

    private Spoofax2MultilangAnalyzerLanguageCompiler.@Nullable Input buildMultilangAnalyzer() {
        if(multilangAnalyzer != null) return multilangAnalyzer.build();
        return null;
    }

    private Spoofax2StrategoRuntimeLanguageCompiler.@Nullable Input buildStrategoRuntime() {
        if(strategoRuntime != null) return strategoRuntime.build();
        return null;
    }
}
