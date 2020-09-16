package mb.spoofax.compiler.language;

import mb.spoofax.compiler.util.Shared;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Facade for consistently and easily building a {@link LanguageProjectCompiler.Input} instance.
 */
public class LanguageProjectBuilder {
    public LanguageProject.Builder project = LanguageProject.builder();
    public ClassloaderResourcesCompiler.Input.Builder classloaderResources = ClassloaderResourcesCompiler.Input.builder();
    public ParserLanguageCompiler.Input.@Nullable Builder parser = null; // Optional
    public StylerLanguageCompiler.Input.@Nullable Builder styler = null; // Optional
    public ConstraintAnalyzerLanguageCompiler.Input.@Nullable Builder constraintAnalyzer = null; // Optional
    public MultilangAnalyzerLanguageCompiler.Input.@Nullable Builder multilangAnalyzer = null; // Optional
    public StrategoRuntimeLanguageCompiler.Input.@Nullable Builder strategoRuntime = null; // Optional
    public CompleterLanguageCompiler.Input.@Nullable Builder completer = null; // Optional
    public LanguageProjectCompiler.Input.Builder languageProject = LanguageProjectCompiler.Input.builder();


    public ParserLanguageCompiler.Input.Builder withParser() {
        return withParser(ParserLanguageCompiler.Input.builder());
    }

    public ParserLanguageCompiler.Input.Builder withParser(Consumer<ParserLanguageCompiler.Input.Builder> f) {
        parser = ParserLanguageCompiler.Input.builder();
        f.accept(parser);
        return parser;
    }

    public ParserLanguageCompiler.Input.Builder withParser(ParserLanguageCompiler.Input.Builder builder) {
        parser = builder;
        return builder;
    }

    public ParserLanguageCompiler.Input.Builder configureParser(Consumer<ParserLanguageCompiler.Input.Builder> f) {
        if(parser == null) parser = ParserLanguageCompiler.Input.builder();
        f.accept(parser);
        return parser;
    }


    public StylerLanguageCompiler.Input.Builder withStyler() {
        return withStyler(StylerLanguageCompiler.Input.builder());
    }

    public StylerLanguageCompiler.Input.Builder withStyler(Consumer<StylerLanguageCompiler.Input.Builder> f) {
        styler = StylerLanguageCompiler.Input.builder();
        f.accept(styler);
        return styler;
    }

    public StylerLanguageCompiler.Input.Builder withStyler(StylerLanguageCompiler.Input.Builder builder) {
        styler = builder;
        return builder;
    }

    public StylerLanguageCompiler.Input.Builder configureStyler(Consumer<StylerLanguageCompiler.Input.Builder> f) {
        if(styler == null) styler = StylerLanguageCompiler.Input.builder();
        f.accept(styler);
        return styler;
    }


    public ConstraintAnalyzerLanguageCompiler.Input.Builder withConstraintAnalyzer() {
        return withConstraintAnalyzer(ConstraintAnalyzerLanguageCompiler.Input.builder());
    }

    public ConstraintAnalyzerLanguageCompiler.Input.Builder withConstraintAnalyzer(Consumer<ConstraintAnalyzerLanguageCompiler.Input.Builder> f) {
        constraintAnalyzer = ConstraintAnalyzerLanguageCompiler.Input.builder();
        f.accept(constraintAnalyzer);
        return constraintAnalyzer;
    }

    public ConstraintAnalyzerLanguageCompiler.Input.Builder withConstraintAnalyzer(ConstraintAnalyzerLanguageCompiler.Input.Builder builder) {
        constraintAnalyzer = builder;
        return builder;
    }

    public ConstraintAnalyzerLanguageCompiler.Input.Builder configureConstraintAnalyzer(Consumer<ConstraintAnalyzerLanguageCompiler.Input.Builder> f) {
        if(constraintAnalyzer == null) constraintAnalyzer = ConstraintAnalyzerLanguageCompiler.Input.builder();
        f.accept(constraintAnalyzer);
        return constraintAnalyzer;
    }


