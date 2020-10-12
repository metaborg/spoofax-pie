package mb.spoofax.compiler.spoofax3.language;

import mb.common.util.Properties;
import mb.spoofax.compiler.util.Shared;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Facade for consistently building a {@link Spoofax3LanguageProjectCompiler.Input} instance.
 */
public class Spoofax3LanguageProjectCompilerInputBuilder {
    public Spoofax3ParserLanguageCompiler.Input.Builder parser = Spoofax3ParserLanguageCompiler.Input.builder();
    private boolean parserEnabled = false;
    public Spoofax3StrategoRuntimeLanguageCompiler.Input.Builder strategoRuntime = Spoofax3StrategoRuntimeLanguageCompiler.Input.builder();
    private boolean strategoRuntimeEnabled = false;
    public Spoofax3LanguageProjectCompiler.Input.Builder project = Spoofax3LanguageProjectCompiler.Input.builder();


    public Spoofax3ParserLanguageCompiler.Input.Builder withParser() {
        parserEnabled = true;
        return parser;
    }

    public Spoofax3StrategoRuntimeLanguageCompiler.Input.Builder withStrategoRuntime() {
        strategoRuntimeEnabled = true;
        return strategoRuntime;
    }


    public Spoofax3LanguageProjectCompiler.Input build(Properties persistentProperties, Shared shared, Spoofax3LanguageProject languageProject) {
        final Spoofax3ParserLanguageCompiler.@Nullable Input parser = buildParser(persistentProperties, shared, languageProject);
        if(parser != null) project.parser(parser);

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

    private Spoofax3StrategoRuntimeLanguageCompiler.@Nullable Input buildStrategoRuntime(
        Spoofax3LanguageProject languageProject,
        Spoofax3ParserLanguageCompiler.@Nullable Input parserInput
    ) {
        if(!strategoRuntimeEnabled) return null;
        if(parserInput != null) {
            parserInput.syncTo(strategoRuntime);
        }
        return strategoRuntime
            .spoofax3LanguageProject(languageProject)
            .build();
    }
}
