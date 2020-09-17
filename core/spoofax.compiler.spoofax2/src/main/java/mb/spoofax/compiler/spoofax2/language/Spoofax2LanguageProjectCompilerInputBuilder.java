package mb.spoofax.compiler.spoofax2.language;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Facade for consistently building a {@link Spoofax2LanguageProjectCompiler.Input} instance.
 */
public class Spoofax2LanguageProjectCompilerInputBuilder {
    public Spoofax2ParserLanguageCompiler.Input.Builder parser = Spoofax2ParserLanguageCompiler.Input.builder();
    private boolean parserEnabled = false;
    public Spoofax2StylerLanguageCompiler.Input.Builder styler = Spoofax2StylerLanguageCompiler.Input.builder();
    private boolean stylerEnabled = false;
    public Spoofax2ConstraintAnalyzerLanguageCompiler.Input.Builder constraintAnalyzer = Spoofax2ConstraintAnalyzerLanguageCompiler.Input.builder();
    private boolean constraintAnalyzerEnabled = false;
    public Spoofax2MultilangAnalyzerLanguageCompiler.Input.Builder multilangAnalyzer = Spoofax2MultilangAnalyzerLanguageCompiler.Input.builder();
    private boolean multilangAnalyzerEnabled = false;
    public Spoofax2StrategoRuntimeLanguageCompiler.Input.Builder strategoRuntime = Spoofax2StrategoRuntimeLanguageCompiler.Input.builder();
    private boolean strategoRuntimeEnabled = false;
    public Spoofax2LanguageProjectCompiler.Input.Builder project = Spoofax2LanguageProjectCompiler.Input.builder();


    public Spoofax2ParserLanguageCompiler.Input.Builder withParser() {
        parserEnabled = true;
        return parser;
    }

    public Spoofax2StylerLanguageCompiler.Input.Builder withStyler() {
        stylerEnabled = true;
        return styler;
    }

    public Spoofax2ConstraintAnalyzerLanguageCompiler.Input.Builder withConstraintAnalyzer() {
        constraintAnalyzerEnabled = true;
        return constraintAnalyzer;
    }

    public Spoofax2MultilangAnalyzerLanguageCompiler.Input.Builder withMultilangAnalyzer() {
        multilangAnalyzerEnabled = true;
        return multilangAnalyzer;
    }

    public Spoofax2StrategoRuntimeLanguageCompiler.Input.Builder withStrategoRuntime() {
        strategoRuntimeEnabled = true;
        return strategoRuntime;
    }


    public Spoofax2LanguageProjectCompiler.Input build() {
        final Spoofax2ParserLanguageCompiler.@Nullable Input parser = buildParser();
        if(parser != null) project.parser(parser);

        final Spoofax2StylerLanguageCompiler.@Nullable Input styler = buildStyler();
        if(styler != null) project.styler(styler);

        final Spoofax2ConstraintAnalyzerLanguageCompiler.@Nullable Input constraintAnalyzer = buildConstraintAnalyzer();
        if(constraintAnalyzer != null) project.constraintAnalyzer(constraintAnalyzer);

        final Spoofax2MultilangAnalyzerLanguageCompiler.@Nullable Input multilangAnalyzer = buildMultilangAnalyzer();
        if(multilangAnalyzer != null) project.multilangAnalyzer(multilangAnalyzer);

        final Spoofax2StrategoRuntimeLanguageCompiler.@Nullable Input strategoRuntime = buildStrategoRuntime();
        if(strategoRuntime != null) project.strategoRuntime(strategoRuntime);

        return project.build();
    }


    private Spoofax2ParserLanguageCompiler.@Nullable Input buildParser() {
        if(!parserEnabled) return null;
        return parser.build();
    }

    private Spoofax2StylerLanguageCompiler.@Nullable Input buildStyler() {
        if(!stylerEnabled) return null;
        return styler.build();
    }

    private Spoofax2ConstraintAnalyzerLanguageCompiler.@Nullable Input buildConstraintAnalyzer() {
        if(!constraintAnalyzerEnabled) return null;
        return constraintAnalyzer.build();
    }

    private Spoofax2MultilangAnalyzerLanguageCompiler.@Nullable Input buildMultilangAnalyzer() {
        if(!multilangAnalyzerEnabled) return null;
        return multilangAnalyzer.build();
    }

    private Spoofax2StrategoRuntimeLanguageCompiler.@Nullable Input buildStrategoRuntime() {
        if(!strategoRuntimeEnabled) return null;
        return strategoRuntime.build();
    }
}
