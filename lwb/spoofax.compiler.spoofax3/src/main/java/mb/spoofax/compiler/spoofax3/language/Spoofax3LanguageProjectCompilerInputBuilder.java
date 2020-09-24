package mb.spoofax.compiler.spoofax3.language;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.LanguageProject;
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


    public Spoofax3LanguageProjectCompiler.Input build(LanguageProject languageProject) {
        final String directorySuffix = "spoofax3Language";
        final ResourcePath generatedResourcesDirectory = languageProject.project().buildGeneratedResourcesDirectory().appendRelativePath(directorySuffix);
        final ResourcePath generatedSourcesDirectory = languageProject.project().buildGeneratedSourcesDirectory().appendRelativePath(directorySuffix);
        final ResourcePath generatedJavaSourcesDirectory = generatedSourcesDirectory.appendRelativePath("java");
        final ResourcePath generatedStrategoSourcesDirectory = generatedSourcesDirectory.appendRelativePath("stratego");

        final Spoofax3ParserLanguageCompiler.@Nullable Input parser = buildParser(languageProject, generatedResourcesDirectory, generatedStrategoSourcesDirectory);
        if(parser != null) project.parser(parser);

        final Spoofax3StrategoRuntimeLanguageCompiler.@Nullable Input strategoRuntime = buildStrategoRuntime(languageProject, generatedJavaSourcesDirectory, parser);
        if(strategoRuntime != null) project.strategoRuntime(strategoRuntime);

        return project
            .generatedResourcesDirectory(generatedResourcesDirectory)
            .generatedJavaSourcesDirectory(generatedJavaSourcesDirectory)
            .build();
    }


    private Spoofax3ParserLanguageCompiler.@Nullable Input buildParser(
        LanguageProject languageProject,
        ResourcePath generatedResourcesDirectory,
        ResourcePath generatedStrategoSourcesDirectory
    ) {
        if(!parserEnabled) return null;
        return parser
            .languageProject(languageProject)
            .generatedResourcesDirectory(generatedResourcesDirectory)
            .generatedStrategoSourcesDirectory(generatedStrategoSourcesDirectory)
            .build();
    }

    private Spoofax3StrategoRuntimeLanguageCompiler.@Nullable Input buildStrategoRuntime(
        LanguageProject languageProject,
        ResourcePath generatedJavaSourcesDirectory,
        Spoofax3ParserLanguageCompiler.@Nullable Input parserInput
    ) {
        if(!strategoRuntimeEnabled) return null;
        if(parserInput != null) {
            parserInput.syncTo(strategoRuntime);
        }
        return strategoRuntime
            .languageProject(languageProject)
            .generatedJavaSourcesDirectory(generatedJavaSourcesDirectory)
            .build();
    }
}
