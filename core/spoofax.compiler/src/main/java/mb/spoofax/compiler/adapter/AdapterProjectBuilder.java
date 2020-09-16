package mb.spoofax.compiler.adapter;

import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.util.Shared;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

/**
 * Facade for consistently and easily building a {@link AdapterProjectCompiler.Input} instance.
 */
public class AdapterProjectBuilder {
    public AdapterProject.Builder project = AdapterProject.builder();
    public ParserAdapterCompiler.Input.@Nullable Builder parser = null; // Optional
    public StylerAdapterCompiler.Input.@Nullable Builder styler = null; // Optional
    public ConstraintAnalyzerAdapterCompiler.Input.@Nullable Builder constraintAnalyzer = null; // Optional
    public MultilangAnalyzerAdapterCompiler.Input.@Nullable Builder multilangAnalyzer = null; // Optional
    public StrategoRuntimeAdapterCompiler.Input.@Nullable Builder strategoRuntime = null; // Optional
    public CompleterAdapterCompiler.Input.@Nullable Builder completer = null; // Optional
    public AdapterProjectCompiler.Input.Builder adapterProject = AdapterProjectCompiler.Input.builder();


    public ParserAdapterCompiler.Input.Builder withParser() {
        return withParser(ParserAdapterCompiler.Input.builder());
    }

    public ParserAdapterCompiler.Input.Builder withParser(Consumer<ParserAdapterCompiler.Input.Builder> f) {
        parser = ParserAdapterCompiler.Input.builder();
        f.accept(parser);
        return parser;
    }

    public ParserAdapterCompiler.Input.Builder withParser(ParserAdapterCompiler.Input.Builder builder) {
        parser = builder;
        return builder;
    }

    public ParserAdapterCompiler.Input.Builder configureParser(Consumer<ParserAdapterCompiler.Input.Builder> f) {
        if(parser == null) parser = ParserAdapterCompiler.Input.builder();
        f.accept(parser);
        return parser;
    }


    public StylerAdapterCompiler.Input.Builder withStyler() {
        return withStyler(StylerAdapterCompiler.Input.builder());
    }

    public StylerAdapterCompiler.Input.Builder withStyler(Consumer<StylerAdapterCompiler.Input.Builder> f) {
        styler = StylerAdapterCompiler.Input.builder();
        f.accept(styler);
        return styler;
    }

    public StylerAdapterCompiler.Input.Builder withStyler(StylerAdapterCompiler.Input.Builder builder) {
        styler = builder;
        return builder;
    }

    public StylerAdapterCompiler.Input.Builder configureStyler(Consumer<StylerAdapterCompiler.Input.Builder> f) {
        if(styler == null) styler = StylerAdapterCompiler.Input.builder();
        f.accept(styler);
        return styler;
    }


    public ConstraintAnalyzerAdapterCompiler.Input.Builder withConstraintAnalyzer() {
        return withConstraintAnalyzer(ConstraintAnalyzerAdapterCompiler.Input.builder());
    }

    public ConstraintAnalyzerAdapterCompiler.Input.Builder withConstraintAnalyzer(Consumer<ConstraintAnalyzerAdapterCompiler.Input.Builder> f) {
        constraintAnalyzer = ConstraintAnalyzerAdapterCompiler.Input.builder();
        f.accept(constraintAnalyzer);
        return constraintAnalyzer;
    }

    public ConstraintAnalyzerAdapterCompiler.Input.Builder withConstraintAnalyzer(ConstraintAnalyzerAdapterCompiler.Input.Builder builder) {
        constraintAnalyzer = builder;
        return builder;
    }

    public ConstraintAnalyzerAdapterCompiler.Input.Builder configureConstraintAnalyzer(Consumer<ConstraintAnalyzerAdapterCompiler.Input.Builder> f) {
        if(constraintAnalyzer == null) constraintAnalyzer = ConstraintAnalyzerAdapterCompiler.Input.builder();
        f.accept(constraintAnalyzer);
        return constraintAnalyzer;
    }


