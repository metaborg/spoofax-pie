package mb.spoofax.compiler.adapter;

import mb.common.option.Option;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.Shared;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Facade for consistently and easily building a {@link AdapterProjectCompiler.Input} instance.
 */
public class AdapterProjectCompilerInputBuilder {
    public final ClassLoaderResourcesCompiler.Input.Builder classLoaderResources = ClassLoaderResourcesCompiler.Input.builder();
    public final ExportsCompiler.Input.Builder exports = ExportsCompiler.Input.builder();
    public final GetSourceFilesAdapterCompiler.Input.Builder getSourceFiles = GetSourceFilesAdapterCompiler.Input.builder();

    private boolean parserEnabled = false;
    public final ParserAdapterCompiler.Input.Builder parser = ParserAdapterCompiler.Input.builder();

    private boolean stylerEnabled = false;
    public final StylerAdapterCompiler.Input.Builder styler = StylerAdapterCompiler.Input.builder();

    private boolean strategoRuntimeEnabled = false;
    public final StrategoRuntimeAdapterCompiler.Input.Builder strategoRuntime = StrategoRuntimeAdapterCompiler.Input.builder();

    private boolean constraintAnalyzerEnabled = false;
    public final ConstraintAnalyzerAdapterCompiler.Input.Builder constraintAnalyzer = ConstraintAnalyzerAdapterCompiler.Input.builder();

    private boolean multilangAnalyzerEnabled = false;
    public final MultilangAnalyzerAdapterCompiler.Input.Builder multilangAnalyzer = MultilangAnalyzerAdapterCompiler.Input.builder();

    private boolean tegoRuntimeEnabled = false;
    public final TegoRuntimeAdapterCompiler.Input.Builder tegoRuntime = TegoRuntimeAdapterCompiler.Input.builder();

    private boolean dynamixEnabled = false;
    public final DynamixAdapterCompiler.Input.Builder dynamix = DynamixAdapterCompiler.Input.builder();

    private boolean referenceResolutionEnabled = false;
    public final ReferenceResolutionAdapterCompiler.Input.Builder referenceResolution = ReferenceResolutionAdapterCompiler.Input.builder();

    private boolean codeCompletionEnabled = false;
    public final CodeCompletionAdapterCompiler.Input.Builder codeCompletion = CodeCompletionAdapterCompiler.Input.builder();

    private boolean hoverEnabled = false;
    public final HoverAdapterCompiler.Input.Builder hover = HoverAdapterCompiler.Input.builder();


    public AdapterProjectCompiler.Input.Builder project = AdapterProjectCompiler.Input.builder();


    public ClassLoaderResourcesCompiler.Input.Builder withClassloaderResources() {
        return classLoaderResources;
    }

    public GetSourceFilesAdapterCompiler.Input.Builder withGetSourceFiles() {
        return getSourceFiles;
    }

    public ExportsCompiler.Input.Builder withExports() {
        return exports;
    }

    public ParserAdapterCompiler.Input.Builder withParser() {
        parserEnabled = true;
        return parser;
    }

    public StylerAdapterCompiler.Input.Builder withStyler() {
        stylerEnabled = true;
        return styler;
    }

    public StrategoRuntimeAdapterCompiler.Input.Builder withStrategoRuntime() {
        strategoRuntimeEnabled = true;
        return strategoRuntime;
    }

    public ConstraintAnalyzerAdapterCompiler.Input.Builder withConstraintAnalyzer() {
        constraintAnalyzerEnabled = true;
        return constraintAnalyzer;
    }

    public MultilangAnalyzerAdapterCompiler.Input.Builder withMultilangAnalyzer() {
        multilangAnalyzerEnabled = true;
        return multilangAnalyzer;
    }

    public TegoRuntimeAdapterCompiler.Input.Builder withTegoRuntime() {
        tegoRuntimeEnabled = true;
        return tegoRuntime;
    }

    public DynamixAdapterCompiler.Input.Builder withDynamix() {
        dynamixEnabled = true;
        return dynamix;
    }

    public CodeCompletionAdapterCompiler.Input.Builder withCodeCompletion() {
        codeCompletionEnabled = true;
        return codeCompletion;
    }

    public ReferenceResolutionAdapterCompiler.Input.Builder withReferenceResolution() {
        referenceResolutionEnabled = true;
        return referenceResolution;
    }

    public HoverAdapterCompiler.Input.Builder withHover() {
        hoverEnabled = true;
        return hover;
    }


