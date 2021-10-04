package mb.spoofax.compiler.language;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Value.Enclosing
public class LanguageProjectCompiler implements TaskDef<Supplier<Result<LanguageProjectCompiler.Input, ?>>, Result<None, ?>> {
    private final TemplateWriter packageInfoTemplate;
    private final ClassLoaderResourcesCompiler classLoaderResourcesCompiler;
    private final ParserLanguageCompiler parserCompiler;
    private final StylerLanguageCompiler stylerCompiler;
    private final ConstraintAnalyzerLanguageCompiler constraintAnalyzerCompiler;
    private final MultilangAnalyzerLanguageCompiler multilangAnalyzerCompiler;
    private final StrategoRuntimeLanguageCompiler strategoRuntimeCompiler;
    private final TegoRuntimeLanguageCompiler tegoRuntimeCompiler;
    private final CompleterLanguageCompiler completerCompiler;
    private final ExportsLanguageCompiler exportsCompiler;


    @Inject public LanguageProjectCompiler(
        TemplateCompiler templateCompiler,
        ClassLoaderResourcesCompiler classLoaderResourcesCompiler,
        ParserLanguageCompiler parserCompiler,
        StylerLanguageCompiler stylerCompiler,
        ConstraintAnalyzerLanguageCompiler constraintAnalyzerCompiler,
        MultilangAnalyzerLanguageCompiler multilangAnalyzerCompiler,
        StrategoRuntimeLanguageCompiler strategoRuntimeCompiler,
        TegoRuntimeLanguageCompiler tegoRuntimeCompiler,
        CompleterLanguageCompiler completerCompiler,
        ExportsLanguageCompiler exportsCompiler
    ) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("package-info.java.mustache");
        this.classLoaderResourcesCompiler = classLoaderResourcesCompiler;
        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
        this.multilangAnalyzerCompiler = multilangAnalyzerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
        this.tegoRuntimeCompiler = tegoRuntimeCompiler;
        this.completerCompiler = completerCompiler;
        this.exportsCompiler = exportsCompiler;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<None, ?> exec(ExecContext context, Supplier<Result<Input, ?>> input) throws IOException {
        return context.require(input).mapThrowing(i -> compile(context, i));
    }