    public MultilangAnalyzerAdapterCompiler.Input.Builder withMultilangAnalyzer() {
        return withMultilangAnalyzer(MultilangAnalyzerAdapterCompiler.Input.builder());
    }

    public MultilangAnalyzerAdapterCompiler.Input.Builder withMultilangAnalyzer(Consumer<MultilangAnalyzerAdapterCompiler.Input.Builder> f) {
        multilangAnalyzer = MultilangAnalyzerAdapterCompiler.Input.builder();
        f.accept(multilangAnalyzer);
        return multilangAnalyzer;
    }

    public MultilangAnalyzerAdapterCompiler.Input.Builder withMultilangAnalyzer(MultilangAnalyzerAdapterCompiler.Input.Builder builder) {
        multilangAnalyzer = builder;
        return builder;
    }

    public MultilangAnalyzerAdapterCompiler.Input.Builder configureMultilangAnalyzer(Consumer<MultilangAnalyzerAdapterCompiler.Input.Builder> f) {
        if(multilangAnalyzer == null) multilangAnalyzer = MultilangAnalyzerAdapterCompiler.Input.builder();
        f.accept(multilangAnalyzer);
        return multilangAnalyzer;
    }


    public StrategoRuntimeAdapterCompiler.Input.Builder withStrategoRuntime() {
        return withStrategoRuntime(StrategoRuntimeAdapterCompiler.Input.builder());
    }

    public StrategoRuntimeAdapterCompiler.Input.Builder withStrategoRuntime(Consumer<StrategoRuntimeAdapterCompiler.Input.Builder> f) {
        strategoRuntime = StrategoRuntimeAdapterCompiler.Input.builder();
        f.accept(strategoRuntime);
        return strategoRuntime;
    }

    public StrategoRuntimeAdapterCompiler.Input.Builder withStrategoRuntime(StrategoRuntimeAdapterCompiler.Input.Builder builder) {
        strategoRuntime = builder;
        return builder;
    }

    public StrategoRuntimeAdapterCompiler.Input.Builder configureStrategoRuntime(Consumer<StrategoRuntimeAdapterCompiler.Input.Builder> f) {
        if(strategoRuntime == null) strategoRuntime = StrategoRuntimeAdapterCompiler.Input.builder();
        f.accept(strategoRuntime);
        return strategoRuntime;
    }


    public CompleterAdapterCompiler.Input.Builder withCompleter() {
        return withCompleter(CompleterAdapterCompiler.Input.builder());
    }

    public CompleterAdapterCompiler.Input.Builder withCompleter(Consumer<CompleterAdapterCompiler.Input.Builder> f) {
        completer = CompleterAdapterCompiler.Input.builder();
        f.accept(completer);
        return completer;
    }

    public CompleterAdapterCompiler.Input.Builder withCompleter(CompleterAdapterCompiler.Input.Builder builder) {
        completer = builder;
        return builder;
    }

    public CompleterAdapterCompiler.Input.Builder configureCompleter(Consumer<CompleterAdapterCompiler.Input.Builder> f) {
        if(completer == null) completer = CompleterAdapterCompiler.Input.builder();
        f.accept(completer);
        return completer;
    }


