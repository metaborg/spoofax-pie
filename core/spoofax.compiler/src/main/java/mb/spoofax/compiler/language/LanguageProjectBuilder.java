package mb.spoofax.compiler.language;

import mb.spoofax.compiler.util.Shared;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Facade for consistently and easily building a {@link LanguageProjectCompiler.Input} instance.
 */
public class LanguageProjectBuilder {
    public LanguageProject.Builder project = LanguageProject.builder();
    public ClassloaderResourcesCompiler.Input.Builder classloaderResources = ClassloaderResourcesCompiler.Input.builder();
    public ParserLanguageCompiler.Input.@Nullable Builder parser = null; // Optional
    public StylerLanguageCompiler.Input.@Nullable Builder styler = null; // Optional
    public CompleterLanguageCompiler.Input.@Nullable Builder completer = null; // Optional
    public ConstraintAnalyzerLanguageCompiler.Input.@Nullable Builder constraintAnalyzer = null; // Optional
    public MultilangAnalyzerLanguageCompiler.Input.@Nullable Builder multilangAnalyzer = null; // Optional
    public StrategoRuntimeLanguageCompiler.Input.@Nullable Builder strategoRuntime = null; // Optional
    public LanguageProjectCompiler.Input.Builder languageProject = LanguageProjectCompiler.Input.builder();

    public LanguageProjectCompiler.Input build(Shared shared) {
        final LanguageProject project = this.project.build();
        languageProject
            .languageProject(project)
            .shared(shared);
        final ClassloaderResourcesCompiler.Input classloaderResources = buildClassLoaderResources(shared, project);
        languageProject.classloaderResources(classloaderResources);
        final ParserLanguageCompiler.@Nullable Input parser = buildParser(shared, project);
        if(parser != null) languageProject.parser(parser);
        final StylerLanguageCompiler.@Nullable Input styler = buildStyler(shared, project);
        if(styler != null) languageProject.styler(styler);
        final CompleterLanguageCompiler.@Nullable Input completer = buildCompleter(shared, project);
        if(completer != null) languageProject.completer(completer);
        final ConstraintAnalyzerLanguageCompiler.@Nullable Input constraintAnalyzer = buildConstraintAnalyzer(shared, project);
        if(constraintAnalyzer != null) languageProject.constraintAnalyzer(constraintAnalyzer);
        final MultilangAnalyzerLanguageCompiler.@Nullable Input multilangAnalyzer = buildMultilangAnalyzer(shared, project);
        if(multilangAnalyzer != null) languageProject.multilangAnalyzer(multilangAnalyzer);
        final StrategoRuntimeLanguageCompiler.@Nullable Input strategoRuntime = buildStrategoRuntime(shared, project, constraintAnalyzer);
        if(strategoRuntime != null) languageProject.strategoRuntime(strategoRuntime);
        return languageProject.build();
    }

    private ClassloaderResourcesCompiler.Input buildClassLoaderResources(
        Shared shared,
        LanguageProject project
    ) {
        return classloaderResources
            .shared(shared)
            .languageProject(project)
            .build();
    }

    private ParserLanguageCompiler.@Nullable Input buildParser(
        Shared shared,
        LanguageProject project
    ) {
        if(parser == null) return null;
        return parser
            .shared(shared)
            .languageProject(project)
            .build();
    }

    private StylerLanguageCompiler.@Nullable Input buildStyler(
        Shared shared,
        LanguageProject project
    ) {
        if(styler == null) return null;
        return styler
            .shared(shared)
            .languageProject(project)
            .build();
    }

    private CompleterLanguageCompiler.@Nullable Input buildCompleter(
        Shared shared,
        LanguageProject project
    ) {
        if(completer == null) return null;
        return completer
            .shared(shared)
            .languageProject(project)
            .build();
    }

    private ConstraintAnalyzerLanguageCompiler.@Nullable Input buildConstraintAnalyzer(
        Shared shared,
        LanguageProject project
    ) {
        if(constraintAnalyzer == null) return null;
        return constraintAnalyzer
            .shared(shared)
            .languageProject(project)
            .build();
    }

    private MultilangAnalyzerLanguageCompiler.@Nullable Input buildMultilangAnalyzer(
        Shared shared,
        LanguageProject project
    ) {
        if(multilangAnalyzer == null) return null;
        return multilangAnalyzer
            .shared(shared)
            .languageProject(project)
            .build();
    }

    private StrategoRuntimeLanguageCompiler.@Nullable Input buildStrategoRuntime(
        Shared shared,
        LanguageProject project,
        ConstraintAnalyzerLanguageCompiler.@Nullable Input constraintAnalyzer
    ) {
        if(strategoRuntime == null) return null;
        if(constraintAnalyzer != null) {
            constraintAnalyzer.syncTo(this.strategoRuntime);
        }
        return strategoRuntime
            .shared(shared)
            .languageProject(project)
            .build();
    }
}