    public AdapterProjectCompiler.Input build(LanguageProjectCompiler.Input languageProjectInput, Option<GradleDependency> languageProjectDependency, AdapterProject adapterProject) {
        final Shared shared = languageProjectInput.shared();

        final ClassLoaderResourcesCompiler.Input classLoaderResources = buildClassLoaderResources(shared, languageProjectInput.languageProject());
        project.classLoaderResources(classLoaderResources);

        final ExportsCompiler.Input exports = buildExports(shared, adapterProject);
        project.exports(exports);

        final GetSourceFilesAdapterCompiler.Input getSourceFiles = buildGetSourceFiles(shared, adapterProject, classLoaderResources);
        project.sourceFiles(getSourceFiles);

        final ParserAdapterCompiler.@Nullable Input parser = buildParser(shared, adapterProject, languageProjectInput, classLoaderResources);
        if(parser != null) project.parser(parser);

        final StylerAdapterCompiler.@Nullable Input styler = buildStyler(shared, adapterProject, languageProjectInput, classLoaderResources);
        if(styler != null) project.styler(styler);

        final StrategoRuntimeAdapterCompiler.@Nullable Input strategoRuntime = buildStrategoRuntime(shared, adapterProject, languageProjectInput, classLoaderResources);
        if(strategoRuntime != null) project.strategoRuntime(strategoRuntime);

        final ConstraintAnalyzerAdapterCompiler.@Nullable Input constraintAnalyzer = buildConstraintAnalyzer(shared, adapterProject, languageProjectInput, parser, getSourceFiles, classLoaderResources, strategoRuntime);
        if(constraintAnalyzer != null) project.constraintAnalyzer(constraintAnalyzer);

        final MultilangAnalyzerAdapterCompiler.@Nullable Input multilangAnalyzer = buildMultilangAnalyzer(shared, adapterProject, languageProjectInput, strategoRuntime);
        if(multilangAnalyzer != null) project.multilangAnalyzer(multilangAnalyzer);

        final TegoRuntimeAdapterCompiler.@Nullable Input tegoRuntime = buildTegoRuntime(shared, adapterProject, languageProjectInput, classLoaderResources);
        if(tegoRuntime != null) project.tegoRuntime(tegoRuntime);

        final DynamixAdapterCompiler.@Nullable Input dynamix = buildDynamix(shared, adapterProject, classLoaderResources, constraintAnalyzer);
        if(dynamix != null) project.dynamix(dynamix);

        final CodeCompletionAdapterCompiler.@Nullable Input codeCompletion = buildCodeCompletion(shared, adapterProject, languageProjectInput, parser, constraintAnalyzer, strategoRuntime, tegoRuntime, classLoaderResources);
        if(codeCompletion != null) project.codeCompletion(codeCompletion);

        final ReferenceResolutionAdapterCompiler.@Nullable Input referenceResolution = buildReferenceResolution(shared, adapterProject, strategoRuntime, constraintAnalyzer, classLoaderResources);
        if(referenceResolution != null) project.referenceResolution(referenceResolution);

        final HoverAdapterCompiler.@Nullable Input hover = buildHover(shared, adapterProject, strategoRuntime, constraintAnalyzer, classLoaderResources);
        if(hover != null) project.hover(hover);

        project.languageProjectDependency(languageProjectDependency);

        return project
            .adapterProject(adapterProject)
            .shared(shared)
            .build();
    }


    private ClassLoaderResourcesCompiler.Input buildClassLoaderResources(Shared shared, LanguageProject languageProject) {
        return classLoaderResources
            .shared(shared)
            .languageProject(languageProject)
            .build();
    }

    private ExportsCompiler.Input buildExports(Shared shared, AdapterProject adapterProject) {
        return exports
            .shared(shared)
            .adapterProject(adapterProject)
            .build();
    }

    private GetSourceFilesAdapterCompiler.Input buildGetSourceFiles(
        Shared shared,
        AdapterProject adapterProject,
        ClassLoaderResourcesCompiler.Input classloaderResources
    ) {
        return getSourceFiles
            .shared(shared)
            .adapterProject(adapterProject)
            .classLoaderResourcesInput(classloaderResources)
            .build();
    }

