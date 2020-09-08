package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
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
public class LanguageProjectCompiler implements TaskDef<LanguageProjectCompiler.Input, LanguageProjectCompiler.Output> {
    private final TemplateWriter packageInfoTemplate;

    private final ParserLanguageCompiler parserCompiler;
    private final ClassloaderResourcesCompiler classloaderResourcesCompiler;
    private final StylerLanguageCompiler stylerCompiler;
    private final CompleterLanguageCompiler completerCompiler;
    private final StrategoRuntimeLanguageCompiler strategoRuntimeCompiler;
    private final ConstraintAnalyzerLanguageCompiler constraintAnalyzerCompiler;
    private final MultilangAnalyzerLanguageCompiler multilangAnalyzerCompiler;

    @Inject public LanguageProjectCompiler(
        TemplateCompiler templateCompiler,
        ClassloaderResourcesCompiler classloaderResourcesCompiler,
        ParserLanguageCompiler parserCompiler,
        StylerLanguageCompiler stylerCompiler,
        CompleterLanguageCompiler completerCompiler,
        StrategoRuntimeLanguageCompiler strategoRuntimeCompiler,
        ConstraintAnalyzerLanguageCompiler constraintAnalyzerCompiler,
        MultilangAnalyzerLanguageCompiler multilangAnalyzerCompiler
    ) {
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("language_project/package-info.java.mustache");

        this.parserCompiler = parserCompiler;
        this.classloaderResourcesCompiler = classloaderResourcesCompiler;
        this.stylerCompiler = stylerCompiler;
        this.completerCompiler = completerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
        this.multilangAnalyzerCompiler = multilangAnalyzerCompiler;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        final Shared shared = input.shared();

        // Class files
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        packageInfoTemplate.write(context, input.genPackageInfo().file(classesGenDirectory), input);

        // Files from other compilers.
        context.require(classloaderResourcesCompiler, input.classloaderResources());
        input.parser().ifPresent((i) -> context.require(parserCompiler, i));
        input.styler().ifPresent((i) -> context.require(stylerCompiler, i));
        input.completer().ifPresent((i) -> context.require(completerCompiler, i));
        input.strategoRuntime().ifPresent((i) -> context.require(strategoRuntimeCompiler, i));
        input.constraintAnalyzer().ifPresent((i) -> context.require(constraintAnalyzerCompiler, i));
        input.multilangAnalyzer().ifPresent((i) -> context.require(multilangAnalyzerCompiler, i));

        return Output.builder().build();
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
        input.parser().ifPresent((i) -> {
            parserCompiler.getDependencies(i).addAllTo(dependencies);
        });
        input.styler().ifPresent((i) -> {
            stylerCompiler.getDependencies(i).addAllTo(dependencies);
        });
        input.completer().ifPresent((i) -> {
            completerCompiler.getDependencies(i).addAllTo(dependencies);
        });
        input.strategoRuntime().ifPresent((i) -> {
            strategoRuntimeCompiler.getDependencies(i).addAllTo(dependencies);
        });
        input.constraintAnalyzer().ifPresent((i) -> {
            constraintAnalyzerCompiler.getDependencies(i).addAllTo(dependencies);
        });
        input.multilangAnalyzer().ifPresent((i) -> {
            multilangAnalyzerCompiler.getDependencies(i).addAllTo(dependencies);
        });
        return dependencies;
    }

    public ArrayList<String> getCopyResources(Input input) {
        final Shared shared = input.shared();
        final ArrayList<String> copyResources = new ArrayList<>(input.additionalCopyResources());
        input.parser().ifPresent((i) -> {
            parserCompiler.getCopyResources(i).addAllTo(copyResources);
        });
        input.styler().ifPresent((i) -> {
            stylerCompiler.getCopyResources(i).addAllTo(copyResources);
        });
        input.completer().ifPresent((i) -> {
            completerCompiler.getCopyResources(i).addAllTo(copyResources);
        });
        input.strategoRuntime().ifPresent((i) -> {
            strategoRuntimeCompiler.getCopyResources(i).addAllTo(copyResources);
        });
        input.constraintAnalyzer().ifPresent((i) -> {
            constraintAnalyzerCompiler.getCopyResources(i).addAllTo(copyResources);
        });
        input.multilangAnalyzer().ifPresent((i) -> {
            multilangAnalyzerCompiler.getCopyResources(i).addAllTo(copyResources);
        });
        return copyResources;
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

        Optional<CompleterLanguageCompiler.Input> completer();

        Optional<StrategoRuntimeLanguageCompiler.Input> strategoRuntime();

        Optional<ConstraintAnalyzerLanguageCompiler.Input> constraintAnalyzer();

        Optional<MultilangAnalyzerLanguageCompiler.Input> multilangAnalyzer();


        /// Configuration

        GradleDependency languageSpecificationDependency();

        List<GradleConfiguredDependency> additionalDependencies();

        List<String> additionalCopyResources();


        /// Gradle files

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return languageProject().project().baseDirectory().appendRelativePath("build.gradle.kts");
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        default ResourcePath classesGenDirectory() {
            return languageProject().project().genSourceSpoofaxJavaDirectory();
        }


        /// Language project classes

        // package-info

        @Value.Default default TypeInfo genPackageInfo() {
            return TypeInfo.of(languageProject().packageId(), "package-info");
        }

        Optional<TypeInfo> manualPackageInfo();

        default TypeInfo packageInfo() {
            if(classKind().isManual() && manualPackageInfo().isPresent()) {
                return manualPackageInfo().get();
            }
            return genPackageInfo();
        }


        /// Provided files

        default ArrayList<ResourcePath> providedFiles() {
            final ArrayList<ResourcePath> providedFiles = new ArrayList<>();
            if(classKind().isGenerating()) {
                providedFiles.add(genPackageInfo().file(classesGenDirectory()));
            }
            classloaderResources().providedFiles().addAllTo(providedFiles);
            parser().ifPresent((i) -> i.providedFiles().addAllTo(providedFiles));
            styler().ifPresent((i) -> i.providedFiles().addAllTo(providedFiles));
            strategoRuntime().ifPresent((i) -> i.providedFiles().addAllTo(providedFiles));
            constraintAnalyzer().ifPresent((i) -> i.providedFiles().addAllTo(providedFiles));
            multilangAnalyzer().ifPresent((i) -> i.providedFiles().addAllTo(providedFiles));
            return providedFiles;
        }


        /// Automatically provided sub-inputs

        Shared shared();


        // TODO: add check
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends LanguageProjectCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
