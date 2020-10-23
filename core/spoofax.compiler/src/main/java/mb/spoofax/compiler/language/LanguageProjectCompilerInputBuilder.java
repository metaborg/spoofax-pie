package mb.spoofax.compiler.language;

import mb.spoofax.compiler.util.Shared;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Facade for consistently and easily building a {@link LanguageProjectCompiler.Input} instance.
 */
public class LanguageProjectCompilerInputBuilder {
    public final ClassloaderResourcesCompiler.Input.Builder classloaderResources = ClassloaderResourcesCompiler.Input.builder();
    public final ParserLanguageCompiler.Input.Builder parser = ParserLanguageCompiler.Input.builder();
    private boolean parserEnabled = false;
    public final StylerLanguageCompiler.Input.Builder styler = StylerLanguageCompiler.Input.builder();
    private boolean stylerEnabled = false;
    public final ConstraintAnalyzerLanguageCompiler.Input.Builder constraintAnalyzer = ConstraintAnalyzerLanguageCompiler.Input.builder();
    private boolean constraintAnalyzerEnabled = false;
    public final MultilangAnalyzerLanguageCompiler.Input.Builder multilangAnalyzer = MultilangAnalyzerLanguageCompiler.Input.builder();
    private boolean multilangAnalyzerEnabled = false;
    public final StrategoRuntimeLanguageCompiler.Input.Builder strategoRuntime = StrategoRuntimeLanguageCompiler.Input.builder();
    private boolean strategoRuntimeEnabled = false;
    public final CompleterLanguageCompiler.Input.Builder completer = CompleterLanguageCompiler.Input.builder();
    private boolean exportsEnabled = false;
    public final ExportsLanguageCompiler.Input.Builder exports = ExportsLanguageCompiler.Input.builder();

    private boolean completerEnabled = false;
    public final LanguageProjectCompiler.Input.Builder project = LanguageProjectCompiler.Input.builder();


    public ClassloaderResourcesCompiler.Input.Builder withClassloaderResources() {
        return classloaderResources;
    }

    public ParserLanguageCompiler.Input.Builder withParser() {
        parserEnabled = true;
        return parser;
    }

    public StylerLanguageCompiler.Input.Builder withStyler() {
        stylerEnabled = true;
        return styler;
    }

    public ConstraintAnalyzerLanguageCompiler.Input.Builder withConstraintAnalyzer() {
        constraintAnalyzerEnabled = true;
        return constraintAnalyzer;
    }

    public MultilangAnalyzerLanguageCompiler.Input.Builder withMultilangAnalyzer() {
        multilangAnalyzerEnabled = true;
        return multilangAnalyzer;
    }

    public StrategoRuntimeLanguageCompiler.Input.Builder withStrategoRuntime() {
        strategoRuntimeEnabled = true;
        return strategoRuntime;
    }

    public CompleterLanguageCompiler.Input.Builder withCompleter() {
        completerEnabled = true;
        return completer;
    }

    public ExportsLanguageCompiler.Input.Builder withExports() {
        exportsEnabled = true;
        return exports;
    }


    public LanguageProjectCompiler.Input build(Shared shared, LanguageProject languageProject) {
        final ClassloaderResourcesCompiler.Input classloaderResources = buildClassLoaderResources(shared, languageProject);
        project.classloaderResources(classloaderResources);

        final ParserLanguageCompiler.@Nullable Input parser = buildParser(shared, languageProject);
        if(parser != null) project.parser(parser);

        final StylerLanguageCompiler.@Nullable Input styler = buildStyler(shared, languageProject);
        if(styler != null) project.styler(styler);

        final ConstraintAnalyzerLanguageCompiler.@Nullable Input constraintAnalyzer = buildConstraintAnalyzer(shared, languageProject);
        if(constraintAnalyzer != null) project.constraintAnalyzer(constraintAnalyzer);

        final MultilangAnalyzerLanguageCompiler.@Nullable Input multilangAnalyzer = buildMultilangAnalyzer(shared, languageProject, classloaderResources);
        if(multilangAnalyzer != null) project.multilangAnalyzer(multilangAnalyzer);

        final StrategoRuntimeLanguageCompiler.@Nullable Input strategoRuntime = buildStrategoRuntime(shared, languageProject, constraintAnalyzer);
        if(strategoRuntime != null) project.strategoRuntime(strategoRuntime);

        final CompleterLanguageCompiler.@Nullable Input completer = buildCompleter(shared, languageProject);
        if(completer != null) project.completer(completer);

        final ExportsLanguageCompiler.@Nullable Input exports = buildExports(shared, languageProject);
        if(exports != null) project.exports(exports);

        return project
            .languageProject(languageProject)
            .shared(shared)
            .build();
    }

    private ClassloaderResourcesCompiler.Input buildClassLoaderResources(Shared shared, LanguageProject languageProject) {
        return classloaderResources
            .shared(shared)
            .languageProject(languageProject)
            .build();
    }

    private ParserLanguageCompiler.@Nullable Input buildParser(Shared shared, LanguageProject languageProject) {
        if(!parserEnabled) return null;
        return parser
            .shared(shared)
            .languageProject(languageProject)
            .build();
    }

    private StylerLanguageCompiler.@Nullable Input buildStyler(Shared shared, LanguageProject languageProject) {
        if(!stylerEnabled) return null;
        return styler
            .shared(shared)
            .languageProject(languageProject)
            .build();
    }

    private ConstraintAnalyzerLanguageCompiler.@Nullable Input buildConstraintAnalyzer(Shared shared, LanguageProject languageProject) {
        if(!constraintAnalyzerEnabled) return null;
        return constraintAnalyzer
            .shared(shared)
            .languageProject(languageProject)
            .build();
    }

    private MultilangAnalyzerLanguageCompiler.@Nullable Input buildMultilangAnalyzer(Shared shared, LanguageProject languageProject, ClassloaderResourcesCompiler.Input classloaderResourcesInput) {
        if(!multilangAnalyzerEnabled) return null;
        return multilangAnalyzer
            .shared(shared)
            .classloaderResources(classloaderResourcesInput.classloaderResources())
            .languageProject(languageProject)
            .build();
    }

    private StrategoRuntimeLanguageCompiler.@Nullable Input buildStrategoRuntime(
        Shared shared,
        LanguageProject languageProject,
        ConstraintAnalyzerLanguageCompiler.@Nullable Input constraintAnalyzer
    ) {
        if(!strategoRuntimeEnabled) return null;

        // Set required parts.
        strategoRuntime
            .shared(shared)
            .languageProject(languageProject)
        ;

        final StrategoRuntimeLanguageCompiler.Input.Builder builder;
        if(constraintAnalyzer != null) {
            // Copy the builder before syncing to ensure that multiple builds do not cause a sync multiple times.
            builder = StrategoRuntimeLanguageCompiler.Input.builder().from(strategoRuntime.build());
            constraintAnalyzer.syncTo(builder);
        } else {
            builder = strategoRuntime;
        }

        return builder.build();
    }

    private CompleterLanguageCompiler.@Nullable Input buildCompleter(Shared shared, LanguageProject languageProject) {
        if(!completerEnabled) return null;
        return completer
            .shared(shared)
            .languageProject(languageProject)
            .build();
    }

    private ExportsLanguageCompiler.@Nullable Input buildExports(Shared shared, LanguageProject languageProject) {
        if(!exportsEnabled) return null;
        return exports
            .shared(shared)
            .languageProject(languageProject)
            .build();
    }
}