    private ParserAdapterCompiler.@Nullable Input buildParser(
        Shared shared,
        AdapterProject adapterProject,
        LanguageProjectCompiler.Input languageProjectInput,
        ClassLoaderResourcesCompiler.Input classloaderResources
    ) {
        if(!parserEnabled) return null;
        return parser
            .shared(shared)
            .adapterProject(adapterProject)
            .languageProjectInput(languageProjectInput.parser().orElseThrow(() -> new RuntimeException("Mismatch between presence of parser input between language project and adapter project")))
            .classLoaderResourcesInput(classloaderResources)
            .build();
    }

    private StylerAdapterCompiler.@Nullable Input buildStyler(
        Shared shared,
        AdapterProject adapterProject,
        LanguageProjectCompiler.Input languageProjectInput,
        ClassLoaderResourcesCompiler.Input classloaderResources
    ) {
        if(!stylerEnabled) return null;
        return styler
            .shared(shared)
            .adapterProject(adapterProject)
            .languageProjectInput(languageProjectInput.styler().orElseThrow(() -> new RuntimeException("Mismatch between presence of styler input between language project and adapter project")))
            .classLoaderResourcesInput(classloaderResources)
            .build();
    }

    private StrategoRuntimeAdapterCompiler.@Nullable Input buildStrategoRuntime(
        Shared shared,
        AdapterProject adapterProject,
        LanguageProjectCompiler.Input languageProjectInput,
        ClassLoaderResourcesCompiler.Input classloaderResources
    ) {
        if(!strategoRuntimeEnabled) return null;
        return strategoRuntime
            .shared(shared)
            .adapterProject(adapterProject)
            .languageProjectInput(languageProjectInput.strategoRuntime().orElseThrow(() -> new RuntimeException("Mismatch between presence of stratego runtime input between language project and adapter project")))
            .classLoaderResourcesInput(classloaderResources)
            .build();
    }

    private ConstraintAnalyzerAdapterCompiler.@Nullable Input buildConstraintAnalyzer(
        Shared shared,
        AdapterProject adapterProject,
        LanguageProjectCompiler.Input languageProjectInput,
        ParserAdapterCompiler.@Nullable Input parseInput,
        GetSourceFilesAdapterCompiler.Input getSourceFilesInput,
        ClassLoaderResourcesCompiler.Input classloaderResources,
        StrategoRuntimeAdapterCompiler.@Nullable Input strategoRuntimeInput
    ) {
        if(!constraintAnalyzerEnabled) return null;
        if(parseInput == null) {
            throw new RuntimeException("Constraint analyzer input requires a parser, but the parser has not been set");
        }
        if(strategoRuntimeInput == null) {
            throw new RuntimeException("Constraint analyzer input requires a Stratego runtime, but the Stratego runtime has not been set");
        }
        return constraintAnalyzer
            .shared(shared)
            .adapterProject(adapterProject)
            .languageProjectInput(languageProjectInput.constraintAnalyzer().orElseThrow(() -> new RuntimeException("Mismatch between presence of constraint analyzer input between language project and adapter project")))
            .classLoaderResourcesInput(classloaderResources)
            .strategoRuntimeInput(strategoRuntimeInput)
            .parseInput(parseInput)
            .sourceFilesInput(getSourceFilesInput)
            .build();
    }

    private MultilangAnalyzerAdapterCompiler.@Nullable Input buildMultilangAnalyzer(
        Shared shared,
        AdapterProject adapterProject,
        LanguageProjectCompiler.Input languageProjectInput,
        StrategoRuntimeAdapterCompiler.@Nullable Input strategoRuntimeInput
    ) {
        if(!multilangAnalyzerEnabled) return null;
        if(strategoRuntimeInput == null) {
            throw new RuntimeException("Multi-language analyzer input requires a Stratego runtime, but the Stratego runtime has not been set");
        }
        return multilangAnalyzer
            .shared(shared)
            .adapterProject(adapterProject)
            .languageProjectInput(languageProjectInput.multilangAnalyzer().orElseThrow(() -> new RuntimeException("Mismatch between presence of multi-language analyzer input between language project and adapter project")))
            .strategoRuntimeInput(strategoRuntimeInput)
            .build();
    }

    private TegoRuntimeAdapterCompiler.@Nullable Input buildTegoRuntime(
        Shared shared,
        AdapterProject adapterProject,
        LanguageProjectCompiler.Input languageProjectInput,
        ClassLoaderResourcesCompiler.Input classloaderResources
    ) {
        if(!tegoRuntimeEnabled) return null;
        return tegoRuntime
            .shared(shared)
            .adapterProject(adapterProject)
            .classLoaderResourcesInput(classloaderResources)
            .build();
    }

