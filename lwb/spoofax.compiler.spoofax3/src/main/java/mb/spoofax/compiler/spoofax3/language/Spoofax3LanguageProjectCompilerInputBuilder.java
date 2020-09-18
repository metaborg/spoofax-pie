package mb.spoofax.compiler.spoofax3.language;

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


    public Spoofax3LanguageProjectCompiler.Input build() {
        final Spoofax3ParserLanguageCompiler.@Nullable Input parser = buildParser();
        if(parser != null) project.parser(parser);

        final Spoofax3StrategoRuntimeLanguageCompiler.@Nullable Input strategoRuntime = buildStrategoRuntime();
        if(strategoRuntime != null) project.strategoRuntime(strategoRuntime);

        return project.build();
    }


    private Spoofax3ParserLanguageCompiler.@Nullable Input buildParser() {
        if(!parserEnabled) return null;
        return parser.build();
    }

    private Spoofax3StrategoRuntimeLanguageCompiler.@Nullable Input buildStrategoRuntime() {
        if(!strategoRuntimeEnabled) return null;
        return strategoRuntime.build();
    }
}
