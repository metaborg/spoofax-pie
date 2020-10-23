package mb.spoofax.compiler.spoofax3.language;

import mb.common.util.Properties;
import mb.spoofax.compiler.util.Shared;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Facade for consistently building a {@link Spoofax3LanguageProjectCompiler.Input} instance.
 */
public class Spoofax3LanguageProjectCompilerInputBuilder {
    private boolean parserEnabled = false;
    public Spoofax3ParserLanguageCompiler.Input.Builder parser = Spoofax3ParserLanguageCompiler.Input.builder();

    private boolean stylerEnabled = false;
    public Spoofax3StylerLanguageCompiler.Input.Builder styler = Spoofax3StylerLanguageCompiler.Input.builder();

    private boolean constraintAnalyzerEnabled = false;
    public Spoofax3ConstraintAnalyzerLanguageCompiler.Input.Builder constraintAnalyzer = Spoofax3ConstraintAnalyzerLanguageCompiler.Input.builder();

    private boolean strategoRuntimeEnabled = false;
    public Spoofax3StrategoRuntimeLanguageCompiler.Input.Builder strategoRuntime = Spoofax3StrategoRuntimeLanguageCompiler.Input.builder();

    public Spoofax3LanguageProjectCompiler.Input.Builder project = Spoofax3LanguageProjectCompiler.Input.builder();


    public Spoofax3ParserLanguageCompiler.Input.Builder withParser() {
        parserEnabled = true;
        return parser;
    }

    public Spoofax3StylerLanguageCompiler.Input.Builder withStyler() {
        stylerEnabled = true;
        return styler;
    }

    public Spoofax3ConstraintAnalyzerLanguageCompiler.Input.Builder withConstraintAnalyzer() {
        constraintAnalyzerEnabled = true;
        return constraintAnalyzer;
    }

    public Spoofax3StrategoRuntimeLanguageCompiler.Input.Builder withStrategoRuntime() {
        strategoRuntimeEnabled = true;
        return strategoRuntime;
    }


    public Spoofax3LanguageProjectCompiler.Input build(Properties persistentProperties, Shared shared, Spoofax3LanguageProject languageProject) {
        final Spoofax3ParserLanguageCompiler.@Nullable Input parser = buildParser(persistentProperties, shared, languageProject);
        if(parser != null) project.parser(parser);

        final Spoofax3StylerLanguageCompiler.@Nullable Input styler = buildStyler(languageProject);
        if(styler != null) project.styler(styler);

        final Spoofax3ConstraintAnalyzerLanguageCompiler.@Nullable Input constraintAnalyzer = buildConstraintAnalyzer(languageProject);
        if(constraintAnalyzer != null) project.constraintAnalyzer(constraintAnalyzer);

        final Spoofax3StrategoRuntimeLanguageCompiler.@Nullable Input strategoRuntime = buildStrategoRuntime(languageProject, parser);
        if(strategoRuntime != null) project.strategoRuntime(strategoRuntime);

        return project
            .spoofax3LanguageProject(languageProject)
            .build();
    }


    private Spoofax3ParserLanguageCompiler.@Nullable Input buildParser(
        Properties persistentProperties,
        Shared shared,
        Spoofax3LanguageProject languageProject
    ) {
        if(!parserEnabled) return null;
        return parser
            .withPersistentProperties(persistentProperties)
            .shared(shared)
            .spoofax3LanguageProject(languageProject)
            .build();
    }

    private Spoofax3StylerLanguageCompiler.@Nullable Input buildStyler(
        Spoofax3LanguageProject languageProject
    ) {
        if(!stylerEnabled) return null;
        return styler
            .spoofax3LanguageProject(languageProject)
            .build();
    }

    private Spoofax3ConstraintAnalyzerLanguageCompiler.@Nullable Input buildConstraintAnalyzer(
        Spoofax3LanguageProject languageProject
    ) {
        if(!constraintAnalyzerEnabled) return null;
        return constraintAnalyzer
            .spoofax3LanguageProject(languageProject)
            .build();
    }

    private Spoofax3StrategoRuntimeLanguageCompiler.@Nullable Input buildStrategoRuntime(
        Spoofax3LanguageProject languageProject,
        Spoofax3ParserLanguageCompiler.@Nullable Input parserInput
    ) {
        if(!strategoRuntimeEnabled) return null;

        // Set required parts.
        strategoRuntime
            .spoofax3LanguageProject(languageProject)
        ;

        final Spoofax3StrategoRuntimeLanguageCompiler.Input.Builder builder;
        if(parserInput != null) {
            // Copy the builder before syncing to ensure that multiple builds do not cause a sync multiple times.
            builder = Spoofax3StrategoRuntimeLanguageCompiler.Input.builder().from(strategoRuntime.build());
            parserInput.syncTo(builder);
        } else {
            builder = strategoRuntime;
        }

        return builder.build();
    }
}