    private DynamixAdapterCompiler.@Nullable Input buildDynamix(
        Shared shared,
        AdapterProject adapterProject,
        ClassLoaderResourcesCompiler.Input classloaderResources,
        ConstraintAnalyzerAdapterCompiler.@Nullable Input constraintAnalyzerInput
    ) {
        if(!dynamixEnabled) return null;
        return dynamix
            .shared(shared)
            .adapterProject(adapterProject)
            .classLoaderResourcesInput(classloaderResources)
            .constraintAnalyzerInput(constraintAnalyzerInput)
            .build();
    }

    private CodeCompletionAdapterCompiler.@Nullable Input buildCodeCompletion(
        Shared shared,
        AdapterProject adapterProject,
        LanguageProjectCompiler.Input languageProjectInput,
        ParserAdapterCompiler.@Nullable Input parseInput,
        ConstraintAnalyzerAdapterCompiler.@Nullable Input constraintAnalyzerInput,
        StrategoRuntimeAdapterCompiler.@Nullable Input strategoRuntimeInput,
        TegoRuntimeAdapterCompiler.@Nullable Input tegoRuntimeInput,
        ClassLoaderResourcesCompiler.Input classloaderResources
    ) {
        if(!codeCompletionEnabled) return null;
        if(parseInput == null) {
            throw new RuntimeException("Code completion input requires a parser, but the parser has not been set");
        }
        if(constraintAnalyzerInput == null) {
            throw new RuntimeException("Code completion input requires a constraint analyzer, but the constraint analyzer has not been set");
        }
        if(strategoRuntimeInput == null) {
            throw new RuntimeException("Code completion input requires a Stratego runtime, but the Stratego runtime has not been set");
        }
        if(tegoRuntimeInput == null) {
            throw new RuntimeException("Code completion input requires a Tego runtime, but the Tego runtime has not been set");
        }
        return codeCompletion
            .shared(shared)
            .adapterProject(adapterProject)
            .parserInput(parseInput)
            .constraintAnalyzerInput(constraintAnalyzerInput)
            .strategoRuntimeInput(strategoRuntimeInput)
            .tegoRuntimeInput(tegoRuntimeInput)
            .classLoaderResourcesInput(classloaderResources)
            .build();
    }

    private ReferenceResolutionAdapterCompiler.@Nullable Input buildReferenceResolution(
        Shared shared,
        AdapterProject adapterProject,
        StrategoRuntimeAdapterCompiler.@Nullable Input strategoRuntimeInput,
        ConstraintAnalyzerAdapterCompiler.@Nullable Input constraintAnalyzerInput,
        ClassLoaderResourcesCompiler.Input classloaderResources
    ) {
        if(!referenceResolutionEnabled) return null;
        if(strategoRuntimeInput == null) {
            throw new RuntimeException("Reference resolution input requires a Stratego runtime, but the Stratego runtime has not been set");
        }
        if(constraintAnalyzerInput == null) {
            throw new RuntimeException("Reference resolution input requires a constraint analyzer, but the constraint analyzer has not been set");
        }
        return referenceResolution
            .shared(shared)
            .adapterProject(adapterProject)
            .strategoRuntimeInput(strategoRuntimeInput)
            .constraintAnalyzerInput(constraintAnalyzerInput)
            .classLoaderResourcesInput(classloaderResources)
            .build();
    }

    private HoverAdapterCompiler.@Nullable Input buildHover(
        Shared shared,
        AdapterProject adapterProject,
        StrategoRuntimeAdapterCompiler.@Nullable Input strategoRuntimeInput,
        ConstraintAnalyzerAdapterCompiler.@Nullable Input constraintAnalyzerInput,
        ClassLoaderResourcesCompiler.Input classloaderResources
    ) {
        if(!hoverEnabled) return null;
        if(strategoRuntimeInput == null) {
            throw new RuntimeException("Hover tooltip input requires a Stratego runtime, but the Stratego runtime has not been set");
        }
        if(constraintAnalyzerInput == null) {
            throw new RuntimeException("Hover tooltip input requires a constraint analyzer, but the constraint analyzer has not been set");
        }
        return hover
            .shared(shared)
            .adapterProject(adapterProject)
            .strategoRuntimeInput(strategoRuntimeInput)
            .constraintAnalyzerInput(constraintAnalyzerInput)
            .classLoaderResourcesInput(classloaderResources)
            .build();
    }
}