    public AdapterProjectCompiler.Input build(LanguageProjectCompiler.Input languageProjectInput) {
        final Shared shared = languageProjectInput.shared();
        final AdapterProject project = this.project.build();
        adapterProject
            .adapterProject(project)
            .shared(shared);
        adapterProject.classloaderResources(languageProjectInput.classloaderResources());
        final ParserAdapterCompiler.@Nullable Input parser = buildParser(shared, project, languageProjectInput);
        if(parser != null) adapterProject.parser(parser);
        final StylerAdapterCompiler.@Nullable Input styler = buildStyler(shared, project, languageProjectInput);
        if(styler != null) adapterProject.styler(styler);
        final ConstraintAnalyzerAdapterCompiler.@Nullable Input constraintAnalyzer = buildConstraintAnalyzer(shared, project, languageProjectInput);
        if(constraintAnalyzer != null) adapterProject.constraintAnalyzer(constraintAnalyzer);
        final MultilangAnalyzerAdapterCompiler.@Nullable Input multilangAnalyzer = buildMultilangAnalyzer(shared, project, languageProjectInput);
        if(multilangAnalyzer != null) adapterProject.multilangAnalyzer(multilangAnalyzer);
        final StrategoRuntimeAdapterCompiler.@Nullable Input strategoRuntime = buildStrategoRuntime(shared, project, languageProjectInput);
        if(strategoRuntime != null) adapterProject.strategoRuntime(strategoRuntime);
        final CompleterAdapterCompiler.@Nullable Input completer = buildCompleter(shared, project, languageProjectInput);
        if(completer != null) adapterProject.completer(completer);
        adapterProject.languageProjectDependency(languageProjectInput.languageProject().project().asProjectDependency());
        return adapterProject.build();
    }


    private ParserAdapterCompiler.@Nullable Input buildParser(
        Shared shared,
        AdapterProject project,
        LanguageProjectCompiler.Input languageProjectInput
    ) {
        if(parser == null) return null;
        return parser
            .shared(shared)
            .adapterProject(project)
            .languageProjectInput(languageProjectInput.parser().orElseThrow(() -> new RuntimeException("Mismatch between presence of parser input between language project and adapter project")))
            .build();
    }

    private StylerAdapterCompiler.@Nullable Input buildStyler(
        Shared shared,
        AdapterProject project,
        LanguageProjectCompiler.Input languageProjectInput
    ) {
        if(styler == null) return null;
        return styler
            .shared(shared)
            .adapterProject(project)
            .languageProjectInput(languageProjectInput.styler().orElseThrow(() -> new RuntimeException("Mismatch between presence of styler input between language project and adapter project")))
            .build();
    }

    private ConstraintAnalyzerAdapterCompiler.@Nullable Input buildConstraintAnalyzer(
        Shared shared,
        AdapterProject project,
        LanguageProjectCompiler.Input languageProjectInput
    ) {
        if(constraintAnalyzer == null) return null;
        return constraintAnalyzer
            .shared(shared)
            .adapterProject(project)
            .languageProjectInput(languageProjectInput.constraintAnalyzer().orElseThrow(() -> new RuntimeException("Mismatch between presence of constraint analyzer input between language project and adapter project")))
            .build();
    }

    private MultilangAnalyzerAdapterCompiler.@Nullable Input buildMultilangAnalyzer(
        Shared shared,
        AdapterProject project,
        LanguageProjectCompiler.Input languageProjectInput
    ) {
        if(multilangAnalyzer == null) return null;
        return multilangAnalyzer
            .shared(shared)
            .adapterProject(project)
            .languageProjectInput(languageProjectInput.multilangAnalyzer().orElseThrow(() -> new RuntimeException("Mismatch between presence of multi-language analyzer input between language project and adapter project")))
            .build();
    }

    private StrategoRuntimeAdapterCompiler.@Nullable Input buildStrategoRuntime(
        Shared shared,
        AdapterProject project,
        LanguageProjectCompiler.Input languageProjectInput
    ) {
        if(strategoRuntime == null) return null;
        return strategoRuntime
            .languageProjectInput(languageProjectInput.strategoRuntime().orElseThrow(() -> new RuntimeException("Mismatch between presence of stratego runtime input between language project and adapter project")))
            .build();
    }

    private CompleterAdapterCompiler.@Nullable Input buildCompleter(
        Shared shared,
        AdapterProject project,
        LanguageProjectCompiler.Input languageProjectInput
    ) {
        if(completer == null) return null;
        return completer
            .shared(shared)
            .adapterProject(project)
            .languageProjectInput(languageProjectInput.completer().orElseThrow(() -> new RuntimeException("Mismatch between presence of completer input between language project and adapter project")))
            .build();
    }
}