    @Override public boolean shouldExecWhenAffected(Supplier<Result<Input, ?>> input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    private None compile(ExecContext context, Input input) throws IOException {
        // Class files.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        packageInfoTemplate.write(context, input.basePackageInfo().file(generatedJavaSourcesDirectory), input);

        // Files from other compilers.
        classLoaderResourcesCompiler.compile(context, input.classLoaderResources());
        Option.ofOptional(input.parser()).ifSomeThrowing((i) -> parserCompiler.compile(context, i));
        Option.ofOptional(input.styler()).ifSomeThrowing((i) -> stylerCompiler.compile(context, i));
        Option.ofOptional(input.constraintAnalyzer()).ifSomeThrowing((i) -> constraintAnalyzerCompiler.compile(context, i));
        Option.ofOptional(input.multilangAnalyzer()).ifSomeThrowing((i) -> multilangAnalyzerCompiler.compile(context, i));
        Option.ofOptional(input.strategoRuntime()).ifSomeThrowing((i) -> strategoRuntimeCompiler.compile(context, i));
        Option.ofOptional(input.tegoRuntime()).ifSomeThrowing((i) -> tegoRuntimeCompiler.compile(context, i));
        Option.ofOptional(input.completer()).ifSomeThrowing((i) -> completerCompiler.compile(context, i));
        Option.ofOptional(input.exports()).ifSomeThrowing((i) -> exportsCompiler.compile(context, i));

        return None.instance;
    }


    public ArrayList<GradleConfiguredDependency> getDependencies(Input input) {
        final Shared shared = input.shared();
        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.apiPlatform(shared.spoofaxDependencyConstraintsDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessorPlatform(shared.spoofaxDependencyConstraintsDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.logApiDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.logApiDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.resourceDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.spoofaxCompilerInterfacesDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.commonDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.spoofaxResourceDep()));
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
        input.parser().ifPresent((i) -> parserCompiler.getDependencies(i).addAllTo(dependencies));
        input.styler().ifPresent((i) -> stylerCompiler.getDependencies(i).addAllTo(dependencies));
        input.constraintAnalyzer().ifPresent((i) -> constraintAnalyzerCompiler.getDependencies(i).addAllTo(dependencies));
        input.multilangAnalyzer().ifPresent((i) -> multilangAnalyzerCompiler.getDependencies(i).addAllTo(dependencies));
        input.strategoRuntime().ifPresent((i) -> strategoRuntimeCompiler.getDependencies(i).addAllTo(dependencies));
        input.tegoRuntime().ifPresent((i) -> tegoRuntimeCompiler.getDependencies(i).addAllTo(dependencies));
        input.completer().ifPresent((i) -> completerCompiler.getDependencies(i).addAllTo(dependencies));
        input.exports().ifPresent((i) -> exportsCompiler.getDependencies(i).addAllTo(dependencies));
        return dependencies;
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends LanguageProjectCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Project

        LanguageProject languageProject();


        /// Sub-inputs

        ClassLoaderResourcesCompiler.Input classLoaderResources();

        Optional<ParserLanguageCompiler.Input> parser();

        Optional<StylerLanguageCompiler.Input> styler();

        Optional<ConstraintAnalyzerLanguageCompiler.Input> constraintAnalyzer();

        Optional<MultilangAnalyzerLanguageCompiler.Input> multilangAnalyzer();

        Optional<StrategoRuntimeLanguageCompiler.Input> strategoRuntime();

        Optional<TegoRuntimeLanguageCompiler.Input> tegoRuntime();

        Optional<CompleterLanguageCompiler.Input> completer();

        Optional<ExportsLanguageCompiler.Input> exports();


        /// Configuration

        List<GradleConfiguredDependency> additionalDependencies();


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Language project classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return languageProject().generatedJavaSourcesDirectory();
        }

        // package-info

        @Value.Default default TypeInfo basePackageInfo() {
            return TypeInfo.of(languageProject().packageId(), "package-info");
        }

        Optional<TypeInfo> manualPackageInfo();

        default TypeInfo packageInfo() {
            if(classKind().isManual() && manualPackageInfo().isPresent()) {
                return manualPackageInfo().get();
            }
            return basePackageInfo();
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ArrayList<ResourcePath> javaSourcePaths() {
            final ArrayList<ResourcePath> sourcePaths = new ArrayList<>();
            sourcePaths.add(generatedJavaSourcesDirectory());
            return sourcePaths;
        }

        default ArrayList<ResourcePath> javaSourceFiles() {
            final ArrayList<ResourcePath> providedFiles = new ArrayList<>();
            if(classKind().isGenerating()) {
                providedFiles.add(basePackageInfo().file(generatedJavaSourcesDirectory()));
            }
            classLoaderResources().javaSourceFiles().addAllTo(providedFiles);
            parser().ifPresent((i) -> i.javaSourceFiles().addAllTo(providedFiles));
            styler().ifPresent((i) -> i.javaSourceFiles().addAllTo(providedFiles));
            constraintAnalyzer().ifPresent((i) -> i.javaSourceFiles().addAllTo(providedFiles));
            multilangAnalyzer().ifPresent((i) -> i.javaSourceFiles().addAllTo(providedFiles));
            strategoRuntime().ifPresent((i) -> i.javaSourceFiles().addAllTo(providedFiles));
            tegoRuntime().ifPresent((i) -> i.javaSourceFiles().addAllTo(providedFiles));
            completer().ifPresent((i) -> i.javaSourceFiles().addAllTo(providedFiles));
            exports().ifPresent((i) -> i.javaSourceFiles().addAllTo(providedFiles));
            return providedFiles;
        }


        /// Automatically provided sub-inputs

        Shared shared();
    }
}
