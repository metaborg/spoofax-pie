package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class LanguageProjectCompiler {
    private final TemplateWriter buildGradleTemplate;
    private final TemplateWriter packageInfoTemplate;

    private final ParserCompiler parserCompiler;
    private final ClassloaderResourcesCompiler classloaderResourcesCompiler;
    private final StylerCompiler stylerCompiler;
    private final CompleterCompiler completerCompiler;
    private final StrategoRuntimeCompiler strategoRuntimeCompiler;
    private final ConstraintAnalyzerCompiler constraintAnalyzerCompiler;
    private final MultilangAnalyzerCompiler multilangAnalyzerCompiler;

    public LanguageProjectCompiler(
        TemplateCompiler templateCompiler,
        ClassloaderResourcesCompiler classloaderResourcesCompiler,
        ParserCompiler parserCompiler,
        StylerCompiler stylerCompiler,
        CompleterCompiler completerCompiler,
        StrategoRuntimeCompiler strategoRuntimeCompiler,
        ConstraintAnalyzerCompiler constraintAnalyzerCompiler,
        MultilangAnalyzerCompiler multilangAnalyzerCompiler
    ) {
        this.buildGradleTemplate = templateCompiler.getOrCompileToWriter("language_project/build.gradle.kts.mustache");
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("language_project/package-info.java.mustache");

        this.parserCompiler = parserCompiler;
        this.classloaderResourcesCompiler = classloaderResourcesCompiler;
        this.stylerCompiler = stylerCompiler;
        this.completerCompiler = completerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
        this.multilangAnalyzerCompiler = multilangAnalyzerCompiler;
    }

    public void generateInitial(Input input) throws IOException {
        buildGradleTemplate.write(input.buildGradleKtsFile(), input);
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
            parserCompiler.getLanguageProjectDependencies(i).addAllTo(dependencies);
        });
        input.styler().ifPresent((i) -> {
            stylerCompiler.getLanguageProjectDependencies(i).addAllTo(dependencies);
        });
        input.completer().ifPresent((i) -> {
            completerCompiler.getLanguageProjectDependencies(i).addAllTo(dependencies);
        });
        input.strategoRuntime().ifPresent((i) -> {
            strategoRuntimeCompiler.getLanguageProjectDependencies(i).addAllTo(dependencies);
        });
        input.constraintAnalyzer().ifPresent((i) -> {
            constraintAnalyzerCompiler.getLanguageProjectDependencies(i).addAllTo(dependencies);
        });
        input.multilangAnalyzer().ifPresent((i) -> {
            multilangAnalyzerCompiler.getLanguageProjectDependencies(i).addAllTo(dependencies);
        });
        return dependencies;
    }

    public ArrayList<String> getCopyResources(Input input) {
        final Shared shared = input.shared();
        final ArrayList<String> copyResources = new ArrayList<>(input.additionalCopyResources());
        input.parser().ifPresent((i) -> {
            parserCompiler.getLanguageProjectCopyResources(i).addAllTo(copyResources);
        });
        input.styler().ifPresent((i) -> {
            stylerCompiler.getLanguageProjectCopyResources(i).addAllTo(copyResources);
        });
        input.completer().ifPresent((i) -> {
            completerCompiler.getLanguageProjectCopyResources(i).addAllTo(copyResources);
        });
        input.strategoRuntime().ifPresent((i) -> {
            strategoRuntimeCompiler.getLanguageProjectCopyResources(i).addAllTo(copyResources);
        });
        input.constraintAnalyzer().ifPresent((i) -> {
            constraintAnalyzerCompiler.getLanguageProjectCopyResources(i).addAllTo(copyResources);
        });
        input.multilangAnalyzer().ifPresent((i) -> {
            multilangAnalyzerCompiler.getLanguageProjectCopyResources(i).addAllTo(copyResources);
        });
        return copyResources;
    }

    public Output compile(Input input) throws IOException {
        final Shared shared = input.shared();

        // Class files
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        packageInfoTemplate.write(input.genPackageInfo().file(classesGenDirectory), input);

        // Files from other compilers.
        classloaderResourcesCompiler.compileLanguageProject(input.classloaderResources());

        try {
            input.parser().ifPresent((i) -> {
                try {
                    parserCompiler.compileLanguageProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            input.styler().ifPresent((i) -> {
                try {
                    stylerCompiler.compileLanguageProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            input.completer().ifPresent((i) -> {
                try {
                    completerCompiler.compileLanguageProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            input.strategoRuntime().ifPresent((i) -> {
                try {
                    strategoRuntimeCompiler.compileLanguageProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            input.constraintAnalyzer().ifPresent((i) -> {
                try {
                    constraintAnalyzerCompiler.compileLanguageProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            input.multilangAnalyzer().ifPresent((i) -> {
                try {
                    multilangAnalyzerCompiler.compileLanguageProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        return Output.builder().addAllProvidedFiles(input.providedFiles()).build();
    }

    // Inputs

    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends LanguageProjectCompilerData.Input.Builder {
        }

        static Builder builder() {
            return new Builder();
        }


        /// Project

        LanguageProject languageProject();


        /// Sub-inputs

        ClassloaderResourcesCompiler.LanguageProjectInput classloaderResources();

        Optional<ParserCompiler.LanguageProjectInput> parser();

        Optional<StylerCompiler.LanguageProjectInput> styler();

        Optional<CompleterCompiler.LanguageProjectInput> completer();

        Optional<StrategoRuntimeCompiler.LanguageProjectInput> strategoRuntime();

        Optional<ConstraintAnalyzerCompiler.LanguageProjectInput> constraintAnalyzer();

        Optional<MultilangAnalyzerCompiler.LanguageProjectInput> multilangAnalyzer();


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

    @Value.Immutable
    public interface Output {
        class Builder extends LanguageProjectCompilerData.Output.Builder {
        }

        static Builder builder() {
            return new Builder();
        }

        List<ResourcePath> providedFiles();
    }
}