    public MultilangAnalyzerLanguageCompiler.Input.Builder withMultilangAnalyzer() {
        return withMultilangAnalyzer(MultilangAnalyzerLanguageCompiler.Input.builder());
    }

    public MultilangAnalyzerLanguageCompiler.Input.Builder withMultilangAnalyzer(Consumer<MultilangAnalyzerLanguageCompiler.Input.Builder> f) {
        multilangAnalyzer = MultilangAnalyzerLanguageCompiler.Input.builder();
        f.accept(multilangAnalyzer);
        return multilangAnalyzer;
    }

    public MultilangAnalyzerLanguageCompiler.Input.Builder withMultilangAnalyzer(MultilangAnalyzerLanguageCompiler.Input.Builder builder) {
        multilangAnalyzer = builder;
        return builder;
    }

    public MultilangAnalyzerLanguageCompiler.Input.Builder configureMultilangAnalyzer(Consumer<MultilangAnalyzerLanguageCompiler.Input.Builder> f) {
        if(multilangAnalyzer == null) multilangAnalyzer = MultilangAnalyzerLanguageCompiler.Input.builder();
        f.accept(multilangAnalyzer);
        return multilangAnalyzer;
    }


    public StrategoRuntimeLanguageCompiler.Input.Builder withStrategoRuntime() {
        return withStrategoRuntime(StrategoRuntimeLanguageCompiler.Input.builder());
    }

    public StrategoRuntimeLanguageCompiler.Input.Builder withStrategoRuntime(Consumer<StrategoRuntimeLanguageCompiler.Input.Builder> f) {
        strategoRuntime = StrategoRuntimeLanguageCompiler.Input.builder();
        f.accept(strategoRuntime);
        return strategoRuntime;
    }

    public StrategoRuntimeLanguageCompiler.Input.Builder withStrategoRuntime(StrategoRuntimeLanguageCompiler.Input.Builder builder) {
        strategoRuntime = builder;
        return builder;
    }

    public StrategoRuntimeLanguageCompiler.Input.Builder configureStrategoRuntime(Consumer<StrategoRuntimeLanguageCompiler.Input.Builder> f) {
        if(strategoRuntime == null) strategoRuntime = StrategoRuntimeLanguageCompiler.Input.builder();
        f.accept(strategoRuntime);
        return strategoRuntime;
    }


    public CompleterLanguageCompiler.Input.Builder withCompleter() {
        return withCompleter(CompleterLanguageCompiler.Input.builder());
    }

    public CompleterLanguageCompiler.Input.Builder withCompleter(Consumer<CompleterLanguageCompiler.Input.Builder> f) {
        completer = CompleterLanguageCompiler.Input.builder();
        f.accept(completer);
        return completer;
    }

    public CompleterLanguageCompiler.Input.Builder withCompleter(CompleterLanguageCompiler.Input.Builder builder) {
        completer = builder;
        return builder;
    }

    public CompleterLanguageCompiler.Input.Builder configureCompleter(Consumer<CompleterLanguageCompiler.Input.Builder> f) {
        if(completer == null) completer = CompleterLanguageCompiler.Input.builder();
        f.accept(completer);
        return completer;
    }


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
        final ConstraintAnalyzerLanguageCompiler.@Nullable Input constraintAnalyzer = buildConstraintAnalyzer(shared, project);
        if(constraintAnalyzer != null) languageProject.constraintAnalyzer(constraintAnalyzer);
        final MultilangAnalyzerLanguageCompiler.@Nullable Input multilangAnalyzer = buildMultilangAnalyzer(shared, project);
        if(multilangAnalyzer != null) languageProject.multilangAnalyzer(multilangAnalyzer);
        final StrategoRuntimeLanguageCompiler.@Nullable Input strategoRuntime = buildStrategoRuntime(shared, project, constraintAnalyzer);
        if(strategoRuntime != null) languageProject.strategoRuntime(strategoRuntime);
        final CompleterLanguageCompiler.@Nullable Input completer = buildCompleter(shared, project);
        if(completer != null) languageProject.completer(completer);
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
}
