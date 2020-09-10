package mb.spoofax.compiler.spoofax2.language;

import mb.spoofax.compiler.language.LanguageProjectBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

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

    public Spoofax2LanguageProjectCompiler.Input buildAndSync(LanguageProjectBuilder languageProjectBuilder) {
        final Spoofax2ParserLanguageCompiler.@Nullable Input parser = buildParser(languageProjectBuilder);
        if(parser != null) languageProject.parser(parser);
        final Spoofax2StylerLanguageCompiler.@Nullable Input styler = buildStyler(languageProjectBuilder);
        if(styler != null) languageProject.styler(styler);
        final Spoofax2ConstraintAnalyzerLanguageCompiler.@Nullable Input constraintAnalyzer = buildConstraintAnalyzer(languageProjectBuilder);
        if(constraintAnalyzer != null) languageProject.constraintAnalyzer(constraintAnalyzer);
        final Spoofax2MultilangAnalyzerLanguageCompiler.@Nullable Input multilangAnalyzer = buildMultilangAnalyzer(languageProjectBuilder);
        if(multilangAnalyzer != null) languageProject.multilangAnalyzer(multilangAnalyzer);
        final Spoofax2StrategoRuntimeLanguageCompiler.@Nullable Input strategoRuntime = buildStrategoRuntime(languageProjectBuilder);
        if(strategoRuntime != null) languageProject.strategoRuntime(strategoRuntime);
        return languageProject.build();
    }

    private Spoofax2ParserLanguageCompiler.@Nullable Input buildParser(LanguageProjectBuilder languageProjectBuilder) {
        if(languageProjectBuilder.parser == null) return null;
        final Spoofax2ParserLanguageCompiler.Input.Builder builder;
        if(this.parser != null) {
            builder = this.parser;
        } else {
            builder = Spoofax2ParserLanguageCompiler.Input.builder();
        }
        final Spoofax2ParserLanguageCompiler.Input input = builder.build();
        input.syncTo(languageProjectBuilder.parser);
        return input;
    }

    private Spoofax2StylerLanguageCompiler.@Nullable Input buildStyler(LanguageProjectBuilder languageProjectBuilder) {
        if(languageProjectBuilder.styler == null) return null;
        final Spoofax2StylerLanguageCompiler.Input.Builder builder;
        if(this.styler != null) {
            builder = this.styler;
        } else {
            builder = Spoofax2StylerLanguageCompiler.Input.builder();
        }
        final Spoofax2StylerLanguageCompiler.Input input = builder.build();
        input.syncTo(languageProjectBuilder.styler);
        return input;
    }

    private Spoofax2ConstraintAnalyzerLanguageCompiler.@Nullable Input buildConstraintAnalyzer(LanguageProjectBuilder languageProjectBuilder) {
        if(languageProjectBuilder.constraintAnalyzer == null) return null;
        final Spoofax2ConstraintAnalyzerLanguageCompiler.Input.Builder builder;
        if(this.constraintAnalyzer != null) {
            builder = this.constraintAnalyzer;
        } else {
            builder = Spoofax2ConstraintAnalyzerLanguageCompiler.Input.builder();
        }
        final Spoofax2ConstraintAnalyzerLanguageCompiler.Input input = builder.build();
        input.syncTo(languageProjectBuilder.constraintAnalyzer);
        return input;
    }

    private Spoofax2MultilangAnalyzerLanguageCompiler.@Nullable Input buildMultilangAnalyzer(LanguageProjectBuilder languageProjectBuilder) {
        if(languageProjectBuilder.multilangAnalyzer == null) return null;
        final Spoofax2MultilangAnalyzerLanguageCompiler.Input.Builder builder;
        if(this.multilangAnalyzer != null) {
            builder = this.multilangAnalyzer;
        } else {
            builder = Spoofax2MultilangAnalyzerLanguageCompiler.Input.builder();
        }
        final Spoofax2MultilangAnalyzerLanguageCompiler.Input input = builder.build();
        input.syncTo(languageProjectBuilder.multilangAnalyzer);
        return input;
    }

    private Spoofax2StrategoRuntimeLanguageCompiler.@Nullable Input buildStrategoRuntime(LanguageProjectBuilder languageProjectBuilder) {
        if(languageProjectBuilder.strategoRuntime == null) return null;
        final Spoofax2StrategoRuntimeLanguageCompiler.Input.Builder builder;
        if(this.strategoRuntime != null) {
            builder = this.strategoRuntime;
        } else {
            builder = Spoofax2StrategoRuntimeLanguageCompiler.Input.builder();
        }
        final Spoofax2StrategoRuntimeLanguageCompiler.Input input = builder.build();
        input.syncTo(languageProjectBuilder.strategoRuntime);
        return input;
    }
}
