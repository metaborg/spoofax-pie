package mb.spoofax.compiler.spoofaxcore;

import mb.common.util.ListView;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.StringUtil;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value.Enclosing
public class LanguageProject {
    private final TemplateWriter buildGradleTemplate;
    private final TemplateWriter generatedGradleTemplate;
    private final TemplateWriter packageInfoTemplate;

    private final Parser parserCompiler;
    private final Styler stylerCompiler;
    private final StrategoRuntime strategoRuntimeCompiler;
    private final ConstraintAnalyzer constraintAnalyzerCompiler;

    public LanguageProject(
        TemplateCompiler templateCompiler,
        Parser parserCompiler,
        Styler stylerCompiler,
        StrategoRuntime strategoRuntimeCompiler,
        ConstraintAnalyzer constraintAnalyzerCompiler
    ) {
        this.buildGradleTemplate = templateCompiler.getOrCompileToWriter("language_project/build.gradle.kts.mustache");
        this.generatedGradleTemplate = templateCompiler.getOrCompileToWriter("language_project/generated.gradle.kts.mustache");
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("language_project/package-info.java.mustache");

        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
    }

    public void generateInitial(Input input) throws IOException {
        final String relativePath = input.shared().languageProject().baseDirectory().relativizeToString(input.generatedGradleKtsFile());
        final HashMap<String, Object> map = new HashMap<>();
        map.put("relativeGeneratedGradleKtsFile", relativePath);
        buildGradleTemplate.write(input, map, input.buildGradleKtsFile());
    }

    public void generateGradleFiles(Input input) throws IOException {
        final Shared shared = input.shared();

        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.api(shared.logApiDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.resourceDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.spoofaxCompilerInterfacesDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.commonDep()));
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
        parserCompiler.getLanguageProjectDependencies(input.parser()).addAllTo(dependencies);

        final ArrayList<String> copyResources = new ArrayList<>(input.additionalCopyResources());
        parserCompiler.getLanguageProjectCopyResources(input.parser()).addAllTo(copyResources);
        input.styler().ifPresent((i) -> {
            stylerCompiler.getLanguageProjectDependencies(i).addAllTo(dependencies);
            stylerCompiler.getLanguageProjectCopyResources(i).addAllTo(copyResources);
        });
        input.strategoRuntime().ifPresent((i) -> {
            strategoRuntimeCompiler.getLanguageProjectDependencies(i).addAllTo(dependencies);
            strategoRuntimeCompiler.getLanguageProjectCopyResources(i).addAllTo(copyResources);
        });
        input.constraintAnalyzer().ifPresent((i) -> {
            constraintAnalyzerCompiler.getLanguageProjectDependencies(i).addAllTo(dependencies);
            constraintAnalyzerCompiler.getLanguageProjectCopyResources(i).addAllTo(copyResources);
        });

        final HashMap<String, Object> map = new HashMap<>();
        final String languageDependencyCode = input.languageSpecificationDependency().caseOf()
            .project((projectPath) -> "createProjectDependency(\"" + projectPath + "\")")
            .module((coordinate) -> "createModuleDependency(\"" + coordinate.toGradleNotation() + "\")")
            .files((filePaths) -> "createFilesDependency(" + filePaths.stream().map((s) -> "\"" + s + "\"").collect(Collectors.joining(", ")) + ")");
        map.put("languageDependencyCode", languageDependencyCode);
        map.put("dependencyCodes", dependencies.stream().map(GradleConfiguredDependency::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));
        map.put("copyResourceCodes", copyResources.stream().map(StringUtil::doubleQuote).collect(Collectors.toCollection(ArrayList::new)));
        generatedGradleTemplate.write(input, map, input.generatedGradleKtsFile());
    }

    public Output compile(Input input) throws IOException {
        final Shared shared = input.shared();
        final Output.Builder outputBuilder = Output.builder();
        // Class files
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        outputBuilder.addProvidedResources(packageInfoTemplate.write(input, input.genPackageInfo().file(classesGenDirectory)));
        // Files from other compilers.
        outputBuilder.addAllProvidedResources(parserCompiler.compileLanguageProject(input.parser()).providedResources());
        try {
            input.styler().ifPresent((i) -> {
                try {
                    outputBuilder.addAllProvidedResources(stylerCompiler.compileLanguageProject(i).providedResources());
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            input.strategoRuntime().ifPresent((i) -> {
                try {
                    outputBuilder.addAllProvidedResources(strategoRuntimeCompiler.compileLanguageProject(i).providedResources());
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            input.constraintAnalyzer().ifPresent((i) -> {
                try {
                    outputBuilder.addAllProvidedResources(constraintAnalyzerCompiler.compileLanguageProject(i).providedResources());
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
        return outputBuilder.build();
    }

    // Input

    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends LanguageProjectData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();

        Parser.Input parser();

        Optional<Styler.Input> styler();

        Optional<StrategoRuntime.Input> strategoRuntime();

        Optional<ConstraintAnalyzer.Input> constraintAnalyzer();


        /// Configuration

        GradleDependency languageSpecificationDependency();

        List<GradleConfiguredDependency> additionalDependencies();

        List<String> additionalCopyResources();


        /// Gradle files

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return shared().languageProject().baseDirectory().appendRelativePath("build.gradle.kts");
        }

        @Value.Default default ResourcePath generatedGradleKtsFile() {
            return shared().languageProject().genSourceSpoofaxGradleDirectory().appendRelativePath("generated.gradle.kts");
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        default ResourcePath classesGenDirectory() {
            return shared().languageProject().genSourceSpoofaxJavaDirectory();
        }


        /// Language project classes

        // package-info

        @Value.Default default TypeInfo genPackageInfo() {
            return TypeInfo.of(shared().languagePackage(), "package-info");
        }

        Optional<TypeInfo> manualPackageInfo();

        default TypeInfo packageInfo() {
            if(classKind().isManual() && manualPackageInfo().isPresent()) {
                return manualPackageInfo().get();
            }
            return genPackageInfo();
        }

        default ArrayList<ResourcePath> generatedLanguageProjectFiles() {
            final ArrayList<ResourcePath> generatedFiles = new ArrayList<>();
            if(classKind().isGenerating()) {
                generatedFiles.add(genPackageInfo().file(classesGenDirectory()));
            }
            parser().generatedLanguageProjectFiles().addAllTo(generatedFiles);
            // TODO: styler
            // TODO: stratego runtime
            // TODO: constraint analyzer
            return generatedFiles;
        }


        // TODO: add check
    }

    @Value.Immutable
    public interface Output {
        class Builder extends LanguageProjectData.Output.Builder {}

        static Builder builder() {
            return new Builder();
        }

        List<HierarchicalResource> providedResources();
    }
}
