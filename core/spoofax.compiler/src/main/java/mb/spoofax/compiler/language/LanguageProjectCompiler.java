package mb.spoofax.compiler.language;

import mb.pie.api.ExecContext;
import mb.pie.api.None;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class LanguageProjectCompiler implements TaskDef<LanguageProjectCompiler.Input, None> {
    private final TemplateWriter packageInfoTemplate;
    private final ClassloaderResourcesCompiler classloaderResourcesCompiler;
    private final ParserLanguageCompiler parserCompiler;
    private final StylerLanguageCompiler stylerCompiler;
    private final ConstraintAnalyzerLanguageCompiler constraintAnalyzerCompiler;
    private final MultilangAnalyzerLanguageCompiler multilangAnalyzerCompiler;
    private final StrategoRuntimeLanguageCompiler strategoRuntimeCompiler;
    private final CompleterLanguageCompiler completerCompiler;
    private final ExportsLanguageCompiler exportsCompiler;


    @Inject public LanguageProjectCompiler(
        TemplateCompiler templateCompiler,
        ClassloaderResourcesCompiler classloaderResourcesCompiler,
        ParserLanguageCompiler parserCompiler,
        StylerLanguageCompiler stylerCompiler,
        ConstraintAnalyzerLanguageCompiler constraintAnalyzerCompiler,
        MultilangAnalyzerLanguageCompiler multilangAnalyzerCompiler,
        StrategoRuntimeLanguageCompiler strategoRuntimeCompiler,
        CompleterLanguageCompiler completerCompiler,
        ExportsLanguageCompiler exportsCompiler
    ) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("package-info.java.mustache");
        this.classloaderResourcesCompiler = classloaderResourcesCompiler;
        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
        this.multilangAnalyzerCompiler = multilangAnalyzerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
        this.completerCompiler = completerCompiler;
        this.exportsCompiler = exportsCompiler;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws Exception {
        final Shared shared = input.shared();

        // Class files.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        packageInfoTemplate.write(context, input.basePackageInfo().file(generatedJavaSourcesDirectory), input);

        // Files from other compilers.
        context.require(classloaderResourcesCompiler, input.classloaderResources());
        input.parser().ifPresent((i) -> context.require(parserCompiler, i));
        input.styler().ifPresent((i) -> context.require(stylerCompiler, i));
        input.constraintAnalyzer().ifPresent((i) -> context.require(constraintAnalyzerCompiler, i));
        input.multilangAnalyzer().ifPresent((i) -> context.require(multilangAnalyzerCompiler, i));
        input.strategoRuntime().ifPresent((i) -> context.require(strategoRuntimeCompiler, i));
        input.completer().ifPresent((i) -> context.require(completerCompiler, i));
        input.exports().ifPresent((i) -> context.require(exportsCompiler, i));

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
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
        input.parser().ifPresent((i) -> parserCompiler.getDependencies(i).addAllTo(dependencies));
        input.styler().ifPresent((i) -> stylerCompiler.getDependencies(i).addAllTo(dependencies));
        input.constraintAnalyzer().ifPresent((i) -> constraintAnalyzerCompiler.getDependencies(i).addAllTo(dependencies));
        input.multilangAnalyzer().ifPresent((i) -> multilangAnalyzerCompiler.getDependencies(i).addAllTo(dependencies));
        input.strategoRuntime().ifPresent((i) -> strategoRuntimeCompiler.getDependencies(i).addAllTo(dependencies));
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

        ClassloaderResourcesCompiler.Input classloaderResources();

        Optional<ParserLanguageCompiler.Input> parser();

        Optional<StylerLanguageCompiler.Input> styler();

        Optional<ConstraintAnalyzerLanguageCompiler.Input> constraintAnalyzer();

        Optional<MultilangAnalyzerLanguageCompiler.Input> multilangAnalyzer();

        Optional<StrategoRuntimeLanguageCompiler.Input> strategoRuntime();

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
            classloaderResources().javaSourceFiles().addAllTo(providedFiles);
            parser().ifPresent((i) -> i.javaSourceFiles().addAllTo(providedFiles));
            styler().ifPresent((i) -> i.javaSourceFiles().addAllTo(providedFiles));
            constraintAnalyzer().ifPresent((i) -> i.javaSourceFiles().addAllTo(providedFiles));
            multilangAnalyzer().ifPresent((i) -> i.javaSourceFiles().addAllTo(providedFiles));
            strategoRuntime().ifPresent((i) -> i.javaSourceFiles().addAllTo(providedFiles));
            completer().ifPresent((i) -> i.javaSourceFiles().addAllTo(providedFiles));
            exports().ifPresent((i) -> i.javaSourceFiles().addAllTo(providedFiles));
            return providedFiles;
        }


        /// Automatically provided sub-inputs

        Shared shared();
    }
}
